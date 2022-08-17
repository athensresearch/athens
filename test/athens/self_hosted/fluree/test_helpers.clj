(ns athens.self-hosted.fluree.test-helpers
  (:require
    [athens.self-hosted.fluree.utils :as utils]
    [athens.self-hosted.fluree.utils :as fu]
    [clojure.string :as str]
    [fluree.db.api :as fdb]))


(def conn-atom (atom nil))
(def ledger-atom (atom nil))
(def last-transacted-block-atom (atom nil))


(defn conn+ledger
  []
  [@conn-atom @ledger-atom])


(defn with-ledger
  [f]
  (reset! conn-atom (fdb/connect "http://localhost:8090"))
  ;; Due to https://github.com/fluree/ledger/issues/98, we can't just
  ;; recreate ledgers with the same name, so we have to be creating and
  ;; discarding the names.
  (reset! ledger-atom (str "athens/" (str/replace (random-uuid) #"-" "")))
  (reset! last-transacted-block-atom nil)
  @(fdb/new-ledger @conn-atom @ledger-atom)
  (fdb/wait-for-ledger-ready @conn-atom @ledger-atom)
  (f)
  @(fdb/delete-ledger @conn-atom @ledger-atom)
  (fdb/close @conn-atom)
  (reset! conn-atom nil)
  (reset! ledger-atom nil)
  (reset! last-transacted-block-atom nil))


(defn transact!
  [tx-data]
  (let [res (utils/transact! @conn-atom @ledger-atom tx-data)]
    (reset! last-transacted-block-atom (:block res))
    res))


(defn query
  [q-data]
  (utils/query @conn-atom @ledger-atom q-data))


(defn wait-for-block
  "Useful when querying right after transacting, as fluree can be behind on queries.
  Only use the 0-arity when the last transaction was via test-helpers/transact!."
  ([]
   (wait-for-block @last-transacted-block-atom))
  ([expected-block]
   (utils/wait-for-block @conn-atom @ledger-atom expected-block)))
