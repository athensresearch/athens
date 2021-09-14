(ns athens.common-events.resolver.atomic
  (:require
    [athens.common-db :as common-db]
   #?(:clj  [datahike.api :as d]
      :cljs [datascript.core :as d])))


(defmulti resolve-atomic-op-to-tx
  "Resolves ⚛️ Atomic Graph Ops to TXs."
  #(:op/type %2))


(defmethod resolve-atomic-op-to-tx :block/new
  [db {:op/keys [args]}]
  (let [{:keys [parent-uid
                block-uid
                block-order]} args
        new-block             {:db/id        -1
                               :block/uid    block-uid
                               :block/string ""
                               :block/order  (inc block-order)
                               :block/open   true}
        reindex               (concat [new-block]
                                      (common-db/inc-after db [:block/uid parent-uid] block-order))
        tx-data               [{:block/uid      parent-uid
                                :block/children reindex}]]
    tx-data))
