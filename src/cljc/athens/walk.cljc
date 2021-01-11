(ns athens.walk
  (:require
    [athens.parser :as parser]
    [clojure.string :as str]
    [instaparse.core :as parse]))


(defn walk-string
  "Walk previous and new strings to delete or add links, block references, etc. to datascript."
  [string]
  (let [data (atom {})]
    (parse/transform
      {:page-link (fn [& title]
                    (let [inner-title (str/join "" title)]
                      (swap! data update :node/titles #(conj % inner-title))
                      (swap! data update :page/refs #(conj % [:node/title inner-title]))
                      (str "[[" inner-title "]]")))
       :hashtag   (fn [& title]
                    (let [inner-title (str/join "" title)]
                      (swap! data update :node/titles #(conj % inner-title))
                      (swap! data update :page/refs #(conj % [:node/title inner-title]))
                      (str "#" inner-title)))
       :block-ref (fn [uid] (swap! data update :block/refs #(conj % uid)))}
      (parser/parse-to-ast string))
    @data))

