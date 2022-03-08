(ns athens.views.comments.inline
  (:require [stylefy.core :as stylefy]))



(def comments-styles
  {:margin-left "30px"
   :background-color "#e7e7e7"})


(def comment-styles
  {:padding "5px 0 5px 0"})


(defn inline-comments
  [data]
  [:<>
   [:hr {:style {:margin "10px 0 0 4%" :color "gray" :width "95%"}}]
   [:div.comments (stylefy/use-style comments-styles)
    (for [item data]
      (let [{:keys [string time author]} item]
        [:<>
         [:div.comment (stylefy/use-style comment-styles)
          [:span (stylefy/use-style {:font-size "80%" :margin-right "15px" :color "gray"})
            author]
          [:span (stylefy/use-style {:font-size "90%"})
           string]
          [:span (stylefy/use-style {:font-size "80%" :color "gray" :float "right"})
           time]]]))]])


