(ns athens.self-hosted.fluree.utils
  (:require
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


(defn sync-to
  [conn ledger block]
  (query conn ledger
         ;; Look up the first collection name.
         ;; This can be any query, the cheaper the better, all that
         ;; matters is the :syncTo option.
         {:selectOne "?o"
          :where     [["?s" "_collection/name" "?o"]]
          :opts      {:syncTo block}}))
