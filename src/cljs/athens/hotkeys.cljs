(ns athens.hotkeys
  (:require
    [athens.router :as router]
    [athens.util :as util]
    ["mousetrap" :as Mousetrap]
    [re-frame.core :refer [dispatch subscribe]]
    [react]
    [reagent.core :as r]))

;; Binds an event in the global Moutrap instance.
(defn mousetrap-bind
  [binding callback]
  (.bind Mousetrap (clj->js binding) callback))

;; Binds a map of "binding" -> "callback" pairs using
;; the provided moustrap-instance bind method.
;; Returns a sequence with functions that unbind the bindings.
;; TODO: Allow to set if an event stops or not the propagation.
(defn mousetrap-bind-all
  [mousetrap-instance bindings]
  (let [unbind-fns (doall
                     (map
                       (fn [[binding callback]]
                         (let [binding-converted (clj->js binding)]
                           (.bind mousetrap-instance binding-converted
                             (fn [event]
                               (.stopPropagation event)
                               (callback event)))
                           #(.unbind mousetrap-instance binding-converted)))
                       bindings))]
    #(doall (map apply unbind-fns))))

(defn mousetrap
  [bindings _]
  (let [mousetrap-instance (r/atom nil)
        unbind-hotkeys (r/atom #())
        bind-hotkeys #(reset! unbind-hotkeys
                        (mousetrap-bind-all @mousetrap-instance bindings))]
    (r/create-class
      {:display-name
       "mousetrap"

       :component-did-mount
       bind-hotkeys

       :component-did-update
       (fn [this old-argv]
         (let [bindings (second (r/argv this))
               prev-bindings (second old-argv)]
           (when (not= bindings prev-bindings)
             (@unbind-hotkeys)
             (bind-hotkeys))))

       :reagent-render
       (fn [_ children]
         [:span
          {:ref #(reset! mousetrap-instance (new Mousetrap %))}
          children])})))



(defn not-editing
  []
  (nil? @(subscribe [:editing/uid])))


(defn selecting-items?
  []
  (not-empty @(subscribe [:selected/items])))


(defn maybe-undo-or-redo
  [operation]
  (when
    (or (not-editing)
      (selecting-items?))
    (dispatch [operation])))


;; Aliases (to integrate with re-frame later)
;; :athena-toggle        "mod+k"
;; :devtool-toggle        "mod+g"
;; :save                  "mod+s"
;; :10x-toggle            "mod+t"
;; :nav-back              "alt+left"
;; :nav-forward           "alt+right"
;; :undo                  "mod+z"
;; :redo                  "mod+shift+z")
;; :left-sidebar-toggle   "mod+\\"
;; :right-sidebar-toggle  "mod+shift+\\"
;; TODO: Integrate with re-frame
(mousetrap-bind-all Mousetrap
  {["up" "down"] #(prn "test")
   "mod+k"       #(dispatch [:athena/toggle])
   "mod+g"       #(dispatch [:devtool/toggle])
   "mod+s"       #(dispatch [:save])
   "mod+t"       util/toggle-10x
   "alt+left"    #(when (not-editing) (.back js/window.history))
   "alt+right"   #(when (not-editing) (.forward js/window.history))
   "mod+1"       #(dispatch [:left-sidebar/toggle])         ;; TODO: Change to "mod+\\"
   "mod+2"       #(dispatch [:right-sidebar/toggle])        ;; TODO: Change to "mod+shift+\\"
   "mod+z"       #(maybe-undo-or-redo :undo)
   "mod+shift+z" #(maybe-undo-or-redo :redo)
   "alt+d"       router/nav-daily-notes})

;; Multi block selection event handlers
;; They are currently "global", but they could be added
;; to a component in the top level of the app, that adds and removes
;; the bindings when there is a selection.
;; TODO: Decide if wrapping this section in a component. If not, move
;; it to an `init` function. (with the bindings above)
(defn selected-items
  []
  @(subscribe [:selected/items]))

;; Select first block of the seleciton and clear it.
(mousetrap-bind "enter"
  (fn []
    (when (selecting-items?)
      (dispatch [:editing/uid (first (selected-items))])
      (dispatch [:selected/clear-items]))))

;; Delete selected items.
(mousetrap-bind ["backspace" "del"]
  (fn []
    (when (selecting-items?)
      (dispatch [:selected/delete (selected-items)]))))

;; Indent selected items.
(mousetrap-bind "tab"
  (fn []
    (when (selecting-items?)
      (dispatch [:indent/multi (selected-items)]))))

;; Unindent selected items
(mousetrap-bind "shift+tab"
  (fn []
    (when (selecting-items?)
      (dispatch [:unindent/multi (selected-items)]))))

;; Add to the selection the block above
(mousetrap-bind "shift+up"
  (fn []
    (when (selecting-items?)
      (dispatch [:selected/up (selected-items)]))))

;; Add to the selection the block below
(mousetrap-bind "shift+down"
  (fn []
    (when (selecting-items?)
      (dispatch [:selected/down (selected-items)]))))

;; Select first block of the selection and clear it.
(mousetrap-bind "up"
  (fn [e]
    (when (selecting-items?)
      (.preventDefault e)
      (dispatch [:selected/clear-items])
      (dispatch [:up (first (selected-items)) e]))))

;; Select last element of the selection and clear it.
(mousetrap-bind "down"
  (fn [e]
    (.preventDefault e)
    (dispatch [:selected/clear-items])
    (dispatch [:down (last (selected-items)) e])))
