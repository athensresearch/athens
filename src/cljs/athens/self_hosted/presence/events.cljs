(ns athens.self-hosted.presence.events
  (:require [re-frame.core :as rf]
            [athens.self-hosted.presence.utils :as utils]))


(rf/reg-event-db
  :presence/all-online
  (fn [db [_ users]]
    (assoc-in db [:presence :users] users)))


;; TODO: what happens if existing user? overrides
(rf/reg-event-db
  :presence/add-user
  (fn [db [_ user]]
    (let [user (merge user {:color (rand-nth utils/PALETTE)})]
      ;; TODO: make sure usernames are unique
      (update-in db [:presence :users]
                 assoc (:username user) user))))


(rf/reg-event-db
  :presence/remove-user
  (fn [db [_ user]]
    (update-in db [:presence :users] dissoc (:username user))))


(rf/reg-event-db
  :presence/update-editing
  (fn [db [_ {:keys [username block/uid]}]]
    (update-in db [:presence :users username] assoc :block/uid uid)))

