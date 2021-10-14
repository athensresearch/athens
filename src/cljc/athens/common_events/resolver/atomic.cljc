(ns athens.common-events.resolver.atomic
  (:require
    [athens.common-db              :as common-db]
    [athens.common-events.resolver :as resolver]
    [athens.common.utils           :as utils]))


(defmulti resolve-atomic-op-to-tx
  "Resolves ⚛️ Atomic Graph Ops to TXs."
  #(:op/type %2))


(defmethod resolve-atomic-op-to-tx :block/new
  [db {:op/keys [args]}]
  (let [{:keys [block-uid
                position]}              args
        {:keys [ref-uid
                relation]}              position
        ref-parent?                     (or (int? relation)
                                            (#{:first :last} relation))
        ref-block-exists?               (int? (common-db/e-by-av db :block/uid ref-uid))
        ref-block                       (when ref-block-exists?
                                          (common-db/get-block db [:block/uid ref-uid]))
        {parent-block-uid :block/uid
         :as              parent-block} (if ref-parent?
                                          (if ref-block-exists?
                                            ref-block
                                            {:block/uid ref-uid})
                                          (common-db/get-parent db [:block/uid ref-uid]))
        parent-block-exists?            (int? (common-db/e-by-av db :block/uid parent-block-uid))
        new-block-order                 (condp = relation
                                          :first  0
                                          :last   (->> parent-block
                                                       :block/children
                                                       (map :block/order)
                                                       (reduce max 0)
                                                       inc)
                                          :before (:block/order ref-block)
                                          :after  (inc (:block/order ref-block))
                                          (inc relation))
        now                             (utils/now-ts)
        new-block                       {:block/uid    block-uid
                                         :block/string ""
                                         :block/order  new-block-order
                                         :block/open   true
                                         :create/time  now
                                         :edit/time    now}
        reindex                         (if-not parent-block-exists?
                                          [new-block]
                                          (concat [new-block]
                                                  (common-db/inc-after db
                                                                       [:block/uid parent-block-uid]
                                                                       (dec new-block-order))))
        tx-data                         [{:block/uid      parent-block-uid
                                          :block/children reindex
                                          :edit/time      now}]]
    tx-data))


;; This is Atomic Graph Op, there is also composite version of it
(defmethod resolve-atomic-op-to-tx :block/save
  [db {:op/keys [args]}]
  (let [{:keys [block-uid
                new-string
                old-string]} args
        stored-old-string    (if-let [block-eid (common-db/e-by-av db :block/uid block-uid)]
                               (common-db/v-by-ea db block-eid :block/string)
                               "")]
    (when-not (= stored-old-string old-string)
      (print (ex-info ":block/save operation started from a stale state."
                      {:op/args           args
                       :actual-old-string stored-old-string})))
    (let [now           (utils/now-ts)
          updated-block {:block/uid    block-uid
                         :block/string new-string
                         :edit/time    now}]
      [updated-block])))


(defmethod resolve-atomic-op-to-tx :block/remove
  [db {:op/keys [args]}]
  (let [{:keys [block-uid]} args
        block-exists?       (common-db/e-by-av db :block/uid block-uid)
        retract             (when block-exists?
                              [:db/retractEntity [:block/uid block-uid]])]
    (when block-exists?
      [retract])))


(defmethod resolve-atomic-op-to-tx :page/new
  [db {:op/keys [args]}]
  (let [{:keys [page-uid
                title]} args
        page-exists?    (common-db/e-by-av db :node/title title)
        now             (utils/now-ts)
        page            {:node/title     title
                         :block/uid      page-uid
                         :block/children []
                         :create/time    now
                         :edit/time      now}
        txs             (if page-exists?
                          []
                          [page])]
    txs))


(defmethod resolve-atomic-op-to-tx :composite/consequence
  [db {:op/keys [consequences] :as _composite}]
  (into []
        (mapcat (fn [consequence]
                  (resolve-atomic-op-to-tx db consequence))
                consequences)))


(defn resolve-to-tx
  [db {:event/keys [type op] :as event}]
  (if (contains? #{:op/atomic} type)
    (resolve-atomic-op-to-tx db op)
    (resolver/resolve-event-to-tx db event)))
