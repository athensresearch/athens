(ns athens.views.blocks.textarea-keydown
  (:require
    ["/components/Icons/Icons" :refer [TimeNowIcon PersonIcon CheckboxIcon CalendarNowIcon CalendarTomorrowIcon CalendarYesterdayIcon BlockEmbedIcon TemplateIcon HTMLEmbedIcon YoutubeIcon]]
    [athens.common-db :as common-db]
    [athens.common.utils :as common.utils]
    [athens.dates :as dates]
    [athens.db :as db]
    [athens.events.inline-search :as inline-search.events]
    [athens.events.selection :as select-events]
    [athens.patterns :as patterns]
    [athens.router :as router]
    [athens.subs.inline-search :as inline-search.subs]
    [athens.subs.selection :as select-subs]
    [athens.util :as util :refer [scroll-if-needed get-caret-position shortcut-key?]]
    [athens.views.blocks.internal-representation :as internal-representation]
    [clojure.string :refer [replace-first blank? includes? lower-case]]
    [goog.dom :refer [getElement]]
    [goog.dom.selection :refer [setStart setEnd getText setCursorPosition getEndPoints]]
    [goog.events.KeyCodes :refer [isCharacterKey]]
    [goog.functions :refer [throttle]]
    [re-frame.core :as rf :refer [dispatch dispatch-sync subscribe]])
  (:import
    (goog.events
      KeyCodes)))


;; Event Helpers


(defn modifier-keys
  [e]
  (let [shift (.. e -shiftKey)
        meta (.. e -metaKey)
        ctrl (.. e -ctrlKey)
        alt (.. e -altKey)]
    {:shift shift :meta meta :ctrl ctrl :alt alt}))


(defn get-end-points
  [target]
  (js->clj (getEndPoints target)))


(defn set-cursor-position
  [target idx]
  (setCursorPosition target idx))


(defn destruct-target
  "Get the current value of a textarea (`:value`) and
   the start (`:start`) and end (`:end`) index of the selection.
   Furthermore, split the selection into three parts:
   text before the selection (`:head`),
   the selection itself (`:selection`),
   and text after the selection (`:tail`)."
  [target]
  (let [value (.. target -value)
        [start end] (get-end-points target)
        selection (getText target)
        head (subs value 0 start)
        tail (subs value end)]
    (merge {:value value}
           {:start start :end end}
           {:head head :tail tail}
           {:selection selection})))


(defn destruct-key-down
  [e]
  (let [key (.. e -key)
        key-code (.. e -keyCode)
        target (.. e -target)
        value (.. target -value)
        event {:key key :key-code key-code :target target :value value}
        modifiers (modifier-keys e)
        target-data (destruct-target target)]
    (merge modifiers
           event
           target-data)))


(def ARROW-KEYS
  #{KeyCodes.UP
    KeyCodes.LEFT
    KeyCodes.DOWN
    KeyCodes.RIGHT})


(defn arrow-key-direction
  [e]
  (contains? ARROW-KEYS (.. e -keyCode)))


;; Dropdown: inline-search and slash commands
;; TODO: some expansions require caret placement after
(defn slash-options
  []
  (cond->
    [["Add Todo"      CheckboxIcon "{{[[TODO]]}} " "cmd-enter" nil]
     ["Current Time"  TimeNowIcon (fn [] (.. (js/Date.) (toLocaleTimeString [] (clj->js {"timeStyle" "short"})))) nil nil]
     ["Today"         CalendarNowIcon (fn [] (str "[[" (:title (dates/get-day 0)) "]] ")) nil nil]
     ["Tomorrow"      CalendarTomorrowIcon (fn [] (str "[[" (:title (dates/get-day -1)) "]]")) nil nil]
     ["Yesterday"     CalendarYesterdayIcon (fn [] (str "[[" (:title (dates/get-day 1)) "]]")) nil nil]
     ["YouTube Embed" YoutubeIcon "{{[[youtube]]: }}" nil 2]
     ["iframe Embed"  HTMLEmbedIcon "{{iframe: }}" nil 2]
     ["Block Embed"   BlockEmbedIcon "{{[[embed]]: (())}}" nil 4]
     ["Template"      TemplateIcon ";;" nil nil]
     ["Property"      TemplateIcon "::" nil nil]]
    @(subscribe [:db-picker/remote-db?])
    (conj (let [username (:username @(rf/subscribe [:presence/current-user]))]
            [(str "Me (" username ")") PersonIcon (fn [] (str "[[" username "]]")) nil nil]))))


