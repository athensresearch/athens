(ns athens.subs.linked-refs
  (:require
    [re-frame.core :as rf]))


(rf/reg-sub
  ::open?
  (fn [db [_ uid]]
    (get-in db [:linked-ref uid] false)))
