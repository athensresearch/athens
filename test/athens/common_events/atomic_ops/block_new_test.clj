(ns athens.common-events.atomic-ops.block-new-test
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
              children    (d/q query-children @@fixture/connection child-1-eid)]
          (t/is (seq children))
          (t/is (= #{[child-2-eid]} children))))))

  (t/testing "Create new block between 2 blocks"
    (let [page-1-uid  "page-2-uid"
          child-1-uid "child-2-1-uid"
          child-2-uid "child-2-2-uid"
          setup-txs   [{:block/uid      page-1-uid
                        :node/title     "test page 2"
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
              children    (d/q query-children @@fixture/connection page-1-eid)
              block       (common-db/get-block @@fixture/connection
                                               [:block/uid child-2-uid])]
          (t/is (= 1 (:block/order block)))
          (t/is (seq children))
          (t/is (= #{[child-1-eid] [child-2-eid]} children))))))

  (t/testing "Create new block in page"
    (let [page-1-uid  "page-3-uid"
          child-1-uid "child-3-1-uid"
          child-2-uid "child-3-2-uid"
          child-3-uid "child-3-3-uid"
          setup-txs   [{:block/uid      page-1-uid
                        :node/title     "test page 3"
                        :block/children [{:block/uid      child-1-uid
                                          :block/string   ""
                                          :block/order    0
                                          :block/children []}
                                         {:block/uid      child-2-uid
                                          :block/string   ""
                                          :block/order    1
                                          :block/children []}]}]]
      (d/transact @fixture/connection setup-txs)
      (let [page-1-eid      (common-db/e-by-av @@fixture/connection
                                               :block/uid page-1-uid)
            child-1-eid     (common-db/e-by-av @@fixture/connection
                                               :block/uid child-1-uid)
            child-2-eid     (common-db/e-by-av @@fixture/connection
                                               :block/uid child-2-uid)
            new-block-event (atomic-graph-ops/make-block-new-op page-1-uid
                                                                child-3-uid
                                                                1)
            new-block-txs   (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                     new-block-event)
            query-children  '[:find ?child
                              :in $ ?eid
                              :where [?eid :block/children ?child]]]
        (t/is (= #{[child-1-eid] [child-2-eid]} (d/q query-children @@fixture/connection page-1-eid)))
        (d/transact @fixture/connection new-block-txs)
        (let [child-3-eid     (common-db/e-by-av @@fixture/connection
                                                 :block/uid child-3-uid)
              children (d/q query-children @@fixture/connection page-1-eid)
              block-1    (common-db/get-block @@fixture/connection
                                              [:block/uid child-1-uid])
              block-2    (common-db/get-block @@fixture/connection
                                              [:block/uid child-2-uid])
              block-3    (common-db/get-block @@fixture/connection
                                              [:block/uid child-3-uid])]
          (t/is (seq children))
          (t/is (= #{[child-1-eid] [child-2-eid] [child-3-eid]} children))
          (t/is (= 0 (:block/order block-1)))
          (t/is (= 1 (:block/order block-3)))
          (t/is (= 2 (:block/order block-2))))))))