;; [ "Block Embed" #(str "[[" (:title (dates/get-day 1)) "]]")]
;; [DateRange "Date Picker"]
;; [Attachment "Upload Image or File"]
;; [ExposurePlus1 "Word Count"]


(defn filter-slash-options
  [query]
  (if (blank? query)
    (slash-options)
    (filterv (fn [[text]]
               (includes? (lower-case text) (lower-case query)))
             (slash-options))))


(defn search-or-create-node-title
  [query]
  (let [results (db/search-in-node-title query)
        create  (if (and (seq query)
                         (not (some #(= query (:node/title %)) results)))
                  [{:text (str "Create property: " query)}]
                  [])]
    (into create results)))


(defn update-query
  "Used by backspace and write-char.
  write-char appends key character. Pass empty string during backspace.
  query-start is determined by doing a greedy regex find up to head.
  Head goes up to the text caret position."
  [block-uid head key type]
  (let [query-fn        (case type
                          :block db/search-in-block-content
                          :page db/search-in-node-title
                          :hashtag db/search-in-node-title
                          :template db/search-in-block-content
                          :property search-or-create-node-title
                          :slash filter-slash-options)
        regex           (case type
                          :block #"(?s).*\(\("
                          :page #"(?s).*\[\["
                          :hashtag #"(?s).*#"
                          :template #"(?s).*;;"
                          :property #"(?s)[^:]*::"
                          :slash #"(?s).*/")
        find            (re-find regex head)
        query-start-idx (count find)
        new-query       (str (subs head query-start-idx) key)
        results         (query-fn new-query)]
    (if (and (= type :slash) (empty? results))
      (rf/dispatch [::inline-search.events/close! block-uid])
      (do
        (rf/dispatch [::inline-search.events/set-index! block-uid 0])
        (rf/dispatch [::inline-search.events/set-results! block-uid results])
        (rf/dispatch [::inline-search.events/set-query! block-uid new-query])))))


;; https://developer.mozilla.org/en-US/docs/Web/API/Document/execCommand
;; textarea setval will lose ability to undo/redo

;; execCommand is obsolete:
;; be wary before updating electron - as chromium might drop support for execCommand
;; electron 11 - uses chromium < 90(latest) which supports execCommand
(defn replace-selection-with
  "replace the current selection with `new-text`"
  [new-text]
  (.execCommand js/document "insertText" false new-text))


(defn set-selection
  "select text from `start` to `end` in the textarea `target`"
  [target start end]
  (setStart target start)
  (setEnd target end))


;; 1- if no results, just hide slash commands so this doesnt get triggered
;; 2- if results, do find and replace properly
(defn auto-complete-slash
  ;; this signature is called to process keyboard events.
  ([block-uid e]
   (let [target (.. e -target)
         inline-search-index (rf/subscribe [::inline-search.subs/index block-uid])
         inline-search-results (rf/subscribe [::inline-search.subs/results block-uid])
         item (nth @inline-search-results @inline-search-index)]
     (auto-complete-slash block-uid target item)))
  ;; here comes the autocompletion logic itself,
  ;; independent of the input method the user used.
  ;; `expansion` is the identifier of the page or block
  ;; (i.e., UID of block or title of page) that shall be
  ;; inserted.
  ([block-uid target item]
   (let [{:keys [start head]} (destruct-target target)
         [caption _ expansion _ pos] item
         expand    (if (fn? expansion) (expansion) expansion)
         ;; the regex is evaluated greedily, yielding the last
         ;; occurrence in head (head = text up to cursor)
         start-idx (dec (count (re-find #"(?s).*/" head)))]
     (rf/dispatch [::inline-search.events/close! block-uid])

     (set-selection target start-idx start)
     (replace-selection-with expand)
     (when pos
       (let [new-idx (+ start-idx (count expand) (- pos))]
         (set-cursor-position target new-idx)
         (when (= caption "Block Embed")
           (rf/dispatch [::inline-search.events/set-type! block-uid :block])
           (rf/dispatch [::inline-search.events/clear-results! block-uid])
           (rf/dispatch [::inline-search.events/clear-query! block-uid]))))
     (when (= caption "Template")
       (rf/dispatch [::inline-search.events/set-type! block-uid :template])
       (rf/dispatch [::inline-search.events/clear-results! block-uid])
       (rf/dispatch [::inline-search.events/clear-query! block-uid]))
     (when (= caption "Property")
       (rf/dispatch [::inline-search.events/set-type! block-uid :property])
       (rf/dispatch [::inline-search.events/clear-results! block-uid])
       (rf/dispatch [::inline-search.events/clear-query! block-uid])))))


;; see `auto-complete-slash` for how this arity-overloaded
;; function is used.
(defn auto-complete-hashtag
  ([block-uid state-hooks e]
   (let [inline-search-index (rf/subscribe [::inline-search.subs/index block-uid])
         inline-search-results (rf/subscribe [::inline-search.subs/results block-uid])
         target (.. e -target)
         {:keys [node/title block/uid]} (nth @inline-search-results @inline-search-index nil)
         expansion (or title uid)]
     (auto-complete-hashtag block-uid state-hooks target expansion)))

  ([block-uid {:as _state-hooks} target expansion]
   (let [{:keys [start head]} (destruct-target target)
         start-idx (count (re-find #"(?s).*#" head))]
     (if (nil? expansion)
       (rf/dispatch [::inline-search.events/close! block-uid])
       (do
         (set-selection target start-idx start)
         (replace-selection-with (str "[[" expansion "]]"))
         (rf/dispatch [::inline-search.events/close! block-uid]))))))


;; see `auto-complete-slash` for how this arity-overloaded
;; function is used.
(defn auto-complete-inline
  ([block-uid _state-hooks e]
   (let [inline-search-index (rf/subscribe [::inline-search.subs/index block-uid])
         inline-search-results (rf/subscribe [::inline-search.subs/results block-uid])
         ;; (nth results (or index 0) nil) returns the index-th result
         ;; If (= index nil) or index is out of bounds, returns nil
         ;; For example, index can be nil if (= results [])
         {:keys [node/title block/uid]} (nth @inline-search-results (or @inline-search-index 0) nil)
         target (.. e -target)
         expansion    (or title uid)]
     (auto-complete-inline block-uid _state-hooks target expansion)))

  ([block-uid _state-hooks target expansion]
   (let [query @(rf/subscribe [::inline-search.subs/query block-uid])
         {:keys [end]} (destruct-target target)
         query        (patterns/escape-str query)]

     ;; assumption: cursor or selection is immediately before the closing brackets

     (when (not (nil? expansion))
       (set-selection target (- end (count query)) end)
       (replace-selection-with expansion))
     (let [new-cursor-pos (+ end
                             (- (count query))
                             ;; Add the expansion count if we have it, but if we
                             ;; don't just add back the query itself so the cursor
                             ;; doesn't move back.
                             (count (or expansion
                                        query))
                             2)]
       (set-cursor-position target new-cursor-pos))
     (rf/dispatch [::inline-search.events/close! block-uid]))))


;; see `auto-complete-slash` for how this arity-overloaded
;; function is used.
(defn auto-complete-template
  ([block-uid {:as state-hooks} e]
   (let [inline-search-index (rf/subscribe [::inline-search.subs/index block-uid])
         inline-search-results (rf/subscribe [::inline-search.subs/results block-uid])
         target (.. e -target)
         {:keys [block/uid]} (nth @inline-search-results @inline-search-index nil)
         expansion uid]
     (auto-complete-template block-uid state-hooks target expansion)))

  ([block-uid {:keys [read-value] :as _state-hooks} target expansion]
   (let [{:keys [start head]} (destruct-target target)
         start-idx (count (re-find #"(?s).*;;" head))
         source-ir (->> [:block/uid expansion]
                        (common-db/get-internal-representation @db/dsdb)
                        :block/children)
         target-ir (->> source-ir
                        internal-representation/new-uids-map
                        (internal-representation/update-uids source-ir)
                        (into []))]
     (if (or (nil? expansion)
             (nil? target-ir))
       (rf/dispatch [::inline-search.events/close! block-uid])
       (do
         (set-selection target (- start-idx 2) start)
         (replace-selection-with "")
         (dispatch [:paste-internal block-uid @read-value target-ir])
         (rf/dispatch [::inline-search.events/close! block-uid]))))))


;; see `auto-complete-slash` for how this arity-overloaded
;; function is used.
(defn auto-complete-property
  ([block-uid {:as state-hooks} e]
   (let [inline-search-index (rf/subscribe [::inline-search.subs/index block-uid])
         inline-search-results (rf/subscribe [::inline-search.subs/results block-uid])
         target (.. e -target)
         {:keys [block/uid]} (nth @inline-search-results @inline-search-index nil)
         expansion uid]
     (auto-complete-property block-uid state-hooks target expansion)))

  ([block-uid {:keys [read-value] :as _state-hooks} target expansion]
   (let [{:keys [start head]} (destruct-target target)
         start-idx (count (re-find #"(?s)[^:]*::" head))
         {:keys [end]} (destruct-target target)
         parent-uid (->> [:block/uid block-uid]
                         (common-db/get-parent @db/dsdb)
                         :block/uid)
         query @(rf/subscribe [::inline-search.subs/query block-uid])
         title (or (common-db/get-page-title @db/dsdb expansion) query)]
     (if (or (empty? title)
             (nil? parent-uid))
       (rf/dispatch [::inline-search.events/close! block-uid])
       (do
         (set-selection target (- start-idx 2) start)
         (replace-selection-with "")
         (dispatch [:block/move {:source-uid block-uid
                                 :target-uid parent-uid
                                 :target-rel {:page/title title}
                                 :local-string (str (subs @read-value 0 start-idx)
                                                    (subs @read-value end))}])
         (rf/dispatch [::inline-search.events/close! block-uid]))))))


;; Arrow Keys


(defn block-start?
  [e]
  (let [[start _] (get-end-points (.. e -target))]
    (zero? start)))


(defn block-end?
  [e]
  (let [{:keys [value end]} (destruct-key-down e)]
    (= end (count value))))


(defn dec-cycle
  [min max idx]
  (if (<= idx min)
    max
    (dec idx)))


(defn inc-cycle
  [min max idx]
  (if (>= idx max)
    min
    (inc idx)))


(defn cycle-list
  "If user has slash menu or inline search dropdown open:
  - pressing down increments index
  - pressing up decrements index
  0 is typically min index
  max index is collection length minus 1"
  [min max idx up? down?]
  (let [f (cond up? dec-cycle
                down? inc-cycle)]
    (f min max idx)))


(defn max-idx
  [coll]
  (-> coll count dec))


(defn handle-arrow-key
  [e uid {:keys [keyboard-navigation?
                 navigation-uid]
          :or   {keyboard-navigation? true}
          :as   _state-hooks}
   caret-position]
  (let [{:keys [key-code
                shift
                meta
                ctrl
                target
                selection]} (destruct-key-down e)
        selection?          (not (blank? selection))
        start?              (block-start? e)
        end?                (block-end? e)
        type                @(rf/subscribe [::inline-search.subs/type uid])
        results             @(rf/subscribe [::inline-search.subs/results uid])
        index               @(rf/subscribe [::inline-search.subs/index uid])
        textarea-height      (.. target -offsetHeight) ; this height is accurate, but caret-position height is not updating
        {:keys [top height]} @caret-position
        rows                 (js/Math.round (/ textarea-height height))
        row                  (js/Math.ceil (/ top height))
        top-row?             (= row 1)
        bottom-row?          (= row rows)
        up?                  (= key-code KeyCodes.UP)
        down?                (= key-code KeyCodes.DOWN)
        left?                (= key-code KeyCodes.LEFT)
        right?               (= key-code KeyCodes.RIGHT)
        [char-offset _]      (get-end-points target)]

    (cond
      ;; Shift: select block if leaving block content boundaries (top or bottom rows). Otherwise select textarea text (default)
      shift (cond
              left?                        nil
              right?                       nil
              (or (and up? top-row?)
                  (and down? bottom-row?)) (do
                                             (.. target blur)
                                             (dispatch [::select-events/add-item uid (cond
                                                                                       up?   :first
                                                                                       down? :last)])))

      ;; Control (Command on mac): fold or unfold blocks
      (shortcut-key? meta ctrl)
      (cond
        left?          nil
        right?         nil
        (or up? down?) (let [[uid _]        (db/uid-and-embed-id uid)
                             new-open-state (cond
                                              up?   false
                                              down? true)
                             event          [:block/open {:block-uid uid
                                                          :open?     new-open-state}]]
                         (.. e preventDefault)
                         (dispatch event)))

      ;; Type, one of #{:slash :block :page}: If slash commands or inline search is open, cycle through options
      type (cond
             (or left? right?) (do
                                 (rf/dispatch [::inline-search.events/close! uid])
                                 (rf/dispatch [::inline-search.events/set-index! uid 0]))
             (or up? down?)    (let [cur-index    index
                                     min-index    0
                                     max-index    (max-idx results)
                                     next-index   (cycle-list min-index max-index cur-index up? down?)
                                     container-el (getElement "dropdown-menu")
                                     target-el    (getElement (str "dropdown-item-" next-index))]
                                 (.. e preventDefault)
                                 (rf/dispatch [::inline-search.events/set-index! uid next-index])
                                 (scroll-if-needed target-el container-el)))

      selection? nil

      ;; Else: navigate across blocks
      ;; FIX: always navigates up or down for header because get-caret-position for some reason returns the wrong value for top

      ;; going LEFT at **0th index** should always go to **last index** of block **above**
      ;; last index is special - always go to last index when going up or down


      (or (and left? start?)
          (and up? end?)) (when keyboard-navigation?
                            (.. e preventDefault)
                            (dispatch [:up (or navigation-uid uid) :end]))

      (and down? end?) (when keyboard-navigation?
                         (.. e preventDefault)
                         (dispatch [:down (or navigation-uid uid) :end]))

      ;; going RIGHT at last index should always go to index 0 of block below
      (and right? end?) (when keyboard-navigation?
                          (.. e preventDefault)
                          (dispatch [:down (or navigation-uid uid) 0]))

      ;; index 0 is special - always go to index 0 when going up or down
      ;; when caret is anywhere between start and end preserve the position and offset by char
      (and up? top-row?)      (when keyboard-navigation?
                                (.. e preventDefault)
                                (dispatch [:up (or navigation-uid uid) char-offset]))
      (and down? bottom-row?) (when keyboard-navigation?
                                (.. e preventDefault)
                                (dispatch [:down (or navigation-uid uid) char-offset])))))


;; Tab

(defn handle-tab
  "Bug: indenting sets the cursor position to 0, likely because a new textarea element is created on the DOM. Set selection appropriately.
  See :indent event for why value must be passed as well."
  [e uid {:keys [read-value tab-handler navigation-uid] :as _state-hooks}]
  (.. e preventDefault)
  (let [{:keys [shift] :as d-key-down} (destruct-key-down e)
        selected-items                 @(subscribe [::select-subs/items])
        current-root-uid               @(subscribe [:current-route/uid])
        [uid embed-id]                 (db/uid-and-embed-id uid)
        local-string                   @read-value]
    (when (empty? selected-items)
      (if (fn? tab-handler)
        (tab-handler uid embed-id d-key-down)
        (if shift
          (dispatch [:unindent {:uid              (or navigation-uid uid)
                                :editing-uid      uid
                                :d-key-down       d-key-down
                                :context-root-uid current-root-uid
                                :embed-id         embed-id
                                :local-string     local-string}])
          (dispatch [:indent {:uid          (or navigation-uid uid)
                              :editing-uid  uid
                              :d-key-down   d-key-down
                              :local-string local-string}]))))))


(defn handle-escape
  "BUG: escape is fired 24 times for some reason."
  [e uid {:keys [esc-handler] :as _state-hooks}]
  (.. e preventDefault)
  (if (fn? esc-handler)
    (esc-handler e uid)
    (if @(rf/subscribe [::inline-search.subs/type uid])
      (rf/dispatch [::inline-search.events/close! uid])
      (dispatch [:editing/uid nil]))))


(def throttled-dispatch-sync
  (throttle #(dispatch-sync %) 50))


(defn handle-enter
  [e uid {:keys [enter-handler navigation-uid] :as state-hooks}]
  (let [{:keys [shift
                ctrl
                meta
                value
                start]
         :as   d-key-down} (destruct-key-down e)
        type               @(rf/subscribe [::inline-search.subs/type uid])]
    (.. e preventDefault)
    (cond
      type                      (case type
                                  :slash    (auto-complete-slash uid e)
                                  :page     (auto-complete-inline uid state-hooks e)
                                  :block    (auto-complete-inline uid state-hooks e)
                                  :hashtag  (auto-complete-hashtag uid state-hooks e)
                                  :template (auto-complete-template uid state-hooks e)
                                  :property (auto-complete-property uid state-hooks e))
      ;; shift-enter: add line break to textarea and move cursor to the next line.
      shift                     (replace-selection-with "\n")
      ;; cmd-enter: cycle todo states, then move cursor to the end of the line.
      ;; 13 is the length of the {{[[TODO]]}} and {{[[DONE]]}} string
      ;; this trick depends on the fact that they are of the same length.
      (shortcut-key? meta ctrl) (let [todo-prefix         "{{[[TODO]]}} "
                                      done-prefix         "{{[[DONE]]}} "
                                      no-prefix           ""
                                      first               (subs value 0 13)
                                      current-prefix      (cond (= first todo-prefix) todo-prefix
                                                                (= first done-prefix) done-prefix
                                                                :else                 no-prefix)
                                      new-prefix          (cond (= current-prefix no-prefix)   todo-prefix
                                                                (= current-prefix todo-prefix) done-prefix
                                                                (= current-prefix done-prefix) no-prefix)
                                      new-cursor-position (+ start (- (count current-prefix)) (count new-prefix))]
                                  (set-selection (.. e -target) 0 (count current-prefix))
                                  (replace-selection-with new-prefix)
                                  (set-cursor-position (.. e -target) new-cursor-position))
      ;; default: may mutate blocks, important action, no delay on 1st event, then throttled
      :else                     (if (fn? enter-handler)
                                  (enter-handler uid d-key-down)
                                  (throttled-dispatch-sync [:enter uid d-key-down navigation-uid])))))


;; Pair Chars: auto-balance for backspace and writing chars

(def PAIR-CHARS
  {"(" ")"
   "[" "]"
   "{" "}"
   "\"" "\""})


;; "`" "`"
;; "*" "*"
;; "_" "_"})


(defn surround
  "https://github.com/tpope/vim-surround"
  [selection around]
  (if-let [complement (get PAIR-CHARS around)]
    (str around selection complement)
    (str around selection around)))


(defn surround-and-set
  ;; Default to n=2 because it's more common.
  ([e surround-text]
   (surround-and-set e surround-text 2))
  ([e surround-text n]
   (let [{:keys [selection start end target]} (destruct-key-down e)
         selection?       (not= start end)]
     (.preventDefault e)
     (.stopPropagation e)
     (let [selection (surround selection surround-text)]

       (replace-selection-with selection)
       (if selection?
         (set-selection target (+ n start) (+ n end))
         (set-cursor-position target (+ start n)))))))


;; TODO: put text caret in correct position
(defn handle-shortcuts
  [e uid {:keys [save-fn] :as _state-hooks}]
  (let [{:keys [key-code head tail selection target value shift alt]} (destruct-key-down e)]
    (cond
      (and (= key-code KeyCodes.A) (= selection value)) (let [closest-node-page  (.. target (closest ".node-page"))
                                                              closest-block-page (.. target (closest ".block-page"))
                                                              closest            (or closest-node-page closest-block-page)
                                                              block              (db/get-block [:block/uid (.getAttribute closest "data-uid")])
                                                              children           (->> (:block/children block)
                                                                                      (sort-by :block/order)
                                                                                      (mapv :block/uid))]
                                                          (dispatch [::select-events/set-items children]))

      (= key-code KeyCodes.B) (surround-and-set e "**")

      (= key-code KeyCodes.I) (surround-and-set e "*" 1)

      (= key-code KeyCodes.Y) (surround-and-set e "~~")

      (= key-code KeyCodes.U) (surround-and-set e "--")

      (= key-code KeyCodes.H) (surround-and-set e "^^")

      ;; if alt is pressed, zoom out of current block page
      ;; if caret within [[brackets]] or #[[brackets]], navigate to that page
      ;; if caret on a #hashtag, navigate to that page
      ;; if caret within ((uid)), navigate to that uid
      ;; otherwise zoom into current block

      (= key-code KeyCodes.O) (let [[uid _]   (db/uid-and-embed-id uid)
                                    link      (str (replace-first head #"(?s)(.*)\[\[" "")
                                                   (replace-first tail #"(?s)\]\](.*)" ""))
                                    hashtag   (str (replace-first head #"(?s).*#" "")
                                                   (replace-first tail #"(?s)\s(.*)" ""))
                                    block-ref (str (replace-first head #"(?s)(.*)\(\(" "")
                                                   (replace-first tail #"(?s)\)\)(.*)" ""))]

                                (.. e preventDefault)

                                ;; save block before navigating away
                                (save-fn)

                                (cond
                                  alt
                                  (when-let [parent-uid (->> [:block/uid @(subscribe [:current-route/uid])]
                                                             (common-db/get-parent-eid @db/dsdb)
                                                             second)]
                                    (rf/dispatch [:reporting/navigation {:source :kbd-ctrl-alt-o
                                                                         :target :block
                                                                         :pane   (if shift
                                                                                   :right-pane
                                                                                   :main-pane)}])
                                    (router/navigate-uid parent-uid e))


                                  (and (re-find #"(?s)\[\[" head)
                                       (re-find #"(?s)\]\]" tail)
                                       (nil? (re-find #"(?s)\[" link))
                                       (nil? (re-find #"(?s)\]" link)))
                                  (let [eid (db/e-by-av :node/title link)]
                                    (if eid
                                      (do
                                        (rf/dispatch [:reporting/navigation {:source :kbd-ctrl-o
                                                                             :target :page
                                                                             :pane   (if shift
                                                                                       :right-pane
                                                                                       :main-pane)}])
                                        (router/navigate-page link e))
                                      (let [block-uid (common.utils/gen-block-uid)]
                                        (.blur target)
                                        (dispatch [:page/new {:title     link
                                                              :block-uid block-uid
                                                              :shift?    shift
                                                              :source    :kbd-ctrl-o}]))))

                                  ;; same logic as link
                                  (and (re-find #"(?s)#" head)
                                       (re-find #"(?s)\s" tail))
                                  (let [eid (db/e-by-av :node/title hashtag)]
                                    (if eid
                                      (do
                                        (rf/dispatch [:reporting/navigation {:source :kbd-ctrl-o
                                                                             :target :hashtag
                                                                             :pane   (if shift
                                                                                       :right-pane
                                                                                       :main-pane)}])
                                        (router/navigate-page hashtag e))
                                      (let [block-uid (common.utils/gen-block-uid)]
                                        (.blur target)
                                        (dispatch [:page/new {:title     link
                                                              :block-uid block-uid
                                                              :shift?    shift
                                                              :source    :kbd-ctrl-o}]))))

                                  (and (re-find #"(?s)\(\(" head)
                                       (re-find #"(?s)\)\)" tail)
                                       (nil? (re-find #"(?s)\(" block-ref))
                                       (nil? (re-find #"(?s)\)" block-ref))
                                       (db/e-by-av :block/uid block-ref))
                                  (do
                                    (rf/dispatch [:reporting/navigation {:source :kbd-ctrl-o
                                                                         :target :block
                                                                         :pane   (if shift
                                                                                   :right-pane
                                                                                   :main-pane)}])
                                    (router/navigate-uid block-ref e))

                                  :else (do
                                          (rf/dispatch [:reporting/navigation {:source :kbd-ctrl-o
                                                                               :target :block
                                                                               :pane   (if shift
                                                                                         :right-pane
                                                                                         :main-pane)}])
                                          (router/navigate-uid uid e)))))))


(defn pair-char?
  [e]
  (let [{:keys [key]} (destruct-key-down e)
        pair-char-set (-> PAIR-CHARS
                          seq
                          flatten
                          set)]
    (pair-char-set key)))


(defn handle-pair-char
  [e uid {:keys [read-value]}]
  (let [{:keys [key target start end selection value]} (destruct-key-down e)
        close-pair (get PAIR-CHARS key)
        lookbehind-char (nth value start nil)]
    (.. e preventDefault)

    (cond
      ;; when close char, increment caret index without writing more
      (some #(= % key lookbehind-char)
            [")" "}" "\"" "]"]) (do (set-cursor-position target (inc start))
                                    (rf/dispatch [::inline-search.events/close! uid]))

      (= selection "") (let [new-idx (inc start)]
                         (replace-selection-with (str key close-pair))
                         (set-cursor-position target new-idx)
                         (when (>= (count @read-value) 4)
                           (let [four-char        (subs @read-value (dec start) (+ start 3))
                                 double-brackets? (= "[[]]" four-char)
                                 double-parens?   (= "(())" four-char)
                                 type             (cond double-brackets? :page
                                                        double-parens? :block)]
                             (when type
                               (rf/dispatch [::inline-search.events/set-type! uid type])
                               ;; It's cleaner to explicitly set this to nil to avoid
                               ;; seemingly nondeterministic behavior caused by a
                               ;; previous value of :search/index
                               (rf/dispatch [::inline-search.events/set-index! uid nil])
                               (rf/dispatch [::inline-search.events/clear-results! uid])
                               (rf/dispatch [::inline-search.events/clear-query! uid])))))

      (not= selection "") (let [surround-selection (surround selection key)]
                            (replace-selection-with surround-selection)
                            (set-selection target (inc start) (inc end))
                            (let [four-char        (str (subs @read-value (dec start) (inc start))
                                                        (subs @read-value (+ end 1) (+ end 3)))
                                  double-brackets? (= "[[]]" four-char)
                                  double-parens?   (= "(())" four-char)
                                  type             (cond double-brackets? :page
                                                         double-parens? :block)
                                  query-fn         (cond double-brackets? db/search-in-node-title
                                                         double-parens? db/search-in-block-content)]
                              (when type
                                (rf/dispatch [::inline-search.events/set-type! uid type])
                                (rf/dispatch [::inline-search.events/set-index! uid 0])
                                (rf/dispatch [::inline-search.events/set-results! uid (query-fn selection)])
                                (rf/dispatch [::inline-search.events/set-query! uid selection])))))))


;; Backspace

(defn handle-backspace
  [e uid {:keys [backspace-handler] :as _state-hooks}]
  (let [{:keys [start value target end]} (destruct-key-down e)
        no-selection? (= start end)
        sub-str (subs value (dec start) (inc start))
        possible-pair (#{"[]" "{}" "()"} sub-str)
        head    (subs value 0 (dec start))
        type @(rf/subscribe [::inline-search.subs/type uid])
        look-behind-char (nth value (dec start) nil)]

    (cond
      (and (block-start? e) no-selection?) (if (fn? backspace-handler)
                                             (backspace-handler uid value)
                                             (dispatch [:backspace uid value]))
      ;; pair char: hide inline search and auto-balance
      possible-pair (do
                      (.. e preventDefault)
                      (rf/dispatch [::inline-search.events/close! uid])
                      (set-selection target (dec start) (inc start))
                      (replace-selection-with ""))

      ;; slash: close dropdown
      (and (= "/" look-behind-char) (= type :slash)) (rf/dispatch [::inline-search.events/close! uid])
      ;; hashtag: close dropdown
      (and (= "#" look-behind-char) (= type :hashtag)) (rf/dispatch [::inline-search.events/close! uid])
      ;; semicolon: close dropdown
      (and (= ";" look-behind-char) (= type :template)) (rf/dispatch [::inline-search.events/close! uid])
      ;; colon: close dropdown
      (and (= ":" look-behind-char) (= type :property)) (rf/dispatch [::inline-search.events/close! uid])
      ;; dropdown is open: update query
      type (update-query uid head "" type))))


;; Character: for queries

(defn is-character-key?
  "Closure returns true even when using modifier keys. We do not make that assumption."
  [e]
  (let [{:keys [meta ctrl alt key-code]} (destruct-key-down e)]
    (and (not meta) (not ctrl) (not alt)
         (isCharacterKey key-code))))


(defn write-char
  "When user types /, trigger slash menu.
  If user writes a character while there is a slash/type, update query and results."
  [e uid]
  (let [{:keys [head key value start]} (destruct-key-down e)
        type @(rf/subscribe [::inline-search.subs/type uid])
        look-behind-char (nth value (dec start) nil)]
    (cond
      (and (= key " ") (= type :hashtag)) (do
                                            (rf/dispatch [::inline-search.events/close! uid])
                                            (rf/dispatch [::inline-search.events/clear-results! uid]))
      (and (= key "/") (nil? type)) (do
                                      (rf/dispatch [::inline-search.events/set-type! uid :slash])
                                      (rf/dispatch [::inline-search.events/set-index! uid 0])
                                      (rf/dispatch [::inline-search.events/set-results! uid (slash-options)])
                                      (rf/dispatch [::inline-search.events/clear-query! uid]))
      (and (= key "#") (nil? type)) (do
                                      (rf/dispatch [::inline-search.events/set-type! uid :hashtag])
                                      (rf/dispatch [::inline-search.events/set-index! uid 0])
                                      (rf/dispatch [::inline-search.events/clear-results! uid])
                                      (rf/dispatch [::inline-search.events/clear-query! uid]))
      (and (= key ";" look-behind-char)
           (nil? type))             (do
                                      (rf/dispatch [::inline-search.events/set-type! uid :template])
                                      (rf/dispatch [::inline-search.events/set-index! uid 0])
                                      (rf/dispatch [::inline-search.events/clear-results! uid])
                                      (rf/dispatch [::inline-search.events/clear-query! uid]))
      (and @(rf/subscribe [:feature-flags/enabled? :properties])
           (= key ":" look-behind-char)
           (nil? type))             (do
                                      (rf/dispatch [::inline-search.events/set-type! uid :property])
                                      (rf/dispatch [::inline-search.events/set-index! uid 0])
                                      (rf/dispatch [::inline-search.events/clear-results! uid])
                                      (rf/dispatch [::inline-search.events/clear-query! uid]))

      type (update-query uid head key type))))


(defn handle-delete
  "Delete has the same behavior as pressing backspace on the next block."
  [e uid {:keys [read-value read-old-value delete-handler]}]
  (let [{:keys [start end value] :as d-key-down} (destruct-key-down e)]
    (if (fn? delete-handler)
      (delete-handler uid d-key-down)
      (let [no-selection?             (= start end)
            end?                      (= end (count value))
            ;; using original block uid(o-uid) data to get next block
            [o-uid embed-id]          (db/uid-and-embed-id uid)
            next-block-uid            (db/next-block-uid o-uid)]
        (when (and no-selection? end? next-block-uid)
          (let [next-block (db/get-block [:block/uid (-> next-block-uid db/uid-and-embed-id first)])]
            (dispatch [:backspace
                       (cond-> next-block-uid
                         embed-id (str "-embed-" embed-id))
                       (:block/string next-block)
                       (when-not (= @read-value @read-old-value)
                         @read-value)])))))))


(defn textarea-key-down
  [e uid {:as state-hooks} caret-position last-key-w-shift? last-event]
  ;; don't process key events from block that lost focus (quick Enter & Tab)
  (when @(subscribe [:editing/is-editing uid])
    (let [d-event (destruct-key-down e)
          {:keys [meta ctrl shift key-code]} d-event]

      (reset! last-event e)
      (reset! last-key-w-shift? shift)

      ;; update caret position for search dropdowns and for up/down
      (when (nil? @(rf/subscribe [::inline-search.subs/type uid]))
        (let [caret-pos (get-caret-position (.. e -target))]
          (reset! caret-position caret-pos)))

      ;; dispatch center
      ;; only when nothing is selected or duplicate/events dispatched
      ;; after some ops(like delete) can cause errors
      (when (empty? @(subscribe [::select-subs/items]))
        (cond
          (arrow-key-direction e)         (handle-arrow-key e uid state-hooks caret-position)
          (pair-char? e)                  (handle-pair-char e uid state-hooks)
          (= key-code KeyCodes.TAB)       (handle-tab e uid state-hooks)
          (= key-code KeyCodes.ENTER)     (handle-enter e uid state-hooks)
          (= key-code KeyCodes.BACKSPACE) (handle-backspace e uid state-hooks)
          (= key-code KeyCodes.DELETE)    (handle-delete e uid state-hooks)
          (= key-code KeyCodes.ESC)       (handle-escape e uid state-hooks)
          (shortcut-key? meta ctrl)       (handle-shortcuts e uid state-hooks)
          (is-character-key? e)           (write-char e uid))))))

