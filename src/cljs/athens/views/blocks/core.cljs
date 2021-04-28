(ns athens.views.blocks.core
  (:require
    [athens.db :as db]
    [athens.electron :as electron]
    [athens.style :as style]
    [athens.util :as util :refer [mouse-offset vertical-center specter-recursive-path]]
    [athens.views.blocks.autocomplete-search :as autocomplete-search]
    [athens.views.blocks.autocomplete-slash :as autocomplete-slash]
    [athens.views.blocks.bullet :as bullet]
    [athens.views.blocks.content :as content]
    [athens.views.blocks.context-menu :as context-menu]
    [athens.views.blocks.drop-area-indicator :as drop-area-indicator]
    [athens.views.blocks.toggle :as toggle]
    [athens.views.blocks.tooltip :as tooltip]
    [athens.views.buttons :as buttons]
    [athens.views.presence :as presence]
    [cljsjs.react]
    [cljsjs.react.dom]
    [com.rpl.specter :as s]
    [re-frame.core :as rf]
    [reagent.core :as r]
    [stylefy.core :as stylefy]))


;;; Styles
;;; 
;;; Blocks use Em units in many places rather than Rem units because
;;; blocks need to scale with their container: sidebar blocks are
;;; smaller than main content blocks, for instance.


(def block-container-style
  {:display "flex"
   :line-height "2em"
   :position "relative"
   :border-radius "0.125rem"
   :justify-content "flex-start"
   :flex-direction "column"
   ::stylefy/manual [[:&.show-tree-indicator:before {:content "''"
                                                     :position "absolute"
                                                     :width "1px"
                                                     :left "calc(1.375em + 1px)"
                                                     :top "2em"
                                                     :bottom "0"
                                                     :transform "translateX(50%)"
                                                     :background (style/color :border-color)}]
                     [:&:after {:content "''"
                                :z-index -1
                                :position "absolute"
                                :top "0.75px"
                                :right 0
                                :bottom "0.75px"
                                :left 0
                                :opacity 0
                                :pointer-events "none"
                                :border-radius "0.25rem"
                                :transition "opacity 0.075s ease"
                                :background (style/color :link-color :opacity-lower)
                                :box-shadow [["0 0.25rem 0.5rem -0.25rem" (style/color :background-color :opacity-med)]]}]
                     [:&.is-selected:after {:opacity 1}]
                     [:.block-body {:display "flex"
                                    :border-radius "0.5rem"
                                    :transition "all 0.1s ease"
                                    :position "relative"}
                      [:button.block-edit-toggle {:position "absolute"
                                                  :appearance "none"
                                                  :width "100%"
                                                  :background "none"
                                                  :border 0
                                                  :cursor "text"
                                                  :display "block"
                                                  :z-index 1
                                                  :top 0
                                                  :right 0
                                                  :bottom 0
                                                  :left 0}]]
                      ;;[:&:hover {:background (color :background-minus-1)}]]
                     ;; Darken block body when block editing,
                     [:&.is-linked-ref {:background-color (style/color :background-plus-2)}]
                     ;;[(selectors/> :.is-editing :.block-body) {:background (color :background-minus-1)}]
                     ;; Inset child blocks
                     [:.block-container {:margin-left "2rem"}]]})


(stylefy/class "block-container" block-container-style)


(def dragging-style
  {:opacity "0.25"})


(stylefy/class "dragging" dragging-style)


;;; Components

