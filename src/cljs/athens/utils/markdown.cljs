(ns athens.utils.markdown
  (:require
    ["turndown" :as turndown]))


(set! (.-escape (.-prototype turndown)) (fn [string] string))


(defonce turndown-instance
  (new turndown))


(defn html->md
  "Transforms text to markdown."
  [text]
  (.turndown turndown-instance text))
