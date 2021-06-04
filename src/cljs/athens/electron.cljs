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
          (println ["new-db-filepath -->" new-db-filepath "-- new dir is --> " new-dir "-- curr-db-filepath -->"
                    curr-db-filepath])
          (if (.existsSync fs new-dir)
            (js/alert (str "Directory " new-dir " already exists, sorry."))
            (do (.mkdirSync fs new-dir)
                (.copyFileSync fs curr-db-filepath new-db-filepath)
                (dispatch [:db-picker/move-db curr-db-filepath new-db-filepath])
                (dispatch [:db/update-filepath new-db-filepath])
                (dispatch [:local-storage/set-db-picker-list])
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
    "Allow user to open db elsewhere from filesystem.
     open file with transit name
     if the file exists, read it dispatch events
    "
    []
    (let [res       (.showOpenDialogSync dialog (clj->js {:properties ["openFile"]
                                                          :filters    [{:name "Transit" :extensions ["transit"]}]}))
          open-file (first res)]
      (println ["Res is --->" res "first --->" open-file])
      (when (and open-file (.existsSync fs open-file))
        (let [read-db (.readFileSync fs open-file)
              db      (dt/read-transit-str read-db)]
          (dispatch-sync [:remote-graph/set-conf :default? false])
          (dispatch-sync [:init-rfdb])
          (dispatch [:fs/watch open-file])
          (dispatch [:reset-conn db])
          (dispatch [:db/update-filepath open-file]) ; This is related to current context of updating db name
          ;; add a new event which updates all-db file list
          (dispatch [:db-picker/add-new-db open-file false nil])
          (dispatch [:remote-graph-conf/load])
          (dispatch [:loading/unset])
          (dispatch [:local-storage/set-db-picker-list])))))


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
          (println ["new db created with location -->" db-location "filepath is ---> " db-filepath])
          (if (.existsSync fs dir)
            (js/alert (str "Directory " dir " already exists, sorry."))
            (do
              (dispatch-sync [:init-rfdb])
              (.mkdirSync fs dir)
              (.mkdirSync fs dir-images)
              (.writeFileSync fs db-filepath (dt/write-transit-str db))
              (dispatch [:fs/watch db-filepath])
              (dispatch [:db/update-filepath db-filepath])
              ;; dispatch this to update the db list
              ;; db-filepath also contains the name of db so no need to explicitly send this
              (dispatch [:db-picker/add-new-db db-filepath false nil])
              (dispatch [:local-storage/set-db-picker-list])
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

  ;; subscribe to all-dbs
  (reg-sub
    :db-picker/all-dbs
    (fn [db _]
      (println ["from subscription handler updated db list is  --->" (get db :db-picker/all-dbs)])
      (:db-picker/all-dbs db)))

  ;; Events

  ;; ------- db -picker--------------------------------
  ;; Event for updating all-dbs in db picker --


  ;; what do I need to know to add this db in the list ?
  ;; file path : will be passed on from the view(electron )
  ;; db name :  in filepath find the text between last and 2nd last '/'
  ;; remote db : can only be passed through "join" otherwise set it to none and if none it means
  ;; token for remote : check if remote and a token is passed
  ;; local db


  (defn get-db-name
    ;; example dbpath :/home/sid597/Desktop/athens db/test db/index.transit --> test db
    ;; dbpath type : string
    [dbpath]
    (let  [split-path (clojure.string/split dbpath #"/")]
      (nth  split-path (- (count split-path) 2))))

  ;; (println ( get-db-name "/home/sid597/Desktop/athens db/test db/index.transit")) returns --> test db

  " Find if a db is already in the list
    2 dbs can have same name ??
    I think so, so we check the file path if it matches then don't allow user to add this db

  Implementation :
  - make a set containing all the paths from list
    - Should this be done whenever a new db is added or should we maintain a set universally and check that?
    - No performance issues if this is done on evry new db addition because not many dbs would be made frequently to
    cause performance issue. What about merging 2 large dbs? Not there yet, but in that case also calculating the set
     of all paths is O(n).
    - Also reframe is in-memory so saving data which can be easly calculated is not a good idea I think
  - check if new db path is there

   How dumb can a person be I was using map instead of reduce, and in my head thinking what I did wrong. Facepalm.
   "
  (defn check-duplicate-db
    [db-list check-path]
    (let [path-set (into #{} (reduce (fn [paths db-list-item]
                                       (conj paths (get db-list-item :path)))
                                     [] db-list))]
      (contains? path-set check-path)))

  ;; TODO add the fillowing test in test
  (def dummy-db-list
    [{:name "Athens Test Remote DB"
      :path "ec2-3-16-89-123.us-east-2.compute.amazonaws.com"
      :token "x"
      :is-remote true}
     {:name "My DB"
      :path "/Users/coolUser/Documents/athens/index.transit"
      :is-remote false}
     {:name "Top Secret"
      :path "/Users/coolUser/Documents/athens2/index.transit"
      :is-remote false}])

  ;; TODO add the following test to test
  ;; (println [ "checking if this path is in dummy list should return true, result is -->" (check-duplicate-db
  ;; dummy-db-list "/Users/coolUser/Documents/athens2/index.transit"}]) ; --> true
  ;; (println ["checking if this path is in dummy list should return false, result is -->" (check-duplicate-db dummy-db-list "/Users/coolUser/Documents/athens2/"}]) ; --> ; false



  ;; under a namespace ?  will add functionality first then on second pass will format it
  ;; Would this create a effect? like side-effect?  Yeah will
  ;; save this data to local storage

  (reg-event-fx
    :db-picker/add-new-db
    ;; Add new db to db-picker, also update the local storage value
    (fn [{:keys [db]} [_ dbpath is-remote token]]
      (println "db-picker/add-new-db newdb add-new-db event occured with args ---->" dbpath is-remote token)
      (let [current-db-list (:db-picker/all-dbs db)
            duplicate (check-duplicate-db current-db-list dbpath)]
        (println ["db-picker/add-new-db newdb Is the new db already added in the list ? --> " duplicate])
        (if (not duplicate)
          (let [dbname (get-db-name dbpath)
                newdb  {:path dbpath
                        :is-remote is-remote
                        :token token
                        :name dbname}
                all-dbs (conj current-db-list newdb)]
            ;; Add this data to newdb data to all-dbs so that view can be updated with this new db
            ;; also save this updated all-dbs in local storage
            (println "db-picker/add-new-db newdb is --->" [newdb])
            (println "db-picker/add-new-db newdb previous db list is ---->" current-db-list)
            (println "db-picker/add-new-db newdb updated db list is ---->" all-dbs)

            {:db (assoc db :db-picker/all-dbs all-dbs)})
          (js/alert (str "Database already in the list"))))))

  ;; ==== Thoughts on add-new-db
  ;; db list got updated but subscription did not work
  ;; Add new db is working but have to close the dialog first to see the results


  ;; ==== Check and get from local storage
  ;; Checkout this link for discussion on how to serialize data to store : https://stackoverflow.com/questions/67821181/how-to-set-and-get-a-vector-in-local-storage
  (reg-event-fx
    ;; check if local storage has data related to db-picker
    :local-storage/check-db-picker-list
    (fn [{:keys [db]} _]
      (let [val                 (cljs.reader/read-string (js/localStorage.getItem "db-picker/all-dbs"))
            current-db-filepath (:db/filepath db)]
        (println ":local-storage/check-db-picker-list val for db picker list is ---->" val)
        (cond
          (nil? val)  (dispatch [:db-picker/add-new-db current-db-filepath false nil])
          :else       {:db (assoc db :db-picker/all-dbs val)}))))


  ;; ==== Save to local storage"
  (reg-event-fx
    :local-storage/set-db-picker-list
    (fn [{:keys [db]} _]
      (let [current-db-list (:db-picker/all-dbs db)]
        (println ["asked to save db-list to local storage with val---->" current-db-list])
        ;; Checkout the stackoverflow url above why pr-str and not str (I was previously using str)
        (js/localStorage.setItem "db-picker/all-dbs" (pr-str current-db-list)))))



  ;; Function to remove a path from db list
  ;; Find the map which has a key matching the db-path then remove it
  (reg-event-fx
    :db-picker/remove-db
    (fn [{:keys [db]} [_ db-path]]
      (println ["asked to remove db with path ----> " db-path])
      (let [current-db-list (:db-picker/all-dbs db)
            new-db-list (into [] (filter
                                   (fn [db-list-item]
                                     (not= db-path (:path db-list-item)))
                                   current-db-list))]
        (println ["new db-list after deleting a path is ---->" new-db-list])
        {:db (assoc db :db-picker/all-dbs new-db-list)})))


  ;; Update db-picker list with new db path when db is moved
  (reg-event-fx
    :db-picker/move-db
    (fn [{:keys [db]} [_ previous_path new_path]]
      (println ["Asked to update the db path"])
      {:dispatch-n [[:db-picker/remove-db previous_path]
                    [:db-picker/add-new-db new_path false nil]]}))



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
        (println "get-db-filepath called")
        (cond
          (some-> remote-graph-conf read-string :default?) {:dispatch [:start-socket]}
          ;; No filepath in local storage, but an existing db suggests a dev chromium is running with a different local storage
          ;; Short-circuit the first load and just use the existing DB
          (and (nil? local-storage) (.existsSync fs default-db-path)) {:dispatch [:db/update-filepath-db-path]}

          :else {:dispatch-n [[:db/update-filepath local-storage]]}))))



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

  ;; If new is created add
  ;; 1. This to all-dbs list
  ;; 2. Make this db active
  (reg-event-fx
    :fs/create-new-db
    (fn []
      (let [db-filepath (.resolve path documents-athens-dir DB-INDEX)
            db-images   (.resolve path documents-athens-dir IMAGES-DIR-NAME)]
        (println "create-new-db called")
        (create-dir-if-needed! documents-athens-dir)
        (create-dir-if-needed! db-images)
        {:fs/write!  [db-filepath (write-transit-str (d/empty-db db/schema))]
         :dispatch-n [[:db/update-filepath db-filepath]
                      [:transact athens-datoms/datoms]
                      [:db-picker/add-new-db db-filepath false nil]]})))



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
       :async-flow {:first-dispatch [:local-storage/get-db-filepath] ; THis is the first dispatch that happens on
                    ;; start, this event then dispatches update-filepath, next we see when this event is seen and
                    ;; when it is seen the following rule handles that.
                    :rules          [{:when        :seen?
                                      :events      :db/update-filepath ; ok so this means when db/update-filepath
                                      ;; event is seen dispatch the following function ?

                                      :dispatch-fn (fn [[_ filepath]]
                                                     (cond
                                                       ;; No database path found in localStorage. Creating new one
                                                       (nil? filepath) (dispatch [:fs/create-new-db])
                                                       ;; Database found in local storage and filesystem:
                                                       (.existsSync fs filepath) (let [read-db (.readFileSync fs filepath)
                                                                                       db      (dt/read-transit-str read-db)]
                                                                                   (dispatch [:fs/watch filepath])
                                                                                   (dispatch [:reset-conn db])
                                                                                   (dispatch [:local-storage/check-db-picker-list]))
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
