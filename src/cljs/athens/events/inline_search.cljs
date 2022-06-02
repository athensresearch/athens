(ns athens.events.inline-search
  "Inline Search Events"
  (:require [re-frame.core :as rf]))


(rf/reg-event-db
  ::set-type!
  (fn [db [_ type]]
    (println ::set-type! (pr-str type))
    (assoc-in db [:inline-search :type] type)))


(rf/reg-event-db
  ::close!
  (fn [db _]
    (println ::close!)
    (assoc-in db [:inline-search :type] nil)))


(rf/reg-event-db
  ::set-index!
  (fn [db [_ index]]
    (println ::set-index! (pr-str index))
    (assoc-in db [:inline-search :index] index)))


(rf/reg-event-db
  ::set-results!
  (fn [db [_ results]]
    (println ::set-results! (pr-str results))
    (assoc-in db [:inline-search :results] results)))


(rf/reg-event-db
  ::clear-results!
  (fn [db _]
    (println ::clear-results!)
    (assoc-in db [:inline-search :results] [])))


(rf/reg-event-db
  ::set-query!
  (fn [db [_ query]]
    (println ::set-query! (pr-str query))
    (assoc-in db [:inline-search :query] query)))


(rf/reg-event-db
  ::clear-query!
  (fn [db _]
    (println ::clear-query!)
    (assoc-in db [:inline-search :query] "")))
