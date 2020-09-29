(ns athens.electron
  (:require
    [athens.db :as db :refer [dsdb]]
    [athens.athens-datoms :refer [datoms]]
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
      (.writeFileSync fs db-path (write-transit-str @dsdb))
      (dispatch [:db/update-filepath db-path])
      (dispatch [:loading/unset]))))


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
                                                                                 (dispatch [:reset-conn db])
                                                                                 (dispatch [:loading/unset]))
                                                     ;; TODO: implement
                                                     :else (dispatch [:dialog/open])))}
                                   {:when :seen? :events :fs/create-new-db :dispatch [:navigate :page {:id "0"}]}
                                   {:when :seen? :events :loading/unset :dispatch [:transact datoms]}
                                   {:when :seen? :events :loading/unset :halt? true}]}}))


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

