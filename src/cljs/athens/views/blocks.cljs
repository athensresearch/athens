(ns athens.views.blocks
  (:require
    ["@material-ui/icons/KeyboardArrowDown" :default KeyboardArrowDown]
    [athens.db :as db]
    [athens.electron :as electron]
    [athens.events :refer [select-up select-down]]
    [athens.keybindings :refer [auto-complete-hashtag
                                auto-complete-inline
                                auto-complete-slash
                                textarea-key-down]]
    [athens.listeners :as listeners]
    [athens.parse-renderer :refer [parse-and-render]]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [color DEPTH-SHADOWS OPACITIES ZINDICES]]
    [athens.util :as util :refer [get-dataset-uid mouse-offset vertical-center specter-recursive-path]]
    [athens.views.buttons :refer [button]]
    [athens.views.dropdown :refer [menu-style dropdown-style]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as str]
    [com.rpl.specter :as s]
    [garden.selectors :as selectors]
    [goog.dom.classlist :refer [contains]]
    [goog.events :as events]
    [komponentit.autosize :as autosize]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]])
  (:import
    (goog.events
      EventType)))


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
                                                     :left "calc(1.25em + 1px)"
                                                     :top "2em"
                                                     :bottom "0"
                                                     :transform "translateX(50%)"
                                                     :background (color :border-color)}]
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
                                :background (color :link-color :opacity-lower)
                                :box-shadow [["0 0.25rem 0.5rem -0.25rem" (color :background-color :opacity-med)]]}]
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
                     [:&.is-linked-ref {:background-color (color :background-plus-2)}]
                     ;;[(selectors/> :.is-editing :.block-body) {:background (color :background-minus-1)}]
                     ;; Inset child blocks
                     [:.block-container {:margin-left "2rem"}]]})


(stylefy/class "block-container" block-container-style)


(def block-disclosure-toggle-style
  {:width "1em"
   :height "2em"
   :position "relative"
   :z-index 2
   :flex-shrink "0"
   :display "flex"
   :background "none"
   :border "none"
   :transition "all 0.05s ease"
   :align-items "center"
   :justify-content "center"
   :padding "0"
   :-webkit-appearance "none"
   :color (color :body-text-color :opacity-med)
   ::stylefy/mode [[:hover {:color (color :link-color)}]
                   [":is(button)" {:cursor "pointer"}]]
   ::stylefy/manual [[:&.closed [:svg {:transform "rotate(-90deg)"}]]
                     [:&:empty {:pointer-events "none"}]]})


(def bullet-style
  {:flex-shrink "0"
   :position "relative"
   :z-index 2
   :cursor "pointer"
   :width "0.75em"
   :margin-right "0.25em"
   :transition "all 0.05s ease"
   :height "2em"
   :color (color :body-text-color :opacity-low)
   ::stylefy/mode [[:after {:content "''"
                            :background "currentColor"
                            :transition "all 0.05s ease"
                            :border-radius "100px"
                            :box-shadow "0 0 0 0.125rem transparent"
                            :display "inline-flex"
                            :margin "50% 0 0 50%"
                            :transform "translate(-50%, -50%)"
                            :height "0.3125em"
                            :width "0.3125em"}]
                   [:hover {:color (color :link-color)}]]
   ::stylefy/manual [[:&.closed-with-children [(selectors/& (selectors/after)) {:box-shadow (str "0 0 0 0.125rem " (color :body-text-color))
                                                                                :opacity (:opacity-med OPACITIES)}]]
                     [:&.closed-with-children [(selectors/& (selectors/before)) {:content "none"}]]
                     [:&:hover:after {:transform "translate(-50%, -50%) scale(1.3)"}]
                     [:&.dragging {:z-index 1
                                   :cursor "grabbing"
                                   :color (color :body-text-color)}]]})


(stylefy/class "bullet" bullet-style)


(stylefy/keyframes "drop-area-appear"
                   [:from
                    {:opacity "0"}]
                   [:to
                    {:opacity "1"}])


