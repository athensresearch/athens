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
  (fn [db _]
    (:athena db)))


(re-frame/reg-sub
  :merge-prompt
  (fn [db _]
    (:merge-prompt db)))
