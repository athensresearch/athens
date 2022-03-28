(ns athens.self-hosted.fluree.utils
  (:require
    [clojure.core.async :as async]
    [fluree.db.api :as fdb]))


(defn transact!
  [conn ledger tx-data]
  (let [ret @(fdb/transact conn ledger tx-data)]
    (if (ex-message ret)
      (throw ret)
      ret)))


(defn query
  ([db q-data]
   @(fdb/query db q-data))
  ([conn ledger q-data]
   (-> conn
       (fdb/db ledger)
       (query q-data))))


(defn wait-for-block
  [conn ledger expected-block]
  (async/<!! (fdb/db conn ledger {:syncTo expected-block})))
