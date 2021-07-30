(ns athens.electron.utils)


;; Documents/athens
;; ├── images
;; └── index.transit



(def electron (js/require "electron"))
(def remote (.. electron -remote))
(def app (.. remote -app))
(def dialog (.. remote -dialog))
(def path (js/require "path"))
(def fs (js/require "fs"))


(def DB-INDEX "index.transit")
(def IMAGES-DIR-NAME "images")


(defn default-dir
  "~/Documents on Linux/Mac
  C:\\\\User\\Documents on Windows"
  []
  (let [DOC-PATH (.getPath app "documents")]
    (.resolve path DOC-PATH "athens")))


(defn default-db-path
  []
  (.resolve path (default-dir) DB-INDEX))


(defn default-image-dir-path
  []
  (.resolve path (default-dir) IMAGES-DIR-NAME))


(defn local-graph
  [db-path]
  (let [base-dir (.dirname path db-path)]
    {:name       (.basename path base-dir)
     :base-dir   base-dir
     :images-dir (.resolve path base-dir IMAGES-DIR-NAME)
     :db-path    db-path}))


(defn local-graph-db-exists? [{:keys [db-path]}]
  (.existsSync fs db-path))


(defn local-graph-dir-exists? [{:keys [base-dir]}]
  (.existsSync fs base-dir))


(defn create-dir-if-needed!
  [dir]
  (when (not (.existsSync fs dir))
    (.mkdirSync fs dir)))

