(ns athens.electron.fs
  (:require
    [athens.athens-datoms :as athens-datoms]
    [athens.db :as db]
    [athens.electron.utils :as utils]
    [datascript.core :as d]
    [datascript.transit :as dt]
    [re-frame.core :as rf]
    [goog.functions :refer [debounce]]))


(def fs (js/require "fs"))
(def path (js/require "path"))
(def stream (js/require "stream"))


;; If new db is created add
;; 1. This db to all-dbs list
;; 2. Make this db active
(rf/reg-event-fx
  :fs/create-new-db
  (fn []
    (let [default-dir (utils/default-dir)
          images-dir (utils/default-image-dir-path)
          db-filepath (utils/default-db-path)]
      (utils/create-dir-if-needed! default-dir)
      (utils/create-dir-if-needed! images-dir)
      {:fs/write!  [db-filepath (dt/write-transit-str (d/empty-db db/schema))]
       :dispatch-n [[:db/update-filepath db-filepath]
                    [:transact athens-datoms/datoms]
                    [:db-picker/add-new-db db-filepath]]})))

(declare write-bkp)

(defn sync-db-from-fs
  "If modified time is newer, update app-db with m-time. Prevents sync happening after db is written from the app."
  [filepath _filename]
  (let [prev-mtime @(rf/subscribe [:db/mtime])
        curr-mtime (.-mtime (.statSync fs filepath))
        newer?     (< prev-mtime curr-mtime)]
    (when newer?
      (let [block-text js/document.activeElement.value
            _          (.. js/navigator -clipboard (writeText block-text))
            _          (write-bkp)
            confirm    (js/window.confirm (str "New file found. Copying your current block to the clipboard, and saving your current db."
                                               "\n\n"
                                               "Accept changes?"))]
        (when confirm
          (rf/dispatch [:db/update-mtime curr-mtime])
          (let [read-db (.readFileSync fs filepath)
                db      (dt/read-transit-str read-db)]
            (rf/dispatch [:reset-conn db])))))))


(def debounce-sync-db-from-fs
  (debounce sync-db-from-fs 1000))


(def watcher
  "Singleton watcher, can only have one at a given time.
   Not part of the ReFrame DB because the actual watcher exists, and must be cleaned up, regardless of the DB."
  (atom nil))

;; Watches directory that db is located in. If db file is updated, sync-db-from-fs.
;; Watching db file directly doesn't always work, so watch directory and regex match.
;; Debounce because files can be changed multiple times per save.
(rf/reg-event-fx
  :fs/watch
  (fn [_ [_ filepath]]
    (swap! watcher
           (fn [x]
             (when x (.close x))
             (let [dirpath (.dirname path filepath)]
               (.. fs (watch dirpath (fn [_event filename]
                                       ;; when filename matches last part of filepath
                                       ;; e.g. "first-db.transit" matches "home/u/Documents/athens/first-db.transit"
                                       (when (re-find #"conflict" (or filename ""))
                                         (throw "Conflict file created by Dropbox"))
                                       (when (re-find (re-pattern (str "\\b" filename "$")) filepath)
                                         (debounce-sync-db-from-fs filepath filename))))))))
    {}))


(rf/reg-event-fx
  :fs/create-and-watch
  (fn [_ [_ {:keys [base-dir images-dir db-path]} :as local-graph]]
    (let [new-db (d/db-with (d/empty-db db/schema) athens-datoms/datoms)]
      (utils/create-dir-if-needed! base-dir)
      (utils/create-dir-if-needed! images-dir)
      (.writeFileSync fs db-path (dt/write-transit-str new-db))
      {:dispatch-n [[:db-picker/add-new-db local-graph]
                    [:reset-conn new-db]
                    [:fs/watch db-path]]})))


(rf/reg-event-fx
  :fs/read-and-watch
  (fn [_ [_ {:keys [db-path]}]]
    (let [datoms (-> (.readFileSync fs db-path)
                     dt/read-transit-str)]
      {:dispatch-n [[:reset-conn datoms]
                    [:fs/watch db-path]]})))


(rf/reg-event-fx
 :fs/add-read-and-watch
 (fn [_ [_ local-graph]]
   {:dispatch-n [[:db-picker/add-new-db local-graph]
                 [:fs/read-and-watch local-graph]]}))


(rf/reg-event-db
  :db/update-mtime
  (fn [db [_ mtime1]]
    (let [{:db/keys [filepath]} db
          mtime (or mtime1 (.. fs (statSync filepath) -mtime))]
      (assoc db :db/mtime mtime))))



;; Effects

(defn os-username
  []
  (.. (js/require "os") userInfo -username))


(defn write-db
  "Tries to create a write stream to {timestamp}-index.transit.bkp. Then tries to copy backup to index.transit.
  If the write operation fails, the backup file is corrupted and no copy is attempted, thus index.transit is assumed to be untouched.
  If the write operation succeeds, a backup is created and index.transit is overwritten.
  Reading and writing will occur asynchronously.
  Path and data to be written are retrieved from the reframe db directly, not passed as arguments.
  User should eventually have MANY backups files. It's their job to manage these backups :)"
  [copy?]
  (let [filepath     @(rf/subscribe [:db/filepath])
        data         (dt/write-transit-str @db/dsdb)
        r            (.. stream -Readable (from data))
        dirname      (.dirname path filepath)
        time         (.. (js/Date.) getTime)
        bkp-filename (str time "-" (os-username) "-" "index.transit.bkp")
        bkp-filepath (.resolve path dirname bkp-filename)
        w            (.createWriteStream fs bkp-filepath)
        error-cb     (fn [err]
                       (when err
                         (js/alert (js/Error. err))
                         (js/console.error (js/Error. err))))]
    (.setEncoding r "utf8")
    (.on r "error" error-cb)
    (.on w "error" error-cb)
    (.on w "finish" (fn []
                      ;; copyFile is not atomic, unlike rename, but is still a short operation and has the nice side effect of creating a backup file
                      ;; If copy fails, by default, node.js deletes the destination file (index.transit): https://nodejs.org/api/fs.html#fs_fs_copyfilesync_src_dest_mode
                      (when copy?
                        (.. fs (copyFileSync bkp-filepath filepath))
                        (let [mtime (.-mtime (.statSync fs filepath))]
                          (rf/dispatch-sync [:db/update-mtime mtime])
                          (rf/dispatch [:db/sync])))))
    (.pipe r w)))


(def debounce-write-db
  (let [debounce-save-time (js/localStorage.getItem "debounce-save-time")]
    (if (nil? debounce-save-time)
      (let [debounce-save-time 15]
        (js/localStorage.setItem "debounce-save-time" debounce-save-time)
        (debounce write-db (* 1000 debounce-save-time)))

      (let [debounce-save-time (js/Number debounce-save-time)]
        (debounce write-db (* 1000 debounce-save-time))))))


(defn write-bkp
  []
  (write-db false))


;; The write happens asynchronously due to the debounce and write-db both being asynchronous.
;; write-db also takes the value of dsdb and filepath at the time it actually runs, not when
;; this is called.
(rf/reg-fx
  :fs/write!
  (fn []
    (debounce-write-db true)))
