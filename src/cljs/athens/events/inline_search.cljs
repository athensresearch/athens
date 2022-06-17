(ns athens.events.inline-search
  "Inline Search Events"
  (:require
    [re-frame.core :as rf]))


(rf/reg-event-db
  ::set-type!
  (fn [db [_ uid type]]
    (assoc-in db [:inline-search uid :type] type)))


(rf/reg-event-db
  ::close!
  (fn [db [_ uid]]
    (assoc-in db [:inline-search uid :type] nil)))


(rf/reg-event-db
  ::set-index!
  (fn [db [_ uid index]]
    (assoc-in db [:inline-search uid :index] index)))


(rf/reg-event-db
  ::set-results!
  (fn [db [_ uid results]]
    (assoc-in db [:inline-search uid :results] results)))


(rf/reg-event-db
  ::clear-results!
  (fn [db [_ uid]]
    (assoc-in db [:inline-search uid :results] [])))


(rf/reg-event-db
  ::set-query!
  (fn [db [_ uid query]]
    (assoc-in db [:inline-search uid :query] query)))


(rf/reg-event-db
  ::clear-query!
  (fn [db [_ uid]]
    (assoc-in db [:inline-search uid :query] "")))
