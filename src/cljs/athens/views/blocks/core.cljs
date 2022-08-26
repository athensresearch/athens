(ns athens.views.blocks.core
  (:require
    ["/components/Block/Anchor"                :refer [Anchor]]
    ["/components/Block/Container"             :refer [Container]]
    ["/components/Block/PropertyName"          :refer [PropertyName]]
    ["/components/Block/Reactions"             :refer [Reactions]]
    ["/components/Block/Toggle"                :refer [Toggle]]
    ["/components/Icons/Icons"                 :refer [ArchiveIcon
                                                       ArrowRightOnBoxIcon
                                                       BlockEmbedIcon
                                                       ChatBubbleIcon
                                                       ExpandIcon
                                                       TextIcon]]
    ["/components/References/InlineReferences" :refer [ReferenceBlock
                                                       ReferenceGroup]]
    ["@chakra-ui/react"                        :refer [Box
                                                       Breadcrumb
                                                       BreadcrumbItem
                                                       BreadcrumbLink
                                                       Button
                                                       Divider
                                                       HStack
                                                       MenuDivider
                                                       MenuGroup
                                                       MenuItem
                                                       VStack]]
    ["react"                                   :as react]
    ["react-intersection-observer"             :refer [useInView]]
    [athens.common-db                          :as common-db]
    [athens.common-events.graph.ops            :as graph-ops]
    [athens.common.logging                     :as log]
    [athens.db                                 :as db]
    [athens.electron.images                    :as images]
    [athens.electron.utils                     :as electron.utils]
    [athens.events.dragging                    :as drag.events]
    [athens.events.inline-refs                 :as inline-refs.events]
    [athens.events.linked-refs                 :as linked-ref.events]
    [athens.events.selection                   :as select-events]
    [athens.parse-renderer                     :as parse-renderer]
    [athens.reactive                           :as reactive]
    [athens.router                             :as router]
    [athens.self-hosted.presence.views         :as presence]
    [athens.subs.dragging                      :as drag.subs]
    [athens.subs.inline-refs                   :as inline-refs.subs]
    [athens.subs.linked-refs                   :as linked-ref.subs]
    [athens.subs.selection                     :as select-subs]
    [athens.time-controls                      :as time-controls]
    [athens.types.core                         :as types]
    ;; need to require it for multimethod participation
    [athens.types.default.view]
    [athens.types.dispatcher                   :as block-type-dispatcher]
    [athens.types.query.view]
    ;; need to require it for multimethod participation
    [athens.types.tasks.view]
    [athens.util                               :as util]
    [athens.views.blocks.bullet                :as block-bullet]
    [athens.views.blocks.context-menu          :as ctx-menu]
    [athens.views.blocks.drop-area-indicator   :as drop-area-indicator]
    [athens.views.blocks.reactions             :as block-reaction]
    [athens.views.comments.core                :as comments]
    [athens.views.comments.inline              :as inline-comments]
    [athens.views.notifications.actions        :as actions]
    [com.rpl.specter                           :as s]
    [re-frame.core                             :as rf]
    [reagent.core                              :as r]))


;; Components


(defn block-drag-over
  "If block or ancestor has CSS dragging class, do not show drop indicator; do not allow block to drop onto itself.
  If above midpoint, show drop indicator above block.
  If no children and over X pixels from the left, show child drop indicator.
  If below midpoint, show drop indicator below."
  [e block]
  (.. e preventDefault)
  (.. e stopPropagation)
  (let [{:block/keys [children
                      uid
                      open]} block
        closest-container    (.. e -target (closest ".block-container"))
        {:keys [x y]}        (util/mouse-offset e closest-container)
        middle-y             (util/vertical-center closest-container)
        dragging-ancestor    (.. e -target (closest ".dragging"))
        dragging?            dragging-ancestor
        is-selected?         @(rf/subscribe [::select-subs/selected? uid])
        target               (cond
                               dragging?           nil
                               is-selected?        nil
                               (or (neg? y)
                                   (< y middle-y)) :before
                               (and (< middle-y y)
                                    (> 50 x))      :after
                               (or (not open)
                                   (and (empty? children)
                                        (< 50 x))) :first)
        prev-target          @(rf/subscribe [::drag.subs/drag-target uid])]
    (when (and target
               (not= prev-target target))
      (rf/dispatch [::drag.events/set-drag-target! uid target]))))


