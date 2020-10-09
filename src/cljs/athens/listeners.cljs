(ns athens.listeners
  (:require
    [athens.db :as db]
    [athens.util :as util]
    [cljsjs.react]
    [cljsjs.react.dom]
    [goog.events :as events]
    [re-frame.core :refer [dispatch subscribe]])
  (:import
    (goog.events
      EventType
      KeyCodes)))


(defn multi-block-selection
  "When blocks are selected, handle various keypresses:
  - shift+up/down: increase/decrease selection.
  - enter: deselect and begin editing textarea
  - backspace: delete all blocks
  - up/down: change editing textarea
  - tab: indent/unindent blocks
  Can't use textarea-key-down from keybindings.cljs because textarea is no longer focused."
  [e]
  (let [selected-items @(subscribe [:selected/items])]
    (when (not-empty selected-items)
      (let [shift    (.. e -shiftKey)
            key-code (.. e -keyCode)
            enter?   (= key-code KeyCodes.ENTER)
            bksp?    (= key-code KeyCodes.BACKSPACE)
            up?      (= key-code KeyCodes.UP)
            down?    (= key-code KeyCodes.DOWN)
            tab?     (= key-code KeyCodes.TAB)
            delete?  (= key-code KeyCodes.DELETE)]
        (cond
          enter? (do
                   (dispatch [:editing/uid (first selected-items)])
                   (dispatch [:selected/clear-items]))
          (or bksp? delete?) (dispatch [:selected/delete selected-items])
          tab? (do
                 (.preventDefault e)
                 (if shift
                   (dispatch [:unindent/multi selected-items])
                   (dispatch [:indent/multi selected-items])))
          (and shift up?) (dispatch [:selected/up selected-items])
          (and shift down?) (dispatch [:selected/down selected-items])
          (or up? down?) (do
                           (.preventDefault e)
                           (dispatch [:selected/clear-items])
                           (if up?
                             (dispatch [:up (first selected-items)])
                             (dispatch [:down (last selected-items)]))))))))


(defn unfocus
  "Clears editing/uid when user clicks anywhere besides bullets, header, or on a block.
  Clears selected/items when user clicks somewhere besides a bullet point."
  [e]
  (let [selected-items?      (not-empty @(subscribe [:selected/items]))
        editing-uid          @(subscribe [:editing/uid])
        closest-block        (.. e -target (closest ".block-content"))
        closest-block-header (.. e -target (closest ".block-header"))
        closest-page-header  (.. e -target (closest ".page-header"))
        closest-bullet       (.. e -target (closest ".bullet"))
        closest-dropdown     (.. e -target (closest "#dropdown-menu"))
        closest              (or closest-block closest-block-header closest-page-header closest-dropdown)]
    (when (and selected-items?
               (nil? closest-bullet))
      (dispatch [:selected/clear-items]))
    (when (and (nil? closest)
               editing-uid)
      (dispatch [:editing/uid nil]))))


;; -- Hotkeys ------------------------------------------------------------


(defn key-down
  [e]
  (let [key      (.. e -keyCode)
        ctrl     (.. e -ctrlKey)
        meta     (.. e -metaKey)
        shift    (.. e -shiftKey)]

    (when (util/shortcut-key? meta ctrl)

      (condp = key
        KeyCodes.S (dispatch [:save])

        KeyCodes.K (dispatch [:athena/toggle])

        KeyCodes.G (dispatch [:devtool/toggle])

        KeyCodes.Z (let [editing-uid    @(subscribe [:editing/uid])
                         selected-items @(subscribe [:selected/items])]
                     ;; editing/uid must be nil or selected-items must be non-empty
                     (when (or (nil? editing-uid)
                               (not-empty selected-items))
                       (if shift
                         (dispatch [:redo])
                         (dispatch [:undo]))))

        KeyCodes.BACKSLASH (if shift
                             (dispatch [:right-sidebar/toggle])
                             (dispatch [:left-sidebar/toggle]))
        nil))))


;; -- Clipboard ----------------------------------------------------------

(defn walk-str
  "Four spaces per depth level."
  [depth node]
  (let [{:block/keys [string children]} node
        left-offset   (apply str (repeat depth "    "))
        walk-children (apply str (map #(walk-str (inc depth) %) children))]
    (str left-offset "- " string "\n" walk-children)))


(defn copy
  "If blocks are selected, copy blocks as markdown list.
  Use -event_ because goog events quirk "
  [^js e]
  (let [uids @(subscribe [:selected/items])]
    (when (not-empty uids)
      (let [copy-data (->> (map #(db/get-block-document [:block/uid %]) uids)
                           (map #(walk-str 0 %))
                           (apply str))]
        (.. e preventDefault)
        (.. e -event_ -clipboardData (setData "text/plain" copy-data))))))


(defn cut
  "Cut is essentially copy AND delete selected blocks"
  [^js e]
  (let [uids @(subscribe [:selected/items])]
    (when (not-empty uids)
      (let [copy-data (->> (map #(db/get-block-document [:block/uid %]) uids)
                           (map #(walk-str 0 %))
                           (apply str))]
        (.. e preventDefault)
        (.. e -event_ -clipboardData (setData "text/plain" copy-data))
        (dispatch [:selected/delete uids])))))


(defn init
  []
  (events/listen js/document EventType.MOUSEDOWN unfocus)
  (events/listen js/window EventType.KEYDOWN multi-block-selection)
  (events/listen js/window EventType.KEYDOWN key-down)
  (events/listen js/window EventType.COPY copy)
  (events/listen js/window EventType.CUT cut))

