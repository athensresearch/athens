(ns athens.subs.linked-refs
  (:require
    [re-frame.core :as rf]))


(rf/reg-sub
  ::open?
  (fn [db [_ uid]]
    (let [val (get-in db [:linked-ref uid] false)]
      #_(println ::open? (pr-str uid) (pr-str val))
      val)))
