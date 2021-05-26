(ns athens.views.find-in-page
  (:require
    [clojure.string :as str]
    [re-frame.core :as rf :refer [subscribe dispatch]]))


(def electron (js/require "electron"))

(defonce !find-window-id (atom nil))

(defonce !main-window-id (atom nil))


(defn window-id->window
  [window-id]
  (.. electron -remote -BrowserWindow (fromId window-id)))


;; --------------------------------------------------------------------
;; ---- find fn ---


(defn find-in-page!
  ([] (find-in-page! (:text @(subscribe [:find-in-page-info]))))
  ([text] (find-in-page! text false))
  ([text-to-search back?]
   (when (some-> text-to-search seq str/join)
     (let [opts (cond-> {:forward  true
                         :findNext false}

                  (= text-to-search
                     (:text @(subscribe [:find-in-page-info])))
                  (merge {:findNext true})

                  back? (merge {:forward false})
                  true clj->js)]

       (.. (window-id->window @!main-window-id)
           -webContents
           (findInPage text-to-search opts))))))


;; --------------------------------------------------------------------
;; ---- subs---


(rf/reg-sub
  :find-in-page-info
  (fn [db]
    (:find-in-page-info db)))


;; --------------------------------------------------------------------
;; ---- evts ---

(rf/reg-fx :find-in-page find-in-page!)


(rf/reg-event-fx
  :set-find-in-page-text
  (fn [{:keys [db]} [_ text]]
    (merge
      {:db           (assoc-in db [:find-in-page-info :text] text)
       :find-in-page text}
      (when (str/blank? text)
        {:clear-current-selection _}))))


(rf/reg-event-fx
  :next-result
  (fn [_ _] (find-in-page!)))


(rf/reg-fx
  :prev-result
  (fn []
    (find-in-page!
      (:text @(subscribe [:find-in-page-info])) true)))


;; --------------------------------------------------------------------
;; ---- messages from find window ---


(declare stop-find-in-page!)


(defonce __ipc-listener-main__
  (.. electron -remote -ipcMain
      (on "find-window->parent"
          (fn [_ msg]
            (let [[event & args] (js->clj msg)]
              (case (keyword event)
                :text (dispatch [:set-find-in-page-text (first args)])
                :close (stop-find-in-page!)
                :next (find-in-page!)

                :prev
                (find-in-page!
                  (:text @(subscribe [:find-in-page-info]))
                  true)))))))


;; --------------------------------------------------------------------
;; ---- life cycle ---


(defn send-msg-to-find-window!
  [& msg]
  (.. electron -ipcRenderer
      (send "parent->find-window"
            (-> msg vec clj->js))))


(defn get-find-win-pos
  ([] (get-find-win-pos
        (window-id->window @!main-window-id)))
  ([win]
   (let [[x y] (-> win (. getPosition) js->clj)]
     [(+ x 20)
      ;; + y 45(below toolbar) 25(title bar)
      (+ y 45 (when-not (. win isFullScreen) 25))])))


(defn init
  []
  (let [browser-win (.. electron -remote -BrowserWindow)
        win         (. browser-win getFocusedWindow)
        [x y] (get-find-win-pos win)]

    ;; find window setup
    (-> (new browser-win
             (clj->js
               {:modal          false
                :show           false
                :x              x
                :y              y

                :width          300
                :height         40

                :movable        false
                :resizable      false

                :frame          false
                :useContentSize true
                :skipTaskbar    true
                :hasShadow      true

                :minimizable    false
                :maximizable    false
                :closable       false
                :fullscreenable false

                :parent         win

                :webPreferences {:nodeIntegration       true
                                 :enableRemoteModule    true
                                 :nodeIntegrationWorker true
                                 :contextIsolation      false
                                 :webviewTag            true}}))
        (. -id)
        (#(reset! !find-window-id %)))

    (.loadURL
      ^js (window-id->window @!find-window-id)
      (str "file://" js/__dirname "/find-in-page.html"))

    ;; main window
    (reset! !main-window-id (. win -id))

    (.. (window-id->window @!main-window-id)
        (on "resize"
            (fn [_]
              (let [[x y] (get-find-win-pos)]
                (-> (window-id->window @!find-window-id)
                    (.. (setPosition x y)))))))))


(defn clear-current-selection
  []
  (.. (window-id->window @!main-window-id)
      -webContents
      (stopFindInPage "clearSelection")))


(rf/reg-fx :clear-current-selection clear-current-selection)


(defn stop-find-in-page!
  []
  (. (window-id->window @!find-window-id) hide)
  (clear-current-selection))


(defn start-find-in-page!
  []
  (if-let [find-win (window-id->window @!find-window-id)]
    (. find-win show)
    (init))
  (find-in-page!)
  (send-msg-to-find-window!
    :opened {:text     (:text @(subscribe [:find-in-page-info]))
             :is-dark? @(subscribe [:theme/dark])}))