(stylefy/keyframes "drop-area-color-pulse"
                   [:from
                    {:opacity (:opacity-lower OPACITIES)}]
                   [:to
                    {:opacity (:opacity-med OPACITIES)}])


(def drop-area-indicator
  {:display "block"
   :height "1px"
   :pointer-events "none"
   :margin-bottom "-1px"
   :color (color :link-color :opacity-high)
   :position "relative"
   :transform-origin "left"
   :z-index 3
   :width "100%"
   :opacity 0
   ::stylefy/manual [[:&:after {:position "absolute"
                                :content "''"
                                :top "-0.5px"
                                :right "0"
                                :bottom "-0.5px"
                                :left "2em"
                                :border-radius "100px"
                                :background "currentColor"}]]})


(def block-content-style
  {:display "grid"
   :grid-template-areas "'main'"
   :align-items "stretch"
   :justify-content "stretch"
   :position "relative"
   :overflow "visible"
   :z-index 2
   :flex-grow "1"
   :word-break "break-word"
   ::stylefy/manual [[:textarea {:display "none"}]
                     [:&:hover [:textarea [(selectors/& (selectors/not :.is-editing)) {:display "block"
                                                                                       :z-index 1}]]]
                     [:textarea {:-webkit-appearance "none"
                                 :cursor "text"
                                 :resize "none"
                                 :transform "translate3d(0,0,0)"
                                 :color "inherit"
                                 :outline "none"
                                 :overflow "hidden"
                                 :padding "0"
                                 :background (color :background-minus-1)
                                 :grid-area "main"
                                 :min-height "100%"
                                 :caret-color (color :link-color)
                                 :margin "0"
                                 :font-size "inherit"
                                 :line-height "inherit"
                                 :border-radius "0.25rem"
                                 :transition "opacity 0.15s ease"
                                 :box-shadow (str "-0.25rem 0 0 0" (color :background-minus-1))
                                 :border "0"
                                 :opacity "0"
                                 :font-family "inherit"}]
                     [:.is-editing {:z-index 3
                                    :display "block"
                                    :opacity "1"}]
                     [:span
                      {:grid-area "main"}
                      [:>span
                       :>a {:position "relative"
                            :z-index 2}]]
                     ;; May want to refactor specific component styles to somewhere else.
                     ;; Closer to the component perhaps?
                     ;; Code
                     [:code :pre {:font-family "IBM Plex Mono"}]
                     ;; Media Containers
                     ;; Using a CSS hack/convention here to create a responsive container
                     ;; of a specific aspect ratio.
                     ;; TODO: Replace this with the CSS aspect-ratio property once available.
                     [:.media-16-9 {:height 0
                                    :width "calc(100% - 0.25rem)"
                                    :z-index 1
                                    :transform-origin "right center"
                                    :transition "all 0.2s ease"
                                    :padding-bottom (str (* (/ 9 16) 100) "%")
                                    :margin-block "0.25rem"
                                    :margin-inline-end "0.25rem"
                                    :position "relative"}]
                     ;; Media (YouTube embeds, map embeds, etc.)
                     [:iframe {:border 0
                               :box-shadow [["inset 0 0 0 0.125rem" (color :background-minus-1)]]
                               :position "absolute"
                               :height "100%"
                               :width "100%"
                               :cursor "default"
                               :top 0
                               :right 0
                               :left 0
                               :bottom 0
                               :border-radius "0.25rem"}]
                     ;; Images
                     [:img {:border-radius "0.25rem"
                            :max-width "calc(100% - 0.25rem)"}]
                     ;; Checkboxes
                     ;; TODO: Refactor these complicated styles into clip paths or SVGs
                     ;; or something nicer than this
                     [:input [:& (selectors/attr= :type :checkbox) {:appearance "none"
                                                                    :border-radius "0.25rem"
                                                                    :cursor "pointer"
                                                                    :color (color :link-color)
                                                                    :margin-inline-end "0.25rem"
                                                                    :position "relative"
                                                                    :top "0.13em"
                                                                    :width "1rem"
                                                                    :height "1rem"
                                                                    :transition "all 0.05s ease"
                                                                    :transform "scale(1)"
                                                                    :box-shadow "inset 0 0 0 1px"}
                              [:&:after {:content "''"
                                         :position "absolute"
                                         :top "45%"
                                         :left "20%"
                                         :width "30%"
                                         :height "50%"
                                         :border-width "0 2px 2px 0"
                                         :border-style "solid"
                                         :opacity 0
                                         :transform "rotate(45deg) translate(-40%, -50%)"}]
                              [:&:checked {:background (color :link-color)}
                               [:&:after {:opacity 1
                                          :color (color :background-color)}]]
                              [:&:active {:transform "scale(0.9)"}]]]

                     [:mark.contents.highlight {:padding "0 2px"
                                                :background-color "#ffeb7a"}]]})


