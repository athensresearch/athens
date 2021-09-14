(ns athens.common-events.resolver.atomic
  (:require
    [athens.common-db :as common-db]
   #?(:clj  [datahike.api :as d]
      :cljs [datascript.core :as d]))
  #?(:clj
     (:import
      (java.util
       Date))))


(defn- now-ts
  []
  #?(:clj  (.getTime (Date.))
     :cljs (.getTime (js/Date.))))


(defmulti resolve-atomic-op-to-tx
  "Resolves ⚛️ Atomic Graph Ops to TXs."
  #(:op/type %2))


(defmethod resolve-atomic-op-to-tx :block/new
  [db {:op/keys [args]}]
  (let [{:keys [parent-uid
                block-uid
                block-order]} args
        now                   (now-ts)
        new-block             {:db/id        -1
                               :block/uid    block-uid
                               :block/string ""
                               :block/order  (inc block-order)
                               :block/open   true
                               :create/time  now
                               :edit/time    now}
        reindex               (concat [new-block]
                                      (common-db/inc-after db [:block/uid parent-uid] block-order))
        tx-data               [{:block/uid      parent-uid
                                :block/children reindex
                                :edit/time      now}]]
    tx-data))


(defmethod resolve-atomic-op-to-tx :page/new
  [_db {:op/keys [args]}]
  (let [{:keys [page-uid
                block-uid
                title]} args
        now             (now-ts)
        child           {:block/string ""
                         :block/uid    block-uid
                         :block/order  0
                         :block/open   true
                         :create/time  now
                         :edit/time    now}
        page            {:node/title     title
                         :block/uid      page-uid
                         :block/children [child]
                         :create/time    now
                         :edit/time      now}]
    [page]))