(defn block-drag-leave
  "When mouse leaves block, remove any drop area indicator.
  Ignore if target-uid and related-uid are the same — user went over a child component and we don't want flicker."
  [e block]
  (.. e preventDefault)
  (.. e stopPropagation)
  (let [{target-uid :block/uid} block
        related-uid (util/get-dataset-uid (.. e -relatedTarget))]
    (when-not (= related-uid target-uid)
      ;; (prn target-uid related-uid  "LEAVE")
      (rf/dispatch [::drag.events/cleanup! target-uid]))))


(defn drop-bullet
  "Terminology :
    - source-uid        : The block which is being dropped.
    - target-uid        : The block on which source is being dropped.
    - drag-target       : Represents where the block is being dragged. It can be `:first` meaning
                          dragged as a child, `:before` meaning the source block is dropped above the
                          target block, `:after` meaning the source block is dropped below the target block.
    - action-allowed    : There can be 2 types of actions.
        - `link` action : When a block is DnD by dragging a bullet while
                         `shift` key is pressed to create a block link.
        - `move` action : When a block is DnD to other part of Athens page. "

  [source-uid target-uid drag-target action-allowed]
  (let [move-action? (= action-allowed "move")
        event         [(if move-action?
                         :block/move
                         :block/link)
                       {:source-uid source-uid
                        :target-uid target-uid
                        :target-rel drag-target}]]
    (log/debug "drop-bullet" (pr-str {:source-uid     source-uid
                                      :target-uid     target-uid
                                      :drag-target    drag-target
                                      :action-allowed action-allowed
                                      :event          event}))
    (rf/dispatch event)))


(defn drop-bullet-multi
  "
  Terminology :
    - source-uids       : Uids of the blocks which are being dropped
    - target-uid        : Uid of the block on which source is being dropped"
  [source-uids target-uid drag-target]
  (let [source-uids          (mapv (comp first db/uid-and-embed-id) source-uids)
        target-uid           (first (db/uid-and-embed-id target-uid))
        event                (if (= drag-target :first)
                               [:drop-multi/child {:source-uids source-uids
                                                   :target-uid  target-uid}]
                               [:drop-multi/sibling {:source-uids source-uids
                                                     :target-uid  target-uid
                                                     :drag-target drag-target}])]
    (rf/dispatch [::select-events/clear])
    (rf/dispatch event)))


(defn block-drop
  "Handle dom drop events, read more about drop events at:
  : https://developer.mozilla.org/en-US/docs/Web/API/HTML_Drag_and_Drop_API#Define_a_drop_zone"

  [e block]
  (.. e stopPropagation)
  (let [{target-uid :block/uid} block
        [target-uid _]          (db/uid-and-embed-id target-uid)
        drag-target             @(rf/subscribe [::drag.subs/drag-target target-uid])
        source-uid              (.. e -dataTransfer (getData "text/plain"))
        effect-allowed          (.. e -dataTransfer -effectAllowed)
        items                   (array-seq (.. e -dataTransfer -items))
        item                    (first items)
        datatype                (.. item -type)
        img-regex               #"(?i)^image/(p?jpeg|gif|png)$"
        valid-text-drop         (and (not (nil? drag-target))
                                     (not= source-uid target-uid)
                                     (or (= effect-allowed "link")
                                         (= effect-allowed "move")))
        selected-items          @(rf/subscribe [::select-subs/items])]

    (cond
      (re-find img-regex datatype)     (when electron.utils/electron?
                                         (images/dnd-image target-uid drag-target item (second (re-find img-regex datatype))))
      (re-find #"text/plain" datatype) (when valid-text-drop
                                         (if (empty? selected-items)
                                           (drop-bullet source-uid target-uid drag-target effect-allowed)
                                           (drop-bullet-multi selected-items target-uid drag-target))))

    (rf/dispatch [:mouse-down/unset])
    (rf/dispatch [::drag.events/cleanup! target-uid])))


(defn- block-open-toggle!
  [block-uid open]
  (rf/dispatch [:block/open {:block-uid block-uid
                             :open?     open}]))


(defn block-refs-count-el
  [count click-fn active?]
  [:> Button {:gridArea "refs"
              :size "xs"
              :ml "1em"
              :mt 1
              :mr 1
              :zIndex 10
              :visibility (if (pos? count) "visible" "hidden")
              :isActive active?
              :onClick (fn [e]
                         (.. e stopPropagation)
                         (click-fn e))}
   count])


