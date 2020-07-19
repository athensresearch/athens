(ns athens.components.default-components
  (:require
    [athens.db :as db]
    [athens.util :refer [now-ts]]
    [re-frame.core :refer [dispatch]]))

;; Note to contributors: After you define the component, you should add it in the exported components vector at bottom.

;; ---- Helper functions for default components ----
(defn todo-on-click
  [uid from-str to-str]
  (let [current-block-content (:block/string (db/get-block [:block/uid uid]))]
    (dispatch [:transact [{:block/uid    uid
                           :block/string (clojure.string/replace
                                           current-block-content
                                           from-str
                                           to-str)
                           :edit/time    (now-ts)}]])))


(defn find-weblink
  [content]
  (re-find #"http.*" content))


;; ---- Todo component declaration ----
(def component-todo
  {:match #"\[\[TODO\]\]"
   :render (fn [_ uid]
             [:input {:type     "checkbox"
                      :on-click #(todo-on-click uid #"\{\{\[\[TODO\]\]\}\}" "{{[[DONE]]}}")}])})


(def component-done
  {:match #"\[\[DONE\]\]"
   :render (fn [_ uid]
             [:input {:type     "checkbox"
                      :checked  "true"
                      :on-click #(todo-on-click uid #"\{\{\[\[DONE\]\]\}\}" "{{[[TODO]]}}")}])})


;; ---- Website embed component declaration ----
(def component-youtube-embed
  {:match  #"\[\[youtube\]\]\:.*"
   :render (fn [content _]
             [:iframe {:width       640
                       :height      360
                       :src         (str "https://www.youtube.com/embed/" (get (re-find #".*v=([a-zA-Z0-9_\-]+)" content) 1))
                       :allow       "accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture"}])})


(def component-generic-embed
  {:match  #"iframe\:.*"
   :render (fn [content _]
             [:iframe {:width       640
                       :height      360
                       :src         (find-weblink content)}])})


;; Exports
(def components [component-todo component-done component-youtube-embed component-generic-embed])

