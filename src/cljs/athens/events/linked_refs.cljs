(ns athens.events.linked-refs
  (:require
    [re-frame.core :as rf]))


(rf/reg-event-db
  ::cleanup!
  (fn [db [_ uid]]
    (update db :linked-ref dissoc uid)))


(rf/reg-event-db
  ::set-open!
  (fn [db [_ uid open?]]
    (assoc-in db [:linked-ref uid] open?)))


(rf/reg-event-db
  ::toggle-open!
  (fn [db [_ uid]]
    (update-in db [:linked-ref uid] (fnil not false))))
