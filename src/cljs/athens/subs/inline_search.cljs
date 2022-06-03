(ns athens.subs.inline-search
  (:require
    [re-frame.core :as rf]))


(rf/reg-sub
  ::type
  (fn [db _]
    (let [val (get-in db [:inline-search :type])]
      (println ::type (pr-str val))
      val)))


(rf/reg-sub
  ::index
  (fn [db _]
    (let [val (get-in db [:inline-search :index])]
      (println ::index (pr-str val))
      val)))


(rf/reg-sub
  ::results
  (fn [db _]
    (let [val (get-in db [:inline-search :results])]
      (println ::results (pr-str val))
      val)))


(rf/reg-sub
  ::query
  (fn [db _]
    (let [val (get-in db [:inline-search :query])]
      (println ::query (pr-str val))
      val)))