(defn ref-comp
  [block-el block]
  (let [orig-uid            (:block/uid block)
        has-children?       (-> block :block/children boolean)
        parents             (cond-> (:block/parents block)
                              ;; If the ref has children, move it to breadcrumbs and show children.
                              has-children? (conj block))
        state-reset         {:block    block
                             :embed-id (random-uuid)
                             :open?    true
                             :parents  parents
                             :focus?   (not has-children?)}
        linked-ref-data     {:linked-ref     true
                             :initial-open   false
                             :linked-ref-uid (:block/uid block)
                             :parent-uids    (set (map :block/uid (:block/parents block)))}
        inline-ref-open?    (rf/subscribe [::inline-refs.subs/state-open? orig-uid])
        inline-ref-focus?   (rf/subscribe [::inline-refs.subs/state-focus? orig-uid])
        inline-ref-block    (rf/subscribe [::inline-refs.subs/state-block orig-uid])
        inline-ref-parents  (rf/subscribe [::inline-refs.subs/state-parents orig-uid])
        inline-ref-embed-id (rf/subscribe [::inline-refs.subs/state-embed-id orig-uid])]
    ;; Reset state on parent each time the component is created.
    ;; To clear state, open/close the inline refs.
    (rf/dispatch [::inline-refs.events/set-state! orig-uid state-reset])
    (fn [_ _]
      (let [block (reactive/get-reactive-block-document (:db/id @inline-ref-block))]
        [:<>
         [:> HStack {:lineHeight "1"}
          [:> Toggle {:isOpen   @inline-ref-open?
                      :on-click (fn [e]
                                  (.. e stopPropagation)
                                  (rf/dispatch [::inline-refs.events/toggle-state-open! orig-uid]))}]

          [:> Breadcrumb {:fontSize "xs" :color "foreground.secondary"}
           (doall
             (for [{:keys [block/uid] :as breadcrumb-block}
                   (if (or @inline-ref-open?
                           (not @inline-ref-focus?))
                     @inline-ref-parents
                     (conj @inline-ref-parents block))]
               [:> BreadcrumbItem {:key (str "breadcrumb-" uid)}
                [:> BreadcrumbLink {:onClick (fn [e]
                                               (let [shift? (.-shiftKey e)]
                                                 (rf/dispatch [:reporting/navigation {:source :block-bullet
                                                                                      :target :block
                                                                                      :pane   (if shift?
                                                                                                :right-pane
                                                                                                :main-pane)}])
                                                 (let [new-B (db/get-block [:block/uid uid])
                                                       new-P (concat
                                                               (take-while (fn [b] (not= (:block/uid b) uid)) @inline-ref-parents)
                                                               [breadcrumb-block])]
                                                   (.. e stopPropagation)
                                                   (rf/dispatch [::inline-refs.events/set-block! orig-uid new-B])
                                                   (rf/dispatch [::inline-refs.events/set-parents! orig-uid new-P])
                                                   (rf/dispatch [::inline-refs.events/set-focus! orig-uid false]))))}
                 [parse-renderer/parse-and-render (common-db/breadcrumb-string @db/dsdb uid) uid]]]))]]

         (when @inline-ref-open?
           (if @inline-ref-focus?

             ;; Display the single child block only when focusing.
             ;; This is the default behaviour for a ref without children, for brevity.
             [:div.block-embed {:fontSize "0.7em"}
              [:f> block-el
               (util/recursively-modify-block-for-embed block @inline-ref-embed-id)
               linked-ref-data
               {:block-embed? true}]]


             ;; Otherwise display children of the parent directly.
             (for [child (:block/children block)]
               [:<> {:key (:db/id child)}
                [:f> block-el
                 (util/recursively-modify-block-for-embed child @inline-ref-embed-id)
                 linked-ref-data
                 {:block-embed? true}]])))]))))


