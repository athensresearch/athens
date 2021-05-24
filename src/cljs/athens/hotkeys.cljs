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
  (doall
    (map
      (fn [[binding callback]]
        (.bind mousetrap-instance binding
               (fn [event]
                 (.stopPropagation event)
                 (callback event)))
        #(.unbind mousetrap-instance binding))
      bindings)))


;; Not really sure if this does the trick.
;; The idea is that it returns the same JS array
;; for the same cljs vector (using = for equality)
(def deps (memoize clj->js))

;; Ugly but working mousetrap version using hooks.
(defn mousetrap
  [propsjs]
  (let [mouse-trap (react/useRef)
        props (js->clj propsjs)]
    (react/useEffect
      (fn []
        (let [m (.-current mouse-trap)
              bindings (get props "bindings")
              cleanup-fns (mousetrap-bind-all m bindings)]
          #(doall
             (map apply cleanup-fns))))
      (deps [(get props "bindings")]))
    (react/createElement "span"
                         #js{:ref #(set! (.-current mouse-trap) (new Mousetrap %))}
                         (.-children propsjs))))


(def modkey
  (let [os (util/get-os)]
    (or (and (= os :mac) "meta")
        (and (= os :windows) "ctrl")
        (and (= os :linux) "ctrl"))))

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
;; :nav-daily-notes       "alt+d"
;; :undo                  "mod+z"
;; :redo                  "mod+shift+z")
;; :left-sidebar-toggle   "mod+\\"
;; :right-sidebar-toggle  "mod+shift+\\"
;; TODO: Integrate with re-frame
(mousetrap-bind-all Mousetrap
                    {"mod+k"       #(dispatch [:athena/toggle])
                     "mod+g"       #(dispatch [:devtool/toggle])
                     "mod+s"       #(dispatch [:save])
                     "mod+t"       util/toggle-10x
                     "alt+left"    #(when (not-editing) (.back js/window.history))
                     "alt+right"   #(when (not-editing) (.forward js/window.history))
                     "mod+1"       #(dispatch [:left-sidebar/toggle]) ;; TODO: Change to "mod+\\"
                     "mod+2"       #(dispatch [:right-sidebar/toggle]) ;; TODO: Change to "mod+shift+\\"
                     "mod+z"       #(maybe-undo-or-redo :undo)
                     "mod+shift+z" #(maybe-undo-or-redo :redo)
                     "alt+d"       router/nav-daily-notes})

;; Maybe change name or remove
;; TODO: Remove this
(defn hotkeys
  [children]
  [:span children])
