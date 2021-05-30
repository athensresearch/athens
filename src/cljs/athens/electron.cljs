(ns athens.electron
  (:require
    [athens.athens-datoms :as athens-datoms]
    [athens.db :as db]
    [athens.patterns :as patterns]
    [athens.util :as util]
    [cljs.reader :refer [read-string]]
    [datascript.core :as d]
    [datascript.transit :as dt :refer [write-transit-str]]
    [day8.re-frame.async-flow-fx]
    [goog.functions :refer [debounce]]
    [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx reg-fx dispatch dispatch-sync subscribe reg-sub]]))


;; XXX: most of these operations are effectful. They _should_ be re-written with effects, but feels like too much boilerplate.

(when (util/electron?)

  (def electron (js/require "electron"))
  (def remote (.. electron -remote))

  (def dialog (.. remote -dialog))
  (def app (.. remote -app))


  (def fs (js/require "fs"))
  (def path (js/require "path"))
  (def stream (js/require "stream"))


  (def DB-INDEX "index.transit")
  (def IMAGES-DIR-NAME "images")

  (def documents-athens-dir
    (let [DOC-PATH (.getPath app "documents")]
      (.resolve path DOC-PATH "athens")))

  ;; Filesystem Dialogs


  (defn move-dialog!
    "If new-dir/athens already exists, no-op and alert user.
    Else copy db to new db location. When there is an images folder, copy /images folder and all images.
      file:// image urls in block/string don't get updated, so if original images are deleted, links will be broken."
    []
    (let [res     (.showOpenDialogSync dialog (clj->js {:properties ["openDirectory"]}))
          new-dir (first res)]
      (when new-dir
        (let [curr-db-filepath @(subscribe [:db/filepath])
              base-dir         (.dirname path curr-db-filepath)
              base-dir-name    (.basename path base-dir)
              curr-dir-images  (.resolve path base-dir IMAGES-DIR-NAME)
              new-dir          (.resolve path new-dir base-dir-name)
              new-dir-images   (.resolve path new-dir IMAGES-DIR-NAME)
              new-db-filepath  (.resolve path new-dir DB-INDEX)]
          (if (.existsSync fs new-dir)
            (js/alert (str "Directory " new-dir " already exists, sorry."))
            (do (.mkdirSync fs new-dir)
                (.copyFileSync fs curr-db-filepath new-db-filepath)
                (dispatch [:db/update-filepath new-db-filepath])
                (when (.existsSync fs curr-dir-images)
                  (.mkdirSync fs new-dir-images)
                  (let [imgs (->> (.readdirSync fs curr-dir-images)
                                  array-seq
                                  (map (fn [x]
                                         [(.join path curr-dir-images x)
                                          (.join path new-dir-images x)])))]
                    (doseq [[curr new] imgs]
                      (.copyFileSync fs curr new))))))))))


  (defn open-dialog!
    "Allow user to open db elsewhere from filesystem."
    []
    (let [res       (.showOpenDialogSync dialog (clj->js {:properties ["openFile"]
                                                          :filters    [{:name "Transit" :extensions ["transit"]}]}))
          open-file (first res)]
      (when (and open-file (.existsSync fs open-file))
        (let [read-db (.readFileSync fs open-file)
              db      (dt/read-transit-str read-db)]
          (dispatch-sync [:remote-graph/set-conf :default? false])
          (dispatch-sync [:init-rfdb])
          (dispatch [:fs/watch open-file])
          (dispatch [:reset-conn db])
          (dispatch [:db/update-filepath open-file])
          (dispatch [:remote-graph-conf/load])
          (dispatch [:loading/unset])))))


  ;; mkdir db-location/name/
  ;; mkdir db-location/name/images
  ;; write db-location/name/index.transit
  (defn create-dialog!
    "Create a new database."
    [db-name]
    (let [res         (.showOpenDialogSync dialog (clj->js {:properties ["openDirectory"]}))
          db-location (first res)]
      (when (and db-location (not-empty db-name))
        (let [db          (d/empty-db db/schema)
              dir         (.resolve path db-location db-name)
              dir-images  (.resolve path dir IMAGES-DIR-NAME)
              db-filepath (.resolve path dir DB-INDEX)]
          (if (.existsSync fs dir)
            (js/alert (str "Directory " dir " already exists, sorry."))
            (do
              (dispatch-sync [:init-rfdb])
              (.mkdirSync fs dir)
              (.mkdirSync fs dir-images)
              (.writeFileSync fs db-filepath (dt/write-transit-str db))
              (dispatch [:fs/watch db-filepath])
              (dispatch [:db/update-filepath db-filepath])
              (dispatch [:reset-conn db])
              (dispatch [:transact athens-datoms/datoms])
              (dispatch [:loading/unset])))))))


  ;; Image Paste
  (defn save-image
    ([item extension]
     (save-image "" "" item extension))
    ([head tail item extension]
     (let [curr-db-filepath @(subscribe [:db/filepath])
           _                (prn head tail curr-db-filepath item extension)
           curr-db-dir      @(subscribe [:db/filepath-dir])
           img-dir          (.resolve path curr-db-dir IMAGES-DIR-NAME)
           base-dir         (.dirname path curr-db-filepath)
           base-dir-name    (.basename path base-dir)
           file             (.getAsFile item)
           img-filename     (.resolve path img-dir (str "img-" base-dir-name "-" (util/gen-block-uid) "." extension))
           reader           (js/FileReader.)
           new-str          (str head "![](" "file://" img-filename ")" tail)
           cb               (fn [e]
                              (let [img-data (as->
                                               (.. e -target -result) x
                                               (clojure.string/replace-first x #"data:image/(jpeg|gif|png);base64," "")
                                               (js/Buffer. x "base64"))]
                                (when-not (.existsSync fs img-dir)
                                  (.mkdirSync fs img-dir))
                                (.writeFileSync fs img-filename img-data)))]
       (set! (.. reader -onload) cb)
       (.readAsDataURL reader file)
       new-str)))


  (defn dnd-image
    [target-uid drag-target item extension]
    (let [new-str   (save-image item extension)
          {:block/keys [order]} (db/get-block [:block/uid target-uid])
          parent    (db/get-parent [:block/uid target-uid])
          block     (db/get-block [:block/uid target-uid])
          new-block {:block/uid (util/gen-block-uid) :block/order 0 :block/string new-str :block/open true}
          tx-data   (if (= drag-target :child)
                      (let [reindex          (db/inc-after (:db/id block) -1)
                            new-children     (conj reindex new-block)
                            new-target-block {:db/id [:block/uid target-uid] :block/children new-children}]
                        new-target-block)
                      (let [index        (case drag-target
                                           :above (dec order)
                                           :below order)
                            reindex      (db/inc-after (:db/id parent) index)
                            new-children (conj reindex new-block)
                            new-parent   {:db/id (:db/id parent) :block/children new-children}]
                        new-parent))]
      ;; delay because you want to create block *after* the file has been saved to filesystem
      ;; otherwise, <img> is created too fast, and no image is rendered
      (js/setTimeout #(dispatch [:transact [tx-data]]) 50)))


  ;; Subs


  (reg-sub
    :db/mtime
    (fn [db _]
      (:db/mtime db)))


  (reg-sub
    :db/filepath
    (fn [db _]
      (:db/filepath db)))


  (reg-sub
    :db/filepath-dir
    (fn [db _]
      (.dirname path (:db/filepath db))))


  ;; create sub in athens.subs so web-version of Athens works
  ;; (reg-sub
  ;;  :db/remote-graph-conf
  ;;  (fn [db _]
  ;;    (:db/remote-graph-conf db)))


  (reg-sub
    :db/remote-graph
    (fn [db _]
      (:db/remote-graph db)))


  ;; Events


  (reg-event-fx
    :fs/open-dialog
    (fn [{:keys [db]} _]
      (js/alert (str "No DB found at " (:db/filepath db) "."
                     "\nPlease open or create a new db."))
      {:dispatch-n [[:modal/toggle]]}))


  (reg-event-fx
    :local-storage/get-db-filepath
    [(inject-cofx :local-storage "db/filepath")
     (inject-cofx :local-storage-map {:ls-key "db/remote-graph-conf"
                                      :key    :remote-graph-conf})]
    (fn [{:keys [local-storage remote-graph-conf]} _]
      (let [default-db-path (.resolve path documents-athens-dir DB-INDEX)]
        (cond
          (some-> remote-graph-conf read-string :default?) {:dispatch [:start-socket]}
          ;; No filepath in local storage, but an existing db suggests a dev chromium is running with a different local storage
          ;; Short-circuit the first load and just use the existing DB
          (and (nil? local-storage) (.existsSync fs default-db-path)) {:dispatch [:db/update-filepath default-db-path]}
          :else {:dispatch [:db/update-filepath local-storage]}))))


  (reg-event-fx
    :local-storage/navigate
    [(inject-cofx :local-storage "current-route/uid")]
    (fn [{:keys [local-storage]} _]
      {:dispatch [:navigate {:page {:id local-storage}}]}))


  (defn create-dir-if-needed!
    [dir]
    (when (not (.existsSync fs dir))
      (.mkdirSync fs dir)))

  ;; Documents/athens
  ;; ├── images
  ;; └── index.transit
  (reg-event-fx
    :fs/create-new-db
    (fn []
      (let [db-filepath (.resolve path documents-athens-dir DB-INDEX)
            db-images   (.resolve path documents-athens-dir IMAGES-DIR-NAME)]
        (create-dir-if-needed! documents-athens-dir)
        (create-dir-if-needed! db-images)
        {:fs/write!  [db-filepath (write-transit-str (d/empty-db db/schema))]
         :dispatch-n [[:db/update-filepath db-filepath]
                      [:transact athens-datoms/datoms]]})))


  (reg-event-fx
    :db/retract-athens-pages
    (fn []
      {:dispatch [:transact (concat (db/retract-page-recursively "Welcome")
                                    (db/retract-page-recursively "Changelog"))]}))


  (reg-event-fx
    :db/transact-athens-pages
    (fn []
      {:dispatch [:transact athens-datoms/datoms]}))

  (declare write-bkp)

  (defn sync-db-from-fs
    "If modified time is newer, update app-db with m-time. Prevents sync happening after db is written from the app."
    [filepath _filename]
    (let [prev-mtime @(subscribe [:db/mtime])
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
            (dispatch [:db/update-mtime curr-mtime])
            (let [read-db (.readFileSync fs filepath)
                  db      (dt/read-transit-str read-db)]
              (dispatch [:reset-conn db])))))))


  (def debounce-sync-db-from-fs
    (debounce sync-db-from-fs 1000))


  ;; Watches directory that db is located in. If db file is updated, sync-db-from-fs.
  ;; Watching db file directly doesn't always work, so watch directory and regex match.
  ;; Debounce because files can be changed multiple times per save.
  (reg-event-fx
    :fs/watch
    (fn [_ [_ filepath]]
      (let [dirpath (.dirname path filepath)]
        (.. fs (watch dirpath (fn [_event filename]
                                ;; when filename matches last part of filepath
                                ;; e.g. "first-db.transit" matches "home/u/Documents/athens/first-db.transit"
                                (when (re-find #"conflict" (or filename ""))
                                  (throw "Conflict file created by Dropbox"))
                                (when (re-find (re-pattern (str "\\b" filename "$")) filepath)
                                  (debounce-sync-db-from-fs filepath filename))))))
      {}))


  (reg-event-db
    :db/update-mtime
    (fn [db [_ mtime1]]
      (let [{:db/keys [filepath]} db
            mtime (or mtime1 (.. fs (statSync filepath) -mtime))]
        (assoc db :db/mtime mtime))))


  ;; if localStorage is empty, assume first open
  ;; create a Documents/athens directory and Documents/athens/db.transit file
  ;; store path in localStorage and re-frame
  ;; if localStorage has filepath, and there is a file
  ;; Open and set db
  ;; else - localStorage has filepath, but no file at filepath
  ;; open or create a new starter db

  ;; Watch filesystem, e.g. in case db is updated via Dropbox sync
  (reg-event-fx
    :boot/desktop
    (fn [_ _]
      {:db         db/rfdb
       :async-flow {:first-dispatch [:local-storage/get-db-filepath]
                    :rules          [{:when        :seen?
                                      :events      :db/update-filepath
                                      :dispatch-fn (fn [[_ filepath]]
                                                     (cond
                                                       ;; No database path found in localStorage. Creating new one
                                                       (nil? filepath) (dispatch [:fs/create-new-db])
                                                       ;; Database found in local storage and filesystem:
                                                       (.existsSync fs filepath) (let [read-db (.readFileSync fs filepath)
                                                                                       db      (dt/read-transit-str read-db)]
                                                                                   (dispatch [:fs/watch filepath])
                                                                                   (dispatch [:reset-conn db]))
                                                       ;; Database found in localStorage but not on filesystem
                                                       :else (dispatch [:fs/open-dialog])))}

                                     ;; remote graph
                                     {:when        :seen?
                                      :events      :start-socket}

                                     ;; if first time, go to Daily Pages and open left-sidebar
                                     {:when       :seen?
                                      :events     :fs/create-new-db
                                      :dispatch-n [[:navigate :home]
                                                   [:left-sidebar/toggle]]}

                                     ;; if nth time, remember dark/light theme and last page
                                     {:when       :seen?
                                      :events     :reset-conn
                                      :dispatch-n [[:local-storage/set-theme]
                                                   [:keymap/load-from-localstorage]
                                                   #_[:local-storage/navigate]]}

                                     ;; whether first or nth time, update athens pages
                                     #_{:when       :seen-any-of?
                                        :events     [:fs/create-new-db :reset-conn]
                                        :dispatch-n [[:db/retract-athens-pages]
                                                     [:db/transact-athens-pages]]}

                                     {:when        :seen-any-of?
                                      :events      [:fs/create-new-db :reset-conn]
                                      ;; if schema is nil, update to 1 and reparse all block/string's for links
                                      :dispatch-fn (fn [_]
                                                     (let [schemas    (d/q '[:find ?e ?v
                                                                             :where [?e :schema/version ?v]]
                                                                           @db/dsdb)
                                                           schema-cnt (count schemas)]
                                                       (cond
                                                         (= 0 schema-cnt) (let [linked-ref-pattern      (patterns/linked ".*")
                                                                                blocks-with-plain-links (d/q '[:find ?u ?s
                                                                                                               :keys block/uid block/string
                                                                                                               :in $ ?pattern
                                                                                                               :where
                                                                                                               [?e :block/uid ?u]
                                                                                                               [?e :block/string ?s]
                                                                                                               [(re-find ?pattern ?s)]]
                                                                                                             @db/dsdb
                                                                                                             linked-ref-pattern)
                                                                                blocks-orig             (map (fn [{:block/keys [uid string]}]
                                                                                                               {:db/id [:block/uid uid] :block/string string})
                                                                                                             blocks-with-plain-links)
                                                                                blocks-temp             (map (fn [{:block/keys [uid]}]
                                                                                                               {:db/id [:block/uid uid] :block/string ""})
                                                                                                             blocks-with-plain-links)]
                                                                            ;; give all blocks empty string - clears refs
                                                                            ;; give all blocks their original string - adds refs (for the period of time where block/refs were not added to db
                                                                            ;; update schema version, so this doesn't need to happen again
                                                                            (dispatch [:transact blocks-temp])
                                                                            (dispatch [:transact blocks-orig])
                                                                            (dispatch [:transact [[:db/add -1 :schema/version 1]]]))
                                                         (= 1 schema-cnt) (let [schema-version (-> schemas first second)]
                                                                            (case schema-version
                                                                              1 (prn (str "Schema version " schema-version))
                                                                              (js/alert (js/Error (str "No matching case clause for schema version: " schema-version)))))
                                                         (< 1 schema-cnt)
                                                         (js/alert (js/Error (str "Multiple schema versions: " schemas))))

                                                       (dispatch [:loading/unset])))
                                      :halt?       true}]}}))


  ;; Effects

  (defn os-username
    []
    (.. (js/require "os") userInfo -username))


  (defn write-db
    "Tries to create a write stream to {timestamp}-index.transit.bkp. Then tries to copy backup to index.transit.
    If the write operation fails, the backup file is corrupted and no copy is attempted, thus index.transit is assumed to be untouched.
    If the write operation succeeds, a backup is created and index.transit is overwritten.
    User should eventually have MANY backups files. It's their job to manage these backups :)"
    [copy?]
    (when-not @(subscribe [:socket-status])
      (let [filepath     @(subscribe [:db/filepath])
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
                              (dispatch-sync [:db/update-mtime mtime])
                              (dispatch [:db/sync])))))
        (.pipe r w))))


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


  (reg-fx
    :fs/write!
    (fn []
      (debounce-write-db true))))
