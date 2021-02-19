(ns athens.components
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.parse-renderer :refer [component]]
    [athens.style :refer [color]]
    [athens.util :refer [now-ts recursively-modify-block-for-embed]]
    [athens.views.blocks :as blocks]
    [re-frame.core :refer [dispatch subscribe]]
    [stylefy.core :as stylefy :refer [use-style]]))


(defn todo-on-click
  [uid from-str to-str]
  (let [current-block-content (:block/string (db/get-block [:block/uid uid]))]
    (dispatch [:transact [{:block/uid    uid
                           :block/string (clojure.string/replace
                                           current-block-content
                                           from-str
                                           to-str)
                           :edit/time    (now-ts)}]])))


(defn span-click-stop
  []
  [:span {:on-click (fn [e]
                      (.. e stopPropagation))}])


(defmethod component :todo
  [_content uid]
  [span-click-stop
   [:input {:type      "checkbox"
            :checked   false
            :on-change #(todo-on-click uid #"\{\{\[\[TODO\]\]\}\}" "{{[[DONE]]}}")}]])


(defmethod component :done
  [_content uid]
  [span-click-stop
   [:input {:type      "checkbox"
            :checked   true
            :on-change #(todo-on-click uid #"\{\{\[\[DONE\]\]\}\}" "{{[[TODO]]}}")}]])


(defmethod component :youtube
  [content _uid]
  [span-click-stop
   [:div.media-16-9
    [:iframe {:src   (str "https://www.youtube.com/embed/" (get (re-find #".*v=([a-zA-Z0-9_\-]+)" content) 1))
              :allow "accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture"}]]])


(defmethod component :iframe
  [content _uid]
  [span-click-stop
   [:div.media-16-9
    [:iframe {:src (re-find #"http.*" content)}]]])


(defmethod component :self
  [content _uid]
  [span-click-stop
   [:button {:style {:color       "red"
                     :font-family "IBM Plex Mono"}}
    content]])


(def block-embed-adjustments
  {:background (color :background-minus-2 :opacity-med)
   :position   "relative"
   ::stylefy/manual [[:>.block-container {:margin-left "0"
                                          ::stylefy/manual [[:textarea {:background "transparent"}]]}]
                     [:>svg              {:position   "absolute"
                                          :right      "5px"
                                          :top        "5px"
                                          :font-size  "1rem"
                                          :z-index    "5"
                                          :cursor     "pointer"}]]})


(defmethod component :block-embed
  [content uid]
  [:div.block-embed (use-style block-embed-adjustments)
   (let [block (->> (re-find #"\(\((.+)\)\)" content)
                    last (vector :block/uid) db/get-block-document)]
     [:<>
      [blocks/block-el
       (recursively-modify-block-for-embed block (random-uuid))
       {:linked-ref false}
       {:block-embed? true}]
      (when-not @(subscribe [:editing/is-editing uid])
        [:> mui-icons/Edit
         {:on-click (fn [e]
                      (.. e stopPropagation)
                      (dispatch [:editing/uid uid]))}])])])



