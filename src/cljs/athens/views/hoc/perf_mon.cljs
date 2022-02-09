(ns athens.views.hoc.perf-mon
  "Higher Order Component for Performance Monitoring"
  (:require
    [athens.common.logging :as log]
    [athens.utils.sentry   :as sentry]
    [reagent.core          :as r]))


(defn hoc-perfmon
  "Higher Order Component for Performance Monitoring with Sentry"
  [{:keys [span-name]} _component]
  (let [tx-present? (sentry/tx-running?)
        sentry-tx   (if tx-present?
                      (sentry/transaction-get-current)
                      (sentry/transaction-start (str span-name "-hoc-auto-tx")))
        sentry-span (sentry/span-start sentry-tx span-name false)
        did-mount   (fn [_this]
                      (sentry/span-finish sentry-span false)
                      (when-not tx-present?
                        (sentry/transaction-finish sentry-tx)))]
    (log/debug "hoc-perfmon:" span-name "setup-finished")
    (r/create-class
      {:display-name        "hoc-perfmon"
       :reagent-render      (fn [{:keys [_span-name]} component]
                              [:div component])
       :component-did-mount did-mount})))
