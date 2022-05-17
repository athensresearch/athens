(ns athens.electron.window
  (:require
    [athens.electron.utils :as electron.utils]
    [athens.style :refer [zoom-level-min zoom-level-max]]
    [re-frame.core :as rf]))


(rf/reg-event-db
  :zoom/in
  (fn [db _]
    (update db :zoom-level #(min (inc %) zoom-level-max))))


(rf/reg-event-db
  :zoom/out
  (fn [db _]
    (update db :zoom-level #(max (dec %) zoom-level-min))))


(rf/reg-event-db
  :zoom/set
  (fn [db [_ level]]
    (assoc db :zoom-level level)))


(rf/reg-event-db
  :zoom/reset
  (fn [db _]
    (assoc db :zoom-level 0)))


(rf/reg-event-fx
  :toggle-max-min-win
  (fn [_ [_ toggle-min?]]
    {:invoke-win! {:channel (:toggle-max-or-min-win-channel electron.utils/ipcMainChannels)
                   :arg (clj->js toggle-min?)}}))


(rf/reg-event-fx
  :minimize-win
  (fn [_ _]
    {:invoke-win! {:channel (:toggle-max-or-min-win-channel electron.utils/ipcMainChannels)
                   :arg (clj->js true)}}))


(rf/reg-event-fx
  :bind-win-listeners
  (fn [_ _]
    {:bind-win-listeners! {}}))


(rf/reg-event-fx
  :exit-fullscreen-win
  (fn [_ _]
    {:invoke-win! {:channel (:exit-fullscreen-win-channel electron.utils/ipcMainChannels)}}))


(rf/reg-event-fx
  :close-win
  (fn [_ _]
    {:invoke-win! {:channel (:close-win-channel electron.utils/ipcMainChannels)}}))


(rf/reg-event-db
  :toggle-win-maximized
  (fn [db [_ maximized?]]
    (assoc db :win-maximized? maximized?)))


(rf/reg-event-db
  :toggle-win-fullscreen
  (fn [db [_ fullscreen?]]
    (assoc db :win-fullscreen? fullscreen?)))


(rf/reg-event-db
  :toggle-win-focused
  (fn [db [_ focused?]]
    (assoc db :win-focused? focused?)))


(rf/reg-sub
  :win-maximized?
  (fn [db _]
    (:win-maximized? db)))


(rf/reg-sub
  :win-fullscreen?
  (fn [db _]
    (:win-fullscreen? db)))


(rf/reg-sub
  :win-focused?
  (fn [db _]
    (:win-focused? db)))


(rf/reg-fx
  :invoke-win!
  (fn [{:keys [channel arg]} _]
    (if arg
      (.. (electron.utils/ipcRenderer) (invoke channel arg))
      (.. (electron.utils/ipcRenderer) (invoke channel)))))


(rf/reg-fx
  :close-win!
  (fn []
    (let [window (.. (electron.utils/electron) -BrowserWindow getFocusedWindow)]
      (.close window))))


(rf/reg-fx
  :bind-win-listeners!
  (fn []
    (let [active-win (.getCurrentWindow (electron.utils/remote))]
      (doto ^js/BrowserWindow active-win
        (.on "maximize" #(rf/dispatch-sync [:toggle-win-maximized true]))
        (.on "unmaximize" #(rf/dispatch-sync [:toggle-win-maximized false]))
        (.on "blur" #(rf/dispatch-sync [:toggle-win-focused false]))
        (.on "focus" #(rf/dispatch-sync [:toggle-win-focused true]))
        (.on "enter-full-screen" #(rf/dispatch-sync [:toggle-win-fullscreen true]))
        (.on "leave-full-screen" #(rf/dispatch-sync [:toggle-win-fullscreen false]))))))