(defn inline-linked-refs-el
  [block-el uid]
  (let [refs (reactive/get-reactive-linked-references [:block/uid uid])]
    (when (not-empty refs)
      [:> VStack {:as "aside"
                  :align "stretch"
                  :spacing 3
                  :key "Inline Linked References"
                  :zIndex 2
                  :ml 8
                  :pl 4
                  :p2 2
                  :borderRadius "md"
                  :background "background.basement"}
       (doall
         (for [[group-title group] refs]
           [:> ReferenceGroup {:title group-title
                               :key (str "group-" group-title)}
            (doall
              (for [block' group]
                [:> ReferenceBlock {:key (str "ref-" (:block/uid block'))}
                 [ref-comp block-el block']]))]))])))


(defn convert-anon-block-to-task
  [block]
  (let [{:block/keys [uid string]} block
        entity-type-event          [:graph/update-in [:block/uid uid] [":entity/type"]
                                    (fn [db entity-type-uid]
                                      [(graph-ops/build-block-save-op db entity-type-uid "[[athens/task]]")])]
        task-title-event           [:graph/update-in [:block/uid uid] [":task/title"]
                                    (fn [db task-title-uid]
                                      [(graph-ops/build-block-save-op db task-title-uid string)])]]
    (log/debug "convert to task"
               (pr-str {:uid               uid
                        :string            string
                        :entity-type-event entity-type-event
                        :task-title-event  task-title-event}))
    (rf/dispatch entity-type-event)
    (rf/dispatch task-title-event)))


(defn block-el
  "Two checks dec to make sure block is open or not: children exist and :block/open bool"
  ([block]
   [:f> block-el block {:linked-ref false} {}])
  ([block linked-ref-data]
   [:f> block-el block linked-ref-data {}])
  ([block linked-ref-data _opts]
   (let [[block-uid _embed-id]    (-> block :block/uid common-db/uid-and-embed-id)
         {:keys [initial-open
                 parent-uids
                 linked-ref
                 linked-ref-uid]} linked-ref-data
         ident                    [:block/uid block-uid]
         show-edit?               (r/atom false)
         hide-edit-fn             #(reset! show-edit? false)
         show-edit-fn             #(reset! show-edit? true)
         linked-ref-open?         (rf/subscribe [::linked-ref.subs/open? block-uid])
         dragging?                (rf/subscribe [::drag.subs/dragging? block-uid])
         drag-target              (rf/subscribe [::drag.subs/drag-target block-uid])
         selected?                (rf/subscribe [::select-subs/selected? block-uid])
         present-user             (rf/subscribe [:presence/has-presence block-uid])
         selected-items           (rf/subscribe [::select-subs/items])
         feature-flags            (rf/subscribe [:feature-flags])
         current-user             (rf/subscribe [:presence/current-user])
         show-comments?           (rf/subscribe [:comment/show-comments?])
         show-textarea?           (rf/subscribe [:comment/show-editor? block-uid])
         inline-refs-open?        (rf/subscribe [::inline-refs.subs/open? block-uid])
         enable-properties?       (rf/subscribe [:feature-flags/enabled? :properties])
         on-block-mount           (fn []
                                    (rf/dispatch [::linked-ref.events/set-open! block-uid (or (false? linked-ref) initial-open)])
                                    (rf/dispatch [::inline-refs.events/set-open! block-uid false]))
         on-unmount-block         (fn []
                                    (rf/dispatch [::linked-ref.events/cleanup! block-uid])
                                    (rf/dispatch [::inline-refs.events/cleanup! block-uid]))]

     (fn block-core-render
       [block linked-ref-data opts]
       (let [block-o                (reactive/get-reactive-block-document ident)
             {:block/keys [uid
                           open
                           children
                           key
                           properties
                           _refs]}  (merge block block-o)
             block-type             (reactive/reactive-get-entity-type [:block/uid block-uid])
             children-uids          (set (map :block/uid children))
             children?              (seq children-uids)
             presence?              (seq @present-user)
             comments-enabled?      (:comments @feature-flags)
             reactions-enabled?     (:reactions @feature-flags)
             notifications-enabled? (:notifications @feature-flags)
             uid-sanitized-block    (s/transform
                                      (util/specter-recursive-path #(contains? % :block/uid))
                                      (fn [{:block/keys [original-uid uid] :as block}]
                                        (assoc block :block/uid (or original-uid uid)))
                                      block)
             user-id                (or (:username @current-user)
                                        ;; We use empty string for when there is no user information, like in PKM.
                                        "")
             reactions              (and reactions-enabled?
                                         (block-reaction/props->reactions properties))
             menu                   (r/as-element
                                      [:> MenuGroup
                                       (when (< (count @selected-items) 2)
                                         [:> MenuItem {:children "Open block"
                                                       :icon     (r/as-element [:> ExpandIcon])
                                                       :onClick  (fn [e]
                                                                   (let [shift? (.-shiftKey e)]
                                                                     (rf/dispatch [:reporting/navigation {:source :block-bullet
                                                                                                          :target :block
                                                                                                          :pane   (if shift?
                                                                                                                    :right-pane
                                                                                                                    :main-pane)}])
                                                                     (router/navigate-uid uid e)))}])
                                       [:> MenuItem {:children "Open in right sidebar"
                                                     :icon     (r/as-element [:> ArrowRightOnBoxIcon])
                                                     :onClick  (fn [_]
                                                                 (rf/dispatch [:reporting/navigation {:source :block-bullet
                                                                                                      :target :block
                                                                                                      :pane   :right-pane}])
                                                                 (rf/dispatch [:right-sidebar/open-item [:block/uid uid]]))}]
                                       (when-not (= block-type "[[athens/task]]")
                                         [:> MenuItem {:children "Convert to Task"
                                                       :icon     (r/as-element [:> BlockEmbedIcon])
                                                       :onClick  #(convert-anon-block-to-task block-o)}])
                                       [:> MenuItem {:children (if (> (count @selected-items) 1)
                                                                 "Copy selected block refs"
                                                                 "Copy block ref")
                                                     :icon     (r/as-element [:> BlockEmbedIcon])
                                                     :onClick  #(ctx-menu/handle-copy-refs nil uid)}]
                                       [:> MenuItem {:children "Copy unformatted text"
                                                     :icon     (r/as-element [:> TextIcon])
                                                     :onClick  #(ctx-menu/handle-copy-unformatted uid)}]
                                       (when comments-enabled?
                                         [:> MenuItem {:children "Add comment"
                                                       :onClick  #(ctx-menu/handle-click-comment % uid)
                                                       :icon     (r/as-element [:> ChatBubbleIcon])}])
                                       (when reactions-enabled?
                                         [:<>
                                          [:> MenuDivider]
                                          [block-reaction/reactions-menu-list uid user-id]])

                                       (when (and notifications-enabled? (actions/is-block-inbox? properties))
                                         [:<>
                                          [:> Divider]
                                          [:> MenuItem {:children "Archive all notifications"
                                                        :icon     (r/as-element [:> ArchiveIcon])
                                                        :onClick  #(actions/archive-all-notifications uid)}]
                                          [:> MenuItem {:children "Unarchive all notifications"
                                                        :icon     (r/as-element [:> ArchiveIcon])
                                                        :onClick  #(actions/unarchive-all-notifications uid)}]])

                                       (when (and notifications-enabled? (actions/is-block-notification? properties))
                                         [:> MenuItem {:children "Archive"
                                                       :icon     (r/as-element [:> ArchiveIcon])
                                                       :onClick  #(rf/dispatch (actions/update-state-prop uid "athens/notification/is-archived" "true"))}])])
             ff             @(rf/subscribe [:feature-flags])
             renderer-k     (block-type-dispatcher/block-type->protocol-k block-type ff)
             renderer       (block-type-dispatcher/block-type->protocol renderer-k {:linked-ref-data linked-ref-data})
             [ref in-view?] (useInView {:delay 250})
             _              (react/useEffect (fn []
                                               (on-block-mount)
                                               on-unmount-block)
                                             #js [])]
         #_(log/debug "block open render: block-o:" (pr-str (:block/open block-o))
                    "block:" (pr-str (:block/open block))
                    "merge:" (pr-str (:block/open (merge block-o block))))

         [:> Container {:isHidden     (actions/archived-notification? properties)
                        :isDragging   (and @dragging? (not @selected?))
                        :isSelected   @selected?
                        :hasChildren  (seq children)
                        :isOpen       open
                        :isLinkedRef  (and (false? initial-open) (= uid linked-ref-uid))
                        :hasPresence  presence?
                        :uid          uid
                        ;; need to know children for selection resolution
                        :childrenUids children-uids
                        ;; show-edit? allows us to render the editing elements (like the textarea)
                        ;; even when not editing this block. When true, clicking the block content will pass
                        ;; the clicks down to the underlying textarea. The textarea is expensive to render,
                        ;; so we avoid rendering it when it's not needed.
                        :onMouseEnter show-edit-fn
                        :onMouseLeave hide-edit-fn
                        :onDragOver   (fn [e]
                                        (block-drag-over e block))
                        :onDragLeave  (fn [e]
                                        (block-drag-leave e block))
                        :onDrop       (fn [e]
                                        (block-drop e block))
                        :menu         menu
                        :style        (merge {} (time-controls/block-styles block-o))}

          (when (= @drag-target :before) [drop-area-indicator/drop-area-indicator {:placement "above"}])

          [:<>
           [:div.block-body {:ref ref}
            (when (and children?
                       (or (seq children)
                           (seq properties)))
              (when in-view?
                [:> Toggle {:isOpen  (if (or (and (true? linked-ref) @linked-ref-open?)
                                             (and (false? linked-ref) open))
                                       true
                                       false)
                            :onClick (fn [e]
                                       (.. e stopPropagation)
                                       (if (true? linked-ref)
                                         (rf/dispatch [::linked-ref.events/toggle-open! uid])
                                         (block-open-toggle! uid (not open))))}]))

            (when key
              [:> PropertyName {:name          (:node/title key)
                                :onClick       (fn [e]
                                                 (let [shift? (.-shiftKey e)]
                                                   (rf/dispatch [:reporting/navigation {:source :block-property
                                                                                        :target :page
                                                                                        :pane   (if shift?
                                                                                                  :right-pane
                                                                                                  :main-pane)}])
                                                   (router/navigate-page (:node/title key) e)))
                                :on-drag-start (fn [e]
                                                 (block-bullet/bullet-drag-start e uid))
                                :on-drag-stop  (fn [e]
                                                 (block-bullet/bullet-drag-end e uid))}])

            [:> Anchor {:isClosedWithChildren   (when (and (seq children)
                                                           (or (and (true? linked-ref) (not @linked-ref-open?))
                                                               (and (false? linked-ref) (not open))))
                                                  "closed-with-children")
                        :uidSanitizedBlock      uid-sanitized-block
                        :shouldShowDebugDetails (util/re-frame-10x-open?)
                        :menu                   menu
                        :onDoubleClick          (fn [e]
                                                  (let [shift? (.-shiftKey e)]
                                                    (rf/dispatch [:reporting/navigation {:source :block-bullet
                                                                                         :target :block
                                                                                         :pane   (if shift?
                                                                                                   :right-pane
                                                                                                   :main-pane)}])
                                                    (router/navigate-uid uid e)))
                        :on-drag-start          (fn [e]
                                                  (block-bullet/bullet-drag-start e uid))
                        :on-drag-end            (fn [e]
                                                  (block-bullet/bullet-drag-end e uid))
                        :unreadNotification     (actions/unread-notification? properties)}]

            ;; `BlockTypeProtocol` dispatch placement
            [:> Box {:gridArea "content"}
             ^{:key renderer-k}
             [types/outline-view renderer block {:show-edit? show-edit?}]]

            (when (and in-view? reactions-enabled? reactions)
              [:> Reactions {:reactions        (clj->js reactions)
                             :currentUser      user-id
                             :onToggleReaction (partial block-reaction/toggle-reaction [:block/uid uid])}])

            ;; Show comments when the toggle is on
            (when (and @show-comments?
                       (or @show-textarea?
                           (comments/get-comment-thread-uid @db/dsdb uid)))
              (cond
                @show-textarea? [inline-comments/inline-comments (comments/get-comments-in-thread @db/dsdb (comments/get-comment-thread-uid @db/dsdb uid)) uid false]
                :else           [inline-comments/inline-comments (comments/get-comments-in-thread @db/dsdb (comments/get-comment-thread-uid @db/dsdb uid)) uid true]))

            (when in-view?
              [presence/inline-presence-el uid])

            (when (and in-view?
                       (> (count _refs) 0)
                       (not= :block-embed? opts))
              [block-refs-count-el
               (count _refs)
               (fn [e]
                 (if (.. e -shiftKey)
                   (rf/dispatch [:right-sidebar/open-item [:block/uid uid]])
                   (rf/dispatch [::inline-refs.events/toggle-open! uid])))
               @inline-refs-open?])]

           ;; Inline refs
           (when (and in-view?
                      (> (count _refs) 0)
                      (not= :block-embed? opts)
                      @inline-refs-open?)
             [inline-linked-refs-el block-el uid])

           ;; Properties
           (when (and @enable-properties?
                      (or (and (true? linked-ref) @linked-ref-open?)
                          (and (false? linked-ref) open)))
             (for [prop (common-db/sort-block-properties properties)]
               ^{:key (:db/id prop)}
               [:f> block-el prop
                (assoc linked-ref-data :initial-open (contains? parent-uids (:block/uid prop)))
                opts]))

           ;; Children
           (when (and (seq children)
                      (or (and (true? linked-ref) @linked-ref-open?)
                          (and (false? linked-ref) open)))
             (for [child children
                   :let  [child-uid (:block/uid child)]]
               ^{:key (:db/id child)}
               [:f> block-el child
                (assoc linked-ref-data :initial-open (contains? parent-uids child-uid))
                opts]))]

          (when (= @drag-target :first) [drop-area-indicator/drop-area-indicator {:placement "below" :child? true}])
          (when (= @drag-target :after) [drop-area-indicator/drop-area-indicator {:placement "below"}])])))))
