(ns athens.views.blocks.textarea-keydown
  (:require
    ["/components/Icons/Icons" :refer [TimeNowIcon PersonIcon CheckboxIcon CalendarNowIcon CalendarTomorrowIcon CalendarYesterdayIcon BlockEmbedIcon TemplateIcon HTMLEmbedIcon YoutubeIcon]]
    [athens.common-db :as common-db]
    [athens.common.utils :as common.utils]
    [athens.dates :as dates]
    [athens.db :as db]
    [athens.events.selection :as select-events]
    [athens.router :as router]
    [athens.subs.selection :as select-subs]
    [athens.util :as util :refer [scroll-if-needed get-caret-position shortcut-key? escape-str]]
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
     ["Template"      TemplateIcon ";;" nil nil]]
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


(defn update-query
  "Used by backspace and write-char.
  write-char appends key character. Pass empty string during backspace.
  query-start is determined by doing a greedy regex find up to head.
  Head goes up to the text caret position."
  [state head key type]
  (let [query-fn        (case type
                          :block db/search-in-block-content
                          :page db/search-in-node-title
                          :hashtag db/search-in-node-title
                          :template db/search-in-block-content
                          :slash filter-slash-options)
        regex           (case type
                          :block #"(?s).*\(\("
                          :page #"(?s).*\[\["
                          :hashtag #"(?s).*#"
                          :template #"(?s).*;;"
                          :slash #"(?s).*/")
        find            (re-find regex head)
        query-start-idx (count find)
        new-query       (str (subs head query-start-idx) key)
        results         (query-fn new-query)]
    (if (and (= type :slash) (empty? results))
      (swap! state assoc :search/type nil)
      (swap! state assoc
             :search/index 0
             :search/query new-query
             :search/results results))))


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
  ([state e]
   (let [target (.. e -target)
         {:search/keys [index results]} @state
         item (nth results index)]
     (println "ran auto-complete-slash")
     (auto-complete-slash state target item)))
  ;; here comes the autocompletion logic itself,
  ;; independent of the input method the user used.
  ;; `expansion` is the identifier of the page or block
  ;; (i.e., UID of block or title of page) that shall be
  ;; inserted.
  ([state target item]
   (let [{:keys [start head]} (destruct-target target)
         [caption _ expansion _ pos] item
         expand    (if (fn? expansion) (expansion) expansion)
         ;; the regex is evaluated greedily, yielding the last
         ;; occurrence in head (head = text up to cursor)
         start-idx (dec (count (re-find #"(?s).*/" head)))]
     (swap! state assoc
            :search/type nil)

     (println "ran auto-complete-slash")
     (set-selection target start-idx start)
     (replace-selection-with expand)
     (when pos
       (let [new-idx (+ start-idx (count expand) (- pos))]
         (set-cursor-position target new-idx)
         (when (= caption "Block Embed")
           (swap! state assoc
                  :search/type :block
                  :search/query ""
                  :search/results []))))
     (when (= caption "Template")
       (swap! state assoc
              :search/type :template
              :search/query ""
              :search/results [])))))


;; see `auto-complete-slash` for how this arity-overloaded
;; function is used.
(defn auto-complete-hashtag
  ([state e]
   (let [{:search/keys [index results]} @state
         target (.. e -target)
         {:keys [node/title block/uid]} (nth results index nil)
         expansion (or title uid)]
     (auto-complete-hashtag state target expansion)))

  ([state target expansion]
   (let [{:keys [start head]} (destruct-target target)
         start-idx (count (re-find #"(?s).*#" head))]
     (if (nil? expansion)
       (swap! state assoc :search/type nil)
       (do
         (set-selection target start-idx start)
         (replace-selection-with (str "[[" expansion "]]"))
         (swap! state assoc
                :search/type nil))))))


;; see `auto-complete-slash` for how this arity-overloaded
;; function is used.
(defn auto-complete-inline
  ([state e]
   (let [{:search/keys [index results]} @state
         ;; (nth results (or index 0) nil) returns the index-th result
         ;; If (= index nil) or index is out of bounds, returns nil
         ;; For example, index can be nil if (= results [])
         {:keys [node/title block/uid]} (nth results (or index 0) nil)
         target (.. e -target)
         expansion    (or title uid)]
     (auto-complete-inline state target expansion)))

  ([state target expansion]
   (let [{:search/keys [query]} @state
         {:keys [end]} (destruct-target target)
         query        (escape-str query)]

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
     (swap! state assoc :search/type nil))))


;; see `auto-complete-slash` for how this arity-overloaded
;; function is used.
(defn auto-complete-template
  ([state e]
   (let [{:search/keys [index results]} @state
         target (.. e -target)
         {:keys [block/uid]} (nth results index nil)
         expansion uid]
     (auto-complete-template state target expansion)))

  ([state target expansion]
   (let [{:keys [start head]} (destruct-target target)
         start-idx (count (re-find #"(?s).*;;" head))
         source-ir (->> [:block/uid expansion]
                        (common-db/get-internal-representation @db/dsdb)
                        :block/children)
         target-ir (->> source-ir
                        internal-representation/new-uids-map
                        (internal-representation/update-uids source-ir)
                        (into []))
         uid (:block/uid @state)]
     (if (or (nil? expansion)
             (nil? target-ir))
       (swap! state assoc :search/type nil)
       (do
         (set-selection target (- start-idx 2) start)
         (replace-selection-with "")
         (dispatch [:paste-internal uid (:string/local @state) target-ir])
         (swap! state assoc :search/type nil))))))


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
  [e uid state]
  (let [{:keys [key-code
                shift
                ctrl
                target
                selection]}              (destruct-key-down e)
        selection?                       (not (blank? selection))
        start?                           (block-start? e)
        end?                             (block-end? e)
        {:search/keys   [results
                         type
                         index]
         caret-position :caret-position} @state
        textarea-height                  (.. target -offsetHeight) ; this height is accurate, but caret-position height is not updating
        {:keys [top height]}             caret-position
        rows                             (js/Math.round (/ textarea-height height))
        row                              (js/Math.ceil (/ top height))
        top-row?                         (= row 1)
        bottom-row?                      (= row rows)
        up?                              (= key-code KeyCodes.UP)
        down?                            (= key-code KeyCodes.DOWN)
        left?                            (= key-code KeyCodes.LEFT)
        right?                           (= key-code KeyCodes.RIGHT)
        [char-offset _]                  (get-end-points target)]

    (cond
      ;; Shift: select block if leaving block content boundaries (top or bottom rows). Otherwise select textarea text (default)
      shift (cond
              left?                        nil
              right?                       nil
              (or (and up? top-row?)
                  (and down? bottom-row?)) (do
                                             (.. target blur)
                                             (dispatch [::select-events/add-item uid (cond
                                                                                       up? :first
                                                                                       down? :last)])))

      ;; Control: fold or unfold blocks
      ctrl (cond
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
             (or left? right?) (swap! state assoc :search/index 0 :search/type nil)
             (or up? down?)    (let [cur-index    index
                                     min-index    0
                                     max-index    (max-idx results)
                                     next-index   (cycle-list min-index max-index cur-index up? down?)
                                     container-el (getElement "dropdown-menu")
                                     target-el    (getElement (str "dropdown-item-" next-index))]
                                 (.. e preventDefault)
                                 (swap! state assoc :search/index next-index)
                                 (scroll-if-needed target-el container-el)))

      selection? nil

      ;; Else: navigate across blocks
      ;; FIX: always navigates up or down for header because get-caret-position for some reason returns the wrong value for top

      ;; going LEFT at **0th index** should always go to **last index** of block **above**
      ;; last index is special - always go to last index when going up or down


      (or (and left? start?)
          (and up? end?))         (do (.. e preventDefault)
                                      (dispatch [:up uid :end]))

      (and down? end?)            (do (.. e preventDefault)
                                      (dispatch [:down uid :end]))

      ;; going RIGHT at last index should always go to index 0 of block below
      (and right? end?)           (do (.. e preventDefault)
                                      (dispatch [:down uid 0]))

      ;; index 0 is special - always go to index 0 when going up or down
      ;; when caret is anywhere between start and end preserve the position and offset by char
      (and up? top-row?)          (do (.. e preventDefault)
                                      (dispatch [:up uid char-offset]))
      (and down? bottom-row?)     (do (.. e preventDefault)
                                      (dispatch [:down uid char-offset])))))


;; Tab

(defn handle-tab
  "Bug: indenting sets the cursor position to 0, likely because a new textarea element is created on the DOM. Set selection appropriately.
  See :indent event for why value must be passed as well."
  [e _uid state]
  (.. e preventDefault)
  (let [{:keys [shift] :as d-key-down} (destruct-key-down e)
        selected-items                 @(subscribe [::select-subs/items])
        editing-uid                    @(subscribe [:editing/uid])
        current-root-uid               @(subscribe [:current-route/uid])
        [editing-uid embed-id]         (db/uid-and-embed-id editing-uid)
        local-string                   (:string/local @state)]
    (when (empty? selected-items)
      (if shift
        (dispatch [:unindent {:uid              editing-uid
                              :d-key-down       d-key-down
                              :context-root-uid current-root-uid
                              :embed-id         embed-id
                              :local-string     local-string}])
        (dispatch [:indent
                   {:uid           editing-uid
                    :d-key-down    d-key-down
                    :local-string  local-string}])))))


(defn handle-escape
  "BUG: escape is fired 24 times for some reason."
  [e state]
  (.. e preventDefault)
  (if (:search/type @state)
    (swap! state assoc :search/type nil)
    (dispatch [:editing/uid nil])))


(def throttled-dispatch-sync
  (throttle #(dispatch-sync %) 50))


(defn handle-enter
  [e uid state]
  (let [{:keys [shift ctrl meta value start] :as d-key-down} (destruct-key-down e)
        {:search/keys [type]} @state]
    (.. e preventDefault)
    (cond
      type (case type
             :slash (auto-complete-slash state e)
             :page (auto-complete-inline state e)
             :block (auto-complete-inline state e)
             :hashtag (auto-complete-hashtag state e)
             :template (auto-complete-template state e))
      ;; shift-enter: add line break to textarea and move cursor to the next line.
      shift (replace-selection-with "\n")
      ;; cmd-enter: cycle todo states, then move cursor to the end of the line.
      ;; 13 is the length of the {{[[TODO]]}} and {{[[DONE]]}} string
      ;; this trick depends on the fact that they are of the same length.
      (shortcut-key? meta ctrl) (let [todo-prefix         "{{[[TODO]]}} "
                                      done-prefix         "{{[[DONE]]}} "
                                      no-prefix           ""
                                      first               (subs value 0 13)
                                      current-prefix      (cond (= first todo-prefix) todo-prefix
                                                                (= first done-prefix) done-prefix
                                                                :else no-prefix)
                                      new-prefix          (cond (= current-prefix no-prefix) todo-prefix
                                                                (= current-prefix todo-prefix) done-prefix
                                                                (= current-prefix done-prefix) no-prefix)
                                      new-cursor-position (+ start (- (count current-prefix)) (count new-prefix))]
                                  (set-selection (.. e -target) 0 (count current-prefix))
                                  (replace-selection-with new-prefix)
                                  (set-cursor-position (.. e -target) new-cursor-position))
      ;; default: may mutate blocks, important action, no delay on 1st event, then throttled
      :else (throttled-dispatch-sync [:enter uid d-key-down]))))


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
  ([e state surround-text]
   (surround-and-set e state surround-text 2))
  ([e _ surround-text n]
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
  [e uid state]
  (let [{:keys [key-code head tail selection target value shift]} (destruct-key-down e)]
    (cond
      (and (= key-code KeyCodes.A) (= selection value)) (let [closest-node-page  (.. target (closest ".node-page"))
                                                              closest-block-page (.. target (closest ".block-page"))
                                                              closest            (or closest-node-page closest-block-page)
                                                              block              (db/get-block [:block/uid (.getAttribute closest "data-uid")])
                                                              children           (->> (:block/children block)
                                                                                      (sort-by :block/order)
                                                                                      (mapv :block/uid))]
                                                          (dispatch [::select-events/set-items children]))

      (= key-code KeyCodes.B) (surround-and-set e state "**")

      (= key-code KeyCodes.I) (surround-and-set e state "*" 1)

      (= key-code KeyCodes.Y) (surround-and-set e state "~~")

      (= key-code KeyCodes.U) (surround-and-set e state "--")

      (= key-code KeyCodes.H) (surround-and-set e state "^^")

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

                                ;; save block before navigating away
                                ((:string/save-fn @state))

                                (cond
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
  [e _ state]
  (let [{:keys [key target start end selection value]} (destruct-key-down e)
        close-pair (get PAIR-CHARS key)
        lookbehind-char (nth value start nil)]
    (.. e preventDefault)

    (cond
      ;; when close char, increment caret index without writing more
      (some #(= % key lookbehind-char)
            [")" "}" "\"" "]"]) (do (set-cursor-position target (inc start))
                                    (swap! state assoc :search/type nil))

      (= selection "") (let [new-idx (inc start)]
                         (replace-selection-with (str key close-pair))
                         (set-cursor-position target new-idx)
                         (when (>= (count (:string/local @state)) 4)
                           (let [four-char        (subs (:string/local @state) (dec start) (+ start 3))
                                 double-brackets? (= "[[]]" four-char)
                                 double-parens?   (= "(())" four-char)
                                 type             (cond double-brackets? :page
                                                        double-parens? :block)]
                             (when type
                               (swap! state assoc
                                      :search/type type
                                      :search/query ""
                                      :search/results []
                                      ;; It's cleaner to explicitly set this to nil to avoid
                                      ;; seemingly nondeterministic behavior caused by a
                                      ;; previous value of :search/index
                                      :search/index nil)))))

      (not= selection "") (let [surround-selection (surround selection key)]
                            (replace-selection-with surround-selection)
                            (set-selection target (inc start) (inc end))
                            (let [four-char        (str (subs (:string/local @state) (dec start) (inc start))
                                                        (subs (:string/local @state) (+ end 1) (+ end 3)))
                                  double-brackets? (= "[[]]" four-char)
                                  double-parens?   (= "(())" four-char)
                                  type             (cond double-brackets? :page
                                                         double-parens? :block)
                                  query-fn         (cond double-brackets? db/search-in-node-title
                                                         double-parens? db/search-in-block-content)]
                              (when type
                                (swap! state assoc
                                       :search/type type
                                       :search/query selection
                                       :search/results (query-fn selection)
                                       :search/index 0)))))))


;; Backspace

(defn handle-backspace
  [e uid state]
  (let [{:keys [start value target end]} (destruct-key-down e)
        no-selection? (= start end)
        sub-str (subs value (dec start) (inc start))
        possible-pair (#{"[]" "{}" "()"} sub-str)
        head    (subs value 0 (dec start))
        {:search/keys [type]} @state
        look-behind-char (nth value (dec start) nil)]

    (cond
      (and (block-start? e) no-selection?) (dispatch [:backspace uid value])
      ;; pair char: hide inline search and auto-balance
      possible-pair (do
                      (.. e preventDefault)
                      (swap! state assoc :search/type nil)
                      (set-selection target (dec start) (inc start))
                      (replace-selection-with ""))

      ;; slash: close dropdown
      (and (= "/" look-behind-char) (= type :slash)) (swap! state assoc :search/type nil)
      ;; hashtag: close dropdown
      (and (= "#" look-behind-char) (= type :hashtag)) (swap! state assoc :search/type nil)
      ;; semicolon: close dropdown
      (and (= ";" look-behind-char) (= type :template)) (swap! state assoc :search/type nil)
      ;; dropdown is open: update query
      type (update-query state head "" type))))


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
  [e _uid state]
  (let [{:keys [head key value start]} (destruct-key-down e)
        {:search/keys [type]} @state
        look-behind-char (nth value (dec start) nil)]
    (cond
      (and (= key " ") (= type :hashtag)) (swap! state assoc
                                                 :search/type nil
                                                 :search/results [])
      (and (= key "/") (nil? type)) (swap! state assoc
                                           :search/index 0
                                           :search/query ""
                                           :search/type :slash
                                           :search/results (slash-options))
      (and (= key "#") (nil? type)) (swap! state assoc
                                           :search/index 0
                                           :search/query ""
                                           :search/type :hashtag
                                           :search/results [])
      (and (= key ";" look-behind-char)
           (nil? type))             (swap! state assoc
                                           :search/index 0
                                           :search/query ""
                                           :search/type :template
                                           :search/results [])
      type (update-query state head key type))))


(defn handle-delete
  "Delete has the same behavior as pressing backspace on the next block."
  [e uid state]
  (let [{:keys [start end value]} (destruct-key-down e)
        no-selection?             (= start end)
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
                   (when-not (= (:string/local @state)
                                (:block/string @state))
                     (:string/local @state))])))))


(defn textarea-key-down
  [e uid state]
  ;; don't process key events from block that lost focus (quick Enter & Tab)
  (when @(subscribe [:editing/is-editing uid])
    (let [d-event (destruct-key-down e)
          {:keys [meta ctrl key-code]} d-event]

      ;; used for paste, to determine if shift key was held down
      (swap! state assoc :last-keydown d-event)
      (swap! state assoc :last-e e)

      ;; update caret position for search dropdowns and for up/down
      (when (nil? (:search/type @state))
        (let [caret-position (get-caret-position (.. e -target))]
          (swap! state assoc :caret-position caret-position)))

      ;; dispatch center
      ;; only when nothing is selected or duplicate/events dispatched
      ;; after some ops(like delete) can cause errors
      (when (empty? @(subscribe [::select-subs/items]))
        (cond
          (arrow-key-direction e)         (handle-arrow-key e uid state)
          (pair-char? e)                  (handle-pair-char e uid state)
          (= key-code KeyCodes.TAB)       (handle-tab e uid state)
          (= key-code KeyCodes.ENTER)     (handle-enter e uid state)
          (= key-code KeyCodes.BACKSPACE) (handle-backspace e uid state)
          (= key-code KeyCodes.DELETE)    (handle-delete e uid state)
          (= key-code KeyCodes.ESC)       (handle-escape e state)
          (shortcut-key? meta ctrl)       (handle-shortcuts e uid state)
          (is-character-key? e)           (write-char e uid state))))))

