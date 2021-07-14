(ns athens.self-hosted.presence.subs
  (:require
    [athens.db :as db]
    [re-frame.core :as rf]))


(rf/reg-sub
  :presence/users
  (fn [db _]
    (-> db :presence :users)))


;; "From :block/uid, derive :page/uid and :page/title. If no :block/uid, give nil"
(rf/reg-sub
  :presence/users-with-page-data
  :<- [:presence/users]
  (fn [users _]
    (into {} (mapv (fn [[username {:keys [_username color block/uid] :as user}]]
                     (let [{page-title :node/title page-uid :block/uid} (db/get-root-parent-page uid)]
                       [username (assoc user :page/uid page-uid :page/title page-title :block/uid uid)]))
                   users))))


(rf/reg-sub
  :presence/same-page
  :<- [:presence/users-with-page-data]
  :<- [:current-route/name]
  :<- [:current-route/uid]
  (fn [[users current-route-name current-route-uid] _]
    (case current-route-name

      :page
      (into {} (filterv (fn [[_username user]]
                          (= current-route-uid (:page/uid user)))
                        users))

      [])))


(rf/reg-sub
  :presence/diff-page
  :<- [:presence/users-with-page-data]
  :<- [:current-route/name]
  :<- [:current-route/uid]
  (fn [[users current-route-name current-route-uid] _]
    (case current-route-name

      :page
      (into {} (filterv (fn [[_username user]]
                          (not= current-route-uid (:page/uid user)))
                        users))

      users)))


(rf/reg-sub
  :presence/has-presence
  :<- [:presence/users-with-page-data]
  (fn [users [_ uid]]
    (-> (filter (fn [[_username user]]
                  (= uid (:block/uid user)))
                users)
        first
        second)))
