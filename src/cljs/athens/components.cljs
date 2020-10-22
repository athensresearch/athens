(ns athens.components
  (:require
    [athens.db :as db]
    [athens.util :refer [now-ts]]
    [re-frame.core :refer [dispatch]]))


(defn todo-on-click
  [uid from-str to-str]
  (let [current-block-content (:block/string (db/get-block [:block/uid uid]))]
    (dispatch [:transact [{:block/uid    uid
                           :block/string (clojure.string/replace
                                           current-block-content
                                           from-str
                                           to-str)
                           :edit/time    (now-ts)}]])))


(def components
  {#"\[\[TODO\]\]"        (fn [_ uid]
                            [:input {:type      "checkbox"
                                     :checked   false
                                     :on-change #(todo-on-click uid #"\{\{\[\[TODO\]\]\}\}" "{{[[DONE]]}}")}])
   #"\[\[DONE\]\]"        (fn [_ uid]
                            [:input {:type      "checkbox"
                                     :checked   true
                                     :on-change #(todo-on-click uid #"\{\{\[\[DONE\]\]\}\}" "{{[[TODO]]}}")}])
   #"\[\[youtube\]\]\:.*" (fn [content _]
                            [:div.media-16-9
                             [:iframe {:src   (str "https://www.youtube.com/embed/" (get (re-find #".*v=([a-zA-Z0-9_\-]+)" content) 1))
                                       :allow "accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture"}]])
   #"iframe\:.*"          (fn [content _]
                            [:div.media-16-9
                             [:iframe {:src (re-find #"http.*" content)}]])
   #"SELF"                (fn [content _]
                            [:button {:style {:color       "red"
                                              :font-family "IBM Plex Mono"}}
                             content])
   #"embed: \(\((.*)\)\)" (fn [content _]
                            (let [uid (second (re-find #"embed: \(\((.*)\)\)" content))]
                              [:h5 uid]))})


(defn empty-component
  [content _]
  [:button content])


(defn render-component
  "Renders a component using its parse tree & its uid."
  [content uid]
  (let [render (some (fn [[pattern render]]
                       (when (re-matches pattern content)
                         render))
                     components)]
    [:span {:on-click (fn [e]
                        (.. e stopPropagation))}
     (if render
       [render content uid]
       [empty-component content uid])]))
