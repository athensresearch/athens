(ns athens.components
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
             [:div.media-16-9
              [:iframe {:src         (str "https://www.youtube.com/embed/" (get (re-find #".*v=([a-zA-Z0-9_\-]+)" content) 1))
                        :allow       "accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture"}]])})


(def component-generic-embed
  {:match  #"iframe\:.*"
   :render (fn [content _]
             [:div.media-16-9
              [:iframe {:src         (find-weblink content)}]])})


;; Components
(def components [component-todo component-done component-youtube-embed component-generic-embed])


;; ---- Render function for custom components
(defn empty-component
  [content _]
  [:button content])


;; TODO: use metaprogramming to achieve dynamic rendering with both basic components and custom components
(defn render-component
  "Renders a component using its parse tree & its uid."
  [content uid]
  (let [render     (some (fn [comp]
                           (when (re-matches (:match comp) content)
                             (:render comp))) components)]
    [:span {:on-click (fn [e]
                        (.. e stopPropagation))}
     (if render
       [render            content uid]
       [empty-component   content uid])]))
