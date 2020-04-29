(ns athens.subs
  (:require
   [re-frame.core :as rf :refer [subscribe]]
   [re-posh.core :as rp :refer [reg-query-sub reg-pull-sub reg-pull-many-sub]]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))
; note: not refering reg-sub because re-posh and re-frame have different reg-subs


; re-frame subscriptions
(rf/reg-sub
 :user
 (fn [db _]
   (:user db)
   ))

(rf/reg-sub
  :errors
  (fn [db _]
    (:errors db)
    ))

;; datascript queries
(reg-query-sub
 :nodes
 '[:find ?e
   :where
   [?e :node/title ?t]])

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

(rp/reg-sub
 :pull-nodes
 (fn [[_ _]]
   (subscribe [:nodes]))
 (fn [nodes _]
   {:type :pull-many
    :pattern '[*]
    :ids (reduce into [] nodes)}))

(reg-query-sub
 :node/refs
 '[:find ?id
   :in $ ?regex
   :where
   [?e :block/string ?s]
   [(re-find ?regex ?s)]
   [?e :block/uid ?id]])



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
