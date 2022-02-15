(ns athens.events.sentry
  (:require
    [athens.utils.sentry :as sentry]
    [re-frame.core       :as rf]))


(rf/reg-event-fx
  :sentry/end-tx
  (fn [_ [_ tx]]
    (sentry/transaction-finish tx)
    {}))
