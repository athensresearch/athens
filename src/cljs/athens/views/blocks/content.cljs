(ns athens.views.blocks.content
  (:require
    [athens.db :as db]
    [athens.electron :as electron]
    [athens.events :as events]
    [athens.keybindings :refer [mousetrap]]
    [athens.parse-renderer :refer [parse-and-render]]
    [athens.style :as style]
    [athens.util :as util]
    [athens.views.blocks.textarea-keydown :as textarea-keydown]
    [garden.selectors :as selectors]
    [goog.events :as goog-events]
    [komponentit.autosize :as autosize]
    [re-frame.core :as rf]
    [stylefy.core :as stylefy])
  (:import
    (goog.events
      EventType)))


;; Styles

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
   ::stylefy/manual [[:textarea {:display "block"
                                 :line-height 0
                                 :-webkit-appearance "none"
                                 :cursor "text"
                                 :resize "none"
                                 :transform "translate3d(0,0,0)"
                                 :color "inherit"
                                 :outline "none"
                                 :overflow "hidden"
                                 :padding "0"
                                 :background (style/color :background-minus-1)
                                 :grid-area "main"
                                 :min-height "100%"
                                 :caret-color (style/color :link-color)
                                 :margin "0"
                                 :font-size "inherit"
                                 :border-radius "0.25rem"
                                 :box-shadow (str "-0.25rem 0 0 0" (style/color :background-minus-1))
                                 :border "0"
                                 :opacity "0"
                                 :font-family "inherit"}]
                     [:&:hover [:textarea [(selectors/& (selectors/not :.is-editing)) {:line-height 2}]]]
                     [:.is-editing {:z-index 3
                                    :line-height "inherit"
                                    :opacity "1"}]
                     [:span
                      {:grid-area "main"}
                      [:>span
                       :>a {:position "relative"
                            :z-index 2}]]
                     [:abbr
                      {:grid-area "main"
                       :z-index   4}
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
                               :box-shadow [["inset 0 0 0 0.125rem" (style/color :background-minus-1)]]
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
                                                                    :color (style/color :link-color)
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
                              [:&:checked {:background (style/color :link-color)}
                               [:&:after {:opacity 1
                                          :color (style/color :background-color)}]]
                              [:&:active {:transform "scale(0.9)"}]]]

                     [:h1 :h2 :h3 :h4 :h5 :h6 {:margin "0"
                                               :color (style/color :body-text-color :opacity-higher)
                                               :font-weight "500"}]
                     [:h1 {:padding "0"
                           :margin-block-start "-0.1em"}]
                     [:h2 {:padding "0"}]
                     [:h3 {:padding "0"}]
                     [:h4 {:padding "0.25em 0"}]
                     [:h5 {:padding "1em 0"}]
                     [:h6 {:text-transform "uppercase"
                           :letter-spacing "0.06em"
                           :padding "1em 0"}]
                     [:p {:margin "0"
                          :padding-bottom "1em"}]
                     [:blockquote {:margin-inline "0.5em"
                                   :margin-block "0.125rem"
                                   :padding-block "calc(0.5em - 0.125rem - 0.125rem)"
                                   :padding-inline "1.5em"
                                   :border-radius "0.25em"
                                   :background (style/color :background-minus-1)
                                   :border-inline-start [["0.25em solid" (style/color :body-text-color :opacity-lower)]]
                                   :color (style/color :body-text-color :opacity-high)}
                      [:p {:padding-bottom "1em"}]
                      [:p:last-child {:padding-bottom "0"}]]
                     [:.CodeMirror {:background (style/color :background-minus-1)
                                    :margin "0.125rem 0.5rem"
                                    :border-radius "0.25rem"
                                    :font-size "85%"
                                    :color (style/color :body-text-color)
                                    :font-family "IBM Plex Mono"}]
                     [:.CodeMirror-gutters {:border-right "1px solid transparent"
                                            :background (style/color :background-minus-1)}]
                     [:.CodeMirror-cursor {:border-left-color (style/color :link-color)}]
                     [:.CodeMirror-lines {:padding 0}]
                     [:.CodeMirror-linenumber {:color (style/color :body-text-color :opacity-med)}]

                     [:mark.contents.highlight {:padding "0 0.2em"
                                                :border-radius "0.125rem"
                                                :background-color (style/color :text-highlight-color)}]]})


(stylefy/class "block-content" block-content-style)


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
        uids (map util/get-dataset-uid blocks)
        start-idx (first (keep-indexed (fn [i uid] (when (= uid source-uid) i)) uids))
        end-idx   (first (keep-indexed (fn [i uid] (when (= uid target-uid) i)) uids))]
    (when (and start-idx end-idx)
      (let [up? (> start-idx end-idx)
            delta (js/Math.abs (- start-idx end-idx))
            select-fn  (if up? events/select-up events/select-down)
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
        (rf/dispatch [:selected/add-items new-items])))))


;; Event Handlers

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
        {:keys [head tail]} (athens.views.blocks.textarea-keydown/destruct-target (.-target e))
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

      (and line-breaks no-shift)
      (do
        (.. e preventDefault)
        (rf/dispatch [:paste uid text-data]))

      (not no-shift)
      (do
        (.. e preventDefault)
        (rf/dispatch [:paste-verbatim uid text-data]))

      :else
      nil)))


