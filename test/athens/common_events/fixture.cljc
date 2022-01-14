(ns athens.common-events.fixture
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.bfs             :as bfs]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.common-events.resolver.undo   :as undo]
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


(defn setup!
  [repr]
  (->> repr
       (bfs/build-paste-op @@connection)
       common-events/build-atomic-event
       (atomic-resolver/resolve-transact! @connection)))


(defn get-repr
  [lookup]
  (common-db/get-internal-representation @@connection lookup))


(defn op-resolve-transact!
  [op]
  (let [db  @@connection
        evt (common-events/build-atomic-event op)]
    (atomic-resolver/resolve-transact! @connection evt)
    [db evt]))


(defn undo!
  [evt-db evt]
  (let [db       @@connection
        undo-evt (undo/build-undo-event db evt-db evt)]
    (atomic-resolver/resolve-transact! @connection undo-evt)
    [db undo-evt]))


(defn teardown!
  [repr]
  (doseq [title (map :page-title repr)]
    (when title
      (-> (atomic-graph-ops/make-page-remove-op title)
          op-resolve-transact!))))
