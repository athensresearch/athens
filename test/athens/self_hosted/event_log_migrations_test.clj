(ns athens.self-hosted.event-log-migrations-test
  (:require
    [athens.self-hosted.event-log :as event-log]
    [athens.self-hosted.event-log-migrations :as event-log-migrations]
    [athens.self-hosted.fluree.test-helpers :as fth]
    [athens.self-hosted.fluree.utils :as fu]
    [athens.self-hosted.migrate :as migrate]
    [clojure.test :as t :refer [deftest testing is]]
    [fluree.db.api :as fdb])
  (:import
    (clojure.lang
      ExceptionInfo)))


(t/use-fixtures :each fth/with-ledger)


(deftest ^:fluree migration-to-1
  (let [[conn ledger] (fth/conn+ledger)
        all           #(fu/query @fth/conn-atom @fth/ledger-atom {:select ["*"]
                                                                  :from   "event"})]
    (migrate/migrate-ledger! conn ledger event-log-migrations/migrations :up-to 1)
    (run! #(fth/transact! [{:_id :event :event/id %}]) (range 4))
    (fth/wait-for-block)
    (is (= 4 (count (all))) "Should have 4 events")))


(deftest ^:fluree migration-to-2
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


(deftest ^:fluree migration-to-3
  (let [[conn ledger]    (fth/conn+ledger)
        new-event        #(let [self-tempid (str "event$self-" %)]
                            {:_id         self-tempid
                             :event/id    %
                             :event/order self-tempid})
        ordered-all      #(fu/query @fth/conn-atom @fth/ledger-atom
                                    {:select {"?event" ["*"]}
                                     :where  [["?event" "event/order" (str "#(> ?order " %2 ")")]]
                                     :opts   {:orderBy [%1 "?order"]}})
        get-int-event-id #(-> % (get "event/id") parse-long)]
    (migrate/migrate-ledger! conn ledger event-log-migrations/migrations :up-to 1)
    (run! #(fth/transact! [{:_id :event :event/id %}]) (range 4))
    (migrate/migrate-ledger! conn ledger event-log-migrations/migrations :up-to 3)
    (run! #(fth/transact! [(new-event %)]) (range 4 8))
    (let [order-for-0 (event-log/event-id->order (fdb/db @fth/conn-atom @fth/ledger-atom) "0")
          order-for-3 (event-log/event-id->order (fdb/db @fth/conn-atom @fth/ledger-atom) "3")]
      (is (= '(7 6 5 4 3 2 1 0) (map get-int-event-id (ordered-all "DESC" (dec order-for-0)))))
      (is (= '(0 1 2 3 4 5 6 7) (map get-int-event-id (ordered-all "ASC"  (dec order-for-0)))))
      (is (= '(7 6 5 4)         (map get-int-event-id (ordered-all "DESC" order-for-3))))
      (is (= '(4 5 6 7)         (map get-int-event-id (ordered-all "ASC"  order-for-3)))))))
