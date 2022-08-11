(ns athens.views.right-sidebar.events
  (:require [re-frame.core :refer [reg-event-fx reg-event-db]]
            [athens.db :as db]
            [athens.interceptors :as interceptors]
            [datascript.core :as d]))

;; UI

(reg-event-fx
  :right-sidebar/toggle
  [(interceptors/sentry-span-no-new-tx "right-sidebar/toggle")]
  (fn [{:keys [db]} _]
    (let [closing? (:right-sidebar/open db)]
      {:db       (update db :right-sidebar/open not)
       :dispatch [:posthog/report-feature :right-sidebar (not closing?)]})))


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
  :right-sidebar/toggle-item
  [(interceptors/sentry-span-no-new-tx "right-sidebar/toggle-item")]
  (fn [{:keys [db]} [_ item]]
    {:db       (update-in db [:right-sidebar/items item :open] not)
     :dispatch [:posthog/report-feature :right-sidebar true]}))


;; TODO: dec all indices > closed item
(reg-event-fx
  :right-sidebar/close-item
  [(interceptors/sentry-span-no-new-tx "right-sidebar/close-item")]
  (fn [{:keys [db]} [_ uid]]
    (let [{:right-sidebar/keys
           [items]}  db
          last-item? (= 1 (count items))
          new-db     (cond-> (update db :right-sidebar/items dissoc uid)
                             last-item? (assoc :right-sidebar/open false))]
      {:db       new-db
       :dispatch [:posthog/report-feature :right-sidebar (not last-item?)]})))


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



(reg-event-fx
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