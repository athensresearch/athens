^:cljstyle/ignore
(ns
  ^{:doc
    "This sits runs inside main window.
    The actual find in page on screen is a new window altogether
    It follows the main window around and adjusts position on resize

    ctrl/cmd-f and escape - open and close find window
    note: there's an escape listener inside the window as well

    Actions/events to and from inside the window are communicated
    with ipc"}
  athens.views.find-in-page
  (:require
    [clojure.string :as str]
    [re-frame.core :as rf :refer [subscribe dispatch]]))


(def electron (js/require "electron"))

(defonce !find-browser-view (atom nil))

(defonce !main-window-id (atom nil))


(defn window-id->window
  "Window id is more reliable as window object can go out of scope
   resulting in bad states"
  [window-id]
  (.. electron -remote -BrowserWindow (fromId window-id)))


;; --------------------------------------------------------------------
;; ---- find fn ---


(defn find-in-page!
  "Uses webContents findInPage(browser based highlighting.
   Handles back and forward search"
  ([] (find-in-page! (:text @(subscribe [::find-in-page-info]))))
  ([text] (find-in-page! text false))
  ([text-to-search back?]
   (when (some-> text-to-search seq str/join)
     (let [opts (cond-> {:forward  true
                         :findNext false}

                  (= text-to-search
                     (:text @(subscribe [::find-in-page-info])))
                  (merge {:findNext true})

                  back? (merge {:forward false})
                  true clj->js)]

       (.. (window-id->window @!main-window-id)
           -webContents
           (findInPage text-to-search opts))))))


;; --------------------------------------------------------------------
;; ---- subs---


(rf/reg-sub
  ::find-in-page-info
  (fn [db]
    (:find-in-page-info db)))


;; --------------------------------------------------------------------
;; ---- evts ---

(rf/reg-fx ::find-in-page find-in-page!)


(rf/reg-event-fx
  ::set-find-in-page-text
  (fn [{:keys [db]} [_ text]]
    (merge
      {:db            (assoc-in db [:find-in-page-info :text] text)
       ::find-in-page text}
      (when (str/blank? text)
        {::clear-current-selection _}))))


;; --------------------------------------------------------------------
;; ---- messages from find window ---


(declare stop-find-in-page!)


;; listens to messages from find-window
(defonce __ipc-listener-main__
  (.. electron -remote -ipcMain
      (on "find-window->parent"
          (fn [_ msg]
            (let [[event & args] (js->clj msg)]
              (case (keyword event)
                :text (dispatch [::set-find-in-page-text (first args)])
                :close (stop-find-in-page!)
                :next (find-in-page!)

                :prev
                (find-in-page!
                  (:text @(subscribe [::find-in-page-info]))
                  true)))))))


;; --------------------------------------------------------------------
;; ---- life cycle ---


(defn set-find-window!
  []
  (.. (window-id->window @!main-window-id)
      (setBrowserView @!find-browser-view)))


(defn remove-find-window!
  []
  (.. (window-id->window @!main-window-id)
      (removeBrowserView @!find-browser-view)))


(defn send-msg-to-find-window!
  [& msg]
  (.. electron -ipcRenderer
      (send "parent->find-window"
            (-> msg vec clj->js))))


(defn init!
  []
  (let [browser-view (.. electron -remote -BrowserView)
        browser-win  (.. electron -remote -BrowserWindow)
        win          (. browser-win getFocusedWindow)]

    (reset! !main-window-id (. win -id))

    ;; find window setup
    (reset! !find-browser-view
            (new browser-view
                 (clj->js
                   {:webPreferences {:nodeIntegration       true
                                     :enableRemoteModule    true
                                     :nodeIntegrationWorker true
                                     :contextIsolation      false
                                     :webviewTag            true}})))

    ;; add to main and wait for basic things to load then remove
    (set-find-window!)

    (.. @!find-browser-view
        (setBounds (clj->js {:x 20 :y 50
                             :height 42 :width 350})))

    (.. @!find-browser-view -webContents
        (loadURL
          (str "file://" js/__dirname "/find-in-page.html")))

    #_;; debug find-window
    (.. @!find-browser-view -webContents openDevTools)

    ;; remove -> hide
    (remove-find-window!)

    ;; send index (eg 4/10) to find window
    ;; it's inconsequential so directly send to find window
    (.. (window-id->window @!main-window-id)
        -webContents
        (on "found-in-page"
            (fn [_ result]
              (let [{:keys [activeMatchOrdinal matches]}
                    (js->clj result :keywordize-keys true)]
                (send-msg-to-find-window!
                  :search-result-index
                  {:curr  activeMatchOrdinal
                   :total matches})))))))


(defn clear-current-selection
  "Clear all highlights on window"
  []
  (.. (window-id->window @!main-window-id)
      -webContents
      (stopFindInPage "clearSelection")))


(rf/reg-fx ::clear-current-selection clear-current-selection)


(defn stop-find-in-page!
  []
  (remove-find-window!)
  (.. (window-id->window @!main-window-id)
      -webContents focus)
  (clear-current-selection))


(defn destroy-find-in-page!
  []
  (remove-find-window!)
  (. @!find-browser-view destroy))


(defn start-find-in-page!
  "Start window if not started.
   Search highlight also starts when window is open
   When opened send message to window with text and current theme"
  []
  (if @!find-browser-view
    (set-find-window!)
    (init!))
  (find-in-page!)
  (.. @!find-browser-view -webContents focus)
  (send-msg-to-find-window!
    :opened {:text     (:text @(subscribe [::find-in-page-info]))
             :is-dark? @(subscribe [:theme/dark])}))
