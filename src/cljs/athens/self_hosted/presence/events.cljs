(ns athens.self-hosted.presence.events
  (:require [re-frame.core :as rf]))


(rf/reg-event-db
  :presence/all-online
  (fn [db [_ users]]
    (assoc db :presence/users users)))


(rf/reg-event-db
  :presence/add-user
  (fn [db [_ user]]
    (update db :presence/users conj user)))


(rf/reg-event-db
  :presence/remove-user
  (fn [db [_ user]]
    (update db :presence/users (fn [users]
                                 (filterv
                                   (fn [{username :username}]
                                     (not= username (:username user)))
                                   users)))))

