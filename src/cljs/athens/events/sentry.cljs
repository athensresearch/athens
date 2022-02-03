(ns athens.events.sentry
  (:require
    [athens.common.logging :as log]
    [athens.utils.sentry   :as sentry]
    [re-frame.core         :as rf]))


(rf/reg-event-fx
  :sentry/end-tx
  (fn [_ [_ tx]]
    (log/info "Ending Sentry TX")
    (sentry/transaction-finish tx)
    {}))
