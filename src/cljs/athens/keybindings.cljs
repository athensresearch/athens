(ns athens.keybindings
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.parse-renderer :refer [parse-and-render]]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [color DEPTH-SHADOWS OPACITIES]]
    [athens.views.dropdown :refer [slash-menu-component #_menu dropdown]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [garden.selectors :as selectors]
    [goog.dom :refer [getAncestorByClass]]
    [goog.dom.classlist :refer [contains]]
    [goog.dom.selection :refer [setStart getStart setEnd getEnd #_setText getText setCursorPosition getEndPoints]]
    [goog.events.KeyCodes :refer [isCharacterKey]]
    [goog.functions :refer [debounce]]
    [komponentit.autosize :as autosize]
    [re-frame.core  :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]])
  (:import
    (goog.events
      KeyCodes)))


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
        selection    (getText target)
        head (subs value 0 start)
        tail (subs value start)]
    (merge modifiers event
      {:start start :end end}
      {:head head :tail tail}
      {:selection selection})))


(defn arrow-key?
  [e]
  (let [{:keys [key-code]} (destruct-event e)]
    (or (= key-code KeyCodes.UP)
        (= key-code KeyCodes.LEFT)
        (= key-code KeyCodes.DOWN)
        (= key-code KeyCodes.RIGHT))))


(defn block-start?
  [e]
  (let [[start _] (get-end-points e)]
    (zero? start)))


(defn block-end?
  [e]
  (let [{:keys [value end]} (destruct-event e)]
    (= end (count value))))


(defn handle-arrow-key
  [e uid]
  (let [{:keys [key-code]} (destruct-event e)
        top-row?    true                                    ;; TODO
        bottom-row? true]                                   ;; TODO
    (cond
      (and (= key-code KeyCodes.UP) top-row?)           (dispatch [:up uid])
      (and (= key-code KeyCodes.LEFT) (block-start? e)) (dispatch [:left uid])
      (and (= key-code KeyCodes.DOWN) bottom-row?)      (dispatch [:down uid])
      (and (= key-code KeyCodes.RIGHT) (block-end? e))  (dispatch [:right uid]))))


(defn handle-tab
  [e uid]
  (.. e preventDefault)
  (let [{:keys [shift]} (destruct-event e)
        ;; xxx: probably makes more sense to pass block value to handler directly
        block-zero? (zero? (:block/order (db/get-block [:block/uid uid])))]
    (cond
      shift (dispatch [:unindent uid])
      :else (when-not block-zero?
              (dispatch [:indent uid])))))


;;(defn cycle-todo
;;  [])

(defn handle-enter
  [e uid state]
  (let [{:keys [shift meta start head tail value]} (destruct-event e)]
    (cond
      ;; shift-enter: add line break to textarea
      shift (swap! state assoc :atom-string (str head "\n" tail))
      ;; cmd-enter: toggle todo/done
      meta (let [first    (subs value 0 12)
                   new-tail (subs value 12)
                   new-head (cond (= first "{{[[TODO]]}}") "{{[[DONE]]}}"
                                  (= first "{{[[DONE]]}}") ""
                                  :else "{{[[TODO]]}} ")
                   new-str  (str new-head new-tail)]
               (swap! state assoc :atom-string new-str))
      ;; default: may mutate blocks
      :else (do (.. e preventDefault)
                (dispatch [:enter uid value start state])))))


;; TODO: it's ctrl for windows and linux right?
(defn handle-system-shortcuts
  "Assumes meta is selected"
  [e uid state]
  (let [{:keys [key-code target end selection]} (destruct-event e)]
    (cond
      (= key-code KeyCodes.A) (do (setStart target 0)
                                  (setEnd target end))

      ;; TODO: undo. conflicts with datascript undo
      (= key-code KeyCodes.Z) (prn "undo")

      ;; TODO: cut
      (= key-code KeyCodes.X) (prn "cut")

      ;; TODO: paste. magical
      (= key-code KeyCodes.V) (prn "paste")

      ;; TODO: bold
      (= key-code KeyCodes.B) (prn "bold")

      ;; TODO: italicize
      (= key-code KeyCodes.I) (prn "italics"))))


(defn pair-char? [e]
  (let [{:keys [key-code key]} (destruct-event e)]
    ;;(prn key-code key)
    (or (= key-code KeyCodes.OPEN_SQUARE_BRACKET)
        (= key-code KeyCodes.NINE))))

