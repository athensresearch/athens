(ns athens.views.comments.right-side
  (:require [stylefy.core :as stylefy]
            [athens.views.textinput :as textinput]
            ["/components/Button/Button" :refer [Button]]
            ["/components/Input/Input" :refer [Input]]))


(def right-side-comments-styles
  {:position "absolute"
   :right    "-400px"
   :border-radius "10px"
   :border "1px solid gray"})


(def comments-styles
  {:padding "5px 0 5px 0"
   :width "300px"})

(def comment-textarea-styles
  {:position "absolute"
   :right    "-400px"
   :border-radius "10px"
   :border "1px solid gray"
   :padding "5px 0 5px 0"
   :width "300px"})

#_(defn comment-textarea
    []
    [:div (stylefy/use-style comment-textarea-styles)
     "write here"])


(defn right-side-comments
  [data]
  [:div.comments (stylefy/use-style right-side-comments-styles)
   (for [item data]
     [:<>
      [:div.comment (stylefy/use-style comments-styles)

       [:span (stylefy/use-style {:margin "0 0 0 5px"})
        (:string item)]]
      (when (not= item (last data))
        [:hr {:style {:margin 0}}])])
   [:div
    [textinput/textinput {:placeholder "Add a comment..." :style {:width "100%"}}]
    [:> Button {:style {:float "right"}} "Send"]]])


