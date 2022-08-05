(ns athens.views.blocks.editor
  (:require
    ["/components/Block/Anchor"                :refer [Anchor]]
    ["/components/Block/PropertyName"          :refer [PropertyName]]
    ["/components/Block/Reactions"             :refer [Reactions]]
    ["/components/Block/Toggle"                :refer [Toggle]]
    ["/components/EmojiPicker/EmojiPicker"     :refer [EmojiPickerPopoverContent]]
    ["/components/References/InlineReferences" :refer [ReferenceGroup ReferenceBlock]]
    ["@chakra-ui/react"                        :refer [VStack PopoverAnchor Popover Button Breadcrumb BreadcrumbItem BreadcrumbLink HStack]]
    [athens.common-db                          :as common-db]
    [athens.db                                 :as db]
    [athens.events.inline-refs                 :as inline-refs.events]
    [athens.events.linked-refs                 :as linked-ref.events]
    [athens.parse-renderer                     :as parse-renderer]
    [athens.reactive                           :as reactive]
    [athens.router                             :as router]
    [athens.self-hosted.presence.views         :as presence]
    [athens.subs.inline-refs                   :as inline-refs.subs]
    [athens.subs.linked-refs                   :as linked-ref.subs]
    [athens.util                               :as util]
    [athens.views.blocks.bullet                :refer [bullet-drag-start bullet-drag-end]]
    [athens.views.query.core                   :as query]
    [athens.views.blocks.content               :as content]
    [athens.views.blocks.reactions             :refer [toggle-reaction props->reactions]]
    [athens.views.comments.core                :as comments]
    [athens.views.comments.inline              :as inline-comments]
    [athens.views.notifications.actions        :as actions]
    [re-frame.core                             :as rf]))


(defn toggle
  [block-uid open]
  (rf/dispatch [:block/open {:block-uid block-uid
                             :open?     open}]))


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
              [block-el
               (util/recursively-modify-block-for-embed block @inline-ref-embed-id)
               linked-ref-data
               {:block-embed? true}]]


             ;; Otherwise display children of the parent directly.
             (for [child (:block/children block)]
               [:<> {:key (:db/id child)}
                [block-el
                 (util/recursively-modify-block-for-embed child @inline-ref-embed-id)
                 linked-ref-data
                 {:block-embed? true}]])))]))))


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


