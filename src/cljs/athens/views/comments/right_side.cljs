(ns athens.views.comments.right-side
  (:require [stylefy.core :as stylefy]
            [athens.views.textinput :as textinput]
            ["/components/Button/Button" :refer [Button]]
            ["/components/Input/Input" :refer [Input]]
            [re-frame.core :as rf]
            [athens.views.blocks.textarea-keydown :as textarea-keydown])
  (:import
    (goog.events
      KeyCodes)))


(def right-side-comments-styles
  {:position "absolute"
   :right    "-400px"
   :border-radius "10px"
   :border "1px solid gray"
   :width "300px"})


(def comments-styles
  {:padding "5px 0 5px 3px"})




(defn right-side-comment-textarea
  [uid]
  (let [comment-string (reagent.core/atom "")]
    (fn [uid]
      (let [username @(rf/subscribe [:username])]
        [:div
         [textinput/textinput {:placeholder "Add a comment..." :style {:width "95%"
                                                                       :margin-left "9px"}
                               :on-change (fn [e] (reset! comment-string (.. e -target -value)))
                               :value @comment-string
                               :on-key-down  (fn [e]
                                               (when (= (.. e -keyCode) KeyCodes.ENTER)
                                                   (re-frame.core/dispatch [:comment/write-comment uid @comment-string username])
                                                   (re-frame.core/dispatch [:comment/hide-comment-textarea])
                                                   (reset! comment-string nil)))}]
         [:> Button {:style    {:float "right"
                                :margin "5px"}
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
   [right-side-comment-textarea uid]])


