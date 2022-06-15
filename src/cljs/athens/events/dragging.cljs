(ns athens.events.dragging
  (:require
    [re-frame.core :as rf]))


(rf/reg-event-db
  ::cleanup!
  (fn [db [_ uid]]
    (update db :dragging dissoc uid)))


(rf/reg-event-db
  ::set-dragging!
  (fn [db [_ uid dragging?]]
    (assoc-in db [:dragging uid :dragging?] dragging?)))


(rf/reg-event-db
  ::set-drag-target!
  (fn [db [_ uid drag-target]]
    (assoc-in db [:dragging uid :drag-target] drag-target)))
