(ns athens.listeners
  (:require
    [cljsjs.react]
    [cljsjs.react.dom]
    [goog.events :as events]
    [re-frame.core :as rf])
  (:import
    (goog.events
      EventType)))


;;; Drag Bullet to Re-order Block


(declare mouse-move-bullet mouse-up-bullet)


(defn get-client-rect
  [evt]
  (let [r (.getBoundingClientRect (.-target evt))]
    {:left (.-left r), :top (.-top r)}))


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
    (let [x (- (.-clientX evt) (:x start-pos))
          y (- (.-clientY evt) (:y start-pos))
          closest-sibling (.. (js/document.elementFromPoint (.-clientX evt) (.-clientY evt)) (closest ".block-container"))
          closest-child (.. (js/document.elementFromPoint (.-clientX evt) (.-clientY evt)) (closest ".block-contents"))
          closest (or closest-child closest-sibling)
          closest-uid (when closest (.. closest -dataset -uid))
          closest-kind (when closest (if (some #(= "block-container" %) (array-seq (.. closest -classList)))
                                       :sibling
                                       :child))]
      (prn x y uid closest-uid closest-kind)
      (rf/dispatch [:drag-bullet {:x         x :y y
                                  :uid          uid
                                  :closest/uid  closest-uid
                                  :closest/kind closest-kind}]))))


(defn mouse-up-bullet
  [on-move]
  (fn [_evt]
    (let [{:keys [uid closest/kind] target-uid :closest/uid} @(rf/subscribe [:drag-bullet])]
      (rf/dispatch [:drop-bullet {:source uid :target target-uid :kind kind}])
      (rf/dispatch [:drag-bullet {}])
      (.. (js/document.getSelection) empty)
      (events/unlisten js/window EventType.MOUSEMOVE on-move))))


;;; Turn read block into write block


(defn mouse-down-block
  [e]
  (let [closest (.. e -target (closest ".block-contents"))]
    (when closest
      (rf/dispatch [:editing-uid (.. closest -dataset -uid)]))))


;;; Show tooltip


(defn mouse-over-bullet
  [e]
  (let [class-list (array-seq (.. e -target -classList))
        closest (.. e -target (closest ".tooltip"))
        uid (.. e -target -dataset -uid)]
    (cond
      (some #(= "bullet" %) class-list) (rf/dispatch [:tooltip-uid uid])
      closest nil
      :else (rf/dispatch [:tooltip-uid nil]))))


(defn init
  []

  (events/listen js/window EventType.MOUSEDOWN mouse-down-block)

  (events/listen js/window EventType.MOUSEDOWN mouse-down-bullet)

  (events/listen js/window EventType.MOUSEOVER mouse-over-bullet))
