(ns athens.events.inline-refs
  (:require [re-frame.core :as rf]))


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
