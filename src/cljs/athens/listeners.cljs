(ns athens.listeners
  (:require
    [athens.db :refer [dsdb]]
    [athens.keybindings :refer [arrow-key-direction]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as string]
    [datascript.core :as d]
    [goog.events :as events]
    [re-frame.core :refer [dispatch subscribe]])
  (:import
    (goog.events
      EventType
      KeyCodes)))


;; -- shift-up/down when multi-block selection ---------------------------

;; can no longer use on-key-down from keybindings.cljs. textarea is no longer focused, so events must be handled globally
(defn multi-block-selection
  [e]
  (let [selected-items @(subscribe [:selected/items])]
    (when (not-empty selected-items)
      (let [shift     (.. e -shiftKey)
            key-code (.. e -keyCode)
            direction (arrow-key-direction e)]
        ;; what should tab/shift-tab do? roam and workflowy have slightly different behavior
        (cond
          (= key-code KeyCodes.ENTER) (do
                                        (dispatch [:editing/uid (first selected-items)])
                                        (dispatch [:selected/clear-items]))
          (= key-code KeyCodes.BACKSPACE) (dispatch [:selected/delete selected-items])
          (and shift (= direction :up)) (dispatch [:selected/up selected-items])
          (and shift (= direction :down)) (dispatch [:selected/down selected-items])
          (= direction :up) (do
                              (.preventDefault e)
                              (dispatch [:selected/clear-items])
                              (dispatch [:up (first selected-items)]))
          (= direction :down) (do
                                (.preventDefault e)
                                (dispatch [:selected/clear-items])
                                (dispatch [:down (last selected-items)])))))))


;; -- When user clicks elsewhere -----------------------------------------

(defn unfocus
  [e]
  (let [selected-items? (not-empty @(subscribe [:selected/items]))
        editing-uid    @(subscribe [:editing/uid])
        closest-block (.. e -target (closest ".block-content"))
        closest-block-header (.. e -target (closest ".block-header"))
        closest-page-header (.. e -target (closest ".page-header"))
        closest (or closest-block closest-block-header closest-page-header)]
    (when selected-items?
      (dispatch [:selected/clear-items]))
    (when (and (nil? closest) editing-uid selected-items?)
      (dispatch [:editing/uid nil]))))


;; -- Close Athena -------------------------------------------------------

(defn click-outside-athena
  [e]
  (let [athena? @(subscribe [:athena/open])
        closest (.. e -target (closest ".athena"))]
    (when (and athena? (nil? closest))
      (dispatch [:athena/toggle]))))


;; -- Hotkeys ------------------------------------------------------------


(defn key-down
  [e]
  (let [key (.. e -keyCode)
        ctrl (.. e -ctrlKey)
        ;meta (.. e -metaKey)
        shift (.. e -shiftKey)]

    (cond
      (and (= key KeyCodes.S) ctrl)
      (dispatch [:save])

      (and (= key KeyCodes.Z) ctrl shift)
      (dispatch [:redo])

      (and (= key KeyCodes.Z) ctrl)
      (dispatch [:undo])

      (and (= key KeyCodes.K) ctrl)
      (dispatch [:athena/toggle])

      (and (= key KeyCodes.G) ctrl)
      (dispatch [:devtool/toggle])

      (and (= key KeyCodes.L) ctrl shift)
      (dispatch [:right-sidebar/toggle])

      (and (= key KeyCodes.L) ctrl)
      (dispatch [:left-sidebar/toggle]))))


;; -- Clipboard ----------------------------------------------------------

;; TODO: once :selected/items is a nested tree instead of flat list, walk tree and add hyphens instead of mapping
(defn to-markdown-list
  [blocks]
  (->> blocks
       (map (fn [x] [:block/uid x]))
       (d/pull-many @dsdb '[:block/string])
       (map #(str "- " (:block/string %) "\n"))
       (string/join "")))


(defn copy
  "If blocks are selected, copy blocks as markdown list."
  [^js e]
  (let [blocks @(subscribe [:selected/items])]
    (when (not-empty blocks)
      (.. e preventDefault)
      ;; Use -event_ because goog events quirk
      (.. e -event_ -clipboardData (setData "text/plain" (to-markdown-list blocks))))))


;; do same as copy AND delete selected blocks
(defn cut
  [^js e]
  (let [blocks @(subscribe [:selected/items])]
    (when (not-empty blocks)
      (.. e preventDefault)
      (.. e -event_ -clipboardData (setData "text/plain" (to-markdown-list blocks)))
      (dispatch [:selected/delete blocks]))))


(defn init
  []
  (events/listen js/document EventType.MOUSEDOWN unfocus)
  (events/listen js/window EventType.CLICK click-outside-athena)
  (events/listen js/window EventType.KEYDOWN multi-block-selection)
  (events/listen js/window EventType.KEYDOWN key-down)
  (events/listen js/window EventType.COPY copy)
  (events/listen js/window EventType.CUT cut))

