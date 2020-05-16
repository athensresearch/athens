(ns athens.subs
  (:require
   [athens.blocks :as blocks]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [re-frame.core :as re-frame]
   [re-posh.core :as re-posh :refer [subscribe reg-query-sub reg-pull-sub ;; reg-pull-many-sub
                                     ]]))
;; note: not refering reg-sub because re-posh and re-frame have different reg-subs

;; re-frame subscriptions
(re-frame/reg-sub
 :user
 (fn [db _]
   (:user db)
   ))

(re-frame/reg-sub
  :errors
  (fn [db _]
    (:errors db)
    ))

(re-frame/reg-sub
  :loading
  (fn [db _]
    (:loading db)
    ))

;; datascript queries
(reg-query-sub
 :nodes
 '[:find [?e ...]
   :where
   [?e :node/title ?t]])

(reg-query-sub
  :node/refs
  '[:find ?id
    :in $ ?regex
    :where
    [?e :block/string ?s]
    [(re-find ?regex ?s)]
    [?e :block/uid ?id]])

(reg-query-sub
  :page/sidebar
  '[:find ?order ?title ?bid
    :where
    [?e :page/sidebar ?order]
    [?e :node/title ?title]
    [?e :block/uid ?bid]])

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
 '[:block/uid :block/string :block/order :block/open :block/editing :db/id {:block/children ...}])

(re-frame/reg-sub
  :block/children-sorted
  (fn [[_ id] _]
    (subscribe [:block/children id]))
  (fn [block _]
    (blocks/sort-block block)))

(reg-pull-sub
  :block/_children
  '[:block/uid :block/string :node/title {:block/_children ...}])

;; layer 3 subscriptions

(re-frame/reg-sub
 :block/_children2
  (fn [[_ id] _]
   (subscribe [:block/_children id]))
  (fn [block _] ; find path from nested block to origin node
    (reverse
     (rest
      (loop [b block
             res []]
        (if (:node/title b)
          (conj res b)
          (recur (first (:block/_children b))
                 (conj res (dissoc b :block/_children)))))))))

(re-posh/reg-sub
 :pull-nodes
 :<- [:nodes]
 (fn-traced [nodes _]
   {:type :pull-many
    :pattern '[*]
    :ids nodes}))

(re-frame/reg-sub
  :favorites
  :<- [:page/sidebar]
  (fn-traced [nodes _]
    (->> nodes
         (into [])
         (sort-by first))
    ))

;; (rp/reg-sub
;;  :node/refs2
;;  (fn [[_ regex]]
;;    (subscribe [:node/refs regex]))
;;  (fn [ids _] ; for all refs, find their parents with reverse lookup
;;    {:type :pull-many
;;     :pattern '[:node/title :block/uid :block/string {:block/_children ...}]
;;     :ids (reduce into [] ids)}))

;; (rf/reg-sub
;;  :node/refs3
;;  (fn [[_ regex]]
;;    (subscribe [:node/refs2 regex]))
;;  (fn [blocks _]
;;    ;; flatten paths like in :block/_children2 (except keep node/title)
;;    ;; then normalize refs through group by :node/title
;;    (->> blocks
;;         (map (fn [block]
;;                (reverse
;;                 (loop [b block
;;                        res []]
;;                   (if (:node/title b)
;;                     (conj res (dissoc b :block/children))
;;                     (recur (first (:block/_children b))
;;                            (conj res (dissoc b :block/_children))))))))
;;         (group-by #(:node/title (first %)))
;;         (reduce-kv (fn [m k v]
;;                      (assoc m k (map rest v))) {} ))
;;    ))