(stylefy/class "block-content" block-content-style)


(stylefy/keyframes "tooltip-appear"
                   [:from
                    {:opacity "0"
                     :transform "scale(0)"}]
                   [:to
                    {:opacity "1"
                     :transform "scale(1)"}])


(def tooltip-style
  {:z-index (:zindex-dropdown ZINDICES)
   :position "absolute"
   :box-shadow [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px " (color :body-text-color :opacity-lower)]]
   :flex-direction "column"
   :background-color (color :background-plus-1)
   :padding "0.5rem 0.75rem"
   :border-radius "0.25rem"
   :line-height "1.75rem"
   :left "0.5rem"
   :top "2rem"
   :transform-origin "0.5rem 1.5rem"
   :min-width "9rem"
   :animation "tooltip-appear .2s ease"
   :transition "background .1s ease"
   :display "table"
   :color (color :body-text-color :opacity-high)
   :border-spacing "0.25rem"
   ::stylefy/manual [[:div {:display "table-row"}]
                     [:b {:display "table-cell"
                          :user-select "none"
                          :text-align "right"
                          :text-transform "uppercase"
                          :font-size "12px"
                          :letter-spacing "0.1em"
                          :opacity (:opacity-med OPACITIES)}]
                     [:span {:display "table-cell"
                             :user-select "all"}
                      [:&:hover {:color (color :header-text-color)}]]
                     [:&:after {:content "''"
                                :position "absolute"
                                :top "-0.75rem"
                                :bottom "-1rem"
                                :border-radius "inherit"
                                :left "-1rem"
                                :right "-1rem"
                                :z-index -1
                                :display "block"}]]})


(def dragging-style
  {:opacity "0.25"})


(stylefy/class "dragging" dragging-style)

;; Helpers


(defn toggle
  [id open]
  (dispatch [:transact [[:db/add id :block/open (not open)]]]))


;;; Components

(defn toggle-el
  [{:block/keys [open uid children]} state linked-ref]
  (if (seq children)
    [:button (use-style block-disclosure-toggle-style
                        {:class    (if (or (and (true? linked-ref) (:linked-ref/open @state))
                                           (and (false? linked-ref) open))
                                     "open"
                                     "closed")
                         :on-click (fn [_]
                                     (if (true? linked-ref)
                                       (swap! state update :linked-ref/open not)
                                       (toggle [:block/uid uid] open)))})
     [:> KeyboardArrowDown {:style {:font-size "16px"}}]]
    [:span (use-style block-disclosure-toggle-style)]))


(defn tooltip-el
  [block state]
  (let [{:block/keys [uid order open refs] dbid :db/id} block
        {:keys [dragging tooltip]} @state]
    ;; if re-frame-10x is hidden, don't show tooltip. see style.cljs
    (when (and tooltip (not dragging) (util/re-frame-10x-open?))
      [:div (use-style tooltip-style
                       {:class          "tooltip"
                        :on-click       (fn [e] (.. e stopPropagation))
                        :on-mouse-leave #(swap! state assoc :tooltip false)})
       [:div [:b "db/id"] [:span dbid]]
       [:div [:b "uid"] [:span uid]]
       [:div [:b "order"] [:span order]]
       [:div [:b "open"] [:span (str open)]]
       [:div [:b "refs"] [:span (str refs)]]])))


(defn inline-item-click
  [state uid expansion]
  (let [id        (str "#editable-uid-" uid)
        target    (.. js/document (querySelector id))]
    (case (:search/type @state)
      :hashtag (auto-complete-hashtag state target expansion)
      (auto-complete-inline state target expansion))))


(defn inline-search-el
  [_block state]
  (let [ref (atom nil)
        handle-click-outside (fn [e]
                               (let [{:search/keys [type]} @state]
                                 (when (and (or (= type :page) (= type :block) (= type :hashtag))
                                            (not (.. @ref (contains (.. e -target)))))
                                   (swap! state assoc :search/type false))))]
    (r/create-class
      {:display-name           "inline-search"
       :component-did-mount    (fn [_this] (events/listen js/document "mousedown" handle-click-outside))
       :component-will-unmount (fn [_this] (events/unlisten js/document "mousedown" handle-click-outside))
       :reagent-render         (fn [block state]
                                 (let [{:search/keys [query results index type] caret-position :caret-position} @state
                                       {:keys [left top]} caret-position]
                                   (when (some #(= % type) [:page :block :hashtag])
                                     [:div (merge (use-style dropdown-style
                                                             {:ref           #(reset! ref %)
                                                              ;; don't blur textarea when clicking to auto-complete
                                                              :on-mouse-down (fn [e] (.. e preventDefault))})
                                                  {:style {:position   "absolute"
                                                           :max-height "20rem"
                                                           :z-index    (:zindex-popover ZINDICES)
                                                           :top        (+ 24 top)
                                                           :left       (+ 24 left)}})
                                      [:div#dropdown-menu (use-style menu-style)
                                       (if (or (str/blank? query)
                                               (empty? results))
                                         ;; Just using button for styling
                                         [button (use-style {:opacity (OPACITIES :opacity-low)}) (str "Search for a " (symbol type))]
                                         (doall
                                           (for [[i {:keys [node/title block/string block/uid]}] (map-indexed list results)]
                                             [button {:key      (str "inline-search-item" uid)
                                                      :id       (str "dropdown-item-" i)
                                                      :active   (= index i)
                                                      ;; if page link, expand to title. otherwise expand to uid for a block ref
                                                      :on-click (fn [_] (inline-item-click state (:block/uid block) (or title uid)))
                                                      :style    {:text-align "left"}}
                                              (or title string)])))]])))})))


(defn slash-item-click
  [state block item]
  (let [id        (str "#editable-uid-" (:block/uid block))
        target    (.. js/document (querySelector id))]
    (auto-complete-slash state target item)))


(defn slash-menu-el
  [_block state]
  (let [ref (atom nil)
        handle-click-outside (fn [e]
                               (let [{:search/keys [type]} @state]
                                 (when (and (= type :slash)
                                            (not (.. @ref (contains (.. e -target)))))
                                   (swap! state assoc :search/type false))))]
    (r/create-class
      {:display-name           "slash-menu"
       :component-did-mount    (fn [_this] (events/listen js/document "mousedown" handle-click-outside))
       :component-will-unmount (fn [_this] (events/unlisten js/document "mousedown" handle-click-outside))
       :reagent-render         (fn [block state]
                                 (let [{:search/keys [index results type] caret-position :caret-position} @state
                                       {:keys [left top]} caret-position]
                                   (when (= type :slash)
                                     [:div (merge (use-style dropdown-style
                                                             {:ref           #(reset! ref %)
                                                              ;; don't blur textarea when clicking to auto-complete
                                                              :on-mouse-down (fn [e] (.. e preventDefault))})
                                                  {:style {:position "absolute" :left (+ left 24) :top (+ top 24)}})
                                      [:div#dropdown-menu (merge (use-style menu-style) {:style {:max-height "8em"}})
                                       (doall
                                         (for [[i [text icon _expansion kbd _pos :as item]] (map-indexed list results)]
                                           [button {:key      text
                                                    :id       (str "dropdown-item-" i)
                                                    :active   (= i index)
                                                    :on-click (fn [_] (slash-item-click state block item))}
                                            [:<> [(r/adapt-react-class icon)] [:span text] (when kbd [:kbd kbd])]]))]])))})))


(defn textarea-paste
  "Clipboard data can only be accessed if user triggers JavaScript paste event.
  Uses previous keydown event to determine if shift was held, since the paste event has no knowledge of shift key.

  Image Cases:
  - items N=1, image/png
  - items N=2, text/html and image/png
  For both of these, just write image to filesystem. Roam behavior is to copy the <img> src and alt of the copied picture.
  Roam's approach is useful to preserve the original source url and description, but is unsafe in case the link breaks.
  Writing to filesystem (or to Firebase a la Roam) is useful, but has storage costs.
  Writing to filesystem each time for now until get feedback otherwise that user doesn't want to save the image.
  Can eventually become a setting.

  Plaintext cases:
  - User pastes and last keydown has shift -> default
  - User pastes and clipboard data doesn't have new lines -> default
  - User pastes without shift and clipboard data has new line characters -> PREVENT default and convert to outliner blocks"
  [e uid state]
  (let [data        (.. e -clipboardData)
        text-data   (.. data (getData "text"))
        line-breaks (re-find #"\r?\n" text-data)
        no-shift    (-> @state :last-keydown :shift not)
        items       (array-seq (.. e -clipboardData -items))
        {:keys [head tail]} (athens.keybindings/destruct-target (.-target e))
        img-regex   #"(?i)^image/(p?jpeg|gif|png)$"]
    (cond
      (seq (filter (fn [item]
                     (let [datatype (.. item -type)]
                       (re-find img-regex datatype))) items))
      (mapv (fn [item]
              (let [datatype (.. item -type)]
                (cond
                  (re-find img-regex datatype) (when (util/electron?)
                                                 (let [new-str (electron/save-image head tail item "png")]
                                                   (js/setTimeout #(swap! state assoc :string/local new-str) 50)))
                  (re-find #"text/html" datatype) (.getAsString item (fn [_] #_(prn "getAsString" _))))))
            items)
      :else
      (when (and line-breaks no-shift)
        (.. e preventDefault)
        (dispatch [:paste uid text-data])))))


(defn textarea-change
  [e _uid state]
  (swap! state assoc :string/local (.. e -target -value)))


(defn find-selected-items
  "Used by both shift-click and click-drag for multi-block-selection.
  Given a mouse event, a source block, and a target block, highlight blocks.
  Find all blocks on the page using the DOM.
  Determine if direction is up or down.
  Algorithm: call select-up or select-down until start and end of vector are source and target.

  Bug: there isn't an algorithmic path for all pairs of source and target blocks, because sometimes the parent is
  highlighted, meaning a child block might not be selected itself. Rather, it inherits selection from parent.
  
  e.g.: 1 and 3 as source and target, or vice versa.
  • 1
  • 2
   • 3
  Because of this bug, add additional exit cases to prevent stack overflow."
  [e source-uid target-uid]
  (let [target (.. e -target)
        page (or (.. target (closest ".node-page")) (.. target (closest ".block-page")))
        blocks (->> (.. page (querySelectorAll ".block-container"))
                    array-seq
                    vec)
        uids (map get-dataset-uid blocks)
        start-idx (first (keep-indexed (fn [i uid] (when (= uid source-uid) i)) uids))
        end-idx   (first (keep-indexed (fn [i uid] (when (= uid target-uid) i)) uids))]
    (when (and start-idx end-idx)
      (let [up? (> start-idx end-idx)
            delta (js/Math.abs (- start-idx end-idx))
            select-fn  (if up? select-up select-down)
            start-uid (nth uids start-idx)
            end-uid   (nth uids end-idx)
            new-items (loop [new-items [source-uid]
                             prev-items []]
                        (cond
                          (= prev-items new-items) new-items
                          (> (count new-items) delta) new-items
                          (nil? new-items) []
                          (or (and (= (first new-items) start-uid)
                                   (= (last new-items) end-uid))
                              (and (= (last new-items) start-uid)
                                   (= (first new-items) end-uid))) new-items
                          :else (recur (select-fn new-items)
                                       new-items)))]
        (dispatch [:selected/add-items new-items])))))


(defn textarea-click
  "If shift key is held when user clicks across multiple blocks, select the blocks."
  [e target-uid _state]
  (let [[target-uid _] (db/uid-and-embed-id target-uid)
        source-uid @(subscribe [:editing/uid])]
    (when (and source-uid target-uid (not= source-uid target-uid) (.. e -shiftKey))
      (find-selected-items e source-uid target-uid))))


(defn global-mouseup
  "Detach global mouseup listener (self)."
  [_]
  (events/unlisten js/document EventType.MOUSEUP global-mouseup)
  (dispatch [:mouse-down/unset]))


(defn textarea-mouse-down
  "Attach global mouseup listener. Listener can't be local because user might let go of mousedown off of a block.
  See https://javascript.info/mouse-events-basics#events-order"
  [e _uid _]
  (.. e stopPropagation)
  (when (false? (.. e -shiftKey))
    (dispatch [:editing/target (.. e -target)])
    (let [mouse-down @(subscribe [:mouse-down])]
      (when (false? mouse-down)
        (dispatch [:mouse-down/set])
        (events/listen js/document EventType.MOUSEUP global-mouseup)))))


(defn textarea-mouse-enter
  "When mouse-down, user is selecting multiple blocks with click+drag.
  Use same algorithm as shift-enter, only updating the source and target."
  [e target-uid _]
  (let [source-uid @(subscribe [:editing/uid])
        mouse-down @(subscribe [:mouse-down])]
    (when mouse-down
      (dispatch [:selected/clear-items])
      (find-selected-items e source-uid target-uid))))


(defn block-content-el
  "Actual string contents. Two elements, one for reading and one for writing.
  The CSS class is-editing is used for many things, such as block selection.
  Opacity is 0 when block is selected, so that the block is entirely blue, rather than darkened like normal editing.
  is-editing can be used for shift up/down, so it is used in both editing and selection."
  [_ _]
  (fn [block state]
    (let [{:block/keys [uid original-uid header]} block
          {:string/keys [local]} @state
          is-editing @(subscribe [:editing/is-editing uid])
          selected-items @(subscribe [:selected/items])
          font-size (case header
                      1 "2.1em"
                      2 "1.7em"
                      3 "1.3em"
                      "1em")]
      [:div {:class "block-content" :style {:font-size font-size}}
       [autosize/textarea {:value          (:string/local @state)
                           :class          ["textarea" (when (and (empty? selected-items) is-editing) "is-editing")]
                           ;;:auto-focus     true
                           :id             (str "editable-uid-" uid)
                           :on-change      (fn [e] (textarea-change e uid state))
                           :on-paste       (fn [e] (textarea-paste e uid state))
                           :on-key-down    (fn [e] (textarea-key-down e uid state))
                           :on-blur        (fn [_] (db/transact-state-for-uid (or original-uid uid) state))
                           :on-click       (fn [e] (textarea-click e uid state))
                           :on-mouse-enter (fn [e] (textarea-mouse-enter e uid state))
                           :on-mouse-down  (fn [e] (textarea-mouse-down e uid state))}]
       [parse-and-render local (or original-uid uid)]
       [:div (use-style (merge drop-area-indicator
                               (when (= :child (:drag-target @state)) {;;:color "green"
                                                                       :opacity 1})))]])))


(defn bullet-mouse-out
  "Hide tooltip."
  [e _uid state]
  (let [related (.. e -relatedTarget)]
    (when-not (and related (contains related "tooltip"))
      (swap! state assoc :tooltip false))))


(defn bullet-mouse-over
  "Show tooltip."
  [_e _uid state]
  (swap! state assoc :tooltip true))


(defn bullet-context-menu
  "Handle right click. If no blocks are selected, just give option for copying current block's uid."
  [e _uid state]
  (.. e preventDefault)
  (let [rect (.. e -target getBoundingClientRect)]
    (swap! state assoc
           :context-menu/x (.. rect -left)
           :context-menu/y (.. rect -bottom)
           :context-menu/show true)))


(defn bullet-drag-start
  "Begin drag event: https://developer.mozilla.org/en-US/docs/Web/API/HTML_Drag_and_Drop_API#Define_the_drags_data"
  [e uid state]
  (let [effect-allowed (if (.. e -shiftKey) "link" "move")]
    (set! (.. e -dataTransfer -effectAllowed) effect-allowed))
  (.. e -dataTransfer (setData "text/plain" (-> uid db/uid-and-embed-id first)))
  (swap! state assoc :dragging true))


(defn bullet-drag-end
  "End drag event."
  [_e _uid state]
  (swap! state assoc :dragging false))


(defn bullet-el
  [_ _ _]
  (fn [block state linked-ref]
    (let [{:block/keys [uid children open]} block]
      [:span {:class           ["bullet" (when (and (seq children)
                                                    (or (and (true? linked-ref) (not (:linked-ref/open @state)))
                                                        (and (false? linked-ref) (not open))))
                                           "closed-with-children")]
              :draggable       true
              :on-click        (fn [e] (navigate-uid uid e))
              :on-context-menu (fn [e] (bullet-context-menu e uid state))
              :on-mouse-over   (fn [e] (bullet-mouse-over e uid state)) ;; useful during development to check block meta-data
              :on-mouse-out    (fn [e] (bullet-mouse-out e uid state))
              :on-drag-start   (fn [e] (bullet-drag-start e uid state))
              :on-drag-end     (fn [e] (bullet-drag-end e uid state))}])))


(defn copy-refs-mouse-down
  [_ uid state]
  (let [selected-items @(subscribe [:selected/items])
        ;; use this when using datascript-transit
        ;uids (map (fn [x] [:block/uid x]) selected-items)
        ;blocks (d/pull-many @db/dsdb '[*] ids)
        data           (if (empty? selected-items)
                         (str "((" uid "))")
                         (->> (map (fn [uid] (str "((" uid "))\n")) selected-items)
                              (str/join "")))]
    (.. js/navigator -clipboard (writeText data))
    (swap! state assoc :context-menu/show false)))


(defn handle-copy-unformatted
  "If copying only a single block, dissoc children to not copy subtree."
  [^js e uid state]
  (let [uids @(subscribe [:selected/items])]
    (if (empty? uids)
      (let [block (dissoc (db/get-block-document [:block/uid uid]) :block/children)
            data  (listeners/blocks-to-clipboard-data 0 block true)]
        (.. js/navigator -clipboard (writeText data)))
      (let [data (->> (map #(db/get-block-document [:block/uid %]) uids)
                      (map #(listeners/blocks-to-clipboard-data 0 % true))
                      (apply str))]
        (.. js/navigator -clipboard (writeText data)))))
  (.. e preventDefault)
  (swap! state assoc :context-menu/show false))


(defn context-menu-el
  "Only option in context menu right now is copy block ref(s)."
  [_block state]
  (let [ref                  (atom nil)
        handle-click-outside (fn [e]
                               (when (and (:context-menu/show @state)
                                          (not (.. @ref (contains (.. e -target)))))
                                 (swap! state assoc :context-menu/show false)))]
    (r/create-class
      {:display-name           "context-menu"
       :component-did-mount    (fn [_this] (events/listen js/document "mousedown" handle-click-outside))
       :component-will-unmount (fn [_this] (events/unlisten js/document "mousedown" handle-click-outside))
       :reagent-render         (fn [block state]
                                 (let [{:block/keys [uid]} block
                                       {:context-menu/keys [x y show]} @state
                                       selected-items @(subscribe [:selected/items])]
                                   (when show
                                     [:div (merge (use-style dropdown-style
                                                             {:ref #(reset! ref %)})
                                                  {:style {:position "fixed"
                                                           :left     (str x "px")
                                                           :top      (str y "px")}})
                                      [:div (use-style menu-style)
                                       [button {:on-mouse-down (fn [e] (copy-refs-mouse-down e uid state))}
                                        (if (empty? selected-items)
                                          "Copy block ref"
                                          "Copy block refs")]
                                       [button {:on-mouse-down (fn [e] (handle-copy-unformatted e uid state))}
                                        "Copy unformatted"]]])))})))


(defn block-refs-count-el
  [count uid]
  [:div (use-style {:margin-left "1em"
                    :z-index (:zindex-dropdown ZINDICES)
                    :visibility (when-not (pos? count) "hidden")})
    [button {:primary true :on-click #(dispatch [:right-sidebar/open-item uid])} count]])


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
        is-selected?      @(subscribe [:selected/is-selected uid])
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
        selected-items @(subscribe [:selected/items])]

    (cond
      (re-find img-regex datatype) (when (util/electron?)
                                     (electron/dnd-image target-uid drag-target item (second (re-find img-regex datatype))))
      (re-find #"text/plain" datatype) (when valid-text-drop
                                         (if (empty? selected-items)
                                           (dispatch [:drop source-uid target-uid drag-target effect-allowed])
                                           (dispatch [:drop-multi selected-items target-uid drag-target]))))

    (dispatch [:mouse-down/unset])
    (swap! state assoc :drag-target nil)))


(defn block-drag-leave
  "When mouse leaves block, remove any drop area indicator.
  Ignore if target-uid and related-uid are the same — user went over a child component and we don't want flicker."
  [e block state]
  (.. e preventDefault)
  (.. e stopPropagation)
  (let [{target-uid :block/uid} block
        related-uid (get-dataset-uid (.. e -relatedTarget))]
    (when-not (= related-uid target-uid)
      ;;(prn target-uid related-uid  "LEAVE")
      (swap! state assoc :drag-target nil))))


;;TODO: more clarity on open? and closed? predicates, why we use `cond` in one case and `if` in another case)
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
             is-editing  @(subscribe [:editing/is-editing uid])
             is-selected @(subscribe [:selected/is-selected uid])]

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

          [:div (use-style (merge drop-area-indicator (when (= drag-target :above) {:opacity "1"})))]

          [:div.block-body
           [:button.block-edit-toggle
            {:on-click (fn [e]
                         (when (false? (.. e -shiftKey))
                           (dispatch [:editing/uid uid])))}]

           [toggle-el uid-sanitized-block state linked-ref]
           [context-menu-el uid-sanitized-block state]
           [bullet-el block state linked-ref]
           [tooltip-el uid-sanitized-block state]
           [block-content-el block state]
           (when-not (:block-embed? opts)
             [block-refs-count-el (count _refs) uid])]

          [inline-search-el block state]
          [slash-menu-el block state]


          ;; Children
          (when (and (seq children)
                     (or (and (true? linked-ref) (:linked-ref/open @state))
                         (and (false? linked-ref) open)))
            (for [child children]
              [:div {:key (:db/id child)}
               [block-el child
                (assoc linked-ref-data :initial-open (contains? parent-uids (:block/uid child)))
                opts]]))

          [:div (use-style (merge drop-area-indicator (when (= drag-target :below) {;;:color "red"
                                                                                    :opacity "1"})))]])))))


(defn block-component
  [ident]
  (let [block (db/get-block-document ident)]
    [block-el block]))
