(ns athens.common-events.fixture
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.bfs             :as bfs]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.common-events.resolver.undo   :as undo]
    [athens.common.logging                :as log]
    [clojure.test                         :as t]
    [datascript.core                      :as d]))


(def connection (atom nil))


(defn integration-test-fixture
  ([test-fn]
   (integration-test-fixture [] test-fn))

  ([datoms test-fn]
   (let [conn (common-db/create-conn)]
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


(defn get-repr
  ([]
   (->> @@connection
        common-db/get-all-pages
        (mapv (fn [p] (get-repr [:node/title (:node/title p)])))
        set))
  ([lookup]
   (common-db/get-internal-representation @@connection lookup)))


(defn op-resolve-transact!
  [op]
  (let [db  @@connection
        evt (common-events/build-atomic-event op)]
    (atomic-resolver/resolve-transact! @connection evt)
    [db evt]))


(defn undo-resulting-ops
  [event-db event]
  (let [db @@connection]
    (undo/build-undo-event db event-db event)))


(defn undo!
  [evt-db evt]
  (let [db       @@connection
        undo-evt (undo/build-undo-event db evt-db evt)]
    (atomic-resolver/resolve-transact! @connection undo-evt)
    [db undo-evt]))


(defn setup!
  ([repr]
   (setup! repr []))
  ([repr ops]
   (->> repr
        (bfs/build-paste-op @@connection)
        op-resolve-transact!)
   (doseq [op ops]
     (op-resolve-transact! op))))


;; Don't really need to teardown the ops since the only ops right now
;; that aren't part of repr are shortcut ops, and removing the page
;; will remove the shortcut.
(defn teardown!
  [repr]
  (doseq [title (map :page/title repr)]
    (when title
      (-> (atomic-graph-ops/make-page-remove-op title)
          op-resolve-transact!))))


(defn is
  [repr]
  (t/is (= repr (get-repr)))
  ;; Also check there are no blocks outside of pages
  ;; (i.e. get-repr returns all blocks)
  (t/is (empty? (common-db/orphan-block-uids @@connection))))
