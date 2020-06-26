(ns athens.subs
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [re-frame.core :as re-frame]))


(re-frame/reg-sub
  :user
  (fn [db _]
    (:user db)))


(re-frame/reg-sub
  :app-db
  (fn [db _]
    db))


(re-frame/reg-sub
  :errors
  (fn [db _]
    (:errors db)))


(re-frame/reg-sub
  :loading
  (fn [db _]
    (:loading db)))


(re-frame/reg-sub
  :athena
  (fn-traced [db _]
             (:athena db)))


(re-frame/reg-sub
  :devtool
  (fn-traced [db _]
             (:devtool db)))


(re-frame/reg-sub
  :merge-prompt
  (fn [db _]
    (:merge-prompt db)))


(re-frame/reg-sub
  :editing-uid
  (fn-traced [db _]
             (:editing-uid db)))


(re-frame/reg-sub
  :tooltip-uid
  (fn-traced [db _]
             (:tooltip-uid db)))


(re-frame/reg-sub
  :drag-bullet
  (fn-traced [db _]
             (:drag-bullet db)))