(defn textarea-change
  [e _uid state]
  (swap! state assoc :string/local (.. e -target -value)))


(defn textarea-click
  "If shift key is held when user clicks across multiple blocks, select the blocks."
  [e target-uid _state]
  (let [[target-uid _] (db/uid-and-embed-id target-uid)
        source-uid @(rf/subscribe [:editing/uid])]
    (when (and source-uid target-uid (not= source-uid target-uid) (.. e -shiftKey))
      (find-selected-items e source-uid target-uid))))


(defn global-mouseup
  "Detach global mouseup listener (self)."
  [_]
  (goog-events/unlisten js/document EventType.MOUSEUP global-mouseup)
  (rf/dispatch [:mouse-down/unset]))


(defn textarea-mouse-down
  "Attach global mouseup listener. Listener can't be local because user might let go of mousedown off of a block.
  See https://javascript.info/mouse-events-basics#events-order"
  [e _uid _]
  (.. e stopPropagation)
  (when (false? (.. e -shiftKey))
    (rf/dispatch [:editing/target (.. e -target)])
    (let [mouse-down @(rf/subscribe [:mouse-down])]
      (when (false? mouse-down)
        (rf/dispatch [:mouse-down/set])
        (goog-events/listen js/document EventType.MOUSEUP global-mouseup)))))


(defn textarea-mouse-enter
  "When mouse-down, user is selecting multiple blocks with click+drag.
  Use same algorithm as shift-enter, only updating the source and target."
  [e target-uid _]
  (let [source-uid @(rf/subscribe [:editing/uid])
        mouse-down @(rf/subscribe [:mouse-down])]
    (when mouse-down
      (rf/dispatch [:selected/clear-items])
      (find-selected-items e source-uid target-uid))))


;; View
;; Handlers will come from textarea-keydown
(defn content-keybindings
  [uid state child]
  (let [event-wrapper
        ;; Only handle event if the currently editing uid is the same
        ;; as this block; and there aren't any selected item.
        (fn [callback-or-config _]
          (let [wrap-callback
                (fn [callback]
                  (fn [event]
                    (let [editing? (= uid @(rf/subscribe [:editing/uid]))
                          not-selecting-items? (empty? @(rf/subscribe [:selected/items]))]
                      (if (and editing? not-selecting-items?)
                        (callback event)))))]
            (if (map? callback-or-config)
              (update callback-or-config :callback wrap-callback)
              (wrap-callback callback-or-config))))]
    [mousetrap
     (util/map-map-values event-wrapper
                          (merge
                            {["shift+up" "shift+down"]    (partial textarea-keydown/handle-arrow-key-with-shift uid state)
                             ["mod+down" "mod+up"] (partial textarea-keydown/handle-arrow-key-with-mod uid state)
                             ["up" "down" "right" "left"] {:callback (partial textarea-keydown/handle-arrow-key uid state)
                                                           :stop-propagation? false}
                             ["tab" "shift+tab"] textarea-keydown/handle-tab
                             ["enter" "mod+enter" "shift+enter"] (partial textarea-keydown/handle-enter uid state)
                             "backspace"                  (partial textarea-keydown/handle-backspace uid state)
                             "del"                        (partial textarea-keydown/handle-delete uid state)
                             "esc"                        (partial textarea-keydown/handle-escape state)}
                            (textarea-keydown/shortcut-handlers uid state)))
     child]))


(defn block-content-el
  "Actual string contents. Two elements, one for reading and one for writing.
  The CSS class is-editing is used for many things, such as block selection.
  Opacity is 0 when block is selected, so that the block is entirely blue, rather than darkened like normal editing.
  is-editing can be used for shift up/down, so it is used in both editing and selection."
  [block state]
  (let [{:block/keys [uid original-uid header]} block
        editing? (rf/subscribe [:editing/is-editing uid])
        selected-items (rf/subscribe [:selected/items])]
    (fn [_block _state]
      (let [font-size (case header
                        1 "2.1em"
                        2 "1.7em"
                        3 "1.3em"
                        "1em")]
        [:div {:class "block-content" :style {:font-size font-size}}
         ;; NOTE: komponentit forces reflow, likely a performance bottle neck
         ;; When block is in editing mode or the editing DOM elements are rendered
         (when (or (:show-editable-dom @state) editing?)
           [content-keybindings uid state
            [autosize/textarea {:value          (:string/local @state)
                                :class          ["textarea" (when (and (empty? @selected-items) @editing?) "is-editing")]
                                ;; :auto-focus  true
                                :id             (str "editable-uid-" uid)
                                :on-change      (fn [e] (textarea-change e uid state))
                                :on-paste       (fn [e] (textarea-paste e uid state))
                                :on-key-down    (fn [e] (textarea-keydown/textarea-key-down e uid state))
                                :on-blur        (fn [_] (db/transact-state-for-uid (or original-uid uid) state))
                                :on-click       (fn [e] (textarea-click e uid state))
                                :on-mouse-enter (fn [e] (textarea-mouse-enter e uid state))
                                :on-mouse-down  (fn [e] (textarea-mouse-down e uid state))}]])
         ;; TODO pass `state` to parse-and-render
         [parse-and-render (:string/local @state) (or original-uid uid)]]))))

