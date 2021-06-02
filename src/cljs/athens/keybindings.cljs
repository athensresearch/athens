(ns athens.keybindings
  (:require
    ["mousetrap" :as Mousetrap]
    ["mousetrap/plugins/record/mousetrap-record"]
    [athens.router :as router]
    [athens.util :as util]
    [re-frame.core :refer [dispatch subscribe]]
    [react]
    [reagent.core :as r]))

(defn mousetrap-record
  [callback]
  (.record Mousetrap callback))


;; Converts an hotkey alias (:athena/toggle) to a hotkey ("mod+k")
(defn convert-to-hotkey
  [alias-or-hotkey]
  (let [keymap @(subscribe [:keymap])]
    (if (keyword? alias-or-hotkey)
      (alias-or-hotkey keymap)
      (clj->js alias-or-hotkey))))


;; Binds a hotkey or hotkey alias to an event handler.
;; Returns a function to unbind the event.
(defn mousetrap-bind
  ([alias-or-hotkey callback & {:keys [stop-propagation? mousetrap-instance]
                                :or   {stop-propagation? true, mousetrap-instance Mousetrap}}]
   (let [keys (convert-to-hotkey alias-or-hotkey)]
     (.bind mousetrap-instance
            keys
            (fn [event]
              (when stop-propagation? (.stopPropagation event))
              (callback event)))
     #(.unbind mousetrap-instance keys))))


;; Same as mousetrap-bind but binds a map of keybindings (aliases-or-hotkeys and event handler pairs).
;; API is a bit different since it accepts the mousetrap-instance as the first argument.
;; and always stop the event propagation.
(defn mousetrap-bind-all
  [mousetrap-instance keybindings]
  (let [unbind-fns
        (doall
          (map
            (fn [[alias-or-hotkey callback-or-config]]
              (let [callback (if (map? callback-or-config)
                               (:callback callback-or-config)
                               callback-or-config)
                    stop-propagation? (if (map? callback-or-config)
                                        (:stop-propagation? callback-or-config)
                                        true)]
                (mousetrap-bind
                  alias-or-hotkey
                  callback
                  :stop-propagation? stop-propagation?
                  :mousetrap-instance mousetrap-instance)))
            keybindings))]
    #(doall (map apply unbind-fns))))


;; React component that listen binds event handlers to a local DOM element.
;; It also listen for changes on keymap to re-bind the event handlers.
;; Note: this is not meant to be used directly, use mousetrap instead.
(defn mousetrap-binder
  [_keymap keybindings _child]
  (let [mousetrap-instance (r/atom nil)
        unbind-all (r/atom #())
        bind (fn [keybindings]
               (reset! unbind-all
                       (mousetrap-bind-all @mousetrap-instance keybindings)))]

    (r/create-class
      {:display-name
       "mousetrap-binder"

       :component-did-mount
       #(bind keybindings)

       :component-will-unmount
       #(@unbind-all)

       :component-did-update
       (fn [this old-argv]
         (let [new-keymap (second (r/argv this))
               prev-keymap (second old-argv)
               keybindings (nth (r/argv this) 2)]
           (when (not= new-keymap prev-keymap)
             (@unbind-all)
             (bind keybindings))))

       :reagent-render
       (fn [_ _ child]
         [:span
          {:style {:display "contents"}
           :ref #(reset! mousetrap-instance (new Mousetrap %))}
          child])})))


;; Wrap what is passed as "child" into a DOM node that listen for hotkeys that
;; are bound to event handlers.
;; If the application keymap changes, this component will re-bind the event
;; handlers with the new hotkeys.
;; keybindings is a pair of aliases-or-hotkeys and event handler pairs
;; e.g: {:athena/toggle some-function1 "mod+z" some-function2}
(defn mousetrap
  [keybindings child]
  (let [keymap @(subscribe [:keymap])]
    [mousetrap-binder keymap keybindings child]))


;; Helpers to read from re-frame db
(defn not-editing?
  []
  (nil? @(subscribe [:editing/uid])))


(defn selecting-items?
  []
  (not-empty @(subscribe [:selected/items])))


(defn selected-items
  []
  @(subscribe [:selected/items]))


(defn dispatch-when-undo-or-redo-allowed
  [operation]
  (when
    (or (not-editing?)
        (selecting-items?))
    (dispatch [operation])))


(def changeable-global-keybindings
  {:athena/toggle        #(do (prn "Athena") (dispatch [:athena/toggle]))
   :10x/toggle           util/toggle-10x
   :nav/back             #(when (not-editing?) (.back js/window.history))
   :nav/forward          #(when (not-editing?) (.forward js/window.history))
   :left-sidebar/toggle  #(dispatch [:left-sidebar/toggle]) ; TODO: Change to "mod+\\"
   :right-sidebar/toggle #(dispatch [:right-sidebar/toggle]) ; TODO: Change to "mod+shift+\\"
   :nav/daily-notes      router/nav-daily-notes
   :nav/pages            #(router/navigate :pages)
   :nav/graph            #(router/navigate :graph)})


(defn bind-changeable-global-keybindings
  []
  (mousetrap-bind-all Mousetrap changeable-global-keybindings))

;; Global, unchangeable keybindings.
;; NOTE: This cannot use hotkey aliases (:athena/toggle) because
;; they are executed before the re-frame db is setup.
(defn init
  []
  ;; Overriding the stopCallback in Mousetrap prototype.
  ;; This allows to listen for events inside textareas.
  (set! (.. Mousetrap -prototype -stopCallback) (constantly false))

  (mousetrap-bind "mod+z" #(dispatch-when-undo-or-redo-allowed :undo))
  (mousetrap-bind "mod+shift+z" #(dispatch-when-undo-or-redo-allowed :redo))
  (mousetrap-bind "mod+s" #(dispatch [:save]))

  ;; Multi block selection event handlers
  ;; They are currently "global", but they could be added
  ;; to a component in the top level of the app, that adds and removes
  ;; the bindings when there is a selection.
  ;; it to an `init` function. (with the bindings above)

  ;; Select first block of the seleciton and clear it.
  (mousetrap-bind "enter"
                  (fn []
                    (when (selecting-items?)
                      (dispatch [:editing/uid (first (selected-items))])
                      (dispatch [:selected/clear-items])))
                  :stop-propagation? false)

  ;; Delete selected items.
  (mousetrap-bind ["backspace" "del"]
                  (fn []
                    (when (selecting-items?)
                      (dispatch [:selected/delete (selected-items)])))
                  :stop-propagation? false)

  ;; Indent selected items.
  (mousetrap-bind "tab"
                  (fn []
                    (when (selecting-items?)
                      (dispatch [:indent/multi (selected-items)])))
                  :stop-propagation? false)

  ;; Unindent selected items
  (mousetrap-bind "shift+tab"
                  (fn []
                    (when (selecting-items?)
                      (dispatch [:unindent/multi (selected-items)])))
                  :stop-propagation? false)

  ;; Add to the selection the block above
  (mousetrap-bind "shift+up"
                  (fn []
                    (when (selecting-items?)
                      (dispatch [:selected/up (selected-items)])))
                  :stop-propagation? false)

  ;; Add to the selection the block below
  (mousetrap-bind "shift+down"
                  (fn []
                    (when (selecting-items?)
                      (dispatch [:selected/down (selected-items)])))
                  :stop-propagation? false)

  ;; Select first block of the selection and clear it.
  (mousetrap-bind "up"
                  (fn [e]
                    (when (selecting-items?)
                      (.preventDefault e)
                      (dispatch [:selected/clear-items])
                      (dispatch [:up (first (selected-items)) e])))
                  :stop-propagation? false)

  ;; Select last element of the selection and clear it.
  (mousetrap-bind "down"
                  (fn [e]
                    (when (selecting-items?)
                      (.preventDefault e)
                      (dispatch [:selected/clear-items])
                      (dispatch [:down (last (selected-items)) e])))
                  :stop-propagation? false))
