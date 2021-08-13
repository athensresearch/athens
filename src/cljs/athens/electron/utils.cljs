(ns athens.electron.utils)


;; Documents/athens
;; ├── images
;; └── index.transit



(def electron (js/require "electron"))
(def remote (.. electron -remote))
(def app (.. remote -app))
(def path (js/require "path"))
(def fs (js/require "fs"))


(def DB-INDEX "index.transit")
(def IMAGES-DIR-NAME "images")


(defn default-dbs-dir
  "~/Documents on Linux/Mac
  C:\\\\User\\Documents on Windows"
  []
  (.getPath app "documents"))


(defn default-base-dir
  []
  (.resolve path (default-dbs-dir) "athens"))


(defn local-db
  "Returns a map representing a local db.
   Local dbs are uniquely identified by the base-dir."
  [base-dir]
  {:name       (.basename path base-dir)
   :location   base-dir
   :base-dir   base-dir
   :images-dir (.resolve path base-dir IMAGES-DIR-NAME)
   :db-path    (.resolve path base-dir DB-INDEX)})


(defn local-db-exists?
  [{:keys [db-path] :as db}]
  (when db db-path (.existsSync fs db-path)))


(defn local-db-dir-exists?
  [{:keys [base-dir] :as db}]
  (when db base-dir (.existsSync fs base-dir)))


(defn create-dir-if-needed!
  [dir]
  (when (not (.existsSync fs dir))
    (.mkdirSync fs dir)))


(defn self-hosted-db
  "Returns a map representing a self-hosted db.
   Self-hosted dbs are uniquely identified by the url."
  [name url]
  {:name     name
   :location url
   :url      url
   :ws-url   (str "ws://" url "/ws")})


(defn local-db? [db]
  (boolean (:base-dir db)))


(defn remote-db? [db]
  (boolean (:url db)))


(defn db-exists? [db]
  (cond
    (local-db? db)  (local-db-exists? db)
    (remote-db? db) true
    :else           false))