(defn block-refs-count-el
  [count uid]
  [:div (stylefy/use-style {:margin-left "1em"
                            :z-index (:zindex-dropdown style/ZINDICES)
                            :visibility (when-not (pos? count) "hidden")})
   [buttons/button {:primary true :on-click #(rf/dispatch [:right-sidebar/open-item uid])} count]])


(defn block-drag-over
  "If block or ancestor has CSS dragging class, do not show drop indicator; do not allow block to drop onto itself.
  If above midpoint, show drop indicator above block.
  If no children and over X pixels from the left, show child drop indicator.
  If below midpoint, show drop indicator below."
  [e block state]
  (.. e preventDefault)
  (.. e stopPropagation)
  (let [{:block/keys [children uid open]} block
        closest-container (.. e -target (closest ".block-container"))
        {:keys [x y]} (mouse-offset e closest-container)
        middle-y          (vertical-center closest-container)
        dragging-ancestor (.. e -target (closest ".dragging"))
        dragging?         dragging-ancestor
        is-selected?      @(rf/subscribe [:selected/is-selected uid])
        target            (cond
                            dragging? nil
                            is-selected? nil
                            (or (neg? y) (< y middle-y)) :above
                            (or (not open) (and (empty? children) (< 50 x))) :child
                            (< middle-y y) :below)]
    (when target
      (swap! state assoc :drag-target target))))


(defn block-drop
  "When a drop occurs: https://developer.mozilla.org/en-US/docs/Web/API/HTML_Drag_and_Drop_API#Define_a_drop_zone"
  [e block state]
  (.. e stopPropagation)
  (let [{target-uid :block/uid} block
        [target-uid _]          (db/uid-and-embed-id target-uid)
        {:keys [drag-target]} @state
        source-uid     (.. e -dataTransfer (getData "text/plain"))
        effect-allowed (.. e -dataTransfer -effectAllowed)

        items          (array-seq (.. e -dataTransfer -items))
        item           (first items)
        datatype       (.. item -type)

        img-regex      #"(?i)^image/(p?jpeg|gif|png)$"

        valid-text-drop     (and (not (nil? drag-target))
                                 (not= source-uid target-uid)
                                 (or (= effect-allowed "link")
                                     (= effect-allowed "move")))
        selected-items @(rf/subscribe [:selected/items])]

    (cond
      (re-find img-regex datatype) (when (util/electron?)
                                     (electron/dnd-image target-uid drag-target item (second (re-find img-regex datatype))))
      (re-find #"text/plain" datatype) (when valid-text-drop
                                         (if (empty? selected-items)
                                           (rf/dispatch [:drop source-uid target-uid drag-target effect-allowed])
                                           (rf/dispatch [:drop-multi selected-items target-uid drag-target]))))

    (rf/dispatch [:mouse-down/unset])
    (swap! state assoc :drag-target nil)))


(defn block-drag-leave
  "When mouse leaves block, remove any drop area indicator.
  Ignore if target-uid and related-uid are the same — user went over a child component and we don't want flicker."
  [e block state]
  (.. e preventDefault)
  (.. e stopPropagation)
  (let [{target-uid :block/uid} block
        related-uid (util/get-dataset-uid (.. e -relatedTarget))]
    (when-not (= related-uid target-uid)
      ;;(prn target-uid related-uid  "LEAVE")
      (swap! state assoc :drag-target nil))))


(defn block-el
  "Two checks dec to make sure block is open or not: children exist and :block/open bool"
  ([block]
   [block-el block {:linked-ref false} {}])
  ([block linked-ref-data]
   [block-el block linked-ref-data {}])
  ([_block linked-ref-data _opts]
   (let [{:keys [linked-ref initial-open linked-ref-uid parent-uids]} linked-ref-data
         state (r/atom {:string/local      nil
                        :string/previous   nil
                        :search/type       nil              ;; one of #{:page :block :slash :hashtag}
                        :search/results    nil
                        :search/query      nil
                        :search/index      nil
                        :dragging          false
                        :drag-target       nil
                        :last-keydown      nil
                        :context-menu/x    nil
                        :context-menu/y    nil
                        :context-menu/show false
                        :caret-position    nil
                        :linked-ref/open (or (false? linked-ref) initial-open)})]

     (fn [block linked-ref-data opts]
       (let [{:block/keys [uid string open children _refs]} block
             uid-sanitized-block (s/transform
                                   (specter-recursive-path #(contains? % :block/uid))
                                   (fn [{:block/keys [original-uid uid] :as block}]
                                     (assoc block :block/uid (or original-uid uid)))
                                   block)
             {:search/keys [] :keys [dragging drag-target]} @state
             is-editing  @(rf/subscribe [:editing/is-editing uid])
             is-selected @(rf/subscribe [:selected/is-selected uid])]

         ;;(prn uid is-selected)

         ;; If datascript string value does not equal local value, overwrite local value.
         ;; Write on initialization
         ;; Write also from backspace, which can join bottom block's contents to top the block.
         (when (not= string (:string/previous @state))
           (swap! state assoc :string/previous string :string/local string))

         [:div
          {:class         ["block-container"
                           (when (and dragging (not is-selected)) "dragging")
                           (when is-editing "is-editing")
                           (when is-selected "is-selected")
                           (when (and (seq children) open) "show-tree-indicator")
                           (when (and (false? initial-open) (= uid linked-ref-uid)) "is-linked-ref")]
           :data-uid      uid
           :on-drag-over  (fn [e] (block-drag-over e block state))
           :on-drag-leave (fn [e] (block-drag-leave e block state))
           :on-drop       (fn [e] (block-drop e block state))}

          [presence/presence-popover-info uid {:inline? true}]

          [drop-area-indicator/drop-area-indicator #(when (= drag-target :above) {:opacity "1"})]

          [:div.block-body
           [:button.block-edit-toggle
            {:on-click (fn [e]
                         (when (false? (.. e -shiftKey))
                           (rf/dispatch [:editing/uid uid])))}]

           [toggle/toggle-el uid-sanitized-block state linked-ref]
           [context-menu/context-menu-el uid-sanitized-block state]
           [bullet/bullet-el block state linked-ref]
           [tooltip/tooltip-el uid-sanitized-block state]
           [content/block-content-el block state]

           (when-not (:block-embed? opts)
             [block-refs-count-el (count _refs) uid])]

          [autocomplete-search/inline-search-el block state]
          [autocomplete-slash/slash-menu-el block state]

          ;; Children
          (when (and (seq children)
                     (or (and (true? linked-ref) (:linked-ref/open @state))
                         (and (false? linked-ref) open)))
            (for [child children]
              [:div {:key (:db/id child)}
               [block-el child
                (assoc linked-ref-data :initial-open (contains? parent-uids (:block/uid child)))
                opts]]))

          [drop-area-indicator/drop-area-indicator #(when (= drag-target :below) {;;:color "red"
                                                                                  :opacity "1"})]])))))


(defn block-component
  [ident]
  (let [block (db/get-block-document ident)]
    [block-el block]))
