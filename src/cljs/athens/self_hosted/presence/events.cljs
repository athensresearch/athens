(ns athens.self-hosted.presence.events
  (:require
    [athens.common.logging :as log]
    [re-frame.core :as rf]))


(rf/reg-event-db
  :presence/add-session-id
  (fn [db [_ session-id]]
    (assoc-in db [:presence :session-id] session-id)))


(rf/reg-event-fx
  :presence/all-online
  (fn [_ [_ users]]
    {:fx [[:dispatch-n (mapv (fn [user-map]
                               [:presence/add-user user-map])
                             users)]]}))


(rf/reg-event-db
  :presence/add-user
  (fn [db [_ {:keys [session-id] :as user}]]
    (assoc-in db [:presence :users session-id] user)))


(rf/reg-event-db
  :presence/remove-user
  (fn [db [_ {:keys [session-id]}]]
    (update-in db [:presence :users] dissoc session-id)))


(rf/reg-event-db
  :presence/update
  (fn [db [_ {:keys [session-id] :as session}]]
    (if (get-in db [:presence :users session-id])
      (update-in db [:presence :users session-id] merge session)
      (do (log/warn "No matching session-id for update" session)
          db))))


(rf/reg-event-db
  :presence/clear
  (fn [db _]
    (dissoc db :presence)))


(rf/reg-event-fx
  :presence/send-update
  (fn [{:keys [db]} [_ m]]
    {;; Optimistically update own presence to not have weird delay.
     :dispatch-n [[:presence/update (merge m {:session-id (get-in db [:presence :session-id])})]
                  [:success-self-presence-updated]]
     :fx [[:presence/send-update-fx m]]}))
