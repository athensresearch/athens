(ns athens.electron
  (:require
    [athens.athens-datoms :as athens-datoms]
    [athens.db :as db]
    [datascript.transit :as dt :refer [write-transit-str]]
    [day8.re-frame.async-flow-fx]
    [goog.functions :refer [debounce]]
    [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx reg-fx dispatch #_subscribe]]))


;; XXX: most of these operations are effectful. They _should_ be re-written with effects, but feels like too much boilerplate.


(def electron (js/require "electron"))
(def remote (.. electron -remote))

(def dialog (.. remote -dialog))
(def app (.. remote -app))


(def fs (js/require "fs"))
(def path (js/require "path"))


(reg-event-fx
  :local-storage/get-db-filepath
  [(inject-cofx :local-storage "db/filepath")]
  (fn [{:keys [local-storage]} _]
    {:dispatch [:db/update-filepath local-storage]}))


(reg-event-fx
  :fs/create-new-db
  (fn []
    (let [doc-path (.getPath app "documents")
          db-name "first-db.transit"
          db-dir (.resolve path doc-path "athens")
          db-path (.resolve path db-dir db-name)]
      (when (not (.existsSync fs db-dir))
        (.mkdirSync fs db-dir))
      (js/localStorage.setItem "db/filepath" db-path)
      (.writeFileSync fs db-path (write-transit-str athens-datoms/datoms))
      (dispatch [:db/update-filepath db-path])
      (dispatch [:loading/unset]))))


(reg-event-fx
  :db/retract-athens-pages
  (fn []
    {:dispatch [:transact (concat (db/retract-page-recursively "athens/Welcome")
                                  (db/retract-page-recursively "athens/Changelog"))]}))


(reg-event-fx
  :db/transact-athens-pages
  (fn []
    {:dispatch [:transact athens-datoms/datoms]}))


(defn sync-db-from-fs
  "If modified time is newer, update app-db with m-time. Prevents sync happening after db is written from the app."
  [db filepath _filename]
  (let [prev-mtime (:db/mtime db)
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
  (fn [{:keys [db]} [_ filepath]]
    (let [dirpath (.dirname path filepath)]
      (.. fs (watch dirpath (fn [_event filename]
                              ;; when filename matches last part of filepath
                              ;; e.g. "first-db.transit" matches "home/u/Documents/athens/first-db.transit"
                              (when (re-find (re-pattern (str "\\b" filename "$")) filepath)
                                (debounce-sync-db-from-fs db filepath filename))))))
    {}))


(reg-event-db
  :db/update-mtime
  (fn [db [_ mtime]]
    (let [{:db/keys [filepath]} db
          ;; TODO: effect
          mtime (or mtime (.. fs (statSync filepath) -mtime))]
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
                                                     (nil? filepath) (dispatch [:fs/create-new-db])
                                                     (.existsSync fs filepath) (let [read-db (.readFileSync fs filepath)
                                                                                     db      (dt/read-transit-str read-db)]
                                                                                 (dispatch [:fs/watch filepath])
                                                                                 (dispatch [:reset-conn db]))
                                                     ;; TODO: implement
                                                     :else (dispatch [:dialog/open])))}
                                   {:when :seen-any-of?
                                    :events [:fs/create-new-db :reset-conn]
                                    :dispatch-n [[:db/retract-athens-pages]
                                                 [:db/transact-athens-pages]
                                                 [:loading/unset]
                                                 [:navigate :page {:id "0"}]]
                                    :halt? true}]}}))


;; TODO: implement with streams
;;(def r (.. stream -Readable (from (dt/write-transit-str @db/dsdb))))
;;(def w (.createWriteStream fs "./data/my-db.transit"))
;;(.pipe r w)
(reg-fx
  :fs/write!
  (fn [[filepath data]]
    (.writeFileSync fs filepath data)))


(defn open-dialog!
  []
  (let [res (.showOpenDialogSync dialog (clj->js {:properties ["openFile"]
                                                  :filters [{:name "Transit" :extensions ["transit"]}]}))
        filepath (first res)]
    (when filepath
      (dispatch [:db/update-filepath filepath]))))


(defn save-dialog!
  []
  (let [filepath (.showSaveDialogSync dialog (clj->js {:title "my-db"
                                                       :filters [{:name "Transit" :extensions ["transit"]}]}))]
    (dispatch [:db/update-filepath filepath])))

