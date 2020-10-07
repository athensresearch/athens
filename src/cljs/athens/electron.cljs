(ns athens.electron
  (:require
    [athens.athens-datoms :as athens-datoms]
    [athens.db :as db :refer [dsdb]]
    [datascript.transit :as dt :refer [write-transit-str]]
    [day8.re-frame.async-flow-fx]
    [re-frame.core :refer [#_reg-event-db reg-event-fx inject-cofx reg-fx dispatch]]))


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

;; todo: refactor effects
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

;; if localStorage is empty, assume first open
;; create a Documents/athens directory and Documents/athens/db.transit file
;; store path in localStorage and re-frame
;; if localStorage has filepath, and there is a file
;; Open and set db
;; else - localStorage has filepath, but no file at filepath
;; open or create a new starter db

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
                                                                                 (dispatch [:reset-conn db]))
                                                                                 ;;(dispatch [:db/retract-athens-pages])
                                                                                 ;;(dispatch [:db/transact-athens-pages])
                                                                                 ;;(dispatch [:loading/unset]))
                                                     ;; TODO: implement
                                                     :else (dispatch [:dialog/open])))}
                                   {:when :seen-any-of? :events [:fs/create-new-db :reset-conn] :dispatch-n [[:db/retract-athens-pages]
                                                                                                             [:db/transact-athens-pages]
                                                                                                             [:loading/unset]
                                                                                                             [:navigate :page {:id "0"}]]}]}}))


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

