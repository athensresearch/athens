(ns athens.views.comments.right-side
  (:require [stylefy.core :as stylefy]))


(def right-side-comments-styles
  {:position "absolute"
   :right    "-400px"
   :border-radius "10px"
   :border "1px solid gray"})


(def comments-styles
  {:padding "5px 0 5px 0"
   :width "300px"})

(defn right-side-comments
  [data]
  [:div.comments (stylefy/use-style right-side-comments-styles)
   (for [item data]
     [:<>
      [:div.comment (stylefy/use-style comments-styles)

       [:span (stylefy/use-style {:margin "0 0 0 5px"})
        (:string item)]]
      (when (not= item (last data))
        [:hr {:style {:margin 0}}])])])


