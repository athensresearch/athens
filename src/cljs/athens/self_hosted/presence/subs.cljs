(ns athens.self-hosted.presence.subs
  (:require
    [athens.db :as db]
    [athens.self-hosted.presence.utils :as utils]
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
    (into {} (mapv (fn [[username {:keys [_username  block/uid] :as user}]]
                     (let [{page-title :node/title page-uid :block/uid} (db/get-root-parent-page uid)]
                       [username (assoc user :page/uid page-uid :page/title page-title :block/uid uid)]))
                   users))))


(rf/reg-sub
  :presence/current-user
  :<- [:presence/users-with-page-data]
  :<- [:settings]
  (fn [[users settings] [_]]
    (let [user-in-presence (-> (filter (fn [[_ user]]
                                         (= (:username settings) (:username user)))
                                       users)
                               first
                               second)]
      (or user-in-presence
          {:username (:username settings)
           :color    (or (:color settings)
                         (first utils/PALETTE))}))))


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

      {})))


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
    (keep (fn [[_username user]]
            (when (= uid (:block/uid user))
              user))
          users)))
