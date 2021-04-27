(ns athens.views.blocks.textarea-keydown
  (:require
    ["@material-ui/icons/DesktopWindows" :default DesktopWindows]
    ["@material-ui/icons/Done" :default Done]
    ["@material-ui/icons/Timer" :default Timer]
    ["@material-ui/icons/Today" :default Today]
    ["@material-ui/icons/ViewDayRounded" :default ViewDayRounded]
    ["@material-ui/icons/YouTube" :default YouTube]
    [athens.db :as db]
    [athens.router :as router]
    [athens.util :refer [scroll-if-needed get-day get-caret-position shortcut-key? escape-str]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :refer [replace-first blank?]]
    [goog.dom :refer [getElement]]
    [goog.dom.selection :refer [setStart setEnd getText setCursorPosition getEndPoints]]
    [goog.events.KeyCodes :refer [isCharacterKey]]
    [goog.functions :refer [throttle #_debounce]]
    [re-frame.core :refer [dispatch dispatch-sync subscribe]])
  (:import
    (goog.events
      KeyCodes)))


;;; Event Helpers


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


;;; Dropdown: inline-search and slash commands

;; TODO: some expansions require caret placement after
(def slash-options
  [["Add Todo"      Done "{{[[TODO]]}} " "cmd-enter" nil]
   ["Current Time"  Timer (fn [] (.. (js/Date.) (toLocaleTimeString [] (clj->js {"timeStyle" "short"})))) nil nil]
   ["Today"         Today (fn [] (str "[[" (:title (get-day 0)) "]] ")) nil nil]
   ["Tomorrow"      Today (fn [] (str "[[" (:title (get-day -1)) "]]")) nil nil]
   ["Yesterday"     Today (fn [] (str "[[" (:title (get-day 1)) "]]")) nil nil]
   ["YouTube Embed" YouTube "{{[[youtube]]: }}" nil 2]
   ["iframe Embed"  DesktopWindows "{{iframe: }}" nil 2]
   ["Block Embed"   ViewDayRounded "{{[[embed]]: (())}}" nil 4]])

;;[ "Block Embed" #(str "[[" (:title (get-day 1)) "]]")]
;;[DateRange "Date Picker"]
;;[Attachment "Upload Image or File"]
;;[ExposurePlus1 "Word Count"]


(defn filter-slash-options
  [query]
  (if (blank? query)
    slash-options
    (filterv (fn [[text]]
               (re-find (re-pattern (str "(?i)" query)) text))
             slash-options)))


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
                          :slash filter-slash-options)
        regex           (case type
                          :block #"(?s).*\(\("
                          :page #"(?s).*\[\["
                          :hashtag #"(?s).*#"
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


;; 1- if no results, just hide slash commands so this doesnt get triggered
;; 2- if results, do find and replace properly
(defn auto-complete-slash
  ([state e]
   (let [{:search/keys [index results]} @state
         {:keys [value head tail target]} (destruct-key-down e)
         [n _ expansion _ pos] (nth results index)
         expand    (if (fn? expansion) (expansion) expansion)
         start-idx (dec (count (re-find #"(?s).*/" head)))
         new-head  (subs value 0 start-idx)
         new-str   (str new-head expand tail)]
     (swap! state assoc
            :search/type nil
            :string/local new-str)
     (set! (.-value target) new-str)
     (when pos
       (let [new-idx (- (count (str new-head expand)) pos)]
         (set-cursor-position target new-idx)
         (when (= n "Block Embed")
           (swap! state assoc :search/type :block
                  :search/query "" :search/results []))))))
  ([state target item]
   (let [{:keys [value head tail]} (destruct-target target)
         [_ _ expansion _ pos] item
         expand    (if (fn? expansion) (expansion) expansion)
         start-idx (dec (count (re-find #"(?s).*/" head)))
         new-head  (subs value 0 start-idx)
         new-str   (str new-head expand tail)]
     (swap! state assoc
            :search/type nil
            :string/local new-str)
     (set! (.-value target) new-str)
     (when pos
       (let [new-idx (- (count (str new-head expand)) pos)]
         (set-cursor-position target new-idx))))))


(defn auto-complete-hashtag
  ([state e]
   (let [{:search/keys [index results]} @state
         {:keys [node/title block/uid]} (nth results index nil)
         {:keys [value head tail]} (destruct-key-down e)
         expansion (or title uid)
         start-idx (count (re-find #"(?s).*#" head))
         new-head  (subs value 0 start-idx)
         new-str   (str new-head "[[" expansion "]]" tail)]
     (if (nil? expansion)
       (swap! state assoc :search/type nil)
       (swap! state assoc
              :search/type nil
              :string/local new-str))))
  ([state target expansion]
   (let [{:keys [value head tail]} (destruct-target target)
         start-idx (count (re-find #"(?s).*#" head))
         new-head  (subs value 0 start-idx)
         new-str   (str new-head "[[" expansion "]]" tail)]
     (if (nil? expansion)
       (swap! state assoc :search/type nil)
       (swap! state assoc
              :search/type nil
              :string/local new-str)))))


(defn auto-complete-inline
  ([state e]
   (let [{:search/keys [query type index results]} @state
         {:keys [node/title block/uid]} (nth results index nil)
         {:keys [start head tail target]} (destruct-key-down e)
         expansion    (or title uid)
         block?       (= type :block)
         page?        (= type :page)
         query        (escape-str query)
         ;; rewrite this more cleanly
         head-pattern (cond block? (re-pattern (str "(?s)(.*)\\(\\(" query))
                            page? (re-pattern (str "(?s)(.*)\\[\\[" query)))
         tail-pattern (cond block? #"(?s)(\)\))?(.*)"
                            page? #"(?s)(\]\])?(.*)")
         new-head     (cond block? "$1(("
                            page? "$1[[")
         closing-str  (cond block? "))"
                            page? "]]")
         replacement  (str new-head expansion closing-str)
         replace-str  (replace-first head head-pattern replacement)
         matches      (re-matches tail-pattern tail)
         [_ _ after-closing-str] matches
         new-str      (str replace-str after-closing-str)]
     (if (nil? expansion)
       (swap! state assoc :search/type nil)
       (swap! state assoc :search/type nil :string/local new-str))
     (setStart target (+ 2 start))))
  ([state target expansion]
   (let [{:search/keys [query type]} @state
         {:keys [start head tail]} (destruct-target target)
         block?       (= type :block)
         page?        (= type :page)
         query        (escape-str query)
         ;; rewrite this more cleanly
         head-pattern (cond block? (re-pattern (str "(?s)(.*)\\(\\(" query))
                            page? (re-pattern (str "(?s)(.*)\\[\\[" query)))
         tail-pattern (cond block? #"(?s)(\)\))?(.*)"
                            page? #"(?s)(\]\])?(.*)")
         new-head     (cond block? "$1(("
                            page? "$1[[")
         closing-str  (cond block? "))"
                            page? "]]")
         replacement  (str new-head expansion closing-str)
         replace-str  (replace-first head head-pattern replacement)
         matches      (re-matches tail-pattern tail)
         [_ _ after-closing-str] matches
         new-str      (str replace-str after-closing-str)]
     (if (nil? expansion)
       (swap! state assoc :search/type nil)
       (swap! state assoc :search/type nil :string/local new-str))
     (setStart target (+ 2 start)))))


;;; Arrow Keys


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
  (let [{:keys [key-code shift ctrl target selection]} (destruct-key-down e)
        selection?      (not (blank? selection))
        start?          (block-start? e)
        end?            (block-end? e)
        {:search/keys [results type index] caret-position :caret-position} @state
        textarea-height (.. target -offsetHeight) ;; this height is accurate, but caret-position height is not updating
        {:keys [top height]} caret-position
        rows            (js/Math.round (/ textarea-height height))
        row             (js/Math.ceil (/ top height))
        top-row?        (= row 1)
        bottom-row?     (= row rows)
        up?             (= key-code KeyCodes.UP)
        down?           (= key-code KeyCodes.DOWN)
        left?           (= key-code KeyCodes.LEFT)
        right?          (= key-code KeyCodes.RIGHT)
        header          (db/v-by-ea (db/e-by-av :block/uid uid) :block/header)]

    (cond
      ;; Shift: select block if leaving block content boundaries (top or bottom rows). Otherwise select textarea text (default)
      shift (cond
              left? nil
              right? nil
              (or (and up? top-row?)
                  (and down? bottom-row?)) (do
                                             (.. target blur)
                                             (dispatch [:selected/add-item uid])))

      ;; Control: fold or unfold blocks
      ctrl (cond
             left? nil
             right? nil
             (or up? down?) (let [[uid _]        (db/uid-and-embed-id uid)
                                  new-open-state (cond
                                                   up? false
                                                   down? true)
                                  event [:transact [[:db/add [:block/uid uid] :block/open new-open-state]]]]
                              (.. e preventDefault)
                              (dispatch event)))

      ;; Type, one of #{:slash :block :page}: If slash commands or inline search is open, cycle through options
      type (cond
             (or left? right?) (swap! state assoc :search/index 0 :search/type nil)
             (or up? down?) (let [cur-index    index
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
      (or (and up? top-row?)
          (and left? start?)
          (and up? header)) (do (.. e preventDefault)
                                (dispatch [:up uid]))
      (or (and down? bottom-row?)
          (and right? end?)
          (and down? header)) (do (.. e preventDefault)
                                  (dispatch [:down uid])))))


;;; Tab

(defn handle-tab
  "Bug: indenting sets the cursor position to 0, likely because a new textarea element is created on the DOM. Set selection appropriately.
  See :indent event for why value must be passed as well."
  [e _uid _state]
  (.. e preventDefault)
  (let [{:keys [shift] :as d-key-down} (destruct-key-down e)
        selected-items                 @(subscribe [:selected/items])
        editing-uid                    @(subscribe [:editing/uid])]
    (when (empty? selected-items)
      (if shift
        (dispatch [:unindent editing-uid d-key-down])
        (dispatch [:indent editing-uid d-key-down])))))


(defn handle-escape
  "BUG: escape is fired 24 times for some reason."
  [e state]
  (.. e preventDefault)
  (swap! state assoc :search/type nil)
  (dispatch [:editing/uid nil]))


(def throttled-dispatch-sync
  (throttle #(dispatch-sync %) 50))


(defn handle-enter
  [e uid state]
  (let [{:keys [shift ctrl meta head tail value] :as d-key-down} (destruct-key-down e)
        {:search/keys [type]} @state]
    (.. e preventDefault)
    (cond
      type (case type
             :slash (auto-complete-slash state e)
             :page (auto-complete-inline state e)
             :block (auto-complete-inline state e)
             :hashtag (auto-complete-hashtag state e))
      ;; shift-enter: add line break to textarea
      shift (swap! state assoc :string/local (str head "\n" tail))
      ;; cmd-enter: cycle todo states. 13 is the length of the {{[[TODO]]}} string
      (shortcut-key? meta ctrl) (let [first    (subs value 0 13)
                                      new-tail (subs value 13)
                                      new-str  (cond (= first "{{[[TODO]]}} ") (str "{{[[DONE]]}} " new-tail)
                                                     (= first "{{[[DONE]]}} ") new-tail
                                                     :else (str "{{[[TODO]]}} " value))]
                                  (swap! state assoc :string/local new-str))
      ;; default: may mutate blocks, important action, no delay on 1st event, then throttled
      :else (throttled-dispatch-sync [:enter uid d-key-down]))))


;;; Pair Chars: auto-balance for backspace and writing chars

(def PAIR-CHARS
  {"(" ")"
   "[" "]"
   "{" "}"
   "\"" "\""})
  ;;"`" "`"
  ;;"*" "*"
   ;;"_" "_"})


(defn surround
  "https://github.com/tpope/vim-surround"
  [selection around]
  (if-let [complement (get PAIR-CHARS around)]
    (str around selection complement)
    (str around selection around)))


;; TODO: put text caret in correct position
(defn handle-shortcuts
  [e uid state]
  (let [{:keys [key-code head tail selection start end target value]} (destruct-key-down e)
        selection?       (not= start end)

        surround-and-set (fn [surround-text]
                           (.preventDefault e)
                           (.stopPropagation e)
                           (let [selection (surround selection surround-text)
                                 new-str   (str head selection tail)]
                             ;; https://developer.mozilla.org/en-US/docs/Web/API/Document/execCommand
                             ;; textarea setval will lose ability to undo/redo

                             ;; other note: execCommand is probably the simpler way
                             ;; at least until a new standard comes around

                             ;; be wary before updating electron - as chromium might drop support for execCommand
                             ;; electron 11 - uses chromium < 90(latest) which supports execCommand
                             (swap! state assoc :string/local new-str)
                             (.. js/document (execCommand "insertText" false selection))
                             (if selection?
                               (do (setStart target (+ 2 start))
                                   (setEnd target (+ 2 end)))
                               (set-cursor-position target (+ start 2)))))]

    (cond
      (and (= key-code KeyCodes.A) (= selection value)) (let [closest-node-page  (.. target (closest ".node-page"))
                                                              closest-block-page (.. target (closest ".block-page"))
                                                              closest            (or closest-node-page closest-block-page)
                                                              block              (db/get-block [:block/uid (.. closest -dataset -uid)])
                                                              children           (->> (:block/children block)
                                                                                      (sort-by :block/order)
                                                                                      (mapv :block/uid))]
                                                          (dispatch [:selected/add-items children]))
      ;; When undo no longer makes changes for local textarea, do datascript undo.
      (= key-code KeyCodes.Z) (let [{:string/keys [local previous]} @state]
                                (when (= local previous)
                                  (dispatch [:undo])))

      (= key-code KeyCodes.B) (surround-and-set "**")

      (= key-code KeyCodes.I) (surround-and-set "*")

      (= key-code KeyCodes.Y) (surround-and-set "~~")

      (= key-code KeyCodes.U) (surround-and-set "--")

      (= key-code KeyCodes.H) (surround-and-set "^^")

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
                                (db/transact-state-for-uid uid state)

                                (cond
                                  (and (re-find #"(?s)\[\[" head)
                                       (re-find #"(?s)\]\]" tail)
                                       (nil? (re-find #"(?s)\[" link))
                                       (nil? (re-find #"(?s)\]" link)))
                                  (let [eid (db/e-by-av :node/title link)
                                        uid (db/v-by-ea eid :block/uid)]
                                    (if eid
                                      (router/navigate-uid uid e)
                                      (let [new-uid (athens.util/gen-block-uid)]
                                        (.blur target)
                                        (dispatch [:page/create link new-uid])
                                        (js/setTimeout #(router/navigate-uid new-uid e) 50))))

                                  ;; same logic as link
                                  (and (re-find #"(?s)#" head)
                                       (re-find #"(?s)\s" tail))
                                  (let [eid (db/e-by-av :node/title hashtag)
                                        uid (db/v-by-ea eid :block/uid)]
                                    (if eid
                                      (router/navigate-uid uid e)
                                      (let [new-uid (athens.util/gen-block-uid)]
                                        (.blur target)
                                        (dispatch [:page/create link new-uid])
                                        (js/setTimeout #(router/navigate-uid new-uid e) 50))))

                                  (and (re-find #"(?s)\(\(" head)
                                       (re-find #"(?s)\)\)" tail)
                                       (nil? (re-find #"(?s)\(" block-ref))
                                       (nil? (re-find #"(?s)\)" block-ref))
                                       (db/e-by-av :block/uid block-ref))
                                  (router/navigate-uid block-ref e)

                                  :else (router/navigate-uid uid e))))))


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
  (let [{:keys [key head tail target start end selection value]} (destruct-key-down e)
        close-pair (get PAIR-CHARS key)
        lookbehind-char (nth value start nil)]
    (.. e preventDefault)

    (cond
      ;; when close char, increment caret index without writing more
      (or (= ")" key lookbehind-char)
          (= "}" key lookbehind-char)
          (= "\"" key lookbehind-char)
          (= "]" key lookbehind-char)) (do (setStart target (inc start))
                                           (swap! state assoc :search/type nil))

      (= selection "") (let [new-str (str head key close-pair tail)
                             new-idx (inc start)]
                         (swap! state assoc :string/local new-str)
                         (set! (.-value target) new-str)
                         (set-cursor-position target new-idx)
                         (when (>= (count (:string/local @state)) 4)
                           (let [four-char        (subs (:string/local @state) (dec start) (+ start 3))
                                 double-brackets? (= "[[]]" four-char)
                                 double-parens?   (= "(())" four-char)
                                 type             (cond double-brackets? :page
                                                        double-parens? :block)]
                             (when type
                               (swap! state assoc :search/type type :search/query "" :search/results [])))))

      (not= selection "") (let [surround-selection (surround selection key)
                                new-str            (str head surround-selection tail)]
                            (swap! state assoc :string/local new-str)
                            (set! (.-value target) new-str)
                            (set! (.-selectionStart target) (inc start))
                            (set! (.-selectionEnd target) (inc end))
                            (let [four-char        (str (subs (:string/local @state) (dec start) (inc start))
                                                        (subs (:string/local @state) (+ end 1) (+ end 3)))
                                  double-brackets? (= "[[]]" four-char)
                                  double-parens?   (= "(())" four-char)
                                  type             (cond double-brackets? :page
                                                         double-parens? :block)
                                  query-fn         (cond double-brackets? db/search-in-node-title
                                                         double-parens? db/search-in-block-content)]
                              (when type
                                (swap! state assoc :search/type type :search/query selection :search/results (query-fn selection))))))))


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
      possible-pair (let [head    (subs value 0 (dec start))
                          tail    (subs value (inc start))
                          new-str (str head tail)
                          new-idx (dec start)]
                      (.. e preventDefault)
                      (swap! state assoc
                             :search/type nil
                             :string/local new-str)
                      (set! (.-value target) new-str)
                      (set-cursor-position target new-idx))

      ;; slash: close dropdown
      (and (= "/" look-behind-char) (= type :slash)) (swap! state assoc :search/type nil)
      ;; hashtag: close dropdown
      (and (= "#" look-behind-char) (= type :hashtag)) (swap! state assoc :search/type nil)
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
  (let [{:keys [head key]} (destruct-key-down e)
        {:search/keys [type]} @state]
    (cond
      (and (= key " ") (= type :hashtag)) (swap! state assoc
                                                 :search/type nil
                                                 :search/results [])
      (and (= key "/") (nil? type)) (swap! state assoc
                                           :search/index 0
                                           :search/query ""
                                           :search/type :slash
                                           :search/results slash-options)
      (and (= key "#") (nil? type)) (swap! state assoc
                                           :search/index 0
                                           :search/query ""
                                           :search/type :hashtag
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
        (dispatch [:backspace (cond-> next-block-uid
                                embed-id (str "-embed-" embed-id))
                   (str (:block/string state) (:block/string next-block))])))))


(defn textarea-key-down
  [e uid state]
  ;; don't process key events from block that lost focus (quick Enter & Tab)
  (when (= uid @(subscribe [:editing/uid]))
    (let [d-event (destruct-key-down e)
          {:keys [meta ctrl key-code]} d-event]

      ;; used for paste, to determine if shift key was held down
      (swap! state assoc :last-keydown d-event)

      ;; update caret position for search dropdowns and for up/down
      (when (nil? (:search/type @state))
        (let [caret-position (get-caret-position (.. e -target))]
          (swap! state assoc :caret-position caret-position)))

      ;; dispatch center
      ;; only when nothing is selected or duplicate/events dispatched
      ;; after some ops(like delete) can cause errors
      (when (empty? @(subscribe [:selected/items]))
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

