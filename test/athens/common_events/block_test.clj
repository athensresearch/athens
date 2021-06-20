(ns athens.common-events.block-test
  (:require
    [athens.common-db              :as common-db]
    [athens.common-events          :as common-events]
    [athens.common-events.fixture  :as fixture]
    [athens.common-events.resolver :as resolver]
    [clojure.test                  :as t]
    [datahike.api                  :as d]))


(t/use-fixtures :each fixture/integration-test-fixture)


(t/deftest new-block-tests
  (t/testing "Adding new block to new page"
    (let [page-1-uid  "page-1-uid"
          child-1-uid "child-1-1-uid"
          child-2-uid "child-1-2-uid"
          setup-txs   [{:db/id          -1
                        :block/uid      page-1-uid
                        :node/title     "test page 1"
                        :block/children {:db/id          -2
                                         :block/uid      child-1-uid
                                         :block/string   ""
                                         :block/order    1
                                         :block/children []}}]]
      (d/transact @fixture/connection setup-txs)
      (let [page-1-eid      (common-db/e-by-av @@fixture/connection
                                               :block/uid page-1-uid)
            child-1-eid     (common-db/e-by-av @@fixture/connection
                                               :block/uid child-1-uid)
            new-block-event (common-events/build-new-block-event -1
                                                                 page-1-eid
                                                                 1
                                                                 child-2-uid)
            new-block-txs   (resolver/resolve-event-to-tx @@fixture/connection
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
          (t/is (= #{[child-1-eid] [child-2-eid]} children))))))
  ;; TODO more test cases for `:datascript/new-block` event
  )


(t/deftest add-child-tests
  (t/testing "Adding 1st child"
    (let [parent-1-uid "test-parent-1-uid"
          child-1-uid  "test-child-1-1-uid"
          setup-txs    [{:db/id          -1
                         :node/title     "test page"
                         :block/uid      "page-uid"
                         :block/children {:db/id          -2
                                          :block/uid      parent-1-uid
                                          :block/string   ""
                                          :block/order    0
                                          :block/children []}}]]
      (d/transact @fixture/connection setup-txs)
      (let [eid             (common-db/e-by-av @@fixture/connection
                                               :block/uid parent-1-uid)
            add-child-event (common-events/build-add-child-event -1 eid child-1-uid)
            txs             (resolver/resolve-event-to-tx @@fixture/connection
                                                          add-child-event)
            query-children  '[:find ?children
                              :in $ ?eid
                              :where [?eid :block/children ?children]]]
        (t/is (= #{} (d/q query-children @@fixture/connection eid)))
        (d/transact @fixture/connection txs)
        (let [child-eid (common-db/e-by-av @@fixture/connection
                                           :block/uid child-1-uid)
              children  (d/q query-children @@fixture/connection eid)]
          (t/is (seq children))
          (t/is (= #{[child-eid]} children))))))

  (t/testing "Adding 2nd child"
    (let [parent-uid  "test-parent-2-uid"
          child-1-uid "test-child-2-1-uid"
          child-2-uid "test-child-2-2-uid"
          setup-txs   [{:db/id          -1
                        :node/title     "test page"
                        :block/uid      "page-uid"
                        :block/children {:db/id          -2
                                         :block/uid      parent-uid
                                         :block/string   ""
                                         :block/order    1
                                         :block/children {:db/id          -3
                                                          :block/uid      child-1-uid
                                                          :block/string   ""
                                                          :block/order    1
                                                          :block/children []}}}]]
      (d/transact @fixture/connection setup-txs)

      (let [parent-eid      (common-db/e-by-av @@fixture/connection
                                               :block/uid parent-uid)
            child-1-eid     (common-db/e-by-av @@fixture/connection
                                               :block/uid child-1-uid)
            child-1         (d/pull @@fixture/connection
                                    [:block/uid :block/order]
                                    child-1-eid)
            add-child-event (common-events/build-add-child-event -1 parent-eid child-2-uid)
            add-child-txs   (resolver/resolve-event-to-tx @@fixture/connection
                                                          add-child-event)
            query-children  '[:find ?child
                              :in $ ?eid
                              :where [?eid :block/children ?child]]]

        ;; before we add second child, check for 1st one
        (t/is (= #{[child-1-eid]} (d/q query-children @@fixture/connection parent-eid)))
        (t/is (= {:block/uid   child-1-uid
                  :block/order 1}
                 child-1))

        ;; add second child
        (d/transact @fixture/connection add-child-txs)
        (let [child-2-eid (common-db/e-by-av @@fixture/connection
                                             :block/uid child-2-uid)
              children    (d/q query-children @@fixture/connection parent-eid)
              child-2     (d/pull @@fixture/connection
                                  [:block/uid :block/order]
                                  child-2-eid)]
          (t/is (seq children))
          (t/is (= #{[child-2-eid] [child-1-eid]} children))
          (t/is (= {:block/uid   child-2-uid
                    :block/order 0}
                   child-2))
          (t/is (= {:block/uid   child-1-uid
                    :block/order 1}
                   child-1)))))))
