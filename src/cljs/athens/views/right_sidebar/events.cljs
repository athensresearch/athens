(ns athens.views.right-sidebar.events
  (:require
    [athens.common-db :as common-db]
    [athens.common-events :as common-events]
    [athens.common-events.bfs             :as bfs]
    [athens.common-events.graph.ops :as graph-ops]
    [athens.db :as db]
    [athens.interceptors :as interceptors]
    [athens.views.right-sidebar.shared :as shared]
    [re-frame.core :as rf :refer [reg-event-fx]]))


;; UI

(reg-event-fx
  :right-sidebar/toggle
  [(interceptors/sentry-span-no-new-tx "right-sidebar/toggle")]
  (fn [_ _]
    (let [user-page @(rf/subscribe [:presence/user-page])]
      {:fx [[:dispatch [:graph/update-in [:node/title user-page] [(shared/ns-str "/open?")]
                        (fn [db uid]
                          (let [exists? (common-db/block-exists? db [:block/uid uid])]
                            [(if exists?
                               (graph-ops/build-block-remove-op db uid)
                               (graph-ops/build-block-save-op db uid ""))]))]]
            [:dispatch [:posthog/report-feature :right-sidebar true]]]})))


(reg-event-fx
  :right-sidebar/set-width
  [(interceptors/sentry-span-no-new-tx "right-sidebar/set-width")]
  (fn [_ [_ width]]
    (let [user-page @(rf/subscribe [:presence/user-page])]
      {:fx [[:dispatch [:graph/update-in [:node/title user-page] [(shared/ns-str "/width")]
                        (fn [db uid]
                          ;; todo: good place to be using a number primitive type
                          [(graph-ops/build-block-save-op db uid (str width))])]]]})))


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
                    [:graph/update-in [:node/title user-page] [(shared/ns-str "/items")]
                     (fn [db uid]
                       (bfs/internal-representation->atomic-ops db new-item-ir {:block/uid uid :relation :first}))]
                    ;; if athens right sidebar is not open, open
                    [:graph/update-in [:node/title user-page] [(shared/ns-str "/open?")]
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
    {:fx [[:dispatch [:graph/update-in [:block/uid uid] ["athens/right-sidebar/items/open?"]
                      (fn [db uid]
                        (if (common-db/block-exists? db [:block/uid uid])
                          [(graph-ops/build-block-remove-op db uid)]
                          [(graph-ops/build-block-save-op db uid "")]))]]
          [:dispatch [:posthog/report-feature :right-sidebar true]]]}))


(reg-event-fx
  :right-sidebar/navigate-item
  [(interceptors/sentry-span-no-new-tx "right-sidebar/navigate-item")]
  (fn [_ [_ block-page-uid breadcrumb-eid]]
    (let [[_attr value] breadcrumb-eid
          type (shared/eid->type breadcrumb-eid)
          update-uid (->> (shared/get-items)
                          (filter #(= (:name %) block-page-uid))
                          first
                          :source-uid)]
      {:fx [[:dispatch [:graph/update-in [:block/uid update-uid] [(shared/ns-str "/items/type")]
                        (fn [db uid]
                          ;; update type
                          [(graph-ops/build-block-save-op db uid type)
                           ;; update the entity reference
                           (graph-ops/build-block-save-op @athens.db/dsdb update-uid value)])]]
            [:dispatch [:posthog/report-feature :right-sidebar true]]]})))


(reg-event-fx
  :right-sidebar/reorder
  [(interceptors/sentry-span-no-new-tx "right-sidebar/navigate-item")]
  (fn [_ [_ source-uid target-uid old-index new-index]]
    (let [before-after (cond
                         (< old-index new-index) :after
                         (> old-index new-index) :before)
          evt          (common-events/build-atomic-event
                         (graph-ops/build-block-move-op @athens.db/dsdb source-uid {:relation  before-after
                                                                                    :block/uid target-uid}))]
      {:fx [[:dispatch [:resolve-transact-forward evt]]
            [:dispatch [:posthog/report-feature :right-sidebar true]]]})))

