(ns athens.events.linked-refs
  (:require [re-frame.core :as rf]))


(rf/reg-event-db
  ::set-open!
  (fn [db [_ uid open?]]
    #_(println ::set-open! (pr-str uid) (pr-str open?))
    (assoc-in db [:linked-ref uid] open?)))


(rf/reg-event-db
  ::toggle-open!
  (fn [db [_ uid]]
    #_(println ::toggle-open! (pr-str uid))
    (update-in db [:linked-ref uid] (fnil not false))))
