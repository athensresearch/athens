(ns athens.common-events.fixture
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.common.logging                :as log]
    [datascript.core                      :as d]))


(def connection (atom nil))


(defn integration-test-fixture
  ([test-fn]
   (integration-test-fixture [] test-fn))

  ([datoms test-fn]
   (let [conn (d/create-conn common-db/schema)]
     (d/transact! conn datoms)
     (reset! connection conn)

     (test-fn)

     (reset! connection nil))))


(defn transact-with-middleware
  [txs]
  (let [processed-txs (->> txs
                           (common-db/linkmaker @@connection)
                           (common-db/orderkeeper @@connection))]
    (log/debug "transact-with-middleware"
               "\nfrom:" (pr-str txs)
               "\nto:" (pr-str processed-txs))
    (d/transact! @connection processed-txs)))


(defn transact-composite-ops-without-middleware
  [composite-op]
  (let [atomic-ops (graph-ops/extract-atomics composite-op)]
    (doseq [atomic-op atomic-ops
            :let      [atomic-txs (atomic-resolver/resolve-atomic-op-to-tx @@connection atomic-op)]]
      (d/transact! @connection atomic-txs))))
