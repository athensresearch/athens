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
                              [:<> component])
       :component-did-mount did-mount})))


(defn hoc-perfmon-no-new-tx
  "Higher Order Component for Performance Monitoring with Sentry which does not create new tx if it does not exist"
  [{:keys [span-name]} _component]
  (let [tx-present? (sentry/tx-running?)
        sentry-tx   (when tx-present?
                      (sentry/transaction-get-current))
        sentry-span (sentry/span-start sentry-tx span-name false)
        did-mount   (fn [_this]
                      (when sentry-span
                        (sentry/span-finish sentry-span false)))]
    (when sentry-span
      (log/debug "hoc-perfmon-no-new-tx:" span-name "setup-finished"))
    (r/create-class
      {:display-name        "hoc-perfmon-no-new-tx"
       :reagent-render      (fn [{:keys [_span-name]} component]
                              [:<> component])
       :component-did-mount did-mount})))
