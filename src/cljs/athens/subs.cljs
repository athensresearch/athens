(ns athens.subs
  (:require
    [athens.util :as util]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [re-frame.core :as re-frame :refer [subscribe]]))


(re-frame/reg-sub
  :user
  (fn [db _]
    (:user db)))


(re-frame/reg-sub
  :db/synced
  (fn [db _]
    (:db/synced db)))


(re-frame/reg-sub
  :theme/dark
  (fn [db _]
    (:theme/dark db)))


(re-frame/reg-sub
  :app-db
  (fn [db _]
    db))


(re-frame/reg-sub
  :alert
  (fn [db _]
    (:alert db)))


(re-frame/reg-sub
  :loading?
  (fn [db _]
    (:loading? db)))


(re-frame/reg-sub
  :athena/open
  (fn-traced [db _]
             (:athena/open db)))


(re-frame/reg-sub
  :devtool/open
  (fn-traced [db _]
             (:devtool/open db)))


(re-frame/reg-sub
  :left-sidebar/open
  (fn-traced [db _]
             (:left-sidebar/open db)))


(re-frame/reg-sub
  :right-sidebar/open
  (fn-traced [db _]
             (:right-sidebar/open db)))


(re-frame/reg-sub
  :right-sidebar/items
  (fn-traced [db _]
             (:right-sidebar/items db)))


(re-frame/reg-sub
  :right-sidebar/width
  (fn [db _]
    (:right-sidebar/width db)))


(re-frame/reg-sub
  :mouse-down
  (fn [db _]
    (:mouse-down db)))


(re-frame/reg-sub
  :merge-prompt
  (fn [db _]
    (:merge-prompt db)))


(re-frame/reg-sub
  :editing/uid
  (fn-traced [db _]
             (:editing/uid db)))


(re-frame/reg-sub
  :editing/is-editing
  (fn [_]
    [(subscribe [:editing/uid])])
  (fn [[editing-uid] [_ uid]]
    (= editing-uid uid)))


(re-frame/reg-sub
  :selected/items
  (fn [db _]
    (:selected/items db)))


(re-frame/reg-sub
  :selected/is-selected
  (fn [_]
    [(subscribe [:selected/items])])
  (fn [[selected-items] [_ uid]]
    (contains? (set selected-items) uid)))


(re-frame/reg-sub
  :daily-notes/items
  (fn-traced [db _]
             (:daily-notes/items db)))


(re-frame/reg-sub
  :athena/get-recent
  (fn-traced [db _]
             (:athena/recent-items db)))


(re-frame/reg-sub
  :modal
  (fn [db _]
    (:modal db)))


;; really bad that we're checking if electron in a subscription, but short-term solution to get both web app and desktop to build. see athens.electron
(re-frame/reg-sub
  :db/remote-graph-conf
  (fn [db _]
    (if (util/electron?)
      (:db/remote-graph-conf db)
      {})))


(re-frame/reg-sub
  :keymap
  (fn [db _]
    (:keymap db)))
