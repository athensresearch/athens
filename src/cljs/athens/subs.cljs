(ns athens.subs
  (:require
   [re-frame.core :as rf :refer [subscribe]]
   [re-posh.core :as rp :refer [reg-query-sub reg-pull-sub reg-pull-many-sub]]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))
; note: not refering reg-sub because re-posh and re-frame have different reg-subs


; re-frame subscriptions
(rf/reg-sub
 :user/name
 (fn [db _]
   (:user/name db)
   ))

;; datascript queries
(reg-query-sub
 :nodes
 '[:find ?e ?t ?b ?et ?ct
   :where
   [?e :node/title ?t]
   [?e :block/uid ?b]
   [?e :create/time ?ct]
   [?e :edit/time ?et]
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

; layer 3 subscriptions
(reg-pull-sub
 :block/_children
 '[:block/uid :block/string :node/title {:block/_children ...}])

(rf/reg-sub
 :block/_children2
 (fn [[_ identifier] _]
   (subscribe [:block/_children identifier]))
 (fn [block _]
   (reverse
    (rest
     (loop [b block
            res []]
       (if (:node/title b)
         (conj res b)
         (recur (first (:block/_children b))
                (conj res (dissoc b :block/_children)))))))))


(reg-query-sub
 :linked-refs
 '[:find ?e
   :in $ ?regex
   :where
   [?e :block/string ?s]
   [(re-find ?regex ?s)]])

(rp/reg-sub
 :linked-refs2
 (fn [[_ identifier]]
   (subscribe [:linked-refs identifier]))
 (fn [ids _]
   {:type :pull-many
    :pattern '[:node/title :block/uid :block/string :block/children {:block/_children ...}]
    :ids (reduce into [] ids)}))
