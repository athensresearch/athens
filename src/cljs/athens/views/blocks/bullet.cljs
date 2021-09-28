(ns athens.views.blocks.bullet
  (:require
    [athens.db :as db]
    [goog.dom.classlist :as classList]))


;; Helpers

(defn bullet-mouse-out
  "Hide tooltip."
  [e _uid state]
  (let [related (.. e -relatedTarget)]
    (when-not (and related (classList/contains related "tooltip"))
      (swap! state assoc :tooltip false))))


(defn bullet-mouse-over
  "Show tooltip."
  [_e _uid state]
  (swap! state assoc :tooltip true))


(defn bullet-drag-start
  "Begin drag event: https://developer.mozilla.org/en-US/docs/Web/API/HTML_Drag_and_Drop_API#Define_the_drags_data"
  [e uid state]
  (let [effect-allowed (if (.. e -shiftKey) "link" "move")]
    (set! (.. e -dataTransfer -effectAllowed) effect-allowed))
  (.. e -dataTransfer (setData "text/plain" (-> uid db/uid-and-embed-id first)))
  (swap! state assoc :dragging true))


(defn bullet-drag-end
  "End drag event."
  [_e _uid state]
  (js/console.log _e)
  (swap! state assoc :dragging false))
