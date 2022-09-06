(ns athens.views.left-sidebar.events
  (:require
    [athens.common-db :as common-db]
    [athens.common-events :as common-events]
    [athens.common-events.graph.atomic :as atomic-graph-ops]
    [athens.common-events.graph.ops :as graph-ops]
    [athens.common.logging :as log]
    [athens.db :as db]
    [athens.interceptors :as interceptors]
    [athens.views.left-sidebar.shared :as shared]
    [re-frame.core :as rf]))


;; Shortcuts

(rf/reg-event-fx
  :left-sidebar/toggle
  [(interceptors/sentry-span-no-new-tx "left-sidebar/toggle")]
  (fn [_ _]
    (let [user-page @(rf/subscribe [:presence/user-page])]
      {:fx [[:dispatch [:graph/update-in [:node/title user-page] [(shared/ns-str "/closed?")]
                        (fn [db uid]
                          (let [exists? (common-db/block-exists? db [:block/uid uid])]
                            [(if exists?
                               (graph-ops/build-block-remove-op db uid)
                               (graph-ops/build-block-save-op db uid ""))]))]]
            [:dispatch [:posthog/report-feature :left-sidebar]]]})))


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


;; Tasks

(rf/reg-event-fx
  :left-sidebar.tasks/set-max-tasks
  [(interceptors/sentry-span-no-new-tx "left-sidebar/tasks/set-max-tasks")]
  (fn [_ [_ max-tasks]]
    (let [user-page @(rf/subscribe [:presence/user-page])]
      {:fx [[:dispatch [:graph/update-in [:node/title user-page] [(shared/ns-str "/tasks/max-tasks")]
                        (fn [db uid]
                          ;; todo: good place to be using a number primitive type
                          [(graph-ops/build-block-save-op db uid (str max-tasks))])]]
            [:dispatch [:posthog/report-feature "left-sidebar/tasks"]]]})))


;; Widgets

(rf/reg-event-fx
  :left-sidebar.widgets/toggle-widget
  [(interceptors/sentry-span-no-new-tx "left-sidebar/widgets/toggle")]
  (fn [_ [_ widget-id]]
    (let [user-page @(rf/subscribe [:presence/user-page])]
      {:fx [[:dispatch [:graph/update-in [:node/title user-page] [(shared/ns-str "/widgets/" widget-id "/closed?")]
                        (fn [db uid]
                          (let [exists? (common-db/block-exists? db [:block/uid uid])]
                            [(if exists?
                               (graph-ops/build-block-remove-op db uid)
                               (graph-ops/build-block-save-op db uid ""))]))]]
            [:dispatch [:posthog/report-feature "left-sidebar/widgets/toggle"]]]})))


(rf/reg-event-fx
  :left-sidebar.tasks.section/toggle
  [(interceptors/sentry-span-no-new-tx "left-sidebar/tasks/section/toggle")]
  (fn [_ [_ page-id]]
    (let [user-page @(rf/subscribe [:presence/user-page])]
      {:fx [[:dispatch [:graph/update-in [:node/title user-page] [(shared/ns-str "/tasks/" page-id "/closed?")]
                        (fn [db uid]
                          (let [exists? (common-db/block-exists? db [:block/uid uid])]
                            [(if exists?
                               (graph-ops/build-block-remove-op db uid)
                               (graph-ops/build-block-save-op db uid ""))]))]]
            [:dispatch [:posthog/report-feature "left-sidebar/tasks/section/toggle"]]]})))


