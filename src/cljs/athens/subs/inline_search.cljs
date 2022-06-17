(ns athens.subs.inline-search
  (:require
    [re-frame.core :as rf]))


(rf/reg-sub
  ::type
  (fn [db [_ uid]]
    (get-in db [:inline-search uid :type])))


(rf/reg-sub
  ::index
  (fn [db [_ uid]]
    (get-in db [:inline-search uid :index])))


(rf/reg-sub
  ::results
  (fn [db [_ uid]]
    (get-in db [:inline-search uid :results])))


(rf/reg-sub
  ::query
  (fn [db [_ uid]]
    (get-in db [:inline-search uid :query])))
