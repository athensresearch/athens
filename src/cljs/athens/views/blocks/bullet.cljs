(ns athens.views.blocks.bullet
  (:require
    [athens.router :as router]
    [athens.db :as db]
    [athens.views.blocks.context-menu :as context-menu]
    [athens.style :as style]
    [garden.selectors :as selectors]
    [stylefy.core :as stylefy]
    [goog.dom.classlist :as classList]))


;; Styles


(def bullet-style
  {:flex-shrink "0"
   :position "relative"
   :z-index 2
   :cursor "pointer"
   :width "0.75em"
   :margin-right "0.25em"
   :transition "all 0.05s ease"
   :height "2em"
   :color (style/color :body-text-color :opacity-low)
   ::stylefy/mode [[:after {:content "''"
                            :background "currentColor"
                            :transition "all 0.05s ease"
                            :border-radius "100px"
                            :box-shadow "0 0 0 0.125rem transparent"
                            :display "inline-flex"
                            :margin "50% 0 0 50%"
                            :transform "translate(-50%, -50%)"
                            :height "0.3125em"
                            :width "0.3125em"}]
                   [:hover {:color (style/color :link-color)}]]
   ::stylefy/manual [[:&.closed-with-children [(selectors/& (selectors/after)) {:box-shadow (str "0 0 0 0.125rem " (style/color :body-text-color))
                                                                                :opacity (:opacity-med style/OPACITIES)}]]
                     [:&.closed-with-children [(selectors/& (selectors/before)) {:content "none"}]]
                     [:&:hover:after {:transform "translate(-50%, -50%) scale(1.3)"}]
                     [:&.dragging {:z-index 1
                                   :cursor "grabbing"
                                   :color (style/color :body-text-color)}]]})


(stylefy/class "bullet" bullet-style)


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
      [:span {:class           ["bullet" (when (and (seq children)
                                                    (or (and (true? linked-ref) (not (:linked-ref/open @state)))
                                                        (and (false? linked-ref) (not open))))
                                           "closed-with-children")]
              :draggable       true
              :on-click        (fn [e] (router/navigate-uid uid e))
              :on-context-menu (fn [e] (context-menu/bullet-context-menu e uid state))
              :on-mouse-over   (fn [e] (bullet-mouse-over e uid state)) ;; useful during development to check block meta-data
              :on-mouse-out    (fn [e] (bullet-mouse-out e uid state))
              :on-drag-start   (fn [e] (bullet-drag-start e uid state))
              :on-drag-end     (fn [e] (bullet-drag-end e uid state))}])))
