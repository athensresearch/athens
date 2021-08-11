(ns athens.electron.fs
  (:require
    [athens.athens-datoms :as athens-datoms]
    [athens.db :as db]
    [athens.electron.utils :as utils]
    [athens.self-hosted.client :as client]
    [datascript.core :as d]
    [datascript.transit :as dt]
    [goog.functions :refer [debounce]]
    [re-frame.core :as rf]))


(def fs (js/require "fs"))
(def path (js/require "path"))
(def stream (js/require "stream"))


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


;; Watches directory that db is located in. If db file is updated, sync-db-from-fs.
;; Watching db file directly doesn't always work, so watch directory and regex match.
;; Debounce because files can be changed multiple times per save.
;; Adding a new watcher removes the previous one.
(rf/reg-event-fx
  :fs/watch
  (fn [{:keys [db]} [_ filepath]]
    (let [old-watcher (:fs/watcher db)
          dirpath     (.dirname path filepath)
          new-watcher (.. fs (watch dirpath (fn [_event filename]
                                              ;; when filename matches last part of filepath
                                              ;; e.g. "first-db.transit" matches "home/u/Documents/athens/first-db.transit"
                                              (when (re-find #"conflict" (or filename ""))
                                                (throw "Conflict file created by Dropbox"))
                                              (when (re-find (re-pattern (str "\\b" filename "$")) filepath)
                                                (debounce-sync-db-from-fs filepath filename)))))]
      (when old-watcher (.close old-watcher))
      {:db (assoc db :fs/watcher new-watcher)})))


(rf/reg-event-fx
  :fs/create-and-watch
  (fn [_ [_ {:keys [base-dir images-dir db-path] :as local-db}]]
    (let [new-db (d/db-with (d/empty-db db/schema) athens-datoms/datoms)]
      (utils/create-dir-if-needed! base-dir)
      (utils/create-dir-if-needed! images-dir)
      (.writeFileSync fs db-path (dt/write-transit-str new-db))
      {:dispatch [:db-picker/add-and-select-db local-db]})))


(rf/reg-event-fx
  :fs/read-and-watch
  (fn [_ [_ {:keys [db-path]}]]
    (let [datoms (-> (.readFileSync fs db-path)
                     dt/read-transit-str)]
      {:dispatch-n [[:reset-conn datoms]
                    [:fs/watch db-path]]})))


(rf/reg-event-fx
  :fs/add-read-and-watch
  (fn [_ [_ local-db]]
    {:dispatch [:db-picker/add-and-select-db local-db]}))


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
  (when-not (client/open?)
    (let [filepath     (:db-path @(rf/subscribe [:db-picker/selected-db]))
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
      (.pipe r w))))


(defn write-bkp
  []
  (write-db false))


(rf/reg-sub
  :fs/write-db
  (fn [db _]
    (-> db :fs/debounce-write-db)))


(rf/reg-event-fx
  :fs/update-write-db
  (fn [{:keys [db]} _]
    (let [backup-time (-> db :athens/persist :settings :backup-time)
          f           (debounce write-db (* 1000 backup-time))]
      (print "update-write-db" backup-time)
      (print (-> db :athens/persist :settings))
      {:db (assoc db :fs/debounce-write-db f)})))


;; The write happens asynchronously due to the debounce and write-db both being asynchronous.
;; write-db also takes the value of dsdb and filepath at the time it actually runs, not when
;; this is called.
(rf/reg-fx
  :fs/write!
  (fn []
    (@(rf/subscribe [:fs/write-db]) true)))
