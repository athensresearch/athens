(ns athens.keybindings
  (:require
    [athens.router :as router]
    [athens.util :as util]
    ["mousetrap" :as Mousetrap]
    ["mousetrap/plugins/record/mousetrap-record"]
    [re-frame.core :refer [dispatch subscribe]]
    [react]
    [reagent.core :as r]))

(defn mousetrap-record
  [callback]
  (.record Mousetrap callback))

(defn convert-to-keys
  [keys-or-alias]
  (let [keymap @(subscribe [:keymap])]
    (if (keyword? keys-or-alias)
      (keys-or-alias keymap)
      (clj->js keys-or-alias))))

;; Binds an event in the global Moutrap instance.
(defn mousetrap-bind
  ([keys-or-alias callback & {:keys [stop-propagation? mousetrap-instance]
                              :or   {stop-propagation? true, mousetrap-instance Mousetrap}}]
   (let [keys (convert-to-keys keys-or-alias)]
     (.bind mousetrap-instance
            keys
            (fn [event]
              (when stop-propagation? (.stopPropagation event))
              (callback event)))
     #(.unbind mousetrap-instance keys))))


(defn mousetrap-bind-all
  [mousetrap-instance keybindings]
  (let [unbind-fns
        (doall
          (map
            (fn [[keys callback]]
              (mousetrap-bind
                keys
                callback
                :stop-propation true
                :mousetrap-instance mousetrap-instance))
            keybindings))]
    #(doall (map apply unbind-fns))))


(defn mousetrap-binder
  [_keymap bindings _children]
  (let [mousetrap-instance (r/atom nil)
        unbind-all (r/atom #())
        bind (fn [bindings]
               (reset! unbind-all
                       (mousetrap-bind-all @mousetrap-instance bindings)))]

    (r/create-class
      {:display-name
       "mousetrap"

       :component-did-mount
       #(bind bindings)

       :component-will-unmount
       #(@unbind-all)

       :component-did-update
       (fn [this old-argv]
         (let [new-keymap (second (r/argv this))
               prev-keymap (second old-argv)
               bindings (nth (r/argv this) 2)]
           (when (not= new-keymap prev-keymap)
             (prn "KEYMAP CHANGED" new-keymap)
             (@unbind-all)
             (bind bindings))))

       :reagent-render
       (fn [_ _ children]
         [:span
          {:style {:display "contents"}
           :ref #(reset! mousetrap-instance (new Mousetrap %))}
          children])})))


(defn mousetrap
  [bindings children]
  (let [keymap @(subscribe [:keymap])]
    [mousetrap-binder keymap bindings children]))


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


(def changeable-global-keybindings
  {:devtool/toggle       #(dispatch [:devtool/toggle])
   :athena/toggle        #(dispatch [:athena/toggle])
   :10x/toggle           util/toggle-10x
   :nav/back             #(when (not-editing) (.back js/window.history))
   :nav/forward          #(when (not-editing) (.forward js/window.history))
   :left-sidebar/toggle  #(dispatch [:left-sidebar/toggle]) ;; TODO: Change to "mod+\\"
   :right-sidebar/toggle #(dispatch [:right-sidebar/toggle]) ;; TODO: Change to "mod+shift+\\"
   :nav/daily-notes      router/nav-daily-notes})


(defn bind-changeable-global-keybindings
  []
  (mousetrap-bind-all Mousetrap changeable-global-keybindings))


(defn selected-items
  []
  @(subscribe [:selected/items]))

;; Global, unchangeable keybindings
(defn init
  []
  (mousetrap-bind "mod+z" #(maybe-undo-or-redo :undo))
  (mousetrap-bind "mod+shift+z" #(maybe-undo-or-redo :redo))
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
