(ns athens.events.dragging
  (:require [re-frame.core :as rf]))


(rf/reg-event-db
  ::cleanup!
  (fn [db [_ uid]]
    (println ::cleanup! (pr-str uid))
    (update db :dragging dissoc uid)))


(rf/reg-event-db
  ::set-dragging!
  (fn [db [_ uid dragging?]]
    (println ::set-dragging! (pr-str uid) (pr-str dragging?))
    (assoc-in db [:dragging uid :dragging?] dragging?)))


(rf/reg-event-db
  ::set-drag-target!
  (fn [db [_ uid drag-target]]
    (println ::set-drag-target! (pr-str uid) (pr-str drag-target))
    (assoc-in db [:dragging uid :drag-target] drag-target)))
