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


(defn default-db-dir-path
  []
  (.resolve path (default-dir) DB-INDEX))


(defn default-image-dir-path
  []
  (.resolve path (default-dir) IMAGES-DIR-NAME))


(defn create-dir-if-needed!
  [dir]
  (when (not (.existsSync fs dir))
    (.mkdirSync fs dir)))

