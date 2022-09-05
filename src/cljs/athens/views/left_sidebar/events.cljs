(ns athens.views.left-sidebar.events
  (:require
    [athens.db :as db]
    [athens.common-db :as common-db]
    [re-frame.core :as rf]
    [athens.interceptors :as interceptors]
    [athens.common.logging :as log]
    [athens.common-events.graph.atomic :as atomic-graph-ops]
    [athens.common-events :as common-events]))

(rf/reg-event-fx
  :left-sidebar/add-shortcut
  [(interceptors/sentry-span-no-new-tx "left-sidebar/add-shortcut")]
  (fn [_ [_ name]]
    (log/debug ":page/add-shortcut:" name)
    (let [add-shortcut-op (atomic-graph-ops/make-shortcut-new-op name)
          event           (common-events/build-atomic-event add-shortcut-op)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(rf/reg-event-fx
  :left-sidebar/remove-shortcut
  [(interceptors/sentry-span-no-new-tx "left-sidebar/remove-shortcut")]
  (fn [_ [_ name]]
    (log/debug ":page/remove-shortcut:" name)
    (let [remove-shortcut-op (atomic-graph-ops/make-shortcut-remove-op name)
          event              (common-events/build-atomic-event remove-shortcut-op)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(rf/reg-event-fx
  :left-sidebar/drop
  [(interceptors/sentry-span-no-new-tx "left-sidebar/drop")]
  (fn [_ [_ source-order target-order relation]]
    (let [[source-name target-name] (common-db/find-source-target-title @db/dsdb source-order target-order)
          drop-op                   (atomic-graph-ops/make-shortcut-move-op source-name
                                                                            {:page/title target-name
                                                                             :relation relation})
          event (common-events/build-atomic-event drop-op)]
      {:fx [[:dispatch [:resolve-transact-forward event]]
            [:dispatch [:posthog/report-feature :left-sidebar]]]})))

