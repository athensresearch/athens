(ns athens.views.comments.inline
  (:require
    ["/components/Block/Anchor"      :refer [Anchor]]
    ["/components/Comments/Comments" :refer [CommentContainer]]
    ["/components/Icons/Icons"       :refer [ChevronDownIcon ChevronRightIcon BlockEmbedIcon PencilIcon TrashIcon]]
    ["/timeAgo.js"                   :refer [timeAgo]]
    ["@chakra-ui/react"              :refer [AvatarGroup Button Box Text VStack Avatar HStack Badge]]
    [athens.common-events            :as common-events]
    [athens.common-events.graph.ops  :as graph-ops]
    [athens.common.logging           :as log]
    [athens.common.utils             :as common.utils]
    [athens.db                       :as db]
    [athens.parse-renderer           :as parse-renderer]
    [athens.reactive                 :as reactive]
    [athens.util                     :as util]
    [athens.views.blocks.editor      :as editor]
    [athens.views.comments.core      :as comments.core]
    [clojure.string                  :as str]
    [re-frame.core                   :as rf]
    [reagent.core                    :as r]))


(defn copy-comment-uid
  [uid]
  (let [ref (str "((" uid "))")]
    (.. js/navigator -clipboard (writeText ref))
    (util/toast (clj->js {:status "info"
                          :position "top-right"
                          :title "Copied uid to clipboard"}))))


(rf/reg-event-fx
  :comment/remove-comment
  (fn [_ [_ uid]]
    (log/debug ":comment/remove-comment:" uid)
    {:fx [[:dispatch [:resolve-transact-forward (-> (graph-ops/build-block-remove-op @db/dsdb uid)
                                                    (common-events/build-atomic-event))]]]}))


(rf/reg-event-fx
  :comment/edit-comment
  (fn [_ [_ uid]]
    {:fx [[:dispatch [:editing/uid uid :end]]]}))


(rf/reg-event-fx
  :comment/update-comment
  (fn [_ [_ uid string]]
    (log/debug ":comment/update-comment:" uid string)
    {:fx [[:dispatch-n [[:resolve-transact-forward (-> (graph-ops/build-block-save-op @db/dsdb uid string)
                                                       (common-events/build-atomic-event))]
                        [:properties/update-in [:block/uid uid] ["athens/comment/edited"] (fn [db prop-uid]
                                                                                            [(graph-ops/build-block-save-op db prop-uid "")])]
                        [:editing/uid nil]]]]}))