(defn handle-pair-char
  [e uid state]
  nil)
  ;;(cond
  ;;  ;; -- Curly Braces -------------------------------------------------------
  ;;  ;; default: auto-create
  ;;  (and shift (= key-code KeyCodes.OPEN_SQUARE_BRACKET))
  ;;  (let [new-str (str head "{}" tail)]
  ;;    (js/setTimeout #(setCursorPosition target (inc start)) 10)
  ;;    (swap! state assoc :atom-string new-str))
  ;;
  ;;
  ;;  ;; if selection, add brackets around selection
  ;;  (and (not= "" selection) (= key-code KeyCodes.OPEN_SQUARE_BRACKET))
  ;;  (let [surround-selection (str "[" selection "]")
  ;;        new-str (str head surround-selection tail)]
  ;;    (js/setTimeout (fn []
  ;;                     (setStart target (inc start))
  ;;                     (setEnd target (inc end)))
  ;;     10)
  ;;    (swap! state assoc :atom-string new-str))))
  ;;
    ;;;; default: auto-create close bracket
    ;;(= key-code KeyCodes.OPEN_SQUARE_BRACKET)
    ;;(let [new-str (str head "[]" tail)
    ;;      double-brackets? (= "[[]]" (subs new-str (dec start) (+ start 3)))]
    ;;  (js/setTimeout #(setCursorPosition target (inc start)) 10)
    ;;  (swap! state assoc :atom-string new-str)
    ;;  ;; if second bracket, open search
    ;;  (when double-brackets?
    ;;    (swap! state assoc :search/page true)))))

    ;; TODO: close bracket should not be created if open bracket already exists or user just made a link
    ;;(= key-code KeyCodes.CLOSE_SQUARE_BRACKET)

    ;; -- Parentheses --------------------------------------------------------
    ;;(and shift (= key-code KeyCodes.NINE)) (swap! state update :search/block not)))


(defn handle-backspace
  [e uid state]
  (let [{:keys [key-code selection start end value head tail target meta]} (destruct-event e)]
    (prn "BKSPACE" (not= selection "") meta)
    (cond
      ;; if selection, delete selected text
      (not= selection "") (let [new-tail (subs value end)
                                new-str (str head new-tail)]
                            (swap! state assoc :atom-string new-str))

      ;; if meta, delete to start of line
      ;; xxx meta is mainly handled in system cmds. should it be here or there?
      meta (swap! state assoc :atom-string tail)

      ;; if at block start, dispatch (requires context)
      (block-start? e) (dispatch [:backspace uid value])

      ;; if within brackets, delete close bracket as well
      ;; TODO implement for parens and curly braces
      (= "[]" (subs value (dec start) (inc start)))
      (let [head (subs value 0 (dec start))
            tail (subs value (inc start))
            new-str (str head tail)]
        (js/setTimeout #(setCursorPosition target (dec start)) 10)
        (swap! state assoc :atom-string new-str)
        (swap! state assoc :search/page false))

      ;; default backspace: delete a character
      :else (let [head (subs value 0 (dec start))
                  new-str (str head tail)]
              ;;(when (or (:search/page @state) (:search/block @state))
              ;;  (swap! state assoc :search/query (subs query 0 (dec (count query)))))
              (swap! state assoc :atom-string new-str)))))

;; XXX: what happens here when we have multi-block selection? In this case we pass in `uids` instead of `uid`
(defn block-key-down
  "Three big buckets
  - empty text selection
  - non-empty text selection
  - multi-block selection"

  [e uid state]
  (let [{:keys [shift meta ctrl alt key key-code target start end value head tail selection]} (destruct-event e)
        block-start? (block-start? e)
        string       (:atom-string @state)
        query        (:search/query @state)]
    (cond

      (arrow-key? e) (handle-arrow-key e uid)
      (pair-char? e) (handle-pair-char e uid state)
      (= key-code KeyCodes.TAB) (handle-tab e uid)
      (= key-code KeyCodes.ENTER) (handle-enter e uid state)
      (= key-code KeyCodes.BACKSPACE) (handle-backspace e uid state)
      (= key-code KeyCodes.SLASH) (swap! state update :slash? not)
      meta (handle-system-shortcuts e uid state)

      ;; -- Default: Add new character -----------------------------------------
      (and (not meta) (not ctrl) (not alt) (isCharacterKey key-code))
      (let [new-str (str head key tail)]
        ;;(when (or (:search/page @state) (:search/block @state)))
          ;;(swap! state assoc :search/query (str (:search/query @state) key)))
        (swap! state assoc :atom-string new-str)))))

      ;;:else (prn "non-event" key key-code))))

