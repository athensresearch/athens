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
      {:fx [[:dispatch [:properties/update-in [:node/title user-page] ["athens/right-sidebar" "athens/right-sidebar/open?"]
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
    (let [open?     (shared/get-open?)
          user-page @(rf/subscribe [:presence/user-page])
          block     (d/pull @db/dsdb '[:node/title :block/uid] eid)
          name      (second eid)
          type      (cond
                      is-graph? "graph"
                      (= (first eid) :node/title) "page"
                      :else "block")
          new-item-ir        [#:block{:string name
                                      :properties
                                      {(shared/ns-str "/items/type")
                                       {:block/string type}}}]]

      ;; if athens/right-sidebar -> athens/right-sidebar/items doesn't exist yet, create
      {:fx [[:dispatch [:properties/update-in [:node/title user-page] [(shared/ns-str)]
                        (fn [db uid]
                          (when-not (common-db/block-exists? db [:block/uid uid])
                            (bfs/internal-representation->atomic-ops db
                                                                     [#:block{:string ""
                                                                              :children new-item-ir}]
                                                                     {:block/uid uid :relation {:page/title (shared/ns-str "/items")}})))]]
            ;; if athens/right-sidebar -> athens/right-sidebar/items exists, append
            [:dispatch [:properties/update-in [:node/title user-page] [(shared/ns-str) (shared/ns-str "/items")]
                        (fn [db uid]
                          (bfs/internal-representation->atomic-ops db new-item-ir {:block/uid uid :relation :first}))]]
            ;; if athens/right-sidebar -> athens/right-sidebar/open doesn't exist, create
            ;; TODO: this doesn't work if you need to create multiple props at once
            [:dispatch [:properties/update-in [:node/title user-page] [(shared/ns-str) (shared/ns-str "/open?")]
                        (fn [db uid]
                          (when-not (common-db/block-exists? db [:block/uid uid])
                            [(graph-ops/build-block-save-op db uid "")]))]]]}


      #_{:fx [[:dispatch [:properties/update-in [:node/title user-page] [(shared/ns-str) (shared/ns-str "/open?")]
                          (fn [db uid]
                            (when-not (common-db/block-exists? db [:block/uid uid])
                              [(graph-ops/build-block-save-op db uid "")]))]]
              [:dispatch [:properties/update-in [:node/title user-page] [(shared/ns-str) (shared/ns-str "/items")]
                          (fn [db uid]
                            (bfs/internal-representation->atomic-ops db ir {:block/uid uid :relation :first}))]]]})))


#_(reg-event-fx
    :right-sidebar/open-item
    [(interceptors/sentry-span-no-new-tx "right-sidebar/open-item")]
    (fn [{:keys [db]} [_ eid is-graph?]]
      (let [block        (d/pull @db/dsdb '[:node/title :block/uid] eid)
            uid-or-title (second eid)
            new-item     (merge block {:open true :index -1 :is-graph? is-graph?})
            new-items    (into {}
                               (assoc (:right-sidebar/items db) uid-or-title new-item))
            inc-items    (reduce-kv (fn [m k v] (assoc m k (update v :index inc)))
                                    {}
                                    new-items)
            sorted-items (into (sorted-map-by (fn [k1 k2]
                                                (compare
                                                  [(get-in inc-items [k1 :index]) k2]
                                                  [(get-in inc-items [k2 :index]) k1]))) inc-items)]
        {:db         (assoc db :right-sidebar/items sorted-items)
         :dispatch-n [(when (not (:right-sidebar/open db))
                        [:right-sidebar/toggle])
                      [:right-sidebar/scroll-top]
                      [:posthog/report-feature :right-sidebar true]]})))



(reg-event-fx
  :right-sidebar/close-item
  [(interceptors/sentry-span-no-new-tx "right-sidebar/close-item")]
  (fn [_ [_ uid]]
    {:fx [[:dispatch [:resolve-transact-forward (common-events/build-atomic-event
                                                  (graph-ops/build-block-remove-op @db/dsdb uid))]]
          [:dispatch [:posthog/report-feature :right-sidebar true]]]}))


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


;; TODO
(reg-event-fx
  :right-sidebar/navigate-item
  [(interceptors/sentry-span-no-new-tx "right-sidebar/navigate-item")]
  (fn [{:keys [db]} [_ uid breadcrumb-uid]]
    (let [block      (d/pull @db/dsdb '[:node/title :block/string] [:block/uid breadcrumb-uid])
          item-index (get-in db [:right-sidebar/items uid :index])
          new-item   (merge block {:open true :index item-index})]
      {:db       (-> db
                     (update-in [:right-sidebar/items] dissoc uid)
                     (update-in [:right-sidebar/items] assoc breadcrumb-uid new-item))
       :dispatch [:posthog/report-feature :right-sidebar true]})))