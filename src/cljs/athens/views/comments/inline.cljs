(ns athens.views.comments.inline
  (:require [stylefy.core :as stylefy]
            [re-frame.core :as rf]
            ["/components/Button/Button" :refer [Button]]
            ["/components/Input/Input" :refer [Input]]
            ["@material-ui/icons/Comment" :default Comment]
            [athens.views.textinput :as textinput])
  (:import
    (goog.events
      KeyCodes)))



(def comments-styles
  {:margin-left "30px"
   :background-color "#e7e7e7"
   :border-radius "5px"})


(def comment-styles
  {:padding "5px 10px 5px 10px"})

(defn inline-comment-textarea
  [uid]
  (let [comment-string (reagent.core/atom "")]
    (fn [uid]
      (let [username @(rf/subscribe [:username])]
        [:div
         [textinput/textinput {:placeholder "Add a comment..." :style {:width "90%"
                                                                       :margin "10px"}
                               :on-change   (fn [e] (reset! comment-string (.. e -target -value)))
                               :value       @comment-string
                               :on-key-down (fn [e]
                                              (when (= (.. e -keyCode) KeyCodes.ENTER)
                                                (re-frame.core/dispatch [:comment/write-comment uid @comment-string username])
                                                (re-frame.core/dispatch [:comment/hide-comment-textarea])
                                                (reset! comment-string nil)))}]
         [:> Button {:style    {:float "right"
                                :margin-top "9px"
                                :margin-right "1px"}
                     :on-click (fn [_]
                                 (re-frame.core/dispatch [:comment/write-comment uid @comment-string username])
                                 (re-frame.core/dispatch [:comment/hide-comment-textarea])
                                 (reset! comment-string nil))}
          "Send"]]))))

(defn comment-el
  [item]
  (let [{:keys [string time author block/uid]} item
        linked-refs (athens.reactive/get-reactive-linked-references [:block/uid uid])
        linked-refs-count (count linked-refs)]
    [:<>
     [:div.comment (stylefy/use-style comment-styles)
      [:span (stylefy/use-style {:font-size "80%" :margin-right "15px" :color "gray"})
       author]
      [:span (stylefy/use-style {:font-size "90%"})
       ;; In future this should be rendered differently for reply type and ref-type
       [athens.parse-renderer/parse-and-render string uid]]
      [:div (stylefy/use-style {:font-size "80%" :color "gray" :float "right"})
       (when (pos? linked-refs-count)
         [:span {:style {:margin-right "30px"}} linked-refs-count])
       [:span time]]]]))

(defn inline-comments
  [data uid]
  (let [state (reagent.core/atom {:hide? true})]
    (fn [data uid]
      [:<>
       ;; add time, author, and preview
       [:div {:style {:margin-left "30px"}}
        [:> Button {:on-click #(swap! state update :hide? not)}
         [:> Comment]
         (count data)]]
       (when-not (:hide? @state)
         [:div.comments (stylefy/use-style comments-styles)
          (for [item data]
            [comment-el item])
          [inline-comment-textarea uid]])])))


