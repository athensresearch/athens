(ns athens.electron.utils)


(def electron (js/require "electron"))
(def remote (.. electron -remote))
(def app (.. remote -app))
(def dialog (.. remote -dialog))
(def path (js/require "path"))

(def DB-INDEX "index.transit")
(def IMAGES-DIR-NAME "images")


(def documents-athens-dir
  (let [DOC-PATH (.getPath app "documents")]
    (.resolve path DOC-PATH "athens")))
