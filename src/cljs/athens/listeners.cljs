(ns athens.listeners
  (:require
    [cljsjs.react]
    [cljsjs.react.dom]
    [goog.events :as events]
    [re-frame.core :refer [dispatch subscribe]])
  (:import
    (goog.events
      EventType
      KeyCodes)))


;;; Drag Bullet to Re-order Block


(declare mouse-move-bullet mouse-up-bullet)


(defn mouse-down-bullet
  [e]
  (let [class-list (-> (.. e -target -classList) array-seq)]
    (when (some #(= "bullet" %) class-list)
      (let [start-pos {:x (.-clientX e)
                       :y (.-clientY e)}
            uid (.. e -target -dataset -uid)
            on-move (mouse-move-bullet start-pos uid)]
        (events/listen js/window EventType.MOUSEMOVE on-move)
        (events/listen js/window EventType.MOUSEUP (mouse-up-bullet on-move))))))


(defn mouse-move-bullet
  [start-pos uid]
  (fn [evt]
    (let [cX (.-clientX evt)
          cY (.-clientY evt)
          x (- cX (:x start-pos))
          y (- cY (:y start-pos))
          closest-sibling (.. (js/document.elementFromPoint cX cY) (closest ".block-container"))
          closest-child (.. (js/document.elementFromPoint cX cY) (closest ".block-contents"))
          closest (or closest-child closest-sibling)
          closest-uid (when closest (.. closest -dataset -uid))
          closest-kind (when closest (if (some #(= "block-container" %) (array-seq (.. closest -classList)))
                                       :sibling
                                       :child))]
      (prn x y uid closest-uid closest-kind)
      (dispatch [:drag-bullet {:x         x :y y
                               :uid          uid
                               :closest/uid  closest-uid
                               :closest/kind closest-kind}]))))


(defn mouse-up-bullet
  [on-move]
  (fn [_evt]
    (let [{:keys [uid closest/kind] target-uid :closest/uid} @(subscribe [:drag-bullet])]
      (when target-uid
        (dispatch [:drop-bullet {:source uid :target target-uid :kind kind}]))
      (dispatch [:drag-bullet {}])
      ;; FIXME: after the first time `empty` is called, selection stays empty
      ;;(.. (js/document.getSelection) empty)
      (events/unlisten js/window EventType.MOUSEMOVE on-move))))


;;; Turn read block or header into editable on mouse down


(defn mouse-down-block
  [e]
  ;; Consider refactor if we add more editable targets
  (let [closest-block (.. e -target (closest ".block-contents"))
        closest-block-header (.. e -target (closest ".block-header"))
        closest-page-header (.. e -target (closest ".page-header"))
        closest (or closest-block closest-block-header closest-page-header)]
    (when closest
      (dispatch [:editing-uid (.. closest -dataset -uid)]))))


;;; Show tooltip


(defn mouse-over-bullet
  [e]
  (let [class-list (array-seq (.. e -target -classList))
        closest (.. e -target (closest ".tooltip"))
        uid (.. e -target -dataset -uid)
        tooltip-uid @(subscribe [:tooltip-uid])]
    (cond
      ;; if mouse over bullet, show tooltip
      (some #(= "bullet" %) class-list) (dispatch [:tooltip-uid uid])
      ;; if mouse over a child of bullet, keep tooltip-uid
      closest nil
      ;; if tooltip is already nil, don't overwrite tooltip-uid
      (nil? tooltip-uid) nil
      ;; otherwise mouse is no longer over a bullet or tooltip. clear the tooltip-uid
      :else (dispatch [:tooltip-uid nil]))))


;;; Close Athena


(defn mouse-down-outside-athena
  [e]
  (let [athena? @(subscribe [:athena])
        closest (.. e -target (closest ".athena"))]
    (when (and athena? (nil? closest))
      (dispatch [:toggle-athena]))))


;;; Hotkeys

(defn key-down
  [e]
  (let [key (.. e -keyCode)
        _ctrl (.. e -ctrlKey)
        meta (.. e -metaKey)
        shift (.. e -shiftKey)]

    (cond
      (and (= key KeyCodes.Z) meta shift)
      (dispatch [:redo])

      (and (= key KeyCodes.Z) meta)
      (dispatch [:undo])

      (and (= key KeyCodes.K) meta)
      (dispatch [:toggle-athena]))))


(defn init
  []
  (events/listen js/window EventType.MOUSEDOWN mouse-down-block)
  (events/listen js/window EventType.MOUSEDOWN mouse-down-bullet)
  (events/listen js/window EventType.MOUSEOVER mouse-over-bullet)
  (events/listen js/window EventType.MOUSEDOWN mouse-down-outside-athena)
  (events/listen js/window EventType.KEYDOWN key-down))
