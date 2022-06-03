(ns athens.events.inline-refs
  (:require
    [re-frame.core :as rf]))


(rf/reg-event-db
  ::cleanup!
  (fn [db [_ uid]]
    (println ::cleanup! (pr-str uid))
    (update db :inline-refs dissoc uid)))


(rf/reg-event-db
  ::set-open!
  (fn [db [_ uid open?]]
    (println ::set-open! (pr-str uid) (pr-str open?))
    (assoc-in db [:inline-refs uid :open?] open?)))


(rf/reg-event-db
  ::toggle-open!
  (fn [db [_ uid]]
    (println ::toggle-open! (pr-str uid))
    (update-in db [:inline-refs uid :open?] not)))


(rf/reg-event-db
  ::set-state!
  (fn [db [_ uid state]]
    (println ::set-state! (pr-str uid) (pr-str state))
    (assoc-in db [:inline-refs uid :state] state)))


(rf/reg-event-db
  ::toggle-state-open!
  (fn [db [_ uid]]
    (println ::toggle-state-open! (pr-str uid))
    (update-in db [:inline-refs uid :state :open?] not)))


(rf/reg-event-db
  ::set-block!
  (fn [db [_ uid block]]
    (println ::set-block! (pr-str uid) (pr-str block))
    (assoc-in db [:inline-refs uid :state :block] block)))


(rf/reg-event-db
  ::set-parents!
  (fn [db [_ uid parents]]
    (println ::set-parents! (pr-str uid) (pr-str parents))
    (assoc-in db [:inline-refs uid :state :parents] parents)))


(rf/reg-event-db
  ::set-focus!
  (fn [db [_ uid focus?]]
    (println ::set-focus! (pr-str uid) (pr-str focus?))
    (assoc-in db [:inline-refs uid :state :focus?] focus?)))

