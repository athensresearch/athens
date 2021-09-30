(ns athens.self-hosted.presence.subs
  (:require
    [athens.db :as db]
    [athens.util :as util]
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
          (select-keys settings [:username :color])))))


(defn on-page-uid?
  [page-uid [_username user]]
  (= page-uid (:page/uid user)))


(defn on-daily-page?
  [[_username user]]
  (util/is-daily-note (:page-uid user)))


(rf/reg-sub
  :presence/same-page
  :<- [:presence/users-with-page-data]
  :<- [:current-route/name]
  :<- [:current-route/uid]
  (fn [[users current-route-name current-route-uid] _]
    (case current-route-name

      :page
      (into {} (filterv (partial on-page-uid? current-route-uid)
                        users))

      :home
      (into {} (filterv on-daily-page? users))

      {})))


(rf/reg-sub
  :presence/diff-page
  :<- [:presence/users-with-page-data]
  :<- [:current-route/name]
  :<- [:current-route/uid]
  (fn [[users current-route-name current-route-uid] _]
    (case current-route-name

      :page
      (into {} (filterv (complement (partial on-page-uid? current-route-uid))
                        users))

      :home
      (into {} (filterv (complement on-daily-page?) users))

      users)))


(rf/reg-sub
  :presence/has-presence
  :<- [:presence/users-with-page-data]
  (fn [users [_ uid]]
    (keep (fn [[_username user]]
            (when (= uid (:block/uid user))
              user))
          users)))
