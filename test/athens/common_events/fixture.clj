(ns athens.common-events.fixture
  (:require
    [athens.athens-datoms                     :as athens-datoms]
    [athens.common-db                         :as common-db]
    [athens.common.logging                    :as log]
    [datascript.core                          :as d]))


(def connection (atom nil))


(def seed-datoms
  athens-datoms/lan-datoms)


(defn integration-test-fixture
  ([test-fn]
   (integration-test-fixture seed-datoms test-fn))

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
