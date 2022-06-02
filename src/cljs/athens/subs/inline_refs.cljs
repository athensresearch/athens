(ns athens.subs.inline-refs
  (:require [re-frame.core :as rf]))


(rf/reg-sub
  ::open?
  (fn [db [_ uid]]
    (let [val (get-in db [:inline-refs uid :open?] false)]
      (println ::open? (pr-str uid) (pr-str val))
      val)))
