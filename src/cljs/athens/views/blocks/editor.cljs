(ns athens.views.blocks.editor
  (:require
    ["/components/Block/Anchor"                :refer [Anchor]]
    ["/components/Block/Toggle"                :refer [Toggle]]
    ["/components/References/InlineReferences" :refer [ReferenceGroup ReferenceBlock]]
    ["@chakra-ui/react"                        :refer [VStack Button Breadcrumb BreadcrumbItem BreadcrumbLink HStack]]
    [athens.db                                 :as db]
    [athens.events.inline-refs                 :as inline-refs.events]
    [athens.events.linked-refs                 :as linked-ref.events]
    [athens.parse-renderer                     :as parse-renderer]
    [athens.reactive                           :as reactive]
    [athens.router                             :as router]
    [athens.self-hosted.presence.views         :as presence]
    [athens.subs.inline-refs                   :as inline-refs.subs]
    [athens.subs.linked-refs                   :as linked-ref.subs]
    [athens.subs.selection                     :as select-subs]
    [athens.util                               :as util]
    [athens.views.blocks.bullet                :refer [bullet-drag-start bullet-drag-end]]
    [athens.views.blocks.content               :as content]
    [athens.views.blocks.context-menu          :refer [handle-copy-unformatted handle-copy-refs]]
    [re-frame.core                             :as rf]
    [reagent.core                              :as r]))


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
             (for [{:keys [node/title block/string block/uid] :as breadcrumb-block}
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
                 [parse-renderer/parse-and-render (or title string) uid]]]))]]

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
  [block-el block-o children? linked-ref-data uid-sanitized-block state-hooks opts]
  (let [{:keys [linked-ref
                parent-uids]} linked-ref-data
        uid                   (:block/uid block-o)
        linked-ref-open?      (rf/subscribe [::linked-ref.subs/open? uid])
        inline-refs-open?     (rf/subscribe [::inline-refs.subs/open? uid])
        selected-items        (rf/subscribe [::select-subs/items])]
    (fn editor-component-render
      [_block-el _block-o _children? _block _linked-ref-data _uid-sanitized-block _state-hooks _opts]
      (let [{:block/keys [;; uid
                          open
                          children
                          _refs]} (reactive/get-reactive-block-document [:block/uid uid])]
        [:<>
         [:div.block-body
          (when (and children?
                     (seq children))
            [:> Toggle {:isOpen  (if (or (and (true? linked-ref) @linked-ref-open?)
                                         (and (false? linked-ref) open))
                                   true
                                   false)
                        :onClick (fn [e]
                                   (.. e stopPropagation)
                                   (if (true? linked-ref)
                                     (rf/dispatch [::linked-ref.events/toggle-open! uid])
                                     (toggle uid (not open))))}])
          [:> Anchor {:isClosedWithChildren   (when (and (seq children)
                                                         (or (and (true? linked-ref) (not @linked-ref-open?))
                                                             (and (false? linked-ref) (not open))))
                                                "closed-with-children")
                      :uidSanitizedBlock      uid-sanitized-block
                      :shouldShowDebugDetails (util/re-frame-10x-open?)
                      :menuActions            (clj->js [{:children
                                                         (if (> (count @selected-items) 1)
                                                           "[edit] Copy selected block refs"
                                                           "[edit] Copy block ref")
                                                         :onClick #(handle-copy-refs nil uid)}
                                                        {:children "[edit] Copy unformatted text"
                                                         :onClick  #(handle-copy-unformatted uid)}])
                      :onClick                (fn [e]
                                                (let [shift? (.-shiftKey e)]
                                                  (rf/dispatch [:reporting/navigation {:source :block-bullet
                                                                                       :target :block
                                                                                       :pane   (if shift?
                                                                                                 :right-pane
                                                                                                 :main-pane)}])
                                                  (router/navigate-uid uid e)))
                      :on-drag-start          (fn [e] (bullet-drag-start e uid))
                      :on-drag-end            (fn [e] (bullet-drag-end e uid))}]

          [content/block-content-el block-o state-hooks]

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