(defn create-menu
  [{:keys [block/uid]} current-user-is-author?]
  (->> [{:children "Copy comment ref"
         :icon     (r/as-element [:> BlockEmbedIcon])
         :onClick  #(copy-comment-uid uid)}
        (when current-user-is-author?
          {:children "Edit"
           :icon     (r/as-element [:> PencilIcon])
           :onClick  #(rf/dispatch [:comment/edit-comment uid])})
        (when current-user-is-author?
          {:children "Delete"
           :icon     (r/as-element [:> TrashIcon])
           :onClick  #(rf/dispatch [:comment/remove-comment uid])})]
       (filterv seq)))


(defn comment-el
  [item]
  (let [{:keys [string time author block/uid is-followup? edited?]} item
        linked-refs       (reactive/get-reactive-linked-references [:block/uid uid])
        linked-refs-count (count linked-refs)
        current-username  (rf/subscribe [:presence/current-username])
        human-timestamp   (timeAgo time)
        is-editing        (rf/subscribe [:editing/is-editing uid])
        value-atom        (r/atom string)
        show-edit-atom?   (r/atom true)]

    (fn []
      (let [current-user-is-author? (= author @current-username)
            menu                    (create-menu item current-user-is-author?)]
        [:> CommentContainer {:menu menu :isFollowUp is-followup? :isEdited edited?}

         ;; if is-followup?, hide byline and avatar
         ;; else show avatar and byline
         (when-not is-followup?
           [:> HStack {:gridArea   "byline"
                       :alignItems "center"
                       :pt 1
                       :lineHeight 1.25}
            [:<>
             [:> Avatar {:name author :color "#fff" :size "xs"}]
             [:> Text {:fontWeight "bold"
                       :fontSize   "sm"
                       :noOfLines  0}
              author]
             [:> Text {:fontSize "xs"
                       :color    "foreground.secondary"}
              human-timestamp]]])

         [:> Anchor {:ml 0.5
                     :height "2em"}]
         [:> Box {:flex "1 1 100%"
                  :gridArea "comment"
                  :alignItems "center"
                  :display "flex"
                  :overflow "hidden"
                  :fontSize "sm"
                  :py 1
                  :ml 1
                  :sx {"> *" {:lineHeight 1.5}}}
          ;; In future this should be rendered differently for reply type and ref-type
          (if-not @is-editing
            [:<>
             [athens.parse-renderer/parse-and-render string uid]
             (when edited?
               [:> Text {:fontSize "xs"
                         :as       "span"
                         :marginLeft "0.5em"
                         :color    "foreground.tertiary"}
                "(edited)"])]
            (let [block-o           {:block/uid      uid
                                     :block/string   string
                                     :block/children []}
                  blur-fn           #(prn "blur-fn")
                  save-fn           #(reset! value-atom %)
                  enter-handler     (fn jetsam-enter-handler
                                      [_uid _d-key-down]
                                      (rf/dispatch [:comment/update-comment uid @value-atom]))
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
                                     :style                   {:opacity 1
                                                               :background-color "var(--chakra-colors-background-attic)"
                                                               :minHeight "100%"
                                                               :border-radius "5px"}}]
              [editor/block-editor block-o state-hooks]))]

         (when (pos? linked-refs-count)
           [:> Badge {:size "xs"
                      :m 1.5
                      :mr 0
                      :alignSelf "baseline"
                      :lineHeight "1.5"
                      :gridArea "refs"} linked-refs-count])]))))


(defn comments-disclosure
  [hide? num-comments last-comment]
  [:> Button (merge
              {:justifyContent "flex-start"
               :color          "foreground.secondary"
               :variant        "ghost"
               :size           "xs"
               :minHeight      7
               :flex           "1 0 auto"
               :bg             "background.upper"
               :borderRadius "none"
               :leftIcon      (if @hide?
                                (r/as-element [:> ChevronRightIcon {:ml 1}])
                                (r/as-element [:> ChevronDownIcon {:ml 1}]))
               :sx {":after" {:content "''"
                              :opacity (if @hide? 0 1)
                              :position "absolute"
                              :bottom 0
                              :transition "inherit"
                              :left 9
                              :right 0
                              :borderBottom "1px solid"
                              :borderBottomColor "separator.divider"}
                    ":hover:after" {:opacity 0}}
               :onClick        #(reset! hide? (not @hide?))}
              (when @hide?
                {:bg "transparent"
                 :borderColor        "transparent"}))
   [:> HStack
    [:> AvatarGroup [:> Avatar {:size "xs" :name (:author last-comment)}]]
    [:> Text (str num-comments " comments")]
    [:> Text {:color "foreground.tertiary"}
     (timeAgo (:time last-comment))]]])


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
              last-comment  (last data)
              ;; hacky way to detect if user just wanted to start the first comment, but the block-uid of the textarea
              ;; isn't accessible globally
              focus-textarea-if-opening-first-time #(when (zero? num-comments)
                                                      (rf/dispatch [:editing/uid block-uid]))]

          [:> VStack (merge
                       {:gridArea "comments"
                        :color "foreground.secondary"
                        :flex "1 0 auto"
                        :bg "background.upper"
                        :mb 2
                        :borderWidth "1px"
                        :borderStyle "solid"
                        :borderColor "separator.border"
                        :overflow "hidden"
                        :spacing 0
                        :borderRadius "md"
                        :align "stretch"}
                       (when @hide?
                         {:bg "transparent"
                          :borderColor "separator.divider"}))

           [comments-disclosure hide? num-comments last-comment]

           (when-not @hide?
             [:> Box {:p 2}
              (for [item data]
                ^{:key item}
                [comment-el item])

              (let [block-o           {:block/uid      block-uid
                                       ;; :block/string   @value-atom
                                       :block/children []}
                    blur-fn           #(when (not (seq @value-atom))
                                         (rf/dispatch [:comment/hide-editor]))
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
                 [editor/block-editor block-o state-hooks]])])])))))
