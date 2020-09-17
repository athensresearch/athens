(ns athens.keybindings
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.util :refer [scroll-if-needed get-day]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :refer [replace-first blank?]]
    [goog.dom :refer [getElement]]
    [goog.dom.selection :refer [setStart setEnd getText setCursorPosition getEndPoints]]
    [goog.events.KeyCodes :refer [isCharacterKey]]
    [re-frame.core :refer [dispatch]])
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
  [e]
  (js->clj (getEndPoints (.. e -target))))


(defn destruct-event
  [e]
  (let [key (.. e -key)
        key-code (.. e -keyCode)
        target (.. e -target)
        value (.. target -value)
        event {:key key :key-code key-code :target target :value value}
        modifiers (modifier-keys e)
        [start end] (get-end-points e)
        selection (getText target)
        head (subs value 0 start)
        tail (subs value end)]
    (merge modifiers event
           {:start start :end end}
           {:head head :tail tail}
           {:selection selection})))


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
  [["Add Todo"      mui-icons/Done "{{[[TODO]]}} " "cmd-enter"]
   ["Current Time"  mui-icons/Timer (fn [] (.. (js/Date.) (toLocaleTimeString [] (clj->js {"timeStyle" "short"})))) nil]
   ["Today"         mui-icons/Today (fn [] (str "[[" (:title (get-day 0)) "]] ")) nil]
   ["Tomorrow"      mui-icons/Today (fn [] (str "[[" (:title (get-day -1)) "]]")) nil]
   ["Yesterday"     mui-icons/Today (fn [] (str "[[" (:title (get-day 1)) "]]")) nil]
   ["YouTube Embed" mui-icons/YouTube "{{[[youtube]]: }}" nil]
   ["iframe Embed"  mui-icons/DesktopWindows "{{iframe: }}" nil]])

