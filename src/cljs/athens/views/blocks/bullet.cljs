(ns athens.views.blocks.bullet
  (:require
    ["/components/Block/components/Anchor" :refer [Anchor]]
    [athens.db :as db]
    [athens.router :as router]
    [athens.views.blocks.context-menu :as context-menu]
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
  (swap! state assoc :dragging false))


;; View


(defn bullet-el
  [_ _ _]
  (fn [block state linked-ref]
    (let [{:block/keys [uid children open]} block]
      [:> Anchor {:isClosedWithChildren (when (and (seq children)
                                                  (or (and (true? linked-ref) (not (:linked-ref/open @state)))
                                                      (and (false? linked-ref) (not open))))
                                         "closed-with-children")
                  :on-click        (fn [e] (router/navigate-uid uid e))
                  :on-context-menu (fn [e] (context-menu/bullet-context-menu e uid state))
                  :on-mouse-over   (fn [e] (bullet-mouse-over e uid state)) ; useful during development to check block meta-data
                  :on-mouse-out    (fn [e] (bullet-mouse-out e uid state))
                  :on-drag-start   (fn [e] (bullet-drag-start e uid state))
                  :on-drag-end     (fn [e] (bullet-drag-end e uid state))}])))
