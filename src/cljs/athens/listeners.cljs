(ns athens.listeners
  (:require
    ;;[athens.util :refer [get-day]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [goog.events :as events]
    [re-frame.core :refer [dispatch subscribe]])
  (:import
    (goog.events
      EventType
      KeyCodes)))




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
        (set! (.. e -target -onmouseup) (mouse-up-bullet on-move))))))


(defn mouse-up-bullet
  [on-move]
  (fn [_]
    (let [{:keys [uid closest/kind] target-uid :closest/uid} @(subscribe [:drag-bullet])]
      (when (and uid kind target-uid)
        (dispatch [:drop-bullet uid target-uid kind]))
      (dispatch [:drag-bullet nil])
      ;; FIXME: after the first time `empty` is called, selection stays empty
      ;;(.. (js/document.getSelection) empty)
      (events/unlisten js/window EventType.MOUSEMOVE on-move))))


(defn mouse-move-bullet
  "Must set hidden to true for bullet, otherwise bullet is captured when calling `elementFromPoint`.
  Closest child always takes precedent over closest sibling, because .block-contents is nested within .block-container.
  `cljs-oops` provides macros that let you bypass null `when` checks"
  [start-pos uid]
  (fn [e]
    (let [cX (.-clientX e)
          cY (.-clientY e)
          x (- cX (:x start-pos))
          y (- cY (:y start-pos))]
      (set! (.. e -target -hidden) true)
      (let [closest-child   (.. (js/document.elementFromPoint cX cY) (closest ".block-contents"))
            closest-sibling (.. (js/document.elementFromPoint cX cY) (closest ".block-container"))
            closest-child-uid (when closest-child (.. closest-child -dataset -uid))
            closest-sibling-uid (when closest-sibling (.. closest-sibling -dataset -uid))
            ;; nilable
            closest-uid (or closest-child-uid closest-sibling-uid)
            ;; nilable
            closest-kind (cond closest-child-uid   :child
                               closest-sibling-uid :sibling)]
        (set! (.. e -target -hidden) false)
        (dispatch [:drag-bullet
                   {:x            x
                    :y            y
                    :uid          uid
                    :closest/uid  closest-uid
                    :closest/kind closest-kind}])))))



;; -- Drag and Drop Bullet -----------------------------------------------

(defn drag-bullet-start
  [e state]
  (swap! state merge @state {:bullet/active    true
                             :bullet/initial-x (.. e -clientX)
                             :bullet/initial-y (.. e -clientY)}))

(defn drag-bullet
  [e state]
  (when (:bullet/active @state)
    (let [{:bullet/keys [initial-x initial-y]} @state
          client-x  (.. e -clientX)
          client-y  (.. e -clientY)
          current-x (- client-x initial-x)
          current-y (- client-y initial-y)]
      (.. e preventDefault)
      (swap! state merge @state {:bullet/current-x current-x
                                 :bullet/current-y current-y}))))

(defn drag-bullet-end
  [_ state]
  (swap! state merge @state {:bullet/active    false
                             :bullet/initial-x 0
                             :bullet/initial-y 0
                             :bullet/current-x 0
                             :bullet/current-y 0}))




;; -- Turn read block or header into editable on mouse down --------------

(defn edit-block
  [e]
  ;; Consider refactor if we add more editable targets
  (let [closest-block (.. e -target (closest ".block-contents"))
        closest-block-header (.. e -target (closest ".block-header"))
        closest-page-header (.. e -target (closest ".page-header"))
        closest (or closest-block closest-block-header closest-page-header)]
    (when closest
      (dispatch [:editing/uid (.. closest -dataset -uid)]))))


;; -- Show Tooltip--------------------------------------------------------

(defn show-tooltip
  [e]
  (let [class-list (array-seq (.. e -target -classList))
        closest (.. e -target (closest ".tooltip"))
        uid (.. e -target -dataset -uid)
        tooltip-uid @(subscribe [:tooltip/uid])]
    (cond
      ;; if mouse over bullet, show tooltip
      (some #(= "bullet" %) class-list) (dispatch [:tooltip/uid uid])
      ;; if mouse over a child of bullet, keep tooltip-uid
      closest nil
      ;; if tooltip is already nil, don't overwrite tooltip-uid
      (nil? tooltip-uid) nil
      ;; otherwise mouse is no longer over a bullet or tooltip. clear the tooltip-uid
      :else (dispatch [:tooltip/uid nil]))))


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
  ;;(events/listen js/window EventType.MOUSEDOWN drag-bullet-start)
  ;;(events/listen js/window EventType.MOUSEMOVE drag-bullet)
  ;;(events/listen js/window EventType.MOUSEUP   drag-bullet-end)

  ;;(events/listen js/window EventType.MOUSEOVER show-tooltip)
  (events/listen js/window EventType.MOUSEDOWN edit-block)

  (events/listen js/window EventType.MOUSEDOWN mouse-down-outside-athena)
  (events/listen js/window EventType.KEYDOWN key-down))

