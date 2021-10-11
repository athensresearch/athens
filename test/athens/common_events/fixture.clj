(ns athens.common-events.fixture
  (:require
    [athens.athens-datoms                   :as athens-datoms]
    [athens.common-db                       :as common-db]
    [athens.common.logging                  :as log]
    [athens.common.utils                    :as common.utils]
    [athens.self-hosted.components.datahike :as athens-datahike]
    [datahike.api                           :as d]))


(def connection (atom nil))


(def in-mem-config
  {:store {:backend :mem
           :id      "default"}
   #_{:backend :file
    :path    "db-testing"
    :id      "default"}})


(def seed-datoms
  athens-datoms/lan-datoms)


(defn integration-test-fixture
  ([test-fn]
   (integration-test-fixture seed-datoms in-mem-config test-fn))

  ([datoms test-fn]
   (integration-test-fixture datoms in-mem-config test-fn))

  ([datoms config test-fn]
   (d/create-database config)
   (let [conn (d/connect config)]
     (d/transact conn athens-datahike/schema)
     (d/transact conn datoms)
     (reset! connection conn)

     (test-fn)

     (reset! connection nil)
     (d/release conn)
     (d/delete-database config))))


(defn random-tmp-folder-config
  []
  {:store {:backend :file
           :path    (str "/tmp/example-" (common.utils/gen-block-uid))}})


(defn transact-with-middleware
  [txs]
  (let [processed-txs (->> txs
                           (common-db/linkmaker @@connection)
                           (common-db/orderkeeper @@connection))]
    (log/debug "transact-with-middleware"
               "\nfrom:" (pr-str txs)
               "\nto:" (pr-str processed-txs))
    (d/transact @connection processed-txs)))
