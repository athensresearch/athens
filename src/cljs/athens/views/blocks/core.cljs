(ns athens.views.blocks.core
  (:require
    ["/components/Block/Container"           :refer [Container]]
    ["@chakra-ui/react"                      :refer [MenuList MenuItem]]
    [athens.common.logging                   :as log]
    [athens.db                               :as db]
    [athens.electron.images                  :as images]
    [athens.electron.utils                   :as electron.utils]
    [athens.events.dragging                  :as drag.events]
    [athens.events.inline-refs               :as inline-refs.events]
    [athens.events.linked-refs               :as linked-ref.events]
    [athens.events.selection                 :as select-events]
    [athens.reactive                         :as reactive]
    [athens.subs.dragging                    :as drag.subs]
    [athens.subs.selection                   :as select-subs]
    [athens.util                             :as util :refer [mouse-offset vertical-center specter-recursive-path]]
    [athens.views.blocks.context-menu        :as ctx-menu]
    [athens.views.blocks.drop-area-indicator :as drop-area-indicator]
    [athens.views.blocks.editor              :as editor]
    [athens.views.notifications.actions      :as actions]
    [com.rpl.specter                         :as s]
    [goog.functions                          :as gfns]
    [re-frame.core                           :as rf]
    [reagent.core                            :as r]
    [reagent.ratom                           :as ratom]))


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
        {:keys [x y]}        (mouse-offset e closest-container)
        middle-y             (vertical-center closest-container)
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


(defn block-el
  "Two checks dec to make sure block is open or not: children exist and :block/open bool"
  ([block]
   [block-el block {:linked-ref false} {}])
  ([block linked-ref-data]
   [block-el block linked-ref-data {}])
  ([block linked-ref-data _opts]
   (let [{:keys [linked-ref
                 initial-open
                 linked-ref-uid]}     linked-ref-data
         {:block/keys [uid
                       original-uid]} block
         local-value                  (r/atom nil)
         old-value                    (r/atom nil)
         show-edit?                   (r/atom false)
         hide-edit-fn                 #(reset! show-edit? false)
         show-edit-fn                 #(reset! show-edit? true)
         savep-fn                     (partial db/transact-state-for-uid (or original-uid uid))
         save-fn                      #(savep-fn @local-value :block-save)
         idle-fn                      (gfns/debounce #(savep-fn @local-value :autosave)
                                                     2000)
         update-fn                    #(reset! local-value %)
         update-old-fn                #(reset! old-value %)
         read-value                   (ratom/reaction @local-value)
         read-old-value               (ratom/reaction @old-value)
         state-hooks                  {:save-fn        save-fn
                                       :idle-fn        idle-fn
                                       :update-fn      update-fn
                                       :update-old-fn  update-old-fn
                                       :read-value     read-value
                                       :read-old-value read-old-value
                                       :show-edit?     show-edit?}
         dragging?                    (rf/subscribe [::drag.subs/dragging? uid])
         drag-target                  (rf/subscribe [::drag.subs/drag-target uid])
         selected-items               (rf/subscribe [::select-subs/items])]
     (rf/dispatch [::linked-ref.events/set-open! uid (or (false? linked-ref) initial-open)])
     (rf/dispatch [::inline-refs.events/set-open! uid false])

     (r/create-class
       {:component-will-unmount
        (fn will-unmount-block
          [_]
          (rf/dispatch [::linked-ref.events/cleanup! uid])
          (rf/dispatch [::inline-refs.events/cleanup! uid]))
        :reagent-render
        (fn render-block
          [block linked-ref-data opts]
          (let [ident                 [:block/uid (or original-uid uid)]
                block-o               (reactive/get-reactive-block-document ident)
                {:block/keys [uid
                              string
                              open
                              children
                              _refs]} (merge block-o block)
                children-uids         (set (map :block/uid children))
                uid-sanitized-block   (s/transform
                                        (specter-recursive-path #(contains? % :block/uid))
                                        (fn [{:block/keys [original-uid uid] :as block}]
                                          (assoc block :block/uid (or original-uid uid)))
                                        block)
                is-selected           @(rf/subscribe [::select-subs/selected? uid])
                present-user          @(rf/subscribe [:presence/has-presence uid])
                is-presence           (seq present-user)
                properties            (:block/properties block-o)]


            ;; (prn uid is-selected)

            ;; If datascript string value does not equal local value, overwrite local value.
            ;; Write on initialization
            ;; Write also from backspace, which can join bottom block's contents to top the block.
            (when (not= string @old-value)
              (update-fn string)
              (update-old-fn string))

            [:> Container {:isHidden     (actions/hide-notification? properties)
                           :isDragging   (and @dragging? (not is-selected))
                           :isSelected   is-selected
                           :hasChildren  (seq children)
                           :isOpen       open
                           :isLinkedRef  (and (false? initial-open) (= uid linked-ref-uid))
                           :hasPresence  is-presence
                           :uid          uid
                           ;; need to know children for selection resolution
                           :childrenUids children-uids
                           ;; show-edit? allows us to render the editing elements (like the textarea)
                           ;; even when not editing this block. When true, clicking the block content will pass
                           ;; the clicks down to the underlying textarea. The textarea is expensive to render,
                           ;; so we avoid rendering it when it's not needed.
                           :onMouseEnter show-edit-fn
                           :onMouseLeave hide-edit-fn
                           :onDragOver   (fn [e] (block-drag-over e block))
                           :onDragLeave  (fn [e] (block-drag-leave e block))
                           :onDrop       (fn [e] (block-drop e block))
                           :menu         (r/as-element [:> MenuList
                                                        [:> MenuItem {:children (if (> (count @selected-items) 1)
                                                                                  "Copy selected block refs"
                                                                                  "Copy block ref")
                                                                      :onClick  #(ctx-menu/handle-copy-refs nil uid)}]
                                                        [:> MenuItem {:children "Copy unformatted text"
                                                                      :onClick  #(ctx-menu/handle-copy-unformatted uid)}]])}

             (when (= @drag-target :before) [drop-area-indicator/drop-area-indicator {:placement "above"}])

             [editor/editor-component
              block-el
              block-o
              true
              linked-ref-data
              uid-sanitized-block
              state-hooks
              opts]

             (when (= @drag-target :first) [drop-area-indicator/drop-area-indicator {:placement "below" :child? true}])
             (when (= @drag-target :after) [drop-area-indicator/drop-area-indicator {:placement "below"}])]))}))))
