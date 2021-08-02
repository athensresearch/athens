(ns athens.subs
  (:require
    [athens.util           :as util]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [re-frame.core         :as rf]))


(rf/reg-sub
  :user
  (fn [db _]
    (:user db)))


(rf/reg-sub
  :db/synced
  (fn [db _]
    (:db/synced db)))


(rf/reg-sub
  :theme/dark
  (fn [db _]
    (:theme/dark db)))


(rf/reg-sub
  :app-db
  (fn [db _]
    db))


(rf/reg-sub
  :alert
  (fn [db _]
    (:alert db)))


(rf/reg-sub
  :loading?
  (fn [db _]
    (:loading? db)))


(rf/reg-sub
  :athena/open
  (fn-traced [db _]
             (:athena/open db)))


(rf/reg-sub
  :devtool/open
  (fn-traced [db _]
             (:devtool/open db)))


(rf/reg-sub
  :left-sidebar/open
  (fn-traced [db _]
             (:left-sidebar/open db)))


(rf/reg-sub
  :right-sidebar/open
  (fn-traced [db _]
             (:right-sidebar/open db)))


(rf/reg-sub
  :right-sidebar/items
  (fn-traced [db _]
             (:right-sidebar/items db)))


(rf/reg-sub
  :right-sidebar/width
  (fn [db _]
    (:right-sidebar/width db)))


(rf/reg-sub
  :mouse-down
  (fn [db _]
    (:mouse-down db)))


(rf/reg-sub
  :merge-prompt
  (fn [db _]
    (:merge-prompt db)))


(rf/reg-sub
  :editing/uid
  (fn-traced [db _]
             (:editing/uid db)))


(rf/reg-sub
  :editing/is-editing
  (fn [_]
    [(rf/subscribe [:editing/uid])])
  (fn [[editing-uid] [_ uid]]
    (= editing-uid uid)))


(rf/reg-sub
  :selected/items
  (fn [db _]
    (get-in db [:selection :items])))


(rf/reg-sub
  :selected/order
  (fn [db _]
    (get-in db [:selection :order])))


(rf/reg-sub
  :selected/is-selected
  (fn [_]
    [(rf/subscribe [:selected/items])])
  (fn [[selected-items] [_ uid]]
    (contains? (set selected-items) uid)))


(rf/reg-sub
  :daily-notes/items
  (fn-traced [db _]
             (:daily-notes/items db)))


(rf/reg-sub
  :athena/get-recent
  (fn-traced [db _]
             (:athena/recent-items db)))


(rf/reg-sub
  :modal
  (fn [db _]
    (:modal db)))


;; really bad that we're checking if electron in a subscription, but short-term solution to get both web app and desktop to build. see athens.electron
(rf/reg-sub
  :db/remote-graph-conf
  (fn [db _]
    (if (util/electron?)
      (:db/remote-graph-conf db)
      {})))


(rf/reg-sub
  :remote/awaited-events
  (fn [db _]
    (:remote/awaited-events db #{})))


(rf/reg-sub
  :remote/accepted-events
  (fn [db _]
    (:remote/accepted-events db #{})))


(rf/reg-sub
  :remote/rejected-events
  (fn [db _]
    (:remote/rejected-events db #{})))


(rf/reg-sub
  :remote/failed-events
  (fn [db _]
    (:remote/failed-events db #{})))


(rf/reg-sub
  :remote/last-seen-tx
  (fn [db _]
    (:remote/last-seen-tx db -1)))


(rf/reg-sub
  :remote/awaited-tx
  (fn [db _]
    (:remote/awaited-tx db #{})))


(rf/reg-sub
  :remote/followup
  (fn [db _]
    (:remote/followup db {})))


(rf/reg-sub
  :remote/followup-for
  :<- [:remote/followup]
  (fn [followups [_ event-id]]
    (get followups event-id)))
