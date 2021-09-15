(ns athens.common-events.atomic-ops-test
  (:require
   [athens.common-db                     :as common-db]
   [athens.common-events.fixture         :as fixture]
   [athens.common-events.graph.atomic    :as atomic-graph-ops]
   [athens.common-events.resolver.atomic :as atomic-resolver]
   [clojure.test                         :as t]
   [datahike.api                         :as d]))


(t/use-fixtures :each fixture/integration-test-fixture)


(t/deftest block-new-test
  (t/testing "Create new block as child of other block"
    (let [page-1-uid  "page-1-uid"
          child-1-uid "child-1-1-uid"
          child-2-uid "child-1-2-uid"
          setup-txs   [{:block/uid      page-1-uid
                        :node/title     "test page 1"
                        :block/children {:block/uid      child-1-uid
                                         :block/string   ""
                                         :block/order    0
                                         :block/children []}}]]
      (d/transact @fixture/connection setup-txs)
      (let [page-1-eid      (common-db/e-by-av @@fixture/connection
                                               :block/uid page-1-uid)
            child-1-eid     (common-db/e-by-av @@fixture/connection
                                               :block/uid child-1-uid)
            new-block-event (atomic-graph-ops/make-block-new-op child-1-uid
                                                                child-2-uid
                                                                0)
            new-block-txs   (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                     new-block-event)
            query-children  '[:find ?child
                              :in $ ?eid
                              :where [?eid :block/children ?child]]]
        (t/is (= #{[child-1-eid]} (d/q query-children @@fixture/connection page-1-eid)))
        (d/transact @fixture/connection new-block-txs)
        (t/is (= #{[child-1-eid]} (d/q query-children @@fixture/connection page-1-eid)))
        (let [child-2-eid (common-db/e-by-av @@fixture/connection
                                             :block/uid child-2-uid)
              children (d/q query-children @@fixture/connection child-1-eid)]
          (t/is (seq children))
          (t/is (= #{[child-2-eid]} children))))))

  ;; NOTE: order of these `t/testing` is important, because we preserve DB between `t/testing` runs within same `t/deftest`
  (t/testing "Create new block in page"
    (let [page-1-uid  "page-1-uid"
          child-1-uid "child-1-1-uid"
          child-2-uid "child-1-2-uid"
          setup-txs   [{:block/uid      page-1-uid
                        :node/title     "test page 1"
                        :block/children {:block/uid      child-1-uid
                                         :block/string   ""
                                         :block/order    0
                                         :block/children []}}]]
      (d/transact @fixture/connection setup-txs)
      (let [page-1-eid      (common-db/e-by-av @@fixture/connection
                                               :block/uid page-1-uid)
            child-1-eid     (common-db/e-by-av @@fixture/connection
                                               :block/uid child-1-uid)
            new-block-event (atomic-graph-ops/make-block-new-op page-1-uid
                                                                child-2-uid
                                                                1)
            new-block-txs   (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                     new-block-event)
            query-children  '[:find ?child
                              :in $ ?eid
                              :where [?eid :block/children ?child]]]
        (t/is (= #{[child-1-eid]} (d/q query-children @@fixture/connection page-1-eid)))
        (d/transact @fixture/connection new-block-txs)
        (let [child-2-eid (common-db/e-by-av @@fixture/connection
                                             :block/uid child-2-uid)
              children (d/q query-children @@fixture/connection page-1-eid)]
          (t/is (seq children))
          (t/is (= #{[child-1-eid] [child-2-eid]} children)))))))


(test/deftest page-new-test
              (let [test-title        "test page title"
                    test-page-uid     "test-page-uid-1"
                    test-block-uid    "test-block-uid-1"
                    page-new-event   (atomic-graph-ops/make-page-new-op test-title
                                                                        test-page-uid
                                                                        test-block-uid)
                    page-new-txs              (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                                       page-new-event)]
                (d/transact @fixture/connection page-new-txs)
                (let [e-by-title (d/q '[:find ?e
                                        :where [?e :node/title ?title]
                                        :in $ ?title]
                                      @@fixture/connection test-title)
                      e-by-uid (d/q '[:find ?e
                                      :where [?e :block/uid ?uid]
                                      :in $ ?uid]
                                    @@fixture/connection test-page-uid)]
                  (test/is (seq e-by-title))
                  (test/is (= e-by-title e-by-uid)))))