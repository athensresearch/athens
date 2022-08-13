(ns athens.views.right-sidebar.events
  (:require [re-frame.core :refer [reg-event-fx reg-event-db]]
            [athens.db :as db]
            [athens.common-events.graph.ops :as graph-ops]
            [athens.common-events.bfs             :as bfs]
            [athens.interceptors :as interceptors]
            [datascript.core :as d]
            [athens.views.right-sidebar.shared :as shared]
            [athens.common-db :as common-db]
            [athens.common-events :as common-events]
            [re-frame.core :as rf]))

;; UI

(reg-event-fx
  :right-sidebar/toggle
  [(interceptors/sentry-span-no-new-tx "right-sidebar/toggle")]
  (fn [_ _]
    (let [user-page @(rf/subscribe [:presence/user-page])]
      {:fx [[:dispatch [:properties/update-in [:node/title user-page] [(shared/ns-str "/open?")]
                        (fn [db uid]
                          (let [exists? (common-db/block-exists? db [:block/uid uid])]
                            [(if exists?
                               (graph-ops/build-block-remove-op db uid)
                               (graph-ops/build-block-save-op db uid ""))]))]]
            [:dispatch [:posthog/report-feature :right-sidebar true]]]})))


(reg-event-db
  :right-sidebar/set-width
  [(interceptors/sentry-span-no-new-tx "right-sidebar/set-width")]
  (fn [db [_ width]]
    (assoc db :right-sidebar/width width)))

(reg-event-fx
  :right-sidebar/scroll-top
  [(interceptors/sentry-span-no-new-tx "right-sidebar/scroll-top")]
  (fn []
    {:right-sidebar/scroll-top nil}))

;; ITEM

(reg-event-fx
  :right-sidebar/open-item
  [(interceptors/sentry-span-no-new-tx "right-sidebar/open-item")]
  (fn [_ [_ eid is-graph?]]
    (let [user-page   @(rf/subscribe [:presence/user-page])
          name        (second eid)
          type        (cond
                        is-graph? "graph"
                        (= (first eid) :node/title) "page"
                        :else "block")
          new-item-ir [#:block{:string name
                               :properties
                               {(shared/ns-str "/items/type")  {:block/string type}
                                (shared/ns-str "/items/open?") {:block/string ""}}}]]

      {:dispatch-n [;; add item
                    [:properties/update-in [:node/title user-page] [(shared/ns-str "/items")]
                     (fn [db uid]
                       (bfs/internal-representation->atomic-ops db new-item-ir {:block/uid uid :relation :first}))]
                    ;; if athens right sidebar is not open, open
                    [:properties/update-in [:node/title user-page] [(shared/ns-str "/open?")]
                     (fn [db uid]
                       (when-not (common-db/block-exists? db [:block/uid uid])
                         [(graph-ops/build-block-save-op db uid "")]))]
                    [:right-sidebar/scroll-top]
                    [:posthog/report-feature :right-sidebar true]]})))



(reg-event-fx
  :right-sidebar/close-item
  [(interceptors/sentry-span-no-new-tx "right-sidebar/close-item")]
  (fn [_ [_ uid]]
    (let [items (shared/get-items)
          num-items (count items)]
      {:fx [[:dispatch [:resolve-transact-forward (common-events/build-atomic-event
                                                    (graph-ops/build-block-remove-op @db/dsdb uid))]]
            (when (= num-items 1)
              [:dispatch [:right-sidebar/toggle]])
            [:dispatch [:posthog/report-feature :right-sidebar true]]]})))


(reg-event-fx
  :right-sidebar/toggle-item
  [(interceptors/sentry-span-no-new-tx "right-sidebar/toggle-item")]
  (fn [_ [_ uid]]
    {:fx [[:dispatch [:properties/update-in [:block/uid uid] ["athens/right-sidebar/items/open?"]
                      (fn [db uid]
                        (if (common-db/block-exists? db [:block/uid uid])
                          [(graph-ops/build-block-remove-op db uid)]
                          [(graph-ops/build-block-save-op db uid "")]))]]
          [:dispatch [:posthog/report-feature :right-sidebar true]]]}))


(reg-event-fx
  :right-sidebar/navigate-item
  [(interceptors/sentry-span-no-new-tx "right-sidebar/navigate-item")]
  (fn [_ [_ block-page-uid breadcrumb-uid]]
    (let [update-uid (->> (shared/get-items)
                          (filter #(= (:name %) block-page-uid))
                          first
                          :uid)
          evt        (common-events/build-atomic-event
                       (graph-ops/build-block-save-op @athens.db/dsdb update-uid breadcrumb-uid))]
      {:fx [[:dispatch [:resolve-transact-forward evt]]
            [:dispatch [:posthog/report-feature :right-sidebar true]]]})))



;; TODO: drag and drop reorder