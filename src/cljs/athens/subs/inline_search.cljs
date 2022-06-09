(ns athens.subs.inline-search
  (:require
    [re-frame.core :as rf]))


(rf/reg-sub
  ::type
  (fn [db [_ uid]]
    (let [val (get-in db [:inline-search uid :type])]
      (println ::type (pr-str val))
      val)))


(rf/reg-sub
  ::index
  (fn [db [_ uid]]
    (let [val (get-in db [:inline-search uid :index])]
      (println ::index (pr-str val))
      val)))


(rf/reg-sub
  ::results
  (fn [db [_ uid]]
    (let [val (get-in db [:inline-search uid :results])]
      (println ::results (pr-str val))
      val)))


(rf/reg-sub
  ::query
  (fn [db [_ uid]]
    (let [val (get-in db [:inline-search uid :query])]
      (println ::query (pr-str val))
      val)))