(defn editor-component
  [block-el block-o children? linked-ref-data uid-sanitized-block state-hooks opts menu show-emoji-picker? hide-emoji-picker-fn]
  (let [{:keys [linked-ref
                parent-uids]} linked-ref-data
        uid                   (:block/uid block-o)
        linked-ref-open?      (rf/subscribe [::linked-ref.subs/open? uid])
        inline-refs-open?     (rf/subscribe [::inline-refs.subs/open? uid])
        feature-flags         (rf/subscribe [:feature-flags])
        enable-properties?    (rf/subscribe [:feature-flags/enabled? :properties])
        show-comments?        (rf/subscribe [:comment/show-comments?])
        show-textarea         (rf/subscribe [:comment/show-editor? uid])]
    (fn editor-component-render
      [_block-el _block-o _children? _block _linked-ref-data _uid-sanitized-block _state-hooks _opts]
      (let [{:block/keys [;; uid
                          open
                          children
                          key
                          properties
                          _refs] :as block-data} (reactive/get-reactive-block-document [:block/uid uid])
            entity-type (get-in properties [":entity/type" :block/string])
            reactions-enabled?    (:reactions @feature-flags)
            user-id               (or (:username @(rf/subscribe [:presence/current-user]))
                                      ;; We use empty string for when there is no user information, like in PKM.
                                      "")
            reactions             (and reactions-enabled?
                                       (props->reactions properties))]

        [:<>
         [:div.block-body
          (when (and children?
                     (or (seq children)
                         (seq properties)))
            [:> Toggle {:isOpen  (if (or (and (true? linked-ref) @linked-ref-open?)
                                         (and (false? linked-ref) open))
                                   true
                                   false)
                        :onClick (fn [e]
                                   (.. e stopPropagation)
                                   (if (true? linked-ref)
                                     (rf/dispatch [::linked-ref.events/toggle-open! uid])
                                     (toggle uid (not open))))}])

          (when key
            [:> PropertyName {:name    (:node/title key)
                              :onClick (fn [e]
                                         (let [shift? (.-shiftKey e)]
                                           (rf/dispatch [:reporting/navigation {:source :block-property
                                                                                :target :page
                                                                                :pane   (if shift?
                                                                                          :right-pane
                                                                                          :main-pane)}])
                                           (router/navigate-page (:node/title key) e)))}])


          [:> Popover {:isOpen @show-emoji-picker?
                       :placement "bottom-end"
                       :onOpen #(js/console.log "tried to open")
                       :onClose hide-emoji-picker-fn}

           [:> PopoverAnchor
            [:> Anchor {:isClosedWithChildren   (when (and (seq children)
                                                           (or (and (true? linked-ref) (not @linked-ref-open?))
                                                               (and (false? linked-ref) (not open))))
                                                  "closed-with-children")
                        :uidSanitizedBlock      uid-sanitized-block
                        :shouldShowDebugDetails (util/re-frame-10x-open?)
                        :menu                   menu
                        :onClick                (fn [e]
                                                  (let [shift? (.-shiftKey e)]
                                                    (rf/dispatch [:reporting/navigation {:source :block-bullet
                                                                                         :target :block
                                                                                         :pane   (if shift?
                                                                                                   :right-pane
                                                                                                   :main-pane)}])
                                                    (router/navigate-uid uid e)))
                        :on-drag-start          (fn [e] (bullet-drag-start e uid))
                        :on-drag-end            (fn [e] (bullet-drag-end e uid))
                        :unreadNotification     (actions/unread-notification? properties)}]]
           (when reactions-enabled?
             [:> EmojiPickerPopoverContent
              {:onClose hide-emoji-picker-fn
               :onEmojiSelected (fn [e] (toggle-reaction [:block/uid uid] (.. e -detail -unicode) user-id))}])]




          [content/block-content-el block-o state-hooks]

          (when reactions [:> Reactions {:reactions (clj->js reactions)
                                         :currentUser user-id
                                         :onToggleReaction (partial toggle-reaction [:block/uid uid])}])

          ;; Show comments when the toggle is on
          (when (and @show-comments?
                     open
                     (or @show-textarea
                         (comments/get-comment-thread-uid @db/dsdb uid)))
            [inline-comments/inline-comments (comments/get-comments-in-thread @db/dsdb (comments/get-comment-thread-uid @db/dsdb uid)) uid false])

          [presence/inline-presence-el uid]

          (when (and (> (count _refs) 0) (not= :block-embed? opts))
            [block-refs-count-el
             (count _refs)
             (fn [e]
               (if (.. e -shiftKey)
                 (rf/dispatch [:right-sidebar/open-item uid])
                 (rf/dispatch [::inline-refs.events/toggle-open! uid])))
             @inline-refs-open?])]

         ;; Inline refs
         (when (and (> (count _refs) 0)
                    (not= :block-embed? opts)
                    @inline-refs-open?)
           [inline-linked-refs-el block-el uid])

         ;; Properties
         (when (and @enable-properties?
                    (or (and (true? linked-ref) @linked-ref-open?)
                        (and (false? linked-ref) open)))
           (for [prop (common-db/sort-block-properties properties)]
             ^{:key (:db/id prop)}
             [block-el prop
              (assoc linked-ref-data :initial-open (contains? parent-uids (:block/uid prop)))
              opts]))

         (when (= entity-type "[[athens/query]]")
           [query/query-block block-data properties])

           ;; Children
         (when (and (seq children)
                    (or (and (true? linked-ref) @linked-ref-open?)
                        (and (false? linked-ref) open)))
           (for [child children
                 :let  [child-uid (:block/uid child)]]
             ^{:key (:db/id child)}
             [block-el child
              (assoc linked-ref-data :initial-open (contains? parent-uids child-uid))
              opts]))]))))