;;[mui-icons/ "Block Embed" #(str "[[" (:title (get-day 1)) "]]")]
;;[mui-icons/DateRange "Date Picker"]
;;[mui-icons/Attachment "Upload Image or File"]
;;[mui-icons/ExposurePlus1 "Word Count"]


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
  ([state head key type]
   (let [query-fn (case type
                    :block db/search-in-block-content
                    :page  db/search-in-node-title
                    :slash filter-slash-options)
         query-start-idx (case type
                           :block (count (re-find #".*\(\(" head))
                           :page  (count (re-find #".*\[\[" head))
                           :slash (count (re-find #".*/"    head)))
         new-query (str (subs head query-start-idx) key)
         results (query-fn new-query)]
     (if (and (= type :slash) (empty? results))
       (swap! state assoc :search/type nil)
       (swap! state assoc
              :search/index 0
              :search/query new-query
              :search/results results)))))


;; 1- if no results, just hide slash commands so this doesnt get triggered
;; 2- if results, do find and replace properly
(defn auto-complete-slash
  [state e]
  (let [{:keys [string/local] :search/keys [index results]} @state
        {:keys [head tail]} (destruct-event e)
        [_ _ expansion _] (nth results index)
        expand (if (fn? expansion) (expansion) expansion)
        start-idx (dec (count (re-find #".*/" head)))
        new-head (subs local 0 start-idx)
        new-str (str new-head expand tail)]
    (swap! state assoc
           :search/type nil
           :string/generated new-str)))


(defn auto-complete-inline
  [state e]
  (let [{:search/keys [query type index results]} @state
        {:keys [node/title block/uid]} (nth results index)
        {:keys [start head tail target]} (destruct-event e)
        completed-str (or title uid)
        block? (= type :block)
        page? (= type :page)
        ;; rewrite this more cleanly
        head-pattern (cond block? (re-pattern (str "(.*)\\(\\(" query))
                           page?  (re-pattern (str "(.*)\\[\\[" query)))
        tail-pattern (cond block? #"(\)\))?(.*)"
                           page?  #"(\]\])?(.*)")
        new-head (cond block? "$1(("
                       page?  "$1[[")
        closing-str (cond block? "))"
                          page?  "]]")
        new-str (replace-first head head-pattern (str new-head completed-str closing-str))
        [_ closing-delimiter after-closing-str] (re-matches tail-pattern tail)]
    ;; completed-str is nil if there are no results, but user presses enter to auto-complete
    (if (nil? completed-str)
      (swap! state assoc :search/type nil)
      (swap! state assoc :search/type nil :string/generated (str new-str after-closing-str)))
    (when closing-delimiter
      (setStart target (+ 2 start)))))


;;; Arrow Keys


(defn block-start?
  [e]
  (let [[start _] (get-end-points e)]
    (zero? start)))


(defn block-end?
  [e]
  (let [{:keys [value end]} (destruct-event e)]
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
  (let [{:keys [key-code shift target]} (destruct-event e)
        top-row?    (block-start? e)
        bottom-row? (block-end? e)
        {:search/keys [results type index]} @state
        up? (= key-code KeyCodes.UP)
        down? (= key-code KeyCodes.DOWN)
        left? (= key-code KeyCodes.LEFT)
        right? (= key-code KeyCodes.RIGHT)]

    (cond
      ;; Shift: select block if leaving block content boundaries (top or bottom rows). Otherwise select textarea text (default)
      shift (cond
              left? nil
              right? nil
              (or (and up? top-row?)
                  (and down? bottom-row?)) (do
                                             (.. target blur)
                                             (dispatch [:selected/add-item uid])))

      ;; Type, one of #{:slash :block :page}: If slash commands or inline search is open, cycle through options
      type (cond
             (or left? right?) (swap! state assoc :search/index 0 :search/type nil)
             (or up? down?) (let [cur-index index
                                  min-index 0
                                  max-index (max-idx results)
                                  next-index (cycle-list min-index max-index cur-index up? down?)
                                  container-el (getElement "dropdown-menu")
                                  target-el (getElement (str "dropdown-item-" next-index))]
                              (.. e preventDefault)
                              (swap! state assoc :search/index next-index)
                              (scroll-if-needed target-el container-el)))

      ;; Else: navigate across blocks
      :else (cond
              (and up? top-row?)       (dispatch [:up uid])
              (and left? top-row?)     (dispatch [:left uid])
              (and down? bottom-row?)  (dispatch [:down uid])
              (and right? bottom-row?) (dispatch [:right uid])))))


;;; Tab

(defn handle-tab
  "Bug: indenting sets the cursor position to 0, liekely because a new textarea element is created on the DOM. Set selection appropriately.
  See :indent event for why value must be passed as well."
  [e uid _state]
  (.. e preventDefault)
  (let [{:keys [shift value start end]} (destruct-event e)]
    (if shift
      (dispatch [:unindent uid value])
      (dispatch [:indent uid value]))
    (js/setTimeout (fn []
                     (when-let [el (getElement (str "editable-uid-" uid))]
                       (setStart el start)
                       (setEnd el end)))
                   50)))


(defn handle-escape
  [e state]
  (.. e preventDefault)
  (swap! state assoc :search/type nil)
  (dispatch [:editing/uid nil]))

;;; Enter

(defn handle-enter
  [e uid state]
  (let [{:keys [shift ctrl start head tail value]} (destruct-event e)
        {:search/keys [type]} @state]
    (.. e preventDefault)
    (cond

      type (if (= type :slash)
             (auto-complete-slash state e)
             (auto-complete-inline state e))

      ;; shift-enter: add line break to textarea
      shift (swap! state assoc :string/generated (str head "\n" tail))
      ;; cmd-enter: cycle todo states. 13 is the length of the {{[[TODO]]}} string
      ctrl (let [first    (subs value 0 13)
                 new-tail (subs value 13)
                 new-str (cond (= first "{{[[TODO]]}} ") (str "{{[[DONE]]}} " new-tail)
                               (= first "{{[[DONE]]}} ") new-tail
                               :else (str "{{[[TODO]]}} " value))]
             (swap! state assoc :string/generated new-str))
      ;; default: may mutate blocks
      :else (dispatch [:enter uid value start]))))


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
  [e _ state]
  (let [{:keys [key-code head tail selection shift]} (destruct-event e)]
    (cond
      (= key-code KeyCodes.B) (let [new-str (str head (surround selection "**") tail)]
                                (swap! state assoc :string/generated new-str))
      (and (not shift) (= key-code KeyCodes.I)) (let [new-str (str head (surround selection "__") tail)]
                                                  (swap! state assoc :string/generated new-str)))))


(defn pair-char?
  [e]
  (let [{:keys [key]} (destruct-event e)
        pair-char-set (-> PAIR-CHARS
                          seq
                          flatten
                          set)]
    (pair-char-set key)))


(defn handle-pair-char
  [e _ state]
  (let [{:keys [key head tail target start end selection]} (destruct-event e)
        close-pair (get PAIR-CHARS key)]
    (cond
      (= start end) (let [new-str (str head key close-pair tail)]
                      (js/setTimeout #(setCursorPosition target (inc start)) 10)
                      (swap! state assoc :string/generated new-str))
      (not= start end) (let [surround-selection (surround selection key)
                             new-str (str head surround-selection tail)]
                         (swap! state assoc :string/generated new-str)
                         (js/setTimeout (fn []
                                          (setStart target (inc start))
                                          (setEnd target (inc end)))
                                        10)))

    (let [four-char (subs (:string/generated @state) (dec start) (+ start 3))
          double-brackets? (= "[[]]" four-char)
          double-parens?   (= "(())" four-char)
          type (cond double-brackets? :page
                     double-parens? :block)]
      (swap! state assoc :search/type type))))

    ;; TODO: close bracket should not be created if it already exists
    ;;(= key-code KeyCodes.CLOSE_SQUARE_BRACKET)


;; Backspace

(defn handle-backspace
  [e uid state]
  (let [{:keys [start value target end]} (destruct-event e)
        no-selection? (= start end)
        possible-pair (subs value (dec start) (inc start))
        head    (subs value 0 (dec start))
        {:search/keys [type]} @state]
    (cond
      (and (block-start? e) no-selection?) (dispatch [:backspace uid value])
      ;; pair char: hide inline search and auto-balance
      (some #(= possible-pair %) ["[]" "{}" "()"]) (let [head    (subs value 0 (dec start))
                                                         tail    (subs value (inc start))
                                                         new-str (str head tail)]
                                                     (swap! state assoc
                                                            :search/type nil
                                                            :string/generated new-str)
                                                     (js/setTimeout #(setCursorPosition target (dec start)) 10))
      ;; slash: close dropdown
      (= "/" (last value)) (swap! state assoc :search/type nil)
      ;; dropdown is open: update query
      type (update-query state head "" type))))


;; Character: for queries

(defn is-character-key?
  "Closure returns true even when using modifier keys. We do not make that assumption."
  [e]
  (let [{:keys [meta ctrl alt key-code]} (destruct-event e)]
    (and (not meta) (not ctrl) (not alt)
         (isCharacterKey key-code))))


(defn write-char
  "When user types /, trigger slash menu.
  If user writes a character while there is a slash/type, update query and results."
  [e _ state]
  (let [{:keys [head key]} (destruct-event e)
        slash-key? (= key "/")
        {:search/keys [type]} @state]
    (cond
      slash-key? (swap! state assoc
                        :search/index 0
                        :search/query ""
                        :search/type :slash
                        :search/results slash-options)
      type (update-query state head key type))))


(defn textarea-key-down
  [e uid state]
  (let [d-event (destruct-event e)
        {:keys [meta ctrl key-code]} d-event]
    ;; used for paste, to determine if shift key was held down
    (swap! state assoc :last-keydown d-event)
    (cond
      (arrow-key-direction e)         (handle-arrow-key e uid state)
      (pair-char? e)                  (handle-pair-char e uid state)
      (= key-code KeyCodes.TAB)       (handle-tab e uid state)
      (= key-code KeyCodes.ENTER)     (handle-enter e uid state)
      (= key-code KeyCodes.BACKSPACE) (handle-backspace e uid state)
      (= key-code KeyCodes.ESC)       (handle-escape e state)
      (or meta ctrl)                  (handle-shortcuts e uid state)
      (is-character-key? e)           (write-char e uid state))))

;;:else (prn "non-event" key key-code))))

