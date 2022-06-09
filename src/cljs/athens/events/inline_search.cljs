(ns athens.events.inline-search
  "Inline Search Events"
  (:require
    [re-frame.core :as rf]))


(rf/reg-event-db
  ::set-type!
  (fn [db [_ uid type]]
    (println ::set-type! (pr-str type))
    (assoc-in db [:inline-search uid :type] type)))


(rf/reg-event-db
  ::close!
  (fn [db [_ uid]]
    (println ::close! uid)
    (assoc-in db [:inline-search uid :type] nil)))


(rf/reg-event-db
  ::set-index!
  (fn [db [_ uid index]]
    (println ::set-index! (pr-str index))
    (assoc-in db [:inline-search uid :index] index)))


(rf/reg-event-db
  ::set-results!
  (fn [db [_ uid results]]
    (println ::set-results! (pr-str results))
    (assoc-in db [:inline-search uid :results] results)))


(rf/reg-event-db
  ::clear-results!
  (fn [db [_ uid]]
    (println ::clear-results!)
    (assoc-in db [:inline-search uid :results] [])))


(rf/reg-event-db
  ::set-query!
  (fn [db [_ uid query]]
    (println ::set-query! (pr-str query))
    (assoc-in db [:inline-search uid :query] query)))


(rf/reg-event-db
  ::clear-query!
  (fn [db [_ uid]]
    (println ::clear-query!)
    (assoc-in db [:inline-search uid :query] "")))
