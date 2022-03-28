(ns athens.self-hosted.event-log-migrations-test
  (:require
    [athens.self-hosted.event-log-migrations :as event-log-migrations]
    [athens.self-hosted.fluree.test-helpers :as fth]
    [athens.self-hosted.fluree.utils :as fu]
    [athens.self-hosted.migrate :as migrate]
    [clojure.test :as t :refer [deftest testing is]])
  (:import
    (clojure.lang
      ExceptionInfo)))


(t/use-fixtures :each fth/with-ledger)


(defn all-events-subjects
  []
  (fu/query @fth/conn-atom @fth/ledger-atom {:select ["*"]
                                             :from   "event"}))


(deftest migration-to-1
  (let [[conn ledger] (fth/conn+ledger)]
    (migrate/migrate-ledger! conn ledger event-log-migrations/migrations :up-to 1)
    (run! #(fth/transact! [{:_id :event :event/id %}]) (range 4))
    (fth/wait-for-block)
    (is (= 4 (count (all-events-subjects))) "Should have 4 events")))


(deftest migration-to-2
  (let [[conn ledger] (fth/conn+ledger)]
    (migrate/migrate-ledger! conn ledger event-log-migrations/migrations :up-to 2)
    (fth/transact! [{:_id :event :event/id "1" :event/data "1"}])
    (is (thrown-with-msg? ExceptionInfo #"Predicate spec failed"
          (fth/transact! [{:_id [:event/id "1"]
                           :event/id "2"}]))
        "Should not allow changing :event/id")
    (is (thrown-with-msg? ExceptionInfo #"Predicate spec failed"
          (fth/transact! [{:_id [:event/id "1"]
                           :event/data "2"}]))
        "Should not allow changing :event/data")))


(deftest migration-to-3
  (let [[conn ledger] (fth/conn+ledger)]
    (migrate/migrate-ledger! conn ledger event-log-migrations/migrations :up-to 1)
    (run! #(fth/transact! [{:_id :event :event/id %}]) (range 4))
    (migrate/migrate-ledger! conn ledger event-log-migrations/migrations :up-to 3)
    (is (= '(4 3 2 1) (map #(get % "event/order") (all-events-subjects))))))
