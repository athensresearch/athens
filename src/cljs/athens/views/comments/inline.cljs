(ns athens.views.comments.inline
  (:require
    ["/components/Block/Anchor" :refer [Anchor]]
    ["/components/Comments/Comments" :refer [CommentCounter CommentContainer]]
    ["/components/Icons/Icons" :refer [ChevronDownIcon ChevronRightIcon BlockEmbedIcon]]
    ["@chakra-ui/react" :refer [Button Box Text VStack Avatar HStack Badge]]
    [athens.common.utils :as common.utils]
    [athens.parse-renderer :as parse-renderer]
    [athens.reactive :as reactive]
    [athens.util :as util]
    [athens.views.blocks.content :as b-content]
    [athens.views.comments.core :as comments.core]
    [clojure.string :as str]
    [goog.events :as events]
    [re-frame.core :as rf]
    [reagent.core :as r]))


(defn copy-comment-uid
  [comment-data]
  (let [uid (:block/uid comment-data)
        ref (str "((" uid "))")]
    (.. js/navigator -clipboard (writeText ref))
    (util/toast (clj->js {:status "info"
                          :position "top-right"
                          :title "Copied uid to clipboard"}))))


(defn comment-el
  [item is-followup?]
  (let [{:keys [string _time author block/uid]} item
        linked-refs (reactive/get-reactive-linked-references [:block/uid uid])
        linked-refs-count (count linked-refs)
        on-copy-comment-ref #(copy-comment-uid item)
        menu (clj->js [{:children "Copy comment ref"
                        :icon (r/as-element [:> BlockEmbedIcon])
                        :onClick on-copy-comment-ref}])]
    (fn []
      [:> CommentContainer {:menu menu}
       [:> HStack {:gridArea "byline"
                   :alignItems "center"
                   :lineHeight 1.25}
        (when (and
                (not is-followup?)
                author)
          [:> Avatar {:name author :color "#fff"}]
          [:> Text {:fontWeight "bold"
                    :fontSize "sm"
                    :noOfLines 0}
           author])
        (when _time
          [:> Text {:fontSize "xs"
                    :_hover {:color "foreground.secondary"}
                    :color "foreground.tertiary"}
           _time])]
       [:> Anchor {:menuActions menu
                   :ml "0.25em"
                   :height "2em"}]
       [:> Box {:flex "1 1 100%"
                :gridArea "comment"
                :overflow "hidden"
                :fontSize "sm"
                :ml 1
                :sx {"> *" {:lineHeight 1.5}}}
        ;; In future this should be rendered differently for reply type and ref-type
        [athens.parse-renderer/parse-and-render string uid]]
       (when (pos? linked-refs-count)
         [:> Badge {:size "xs"
                    :m 1.5
                    :mr 0
                    :alignSelf "baseline"
                    :lineHeight "1.5"
                    :gridArea "refs"} linked-refs-count])])))


(defn inline-comments
  [data _uid hide?]
  ;; TODO : Remove this state
  (when (comments.core/enabled?)
    (let [state           (reagent.core/atom {:hide? hide?})
          num-comments    (count data)
          block-uid       (common.utils/gen-block-uid)
          value-atom      (r/atom "")
          show-edit-atom? (r/atom false)]
      (fn [data uid]
        [:> VStack (merge
                     (when-not (:hide? @state)
                       {:bg "background.upper"
                        :mb 4})
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
             [:> Text {:pl 1.5} "Comments"]]
            [:<>
             [:> ChevronDownIcon]
             [:> CommentCounter {:count num-comments}]
             [:> Text {:pl 1.5} "Comments"]])]

         (when-not (:hide? @state)
           [:> Box {:pl 8
                    :pr 4
                    :pb 4}
            (for [item data]
              ^{:key item}
              [:<>
               [comment-el item]])

            (let [block-o           {:block/uid      block-uid
                                     ;; :block/string   @value-atom
                                     :block/children []}
                  save-fn           #(reset! value-atom %)
                  enter-handler     (fn jetsam-enter-handler
                                      [_uid _d-key-down]
                                      (when (not (str/blank? @value-atom))
                                        (re-frame.core/dispatch [:comment/write-comment uid @value-atom])
                                        (reset! value-atom "")
                                        (rf/dispatch [:editing/uid block-uid])))
                  tab-handler       (fn jetsam-tab-handler
                                      [_uid _embed-id _d-key-down])
                  backspace-handler (fn jetsam-backspace-handler
                                      [_uid _value])
                  delete-handler    (fn jetsam-delete-handler
                                      [_uid _d-key-down])
                  state-hooks       {:save-fn                 #(println "save-fn" (pr-str %))
                                     :update-fn               #(save-fn %)
                                     :idle-fn                 #(println "idle-fn" (pr-str %))
                                     :read-value              value-atom
                                     :show-edit?              show-edit-atom?
                                     :enter-handler           enter-handler
                                     :tab-handler             tab-handler
                                     :backspace-handler       backspace-handler
                                     :delete-handler          delete-handler
                                     :default-verbatim-paste? true
                                     :keyboard-navigation?    false
                                     :placeholder             "Write your comment here"}]
              (rf/dispatch [:editing/uid block-uid])
              [:> Box {:px 2
                       :mt 2
                       :minHeight "2.125em"
                       :borderRadius "sm"
                       :bg "background.attic"
                       :cursor "text"
                       :_focusWithin {:shadow "focus"}}
               [b-content/block-content-el block-o state-hooks]])])]))))
