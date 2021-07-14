(ns athens.walk
  (:require
    [athens.parser :as parser]
    [clojure.string :as str]
    [instaparse.core :as parse]))


;; NOTE: collecting
;; - :node/titles
;; - :page/refs
;; - :block/refs

(defn walk-string
  "Walk previous and new strings to delete or add links, block references, etc. to datascript."
  [string]
  (let [data (atom {})]
    (parse/transform
      {:page-link (fn [{_from :from} & title]
                    (let [inner-title (str/join "" title)]
                      (swap! data update :node/titles #(conj % inner-title))
                      (swap! data update :page/refs #(conj % [:node/title inner-title]))
                      (str "[[" inner-title "]]")))
       :hashtag   (fn [{_from :from} & title]
                    (let [inner-title (str/join "" title)]
                      (swap! data update :node/titles #(conj % inner-title))
                      (swap! data update :page/refs #(conj % [:node/title inner-title]))
                      (str "#" inner-title)))
       :block-ref (fn [{_from :from} uid] (swap! data update :block/refs #(conj % uid)))}
      (parser/parse-to-ast string))
    #?(:cljs
       (js/console.log "walk-string" (pr-str @data)))
    @data))

