(ns athens.hotkeys
  (:require
    ["react-hotkeys" :refer [GlobalHotKeys HotKeys configure]]
    [re-frame.core :refer [dispatch subscribe]]
    [athens.util :as util]
    [athens.router :as router]))

(configure #js{:ignoreTags #js[]})

(def modkey
  (let [os (util/get-os)]
    (or (and (= os :mac) "meta")
      (and (= os :windows) "ctrl")
      (and (= os :linux) "ctrl"))))

(defn with-mod
  [combination]
  (str modkey "+" combination))

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

(defn global-hotkeys
  []
  [:> GlobalHotKeys
   {:keyMap   {:athena-toggle        (with-mod "k")
               :devtool-toggle       (with-mod "g")
               :save                 (with-mod "s")
               :10x-toggle           (with-mod "t")
               :nav-back             "alt+left"
               :nav-forward          "alt+right"
               :nav-daily-notes      "alt+d"
               :undo                 (with-mod "z")
               :redo                 (with-mod "shift+z")
               :left-sidebar-toggle  (with-mod "1")         ;; TODO: Change to "\\"
               :right-sidebar-toggle (with-mod "2")}        ;; TODO: Change to "shift+\\"

    :handlers {:athena-toggle        #(dispatch [:athena/toggle])
               :devtool-toggle       #(dispatch [:devtool/toggle])
               :save                 #(dispatch [:save])
               :10x-toggle           util/toggle-10x
               :nav-back             #(when (not-editing) (.back js/window.history))
               :nav-forward          #(when (not-editing) (.forward js/window.history))
               :left-sidebar-toggle  #(dispatch [:left-sidebar/toggle])
               :right-sidebar-toggle #(dispatch [:right-sidebar/toggle])
               :undo                 #(maybe-undo-or-redo :undo)
               :redo                 #(maybe-undo-or-redo :redo)
               :nav-daily-notes      router/nav-daily-notes}}])

;; For some reason, input elements events
;; don't work unless the app is wrapped in a root hotkey.
(defn hotkeys-keymap
  [children]
  [:> HotKeys
   {:root   true
    :keyMap {:content-bold (with-mod "b")
             :content-italic (with-mod "i")
             :content-strikethrough (with-mod "y")
             :content-dashed (with-mod "--")
             :content-highlight (with-mod "h")}}
   children])

;; Maybe change name or remove
(defn hotkeys
  [children]
  [:<>
   [global-hotkeys]
   [hotkeys-keymap children]])
