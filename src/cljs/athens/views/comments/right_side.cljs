(ns athens.views.comments.right-side
  (:require [stylefy.core :as stylefy]
            [athens.views.textinput :as textinput]
            ["/components/Button/Button" :refer [Button]]
            ["/components/Input/Input" :refer [Input]]
            [re-frame.core :as rf]))


(def right-side-comments-styles
  {:position "absolute"
   :right    "-400px"
   :border-radius "10px"
   :border "1px solid gray"
   :width "300px"})


(def comments-styles
  {:padding "5px 0 5px 0"})


(defn comment-textarea
  [uid]
  (let [comment-string (reagent.core/atom "")]
    (fn [uid]
      (let [username @(rf/subscribe [:username])]
        [:div
         [textinput/textinput {:placeholder "Add a comment..." :style {:width "100%"}
                               :on-change (fn [e] (reset! comment-string (.. e -target -value)))
                               :value @comment-string}]
         [:> Button {:style    {:float "right"}
                     :on-click (fn [_]
                                 (re-frame.core/dispatch [:comment/write-comment uid @comment-string username])
                                 (re-frame.core/dispatch [:comment/hide-comment-textarea])
                                 (reset! comment-string nil))}
          "Send"]]))))


(defn right-side-comments
  [data uid]
  [:div.comments (stylefy/use-style right-side-comments-styles)
   (for [item data]
     [:<>
      [:div.comment (stylefy/use-style comments-styles)
       [:span (stylefy/use-style {:margin "0 0 0 5px"})
        (:string item)]]
      (when (not= item (last data))
        [:hr {:style {:margin 0}}])])
   [comment-textarea uid]])


