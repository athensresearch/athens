(ns athens.common-events.fixture
  (:require
    [athens.athens-datoms                   :as athens-datoms]
    [athens.self-hosted.components.datahike :as athens-datahike]
    [datahike.api                           :as d]))


(def connection (atom nil))


(def in-mem-config
  {:store {:backend :mem
           :id      "default"}})


(defn integration-test-fixture
  ([test-fn]
   (integration-test-fixture in-mem-config test-fn))

  ([config test-fn]
   (d/create-database config)
   (let [conn (d/connect config)]
     (d/transact conn athens-datahike/schema)
     (d/transact conn athens-datoms/lan-datoms)
     (reset! connection conn)

     (test-fn)

     (reset! connection nil)
     (d/release conn)
     (d/delete-database config))))
