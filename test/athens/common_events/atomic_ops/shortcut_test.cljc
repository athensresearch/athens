(ns athens.common-events.atomic-ops.shortcut-test
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [clojure.test                         :as t]
    [datascript.core                      :as d]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(t/deftest shortcut-add-test
  (let [setup-tx  [{:block/uid      "parent-uid"
                    :node/title     "Hello World!"
                    :block/children []}]]
    (fixture/transact-with-middleware setup-tx)
    (t/is (= 0
             (-> (common-db/get-sidebar-elements @@fixture/connection)
                 (count))))
    (let [shortcut-new-op  (atomic-graph-ops/make-shortcut-new-op "Hello World!")
          shortcut-new-txs (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                    shortcut-new-op)]
      (d/transact! @fixture/connection shortcut-new-txs)
      (t/is (= 1
               (-> (common-db/get-sidebar-elements @@fixture/connection)
                   (count)))))))


(t/deftest shortcut-remove-test
  (let [setup-tx  [{:block/uid      "parent-uid"
                    :node/title     "Hello World!"
                    :block/children []}]]
    (fixture/transact-with-middleware setup-tx)
    (t/is (= 0
             (-> (common-db/get-sidebar-elements @@fixture/connection)
                 (count))))
    (let [shortcut-new-op     (atomic-graph-ops/make-shortcut-new-op "Hello World!")
          shortcut-new-txs    (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                       shortcut-new-op)
          shortcut-remove-op (atomic-graph-ops/make-shortcut-remove-op "Hello World!")
          shortcut-remove-txs   (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                         shortcut-remove-op)]

      (d/transact! @fixture/connection shortcut-new-txs)
      (t/is (= 1
               (-> (common-db/get-sidebar-elements @@fixture/connection)
                   (count))))

      (d/transact! @fixture/connection shortcut-remove-txs)
      (t/is (= 0
               (-> (common-db/get-sidebar-elements @@fixture/connection)
                   (count)))))))


