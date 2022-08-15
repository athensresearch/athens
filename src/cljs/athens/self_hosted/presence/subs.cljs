(ns athens.self-hosted.presence.subs
  (:require
    [athens.dates :as dates]
    [athens.db :as db]
    [re-frame.core :as rf]))


(rf/reg-sub
  :presence/users
  (fn [db _]
    (-> db :presence :users)))


(rf/reg-sub
  :presence/session-id
  (fn [db _]
    (-> db :presence :session-id)))


;; "From :block/uid, derive :page/uid and :page/title. If no :block/uid, give nil"
(rf/reg-sub
  :presence/users-with-page-data
  :<- [:presence/users]
  (fn [users _]
    (into {} (mapv (fn [[session-id {:keys [block-uid] :as user}]]
                     (let [{page-title :node/title page-uid :block/uid} (db/get-root-parent-page block-uid)]
                       [session-id (assoc user :page/uid page-uid :page/title page-title :block/uid block-uid)]))
                   users))))


(rf/reg-sub
  :presence/current-user
  :<- [:presence/users-with-page-data]
  :<- [:presence/session-id]
  :<- [:db-picker/remote-db?]
  (fn [[users session-id remote-db?] [_]]
    (if remote-db?
      (-> (filter (fn [[_ user]]
                    (= session-id (:session-id user)))
                  users)
          first
          second)
      {:username "You"})))


(rf/reg-sub
  :presence/current-username
  :<- [:presence/current-user]
  (fn [current-user _]
    (:username current-user)))


(rf/reg-sub
  :presence/user-page
  :<- [:presence/current-username]
  (fn [current-user _]
    (str "@" current-user)))


(defn on-page-uid?
  [page-uid [_username user]]
  (= page-uid (:page/uid user)))


(defn on-daily-page?
  [[_username user]]
  (dates/is-daily-note (:page/uid user)))


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
