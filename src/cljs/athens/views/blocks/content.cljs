(ns athens.views.blocks.content
  (:require
    [athens.common-db :as common-db]
    [athens.common.utils :as utils]
    [athens.config :as config]
    [athens.db :as db]
    [athens.electron.images :as images]
    [athens.events.selection :as select-events]
    [athens.parse-renderer :refer [parse-and-render]]
    [athens.patterns :as patterns]
    [athens.style :as style]
    [athens.subs.selection :as select-subs]
    [athens.util :as util]
    [athens.views.blocks.textarea-keydown :as textarea-keydown]
    [cljs.pprint :as pp]
    [clojure.edn :as edn]
    [clojure.set :as set]
    [clojure.string :as str]
    [clojure.walk :as walk]
    [datascript.core :as d]
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
                     [:span.text-run
                      {:pointer-events "None"}
                      [:>a {:position "relative"
                            :z-index 2
                            :pointer-events "all"}]]
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
                                                                    :transition "color 0.05s ease, transform 0.05s ease, box-shadow 0.05s ease"
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
  (let [target              (.. e -target)
        page                (or (.. target (closest ".node-page"))
                                (.. target (closest ".block-page")))
        blocks              (->> (.. page (querySelectorAll ".block-container"))
                                 array-seq
                                 vec)
        uids                (map util/get-dataset-uid blocks)
        uids->children-uids (->> (zipmap uids
                                         (map util/get-dataset-children-uids blocks))
                                 (remove #(-> % second empty?))
                                 (into {}))
        indexed-uids        (map-indexed vector uids)
        start-index         (->> indexed-uids
                                 (filter (fn [[_idx uid]]
                                           (= source-uid uid)))
                                 ffirst)
        end-index           (->> indexed-uids
                                 (filter (fn [[_idx uid]]
                                           (= target-uid uid)))
                                 ffirst)
        selected-uids       (set @(rf/subscribe [::select-subs/items]))
        candidate-uids      (->> indexed-uids
                                 (filter (fn [[idx _uid]]
                                           (<= (min start-index end-index)
                                               idx
                                               (max start-index end-index))))
                                 (map second)
                                 (into #{}))
        descendants-uids    (loop [descendants    #{}
                                   ancestors-uids candidate-uids]
                              (if (seq ancestors-uids)
                                (let [ancestors-children (->> ancestors-uids
                                                              (mapcat #(get uids->children-uids %))
                                                              (into #{}))]
                                  (recur (set/union descendants ancestors-children)
                                         ancestors-children))
                                descendants))
        to-remove-uids      (set/intersection selected-uids descendants-uids)
        selection-new-uids  (set/difference candidate-uids descendants-uids)
        new-selected-uids   (-> selected-uids
                                (set/difference to-remove-uids)
                                (set/union selection-new-uids))
        selection-order     (->> indexed-uids
                                 (filter (fn [[_k v]]
                                           (contains? new-selected-uids v)))
                                 (mapv second))]
    (when config/debug?
      (js/console.debug (str "selection: " (pr-str selected-uids)
                             ", candidates: " (pr-str candidate-uids)
                             ", descendants: " (pr-str descendants-uids)
                             ", rm: " (pr-str to-remove-uids)
                             ", add: " (pr-str selection-new-uids)))
      (js/console.debug :find-selected-items (pr-str {:source-uid      source-uid
                                                      :target-uid      target-uid
                                                      :selection-order selection-order})))
    (when (and start-index end-index)
      (rf/dispatch [::select-events/set-items selection-order]))))






;; Event Handlers

;; TODO Move the following to correct location

(defn new-uids-map
  "From Athens representation, extract the uids and create a mapping to new uids."
  [tree]
  (let [all-old-uids (mapcat #(->> %
                                   (tree-seq :block/children :block/children)
                                   (mapv :block/uid))
                             tree)
        mapped-uids (reduce #(assoc %1 %2 (utils/gen-block-uid)) {} all-old-uids)] ; Replace with zipmap
    mapped-uids))


(defn update-strings-with-new-uids
  "Takes a string of text and parses it for block refs, block embeds using regex. Then replace the matched pattern
   with new refs.

   Could also use block ref information from the db instead (refs->uids->replacements).
   Just something to keep in mind for the future if this gets hard to maintain.

   Pattern: Strings should not have a space before, after or in between the block uid
            In the following example no pattern is valid:
            (()) (( uid)) ((uid )) (( uid )) ((Uid with space))

            To understand the regex pattern like lookback etc. checkout this link: https://stackoverflow.com/questions/2973436/regex-lookahead-lookbehind-and-atomic-groups
   "
  [block-string mapped-uids]
  (let [parsed-uids     (into #{} (re-seq patterns/block-refs-pattern
                                          block-string))
        replaced-string (reduce (fn [block-string ref]
                                  (let [embed?       (seq (re-find patterns/block-embed-pattern
                                                                   ref))
                                        uid          (if embed?
                                                       (common-db/strip-markup ref "{{[[embed]]: ((" "))}}")
                                                       (common-db/strip-markup ref "((" "))"))
                                        new-uid      (get mapped-uids uid nil)
                                        replace-with (cond
                                                       (and embed? new-uid)       (str "{{[[embed]]: ((" new-uid "))}}")
                                                       (and (not embed?) new-uid) (str "((" new-uid "))")
                                                       :else                      ref)]
                                    (if new-uid
                                      (str/replace block-string
                                                   ref
                                                   replace-with)
                                      block-string)))
                                block-string
                                parsed-uids)]
    replaced-string))


(defn walk-tree-to-replace
  "Walk the internal representation and replace specific key-value pairs. This is inspired from the
  `walk/postwalk-replace` implementation."
  [tree mapped-uids replace-keyword]
  (walk/postwalk (fn [x]
                   (if (and (vector? x)
                            (= (first x) replace-keyword))
                     (cond
                       (= replace-keyword :block/uid)    [:block/uid    (mapped-uids (last x))]
                       (= replace-keyword :block/string) [:block/string (update-strings-with-new-uids (last x)
                                                                                                      mapped-uids)])
                     x))
                 tree))


(defn update-uids
  "In the internal representation replace the uids and block-strings with new uids."
  [tree mapped-uids]
  (let [block-uids-replaced          (walk-tree-to-replace tree
                                                           mapped-uids
                                                           :block/uid)
        blocks-with-replaced-strings (walk-tree-to-replace block-uids-replaced
                                                           mapped-uids
                                                           :block/string)]
    blocks-with-replaced-strings))


(defn text-to-blocks
  [text uid root-order]
  (let [;; Split raw text by line
        lines       (->> (clojure.string/split-lines text)
                         (filter (comp not clojure.string/blank?)))
        ;; Count left offset
        left-counts (->> lines
                         (map #(re-find #"^\s*(-|\*)?" %))
                         (map #(-> % first count)))
        ;; Trim * - and whitespace
        sanitize    (map (fn [x] (clojure.string/replace x #"^\s*(-|\*)?\s*" ""))
                         lines)
        ;; Generate blocks with tempids
        blocks      (map-indexed (fn [idx x]
                                   {:db/id        (dec (* -1 idx))
                                    :block/string x
                                    :block/open   true
                                    :block/uid    (utils/gen-block-uid)}) ; TODO(BUG): UID generation during resolution
                                 sanitize)
        top_uids    []
        ;; Count blocks
        n           (count blocks)
        ;; Assign parents
        parents     (loop [i   1
                           res [(first blocks)]]
                      (if (= n i)
                        res
                        ;; Nested loop: worst-case O(n^2)
                        (recur (inc i)
                               (loop [j (dec i)]
                                 ;; If j is negative, that means the loop has been compared to every previous line,
                                 ;; and there are no previous lines with smaller left-offsets, which means block i
                                 ;; should be a root block.
                                 ;; Otherwise, block i's parent is the first block with a smaller left-offset
                                 (if (neg? j)
                                   (do
                                     (conj top_uids (nth blocks i))
                                     (conj res (nth blocks i)))
                                   (let [curr-count (nth left-counts i)
                                         prev-count (nth left-counts j nil)]
                                     (if (< prev-count curr-count)
                                       (conj res {:db/id          (:db/id (nth blocks j))
                                                  :block/children (nth blocks i)})
                                       (recur (dec j)))))))))
        ;; assign orders for children. order can be local or based on outer context where paste originated
        ;; if local, look at order within group. if outer, use root-order
        tx-data     (->> (group-by :db/id parents)
                         ;; maps smaller than size 8 are ordered, larger are not https://stackoverflow.com/a/15500064
                         (into (sorted-map-by >))
                         (mapcat (fn [[_tempid blocks]]
                                   (loop [order 0
                                          res   []
                                          data  blocks]
                                     (let [{:block/keys [children] :as block} (first data)]
                                       (cond
                                         (nil? block) res
                                         (nil? children) (let [new-res (conj res {:db/id          [:block/uid uid]
                                                                                  :block/children (assoc block :block/order @root-order)})]
                                                           (swap! root-order inc)
                                                           (recur order
                                                                  new-res
                                                                  (next data)))
                                         :else (recur (inc order)
                                                      (conj res (assoc-in block [:block/children :block/order] order))
                                                      (next data))))))))]
    (into [] tx-data)))


(defn text-to-internal-representation
  [text]
  (let [cpdb                  (d/create-conn db/schema)
        copy-paste-block      [{:db/id          -1
                                :block/uid      "copy-paste-uid"
                                :block/children []
                                :block/string   "Block for copy paste"}]
        tx-data               (text-to-blocks text
                                              "copy-paste-uid"
                                              (atom 0))]
    ;; transact first block
    (d/transact! cpdb copy-paste-block)

    ;; transact the copied blocks
    (d/transact! cpdb tx-data)

    ;; get the internal representation 
    ;; we need the eid of the copy-paste-block because that is where all the blocks are added to
    ;; all the copied data will be added as the children of the `copy-paste-block`
    (:block/children (common-db/get-block-document-for-copy @cpdb
                                                            (:db/id (common-db/get-block @cpdb [:block/uid "copy-paste-uid"]))))))


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
  (let [data                (.. e -clipboardData)
        text-data           (.getData data "text/plain")
        _app-clip           (some-> (.getData data "application/athens")
                                    edn/read-string)
        ;; With internal representation
        internal-representation  (some-> (.getData data "application/athens-representation")
                                         edn/read-string)
        internal?           (seq internal-representation)
        new-uids            (new-uids-map internal-representation)
        repr-with-new-uids  (into [] (update-uids internal-representation new-uids))

        ;; External to internal representation
        text-to-inter       (text-to-internal-representation text-data)
        line-breaks         (re-find #"\r?\n" text-data)
        no-shift            (-> @state :last-keydown :shift not)
        items               (array-seq (.. e -clipboardData -items))
        {:keys [head tail]} (athens.views.blocks.textarea-keydown/destruct-target (.-target e))
        img-regex           #"(?i)^image/(p?jpeg|gif|png)$"]


    (println " Representation with updated uids")
    (pp/pprint repr-with-new-uids)

    (println "External copied data's internal representation")
    (pp/pprint text-to-inter)
    
    (cond
      ;; For internal representation
      internal?
      (do
        (.. e preventDefault)
        (rf/dispatch [:paste-internal uid repr-with-new-uids]))

      ;; For images
      (seq (filter (fn [item]
                     (let [datatype (.. item -type)]
                       (re-find img-regex datatype))) items))
      (mapv (fn [item]
              (let [datatype (.. item -type)]
                (cond
                  (re-find img-regex datatype) (when (util/electron?)
                                                 (let [new-str (images/save-image head tail item "png")]
                                                   (js/setTimeout #(swap! state assoc :string/local new-str) 50)))
                  (re-find #"text/html" datatype) (.getAsString item (fn [_] #_(prn "getAsString" _))))))
            items)

      (and line-breaks no-shift)
      (do
        (.. e preventDefault)
        (rf/dispatch [:paste-internal uid text-to-inter]))

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
        source-uid     @(rf/subscribe [:editing/uid])
        shift?         (.-shiftKey e)]
    (if (and shift?
             source-uid
             target-uid
             (not= source-uid target-uid))
      (find-selected-items e source-uid target-uid)
      (rf/dispatch [::select-events/clear]))))


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
      (rf/dispatch [::select-events/clear])
      (find-selected-items e source-uid target-uid))))


;; View

(defn block-content-el
  "Actual string contents. Two elements, one for reading and one for writing.
  The CSS class is-editing is used for many things, such as block selection.
  Opacity is 0 when block is selected, so that the block is entirely blue, rather than darkened like normal editing.
  is-editing can be used for shift up/down, so it is used in both editing and selection."
  [block state]
  (let [{:block/keys [uid original-uid header]} block
        editing? (rf/subscribe [:editing/is-editing uid])
        selected-items (rf/subscribe [::select-subs/items])]
    (fn [_block _state]
      (let [font-size (case header
                        1 "2.1em"
                        2 "1.7em"
                        3 "1.3em"
                        "1em")]
        [:div {:class ["block-content"]
               :style {:font-size font-size}}
         ;; NOTE: komponentit forces reflow, likely a performance bottle neck
         ;; When block is in editing mode or the editing DOM elements are rendered
         (when (or (:show-editable-dom @state) editing?)
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
                               :on-mouse-down  (fn [e] (textarea-mouse-down e uid state))}])
         ;; TODO pass `state` to parse-and-render
         [parse-and-render (:string/local @state) (or original-uid uid)]]))))

