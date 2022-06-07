(ns athens.views.comments.inline
  (:require
    [re-frame.core               :as rf]
    [goog.events                 :as events]
    [athens.parse-renderer       :as parse-renderer]
    [athens.util :as util]
    ["/components/Block/Anchor"  :refer [Anchor]]
    ["/components/Comments/Comments" :refer [InlineCommentInput CommentCounter]]
    ["@chakra-ui/react"          :refer [Button Input Box Text VStack HStack Textarea]]
    ["/components/Icons/Icons"   :refer [ChatFilledIcon ChevronDownIcon ChevronRightIcon]])
  (:import
    (goog.events
      KeyCodes)))


(defn copy-comment-uid
  [comment-data state]
  (let [uid (:block/uid comment-data)
        ref (str "((" uid "))")]
    (.. js/navigator -clipboard (writeText ref))
    (util/toast (clj->js {:status "info"
                          :position "top-right"
                          :title "Copied uid to clipboard"}))
    (swap! state update :comment/show? not)))


(defn show-comment-context-menu
  [comment-data state]
  (let [{:comment/keys [x y]} @state
        handle-click-outside  #(when (:comment/show? @state)
                                 (swap! state assoc :comment/show? false))]
    (reagent.core/create-class
      {:component-did-mount    (fn [_this] (events/listen js/document "mousedown" handle-click-outside))
       :component-will-unmount (fn [_this] (events/unlisten js/document "mousedown" handle-click-outside))
       :reagent-render         (fn [comment-data state]
                                 [:> Anchor {:menuActions (clj->js [{:children "Copy comment uid"
                                                                     :onClick  #(copy-comment-uid comment-data state)}])}]
                                 #_[:div (merge (stylefy/use-style dropdown-style)
                                              {:style {:position "fixed"
                                                       :left x
                                                       :top  y}})
                                  [:div (stylefy/use-style menu-style)
                                   [:> Button {:on-mouse-down #(copy-comment-uid comment-data state)}
                                    "Copy comment uid"]]])})))


(defn comment-el
  [item]
  (let [{:keys [string time author block/uid]} item
        linked-refs (athens.reactive/get-reactive-linked-references [:block/uid uid])
        linked-refs-count (count linked-refs)
        state (reagent.core/atom {:comment/show? false
                                  :comment/x     nil
                                  :comment/y     nil})]
    (fn [item]
      [:> HStack {:mb "-1px"
                  :borderTop "1px solid"
                  :display "grid"
                  :py 1
                  :alignItems "baseline"
                  :gridTemplateColumns "5em auto 1fr"
                  :gridTemplateRows "2em auto"
                  :gridTemplateAreas "'author anchor comment' '_ _ comment'"
                  :borderTopColor "separator.divider"
                  :sx {"> button.anchor" {:height "100%"}
                       "> button.anchor:not([data-active])" {:opacity 0}
                       ":hover > button.anchor" {:opacity 1}}}
       [:> Text {:fontWeight "bold"
                 :gridArea "author"
                 :fontSize "sm"
                 :flex "0 0 4em"
                 :noOfLines 0}
        author]
       [show-comment-context-menu item state]
       [:> Box {:flex 1
                :gridArea "comment"
                :lineHeight 1.5
                :fontSize "sm"}
        ;; In future this should be rendered differently for reply type and ref-type
        [athens.parse-renderer/parse-and-render string uid]]
       (when (pos? linked-refs-count)
         [:> Text linked-refs-count])])))





(defn inline-comments
  [data uid hide?]
  (let [state         (reagent.core/atom {:hide? hide?})
        num-comments  (count data)
        first-comment (first data)
        username @(rf/subscribe [:username])        
        {:keys [author string time]} first-comment]
    (fn [data uid]
      [:> VStack (merge
                  (when-not (:hide? @state)
                    {:bg "background.upper"})
                  {:gridArea "comments"
                   :color "foreground.secondary"
                   :flex "1 0 auto"
                   :spacing 0
                   :borderRadius "md"
                   :align "stretch"})
       ;; add time, author, and preview
       [:> Button (merge
                   (when-not (:hide? @state)
                     {:bg "background.upper"
                      :borderColor "transparent"
                      :borderBottomRadius 0})
                   {:justifyContent "flex-start"
                    :color "foreground.secondary"
                    :variant "outline"
                    :size "sm"
                    :gap 2
                    :flex "1 0 auto"
                    :onClick #(swap! state update :hide? not)})
        (if (:hide? @state)
          [:<>
           [:> ChevronRightIcon]
           [:> CommentCounter {:count num-comments}]
           [:> Text "Comments"]]
          [:<>
           [:> ChevronDownIcon]
           [:> CommentCounter {:count num-comments}]
           [:> Text "Comments"]])]

       (when-not (:hide? @state)
         [:> Box {:pl 8
                  :pr 4
                  :pb 4}
          (for [item data]
            ^{:key item}
            [comment-el item])
          [:> InlineCommentInput
           {:onSubmitComment #(re-frame.core/dispatch [:comment/write-comment uid % username])}]])])))
