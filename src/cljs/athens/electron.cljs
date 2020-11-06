(ns athens.electron
  (:require
    [athens.athens-datoms :as athens-datoms]
    [athens.db :as db]
    [datascript.core :as d]
    [datascript.transit :as dt :refer [write-transit-str]]
    [day8.re-frame.async-flow-fx]
    [goog.functions :refer [debounce]]
    [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx reg-fx dispatch dispatch-sync subscribe reg-sub]]))


;; XXX: most of these operations are effectful. They _should_ be re-written with effects, but feels like too much boilerplate.


(def electron (js/require "electron"))
(def remote (.. electron -remote))

(def dialog (.. remote -dialog))
(def app (.. remote -app))


(def fs (js/require "fs"))
(def path (js/require "path"))


(def DB-INDEX "index.transit")
(def IMAGES-DIR-NAME "images")

;;; Filesystem Dialogs


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
        (dispatch-sync [:init-rfdb])
        (dispatch [:fs/watch open-file])
        (dispatch [:reset-conn db])
        (dispatch [:db/update-filepath open-file])
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
      (let [db          (d/db-with (d/empty-db db/schema) athens-datoms/datoms)
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
            (dispatch [:loading/unset])))))))


;; Image Paste
(defn save-image
  [e item state]
  (let [{:keys [head tail]} (athens.keybindings/destruct-target (.. e -target))
        curr-db-filepath @(subscribe [:db/filepath])
        curr-db-dir      @(subscribe [:db/filepath-dir])
        img-dir          (.resolve path curr-db-dir IMAGES-DIR-NAME)
        base-dir         (.dirname path curr-db-filepath)
        base-dir-name    (.basename path base-dir)
        file             (.getAsFile item)
        reader           (js/FileReader.)
        cb               (fn [e]
                           (let [img-data     (as->
                                                (.. e -target -result) x
                                                (clojure.string/replace-first x #"data:image/png;base64," "")
                                                (js/Buffer. x "base64"))
                                 img-filename (.resolve path img-dir (str "img-" base-dir-name "-" (athens.util/gen-block-uid) ".png"))
                                 new-str      (str head "![](" "file://" img-filename ")" tail)]
                             (when-not (.existsSync fs img-dir)
                               (.mkdirSync fs img-dir))
                             (.writeFileSync fs img-filename img-data)
                             (swap! state assoc :string/local new-str)))]
    (set! (.. reader -onload) cb)
    (.readAsDataURL reader file)))


;;; Subs


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
    (-> (:db/filepath db)
        path.dirname)))

(reg-sub
  :db/base-dir-name
  (fn [db _]
    (-> (:db/filepath db)
        path.dirname)))


;;; Events


(reg-event-fx
  :fs/open-dialog
  (fn [{:keys [db]} _]
    (js/alert (str "No DB found at " (:db/filepath db) "."
                   "\nPlease open or create a new db."))
    {:dispatch-n [[:modal/toggle]]}))


(reg-event-fx
  :local-storage/get-db-filepath
  [(inject-cofx :local-storage "db/filepath")]
  (fn [{:keys [local-storage]} _]
    {:dispatch [:db/update-filepath local-storage]}))


(reg-event-fx
  :local-storage/navigate
  [(inject-cofx :local-storage "current-route/uid")]
  (fn [{:keys [local-storage]} _]
    {:dispatch [:navigate {:page {:id local-storage}}]}))


;; Documents/athens
;; ├── images
;; └── index.transit
(reg-event-fx
  :fs/create-new-db
  (fn []
    (let [DOC-PATH    (.getPath app "documents")
          athens-dir  (.resolve path DOC-PATH "athens")
          db-filepath (.resolve path athens-dir DB-INDEX)
          db-images   (.resolve path athens-dir IMAGES-DIR-NAME)]
      (when (not (.existsSync fs athens-dir))
        (.mkdirSync fs athens-dir))
      (when (not (.existsSync fs db-images))
        (.mkdirSync fs db-images))
      {:fs/write!  [db-filepath (write-transit-str athens-datoms/datoms)]
       :dispatch-n [[:db/update-filepath db-filepath]]})))


(reg-event-fx
  :db/retract-athens-pages
  (fn []
    {:dispatch [:transact (concat (db/retract-page-recursively "Welcome")
                                  (db/retract-page-recursively "Changelog"))]}))


(reg-event-fx
  :db/transact-athens-pages
  (fn []
    {:dispatch [:transact athens-datoms/datoms]}))


(defn sync-db-from-fs
  "If modified time is newer, update app-db with m-time. Prevents sync happening after db is written from the app."
  [filepath _filename]
  (let [prev-mtime @(subscribe [:db/mtime])
        curr-mtime (.-mtime (.statSync fs filepath))
        newer?     (< prev-mtime curr-mtime)]
    (when newer?
      (dispatch [:db/update-mtime curr-mtime])
      (let [read-db (.readFileSync fs filepath)
            db      (dt/read-transit-str read-db)]
        (dispatch [:reset-conn db])))))


(def debounce-sync-db-from-fs
  (debounce sync-db-from-fs 100))


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
  :desktop/boot
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

                                   ;; if first time, go to Daily Pages and open left-sidebar
                                   {:when       :seen?
                                    :events     :fs/create-new-db
                                    :dispatch-n [[:navigate :home]
                                                 [:left-sidebar/toggle]]}

                                   ;; if nth time, remember dark/light theme and last page
                                   {:when       :seen?
                                    :events     :reset-conn
                                    :dispatch-n [[:local-storage/set-theme]
                                                 [:local-storage/navigate]]}

                                   ;; whether first or nth time, update athens pages
                                   {:when       :seen-any-of?
                                    :events     [:fs/create-new-db :reset-conn]
                                    :dispatch-n [[:db/retract-athens-pages]
                                                 [:db/transact-athens-pages]
                                                 [:loading/unset]]
                                    :halt?      true}]}}))


;;; Effects


;; TODO: implement with streams
;;(def r (.. stream -Readable (from (dt/write-transit-str @db/dsdb))))
;;(def w (.createWriteStream fs "./data/my-db.transit"))
;;(.pipe r w)
(reg-fx
  :fs/write!
  (fn [[filepath data]]
    (.writeFileSync fs filepath data)))

