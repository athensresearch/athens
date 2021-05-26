(ns athens.hotkeys
  (:require
    [athens.router :as router]
    [athens.util :as util]
    ["mousetrap" :as Mousetrap]
    [re-frame.core :refer [dispatch subscribe]]
    [react]
    [reagent.core :as r]))

;; Binds a map of "binding" -> "callback" pairs using
;; the provided moustrap-instance bind method.
;; Returns a sequence with functions that unbind the bindings.
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


(defn selected-items-not-empty
  []
  (not-empty @(subscribe [:selected/items])))


(defn maybe-undo-or-redo
  [operation]
  (when
    (or (not-editing)
        (selected-items-not-empty))
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
