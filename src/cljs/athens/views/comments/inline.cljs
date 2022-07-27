(ns athens.views.comments.inline
  (:require
    ["/components/Block/Anchor" :refer [Anchor]]
    ["/components/Comments/Comments" :refer [CommentCounter CommentContainer]]
    ["/components/Icons/Icons" :refer [ChevronDownIcon ChevronRightIcon BlockEmbedIcon]]
    ["/timeAgo.js" :refer [timeAgo]]
    ["@chakra-ui/react" :refer [Button Box Text VStack Avatar HStack Badge]]
    [athens.common.utils :as common.utils]
    [athens.parse-renderer :as parse-renderer]
    [athens.reactive :as reactive]
    [athens.util :as util]
    [athens.views.blocks.content :as b-content]
    [athens.views.comments.core :as comments.core]
    [clojure.string :as str]
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
  [item]
  (let [{:keys [string time author block/uid is-followup?]} item
        linked-refs (reactive/get-reactive-linked-references [:block/uid uid])
        linked-refs-count (count linked-refs)
        on-copy-comment-ref #(copy-comment-uid item)
        menu (clj->js [{:children "Copy comment ref"
                        :icon (r/as-element [:> BlockEmbedIcon])
                        :onClick on-copy-comment-ref}])
        human-timestamp (timeAgo time)]

    (fn []
      [:> CommentContainer {:menu menu :isFollowUp is-followup?}
       [:> HStack {:gridArea "byline"
                   :alignItems "center"
                   :lineHeight 1.25}
        ;; if is-followup?, hide byline and avatar
        ;; else show avatar and byline

        (when-not is-followup?
          [:<>
           [:> Avatar {:name author :color "#fff" :size "xs"}]
           [:> Text {:fontWeight "bold"
                     :fontSize   "sm"
                     :noOfLines  0}
            author]

           [:> Text {:fontSize "xs"
                     :_hover   {:color "foreground.secondary"}
                     :color    "foreground.tertiary"}
            human-timestamp]])]

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
  [_data _uid hide?]
  (when (comments.core/enabled?)
    (let [hide?           (r/atom hide?)
          block-uid       (common.utils/gen-block-uid)
          value-atom      (r/atom "")
          show-edit-atom? (r/atom true)]
      (fn [data uid _hide?]
        (let [num-comments (count data)
              username     (rf/subscribe [:username])
              ;; hacky way to detect if user just wanted to start the first comment, but the block-uid of the textarea
              ;; isn't accessible globally
              focus-textarea-if-opening-first-time #(when (zero? num-comments)
                                                      (rf/dispatch [:editing/uid block-uid]))]

          [:> VStack (merge
                       (when-not @hide?
                         {:bg "background.upper"
                          :mb 4})
                       {:gridArea "comments"
                        :color "foreground.secondary"
                        :flex "1 0 auto"
                        :spacing 0
                        :borderRadius "md"
                        :align "stretch"})
           [:> Button (merge
                        (when-not @hide?
                          {:bg "background.upper"
                           :borderColor "transparent"
                           :borderBottomRadius 0})
                        {:justifyContent "flex-start"
                         :color "foreground.secondary"
                         :variant "outline"
                         :size "sm"
                         :gap 2
                         :flex "1 0 auto"
                         :onClick #(swap! @hide? not)})
            (if @hide?
              [:<>
               [:> ChevronRightIcon]
               [:> CommentCounter {:count num-comments}]
               [:> Text {:pl 1.5} "Comments"]]
              [:<>
               [:> ChevronDownIcon]
               [:> CommentCounter {:count num-comments}]
               [:> Text {:pl 1.5} "Comments"]])]

           (when-not @hide?
             [:> Box {:pl 8
                      :pr 4
                      :pb 4}
              (for [item data]
                ^{:key item}
                [comment-el item])

              (let [block-o           {:block/uid      block-uid
                                       ;; :block/string   @value-atom
                                       :block/children []}
                    blur-fn           #(when (not (seq @value-atom))
                                         (rf/dispatch [:comment/hide-comment-textarea]))
                    save-fn           #(reset! value-atom %)
                    enter-handler     (fn jetsam-enter-handler
                                        [_uid _d-key-down]
                                        (when (not (str/blank? @value-atom))
                                          ;; Passing username because we need the username for other ops before the block is created.
                                          (rf/dispatch [:comment/write-comment uid @value-atom @username])
                                          (reset! value-atom "")
                                          (rf/dispatch [:editing/uid block-uid])))
                    tab-handler       (fn jetsam-tab-handler
                                        [_uid _embed-id _d-key-down])
                    backspace-handler (fn jetsam-backspace-handler
                                        [_uid _value])
                    delete-handler    (fn jetsam-delete-handler
                                        [_uid _d-key-down])
                    state-hooks       {:save-fn                 blur-fn
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
                                       :style                   {:opacity 1}
                                       :placeholder             "Write your comment here"}]
                (focus-textarea-if-opening-first-time)
                [:> Box {:px 2
                         :mt 2
                         :minHeight "2.125em"
                         :borderRadius "sm"
                         :bg "background.attic"
                         :cursor "text"
                         :_focusWithin {:shadow "focus"}}
                 [b-content/block-content-el block-o state-hooks]])])])))))
