(ns athens.electron.fs
  (:require
    [athens.athens-datoms                 :as athens-datoms]
    [athens.common-db                     :as common-db]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.db                            :as db]
    [athens.electron.utils                :as utils]
    [datascript.core                      :as d]
    [datascript.transit                   :as dt]
    [goog.functions                       :refer [debounce]]
    [re-frame.core                        :as rf]))


(declare write-bkp)


(defn sync-db-from-fs
  "If modified time is newer, update app-db with m-time. Prevents sync happening after db is written from the app."
  [filepath _filename]
  (let [prev-mtime @(rf/subscribe [:db/mtime])
        curr-mtime (try (.-mtime (.statSync (utils/fs) filepath))
                        (catch :default _))
        newer?     (< prev-mtime curr-mtime)]
    (when (and prev-mtime curr-mtime newer?)
      (let [block-text js/document.activeElement.value
            _          (.. js/navigator -clipboard (writeText block-text))
            _          (write-bkp)
            confirm    (js/window.confirm (str "New file found. Copying your current block to the clipboard, and saving your current db."
                                               "\n\n"
                                               "Accept changes?"))]
        (when confirm
          (rf/dispatch [:db/update-mtime curr-mtime])
          (let [read-db (.readFileSync (utils/fs) filepath)
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
          dirpath     (.dirname (utils/path) filepath)
          new-watcher (.. (utils/fs) (watch dirpath (fn [_event filename]
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
    (let [conn (d/create-conn common-db/schema)]
      (doseq [[_id data] athens-datoms/welcome-events]
        (atomic-resolver/resolve-transact! conn data))
      (utils/create-dir-if-needed! base-dir)
      (utils/create-dir-if-needed! images-dir)
      (.writeFileSync (utils/fs) db-path (dt/write-transit-str @conn))
      {:dispatch [:db-picker/add-and-select-db local-db]})))


(rf/reg-event-fx
  :fs/read-and-watch
  (fn [_ [_ {:keys [db-path]}]]
    (let [datoms (-> (.readFileSync (utils/fs) db-path)
                     dt/read-transit-str)]
      {:async-flow {:id             :fs-read-and-watch-async-flow
                    :db-path        [:async-flow :fs/read-and-watch]
                    :first-dispatch [:reset-conn datoms]
                    :rules          [{:when       :seen?
                                      :events     :success-reset-conn
                                      :dispatch-n [[:fs/watch db-path]
                                                   [:stage/success-db-load]]
                                      :halt?      true}]}})))


(rf/reg-event-fx
  :fs/add-read-and-watch
  (fn [_ [_ local-db]]
    {:dispatch [:db-picker/add-and-select-db local-db]}))


(rf/reg-event-db
  :db/update-mtime
  (fn [db [_ mtime1]]
    (let [{:db/keys [filepath]} db
          mtime (or mtime1 (.. (utils/fs) (statSync filepath) -mtime))]
      (assoc db :db/mtime mtime))))


;; Effects

(defn os-username
  []
  (.. (utils/os) userInfo -username))


(defn write-db
  "Tries to create a write utils/stream to {timestamp}-index.transit.bkp. Then tries to copy backup to index.transit.
  If the write operation fails, the backup file is corrupted and no copy is attempted, thus index.transit is assumed to be untouched.
  If the write operation succeeds, a backup is created and index.transit is overwritten.
  Reading and writing will occur asynchronously.
  Path and data to be written are retrieved from the reframe db directly, not passed as arguments.
  User should eventually have MANY backups files. It's their job to manage these backups :)"
  [copy?]
  (let [selected-db @(rf/subscribe [:db-picker/selected-db])
        ;; See test/e2e/electron-test.ts for details about this flag.
        e2e-ignore-save? (= (js/localStorage.getItem "E2E_IGNORE_SAVE") "true")]
    (when (and (utils/local-db? selected-db)
               (not e2e-ignore-save?))
      (let [filepath     (:db-path selected-db)
            data         (dt/write-transit-str @db/dsdb)
            r            (.. (utils/stream) -Readable (from data))
            dirname      (.dirname (utils/path) filepath)
            time         (.. (js/Date.) getTime)
            bkp-filename (str time "-" (os-username) "-" "index.transit.bkp")
            bkp-filepath (.resolve (utils/path) dirname bkp-filename)
            w            (.createWriteStream (utils/fs) bkp-filepath)
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
                            (.. (utils/fs) (copyFileSync bkp-filepath filepath))
                            (let [mtime (.-mtime (.statSync (utils/fs) filepath))]
                              (rf/dispatch-sync [:db/update-mtime mtime])
                              (rf/dispatch [:db/sync])))))
        (.pipe r w)))))


(defn write-bkp
  []
  (write-db false))


(defn default-debounce-write-db
  []
  (debounce write-db (* 1000 15)))


(rf/reg-sub
  :fs/write-db
  (fn [db _]
    (or (-> db :fs/debounce-write-db)
        ;; TODO: This default shouldn't be needed, but write! seems to be called
        ;; before boot is finished sometimes and I'm not sure why.
        (default-debounce-write-db))))


(rf/reg-event-fx
  :fs/update-write-db
  (fn [{:keys [db]} _]
    (let [backup-time (-> db :athens/persist :settings :backup-time)
          f           (debounce write-db (* 1000 backup-time))]
      {:db (assoc db :fs/debounce-write-db f)})))


;; The write happens asynchronously due to the debounce and write-db both being asynchronous.
;; write-db also takes the value of dsdb and filepath at the time it actually runs, not when
;; this is called.
(rf/reg-fx
  :fs/write!
  (fn []
    (@(rf/subscribe [:fs/write-db]) true)))
