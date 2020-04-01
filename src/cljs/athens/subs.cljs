(ns athens.subs
  (:require
   [re-frame.core :as re-frame :refer [reg-sub subscribe]]
   [re-posh.core :as re-posh :refer [reg-query-sub reg-pull-sub reg-pull-many-sub]]
;   [athens.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   ))

(reg-sub
 :user/name
 (fn [db _]
   (:user/name db)
   ))

(reg-query-sub
 :nodes
 '[:find ?e ?v
   :where [?e :node/title ?v]])

(reg-pull-sub
 :node
 '[*])

(reg-pull-sub
 :blocks
 '[:block/string {:block/children ...}])
