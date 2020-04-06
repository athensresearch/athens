(ns athens.subs
  (:require
   [re-frame.core :as rf :refer [reg-sub subscribe]]
   [re-posh.core :as rp :refer [reg-query-sub reg-pull-sub reg-pull-many-sub]]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   ))

; re-frame subscriptions
(reg-sub
 :user/name
 (fn [db _]
   (:user/name db)
   ))


;; datascript queries
(reg-query-sub
 :nodes
 '[:find ?e ?t ?b
   :where
   [?e :node/title ?t]
   [?e :block/uid ?b]
   ])

;; datascript pulls
(reg-pull-sub
 :node
 '[*])

(reg-pull-sub
 :block/uid
 '[:block/uid])

(reg-pull-sub
 :block/string
 '[:block/string])

(reg-pull-sub
 :blocks
 '[:block/string {:block/children ...}])

(reg-pull-sub
 :block/children
 '[:block/uid :block/string {:block/children ...}])
