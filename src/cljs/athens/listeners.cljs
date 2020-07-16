(ns athens.listeners
  (:require
    ;;[athens.util :refer [get-day]]
    [athens.keybindings :refer [arrow-key-direction]]
    [cljsjs.react]
    [cljsjs.react.dom]
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


;; -- When dragging across multiple blocks to select ---------------------

(defn get-dataset-uid
  [el]
  (let [block (when el (.. el (closest ".block-container")))
        uid (when block (.. block -dataset -uid))]
    uid))


(defn multi-block-select-over
  "If going over something, add it.
  If leaving it, remove"
  [e]
  (let [target             (.. e -target)
        related-target     (.. e -relatedTarget)
        target-uid         (get-dataset-uid target)
        _related-target-uid (get-dataset-uid related-target)
        selected-items     @(subscribe [:selected/items])
        _set-items (set selected-items)]
    (.. e stopPropagation)
    (.. target blur)
    (dispatch [:selected/add-item target-uid])))


(defn multi-block-select-up
  [_]
  (events/unlisten js/window EventType.MOUSEOVER multi-block-select-over)
  (events/unlisten js/window EventType.MOUSEUP multi-block-select-up))

;; -- When user clicks elsewhere -----------------------------------------

(defn unfocus
  [e]
  (let [selected-items @(subscribe [:selected/items])
        editing-uid    @(subscribe [:editing/uid])
        closest-block (.. e -target (closest ".block-content"))
        closest-block-header (.. e -target (closest ".block-header"))
        closest-page-header (.. e -target (closest ".page-header"))
        closest (or closest-block closest-block-header closest-page-header)]
    (when (not-empty selected-items)
      (dispatch [:selected/clear-items]))
    (when (and (nil? closest) editing-uid)
      (dispatch [:editing/uid nil]))))


;; -- Turn read block or header into editable on mouse down --------------

;; (defn edit-block
;;   [e]
;;   ;; Consider refactor if we add more editable targets
;;   (let [closest-block (.. e -target (closest ".block-content"))
;;         closest-block-header (.. e -target (closest ".block-header"))
;;         closest-page-header (.. e -target (closest ".page-header"))
;;         closest (or closest-block closest-block-header closest-page-header)]
;;     (when closest
;;       (dispatch [:editing/uid (.. closest -dataset -uid)]))))


;; -- Close Athena -------------------------------------------------------

(defn mouse-down-outside-athena
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
        meta (.. e -metaKey)
        shift (.. e -shiftKey)]

    (cond
      (and (= key KeyCodes.Z) meta shift)
      (dispatch [:redo])

      (and (= key KeyCodes.Z) meta)
      (dispatch [:undo])

      (and (= key KeyCodes.K) meta)
      (dispatch [:athena/toggle])

      (and (= key KeyCodes.G) ctrl)
      (dispatch [:devtool/toggle])

      (and (= key KeyCodes.R) ctrl)
      (dispatch [:right-sidebar/toggle])

      (and (= key KeyCodes.L) ctrl)
      (dispatch [:left-sidebar/toggle]))))


(defn init
  []
  ;; (events/listen js/window EventType.MOUSEDOWN edit-block)
  (events/listen js/window EventType.MOUSEDOWN unfocus)
  (events/listen js/window EventType.MOUSEDOWN mouse-down-outside-athena)
  (events/listen js/window EventType.KEYDOWN multi-block-selection)
  (events/listen js/window EventType.KEYDOWN key-down))

