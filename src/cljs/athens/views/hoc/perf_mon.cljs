(ns athens.views.hoc.perf-mon
  "Higher Order Component for Performance Monitoring"
  (:require [athens.common.logging :as log]
            [athens.utils.sentry   :as sentry]
            [reagent.core          :as r]))


(defn hoc-perfmon
  "Higher Order Component for Performance Monitoring with Sentry"
  [{:keys [span-name]} _component]
  (let [sentry-active-tx (sentry/transaction-get-current)
        sentry-tx        (if sentry-active-tx
                           sentry-active-tx
                           (sentry/transaction-start (str span-name "-hoc-auto-tx")))
        sentry-span      (sentry/span-start sentry-tx (str span-name "-prep"))
        render-span      (atom nil)
        did-mount        (fn [_this]
                           (log/debug "hoc-perfmon:" span-name "did-mount")
                           (sentry/span-finish @render-span)
                           (when-not sentry-active-tx
                             (sentry/transaction-finish sentry-tx)))
        should-update    (fn [_this old-argv new-argv]
                           ;; TODO monitor updates
                           (log/debug "hoc-perfmon:" span-name "should-update")
                           (not= old-argv new-argv))
        did-update       (fn [_this _old-argv _old-state _snapshot]
                           ;; TODO monitor update time
                           (log/debug "hoc-perfmon:" span-name "did-update"))]
    (log/debug "hoc-perfmon:" span-name "setup-finished")
    (r/create-class
     {:display-name            "hoc-perfmon"
      :reagent-render          (fn [{:keys [_span-name]} component]
                                 (log/debug "hoc-perfmon:" span-name "reagent-render")
                                 (sentry/span-finish sentry-span)
                                 (reset! render-span (sentry/span-start sentry-tx (str span-name "-render")))
                                 [:div component])
      :component-did-mount     did-mount
      :should-component-update should-update
      :component-did-update    did-update})))
