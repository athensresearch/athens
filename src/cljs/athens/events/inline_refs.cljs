(ns athens.events.inline-refs
  (:require
    [re-frame.core :as rf]))


(rf/reg-event-db
  ::cleanup!
  (fn [db [_ uid]]
    (update db :inline-refs dissoc uid)))


(rf/reg-event-db
  ::set-open!
  (fn [db [_ uid open?]]
    (assoc-in db [:inline-refs uid :open?] open?)))


(rf/reg-event-db
  ::toggle-open!
  (fn [db [_ uid]]
    (update-in db [:inline-refs uid :open?] not)))


(rf/reg-event-db
  ::set-state!
  (fn [db [_ uid state]]
    (assoc-in db [:inline-refs uid :state] state)))


(rf/reg-event-db
  ::toggle-state-open!
  (fn [db [_ uid]]
    (update-in db [:inline-refs uid :state :open?] not)))


(rf/reg-event-db
  ::set-block!
  (fn [db [_ uid block]]
    (assoc-in db [:inline-refs uid :state :block] block)))


(rf/reg-event-db
  ::set-parents!
  (fn [db [_ uid parents]]
    (assoc-in db [:inline-refs uid :state :parents] parents)))


(rf/reg-event-db
  ::set-focus!
  (fn [db [_ uid focus?]]
    (assoc-in db [:inline-refs uid :state :focus?] focus?)))

