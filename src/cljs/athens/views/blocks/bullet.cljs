(ns athens.views.blocks.bullet
  (:require
    [athens.db :as db]))


;; Helpers

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
  (swap! state assoc :dragging false))
