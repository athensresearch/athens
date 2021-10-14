(ns athens.common-events.atomic-ops.block-remove-test
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [clojure.test                         :as t]
    [datascript.core                      :as d]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(t/deftest block-remove-onlychild-test
  (let [page-uid   "page-1-uid"
        parent-uid "parent-1-uid"
        child-uid  "child-1-uid"
        setup-txs  [{:block/uid      page-uid
                     :node/title     "test page 1"
                     :block/children {:block/uid      parent-uid
                                      :block/string   ""
                                      :block/order    0
                                      :block/children [{:block/uid      child-uid
                                                        :block/string   ""
                                                        :block/order    0
                                                        :block/children []}]}}]]
    (fixture/transact-with-middleware setup-txs)
    (t/is (= 1 (-> (common-db/get-block @@fixture/connection [:block/uid page-uid])
                   :block/children
                   count))
          "Page should have only 1 child block after setup.")
    (t/is (= 1 (-> (common-db/get-block @@fixture/connection [:block/uid parent-uid])
                   :block/children
                   count))
          "Parent should have only 1 child block after setup.")
    (t/is (= 0 (-> (common-db/get-block @@fixture/connection [:block/uid child-uid])
                   :block/children
                   count)))
    (let [block-remove-op (graph-ops/build-block-remove-op @@fixture/connection child-uid)
          block-split-tx  (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection block-remove-op)]
      (d/transact! @fixture/connection block-split-tx)
      (let [page          (common-db/get-block @@fixture/connection [:block/uid page-uid])
            parent        (common-db/get-block @@fixture/connection [:block/uid parent-uid])
            child-exists? (common-db/e-by-av @@fixture/connection :block/uid child-uid)]
        (t/is (not child-exists?)
              "After `:block/remove` block should be gone for good")
        (t/is (= 1 (-> page :block/children count))
              "Page should have 1 child after block split")
        (t/is (= 0 (-> parent :block/children count))
              "Parent should not have children after `:block/remove`")))))


(t/deftest block-remove-childless-kids-test
  (t/testing "removing 1st child"
    (let [page-uid    "page-1-uid"
          parent-uid  "parent-1-uid"
          child-1-uid "child-1-1-uid"
          child-2-uid "child-1-2-uid"
          setup-txs   [{:block/uid      page-uid
                        :node/title     "test page 1"
                        :block/children {:block/uid      parent-uid
                                         :block/string   ""
                                         :block/order    0
                                         :block/children [{:block/uid      child-1-uid
                                                           :block/string   ""
                                                           :block/order    0
                                                           :block/children []}
                                                          {:block/uid      child-2-uid
                                                           :block/string   ""
                                                           :block/order    1
                                                           :block/children []}]}}]]
      (fixture/transact-with-middleware setup-txs)
      (t/is (= 1 (-> (common-db/get-block @@fixture/connection [:block/uid page-uid])
                     :block/children
                     count))
            "Page should have only 1 child block after setup.")
      (t/is (= 2 (-> (common-db/get-block @@fixture/connection [:block/uid parent-uid])
                     :block/children
                     count))
            "Parent should have only 2 child block after setup.")
      (t/is (= 0 (-> (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
                     :block/children
                     count)))
      (t/is (= 0 (-> (common-db/get-block @@fixture/connection [:block/uid child-2-uid])
                     :block/children
                     count)))
      (let [block-remove-op (graph-ops/build-block-remove-op @@fixture/connection child-1-uid)
            block-split-tx  (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection block-remove-op)]
        (d/transact! @fixture/connection block-split-tx)
        (let [page          (common-db/get-block @@fixture/connection [:block/uid page-uid])
              parent        (common-db/get-block @@fixture/connection [:block/uid parent-uid])
              child-exists? (common-db/e-by-av @@fixture/connection :block/uid child-1-uid)
              second-child  (common-db/get-block @@fixture/connection [:block/uid child-2-uid])]
          (t/is (not child-exists?)
                "After `:block/remove` block should be gone for good")
          (t/is (= 1 (-> page :block/children count))
                "Page should have 1 child after block split")
          (t/is (= 1 (-> parent :block/children count))
                "Parent should not have 1 child after `:block/remove`")
          (t/is (= 0 (-> second-child :block/order)))))))

  (t/testing "removing last child"
    (let [page-uid    "page-2-uid"
          parent-uid  "parent-2-uid"
          child-1-uid "child-2-1-uid"
          child-2-uid "child-2-2-uid"
          setup-txs   [{:block/uid      page-uid
                        :node/title     "test page 1"
                        :block/children {:block/uid      parent-uid
                                         :block/string   ""
                                         :block/order    0
                                         :block/children [{:block/uid      child-1-uid
                                                           :block/string   ""
                                                           :block/order    0
                                                           :block/children []}
                                                          {:block/uid      child-2-uid
                                                           :block/string   ""
                                                           :block/order    1
                                                           :block/children []}]}}]]
      (fixture/transact-with-middleware setup-txs)
      (t/is (= 1 (-> (common-db/get-block @@fixture/connection [:block/uid page-uid])
                     :block/children
                     count))
            "Page should have only 1 child block after setup.")
      (t/is (= 2 (-> (common-db/get-block @@fixture/connection [:block/uid parent-uid])
                     :block/children
                     count))
            "Parent should have only 2 child block after setup.")
      (t/is (= 0 (-> (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
                     :block/children
                     count)))
      (t/is (= 0 (-> (common-db/get-block @@fixture/connection [:block/uid child-2-uid])
                     :block/children
                     count)))
      (let [block-remove-op (graph-ops/build-block-remove-op @@fixture/connection child-2-uid)
            block-split-tx  (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection block-remove-op)]
        (d/transact! @fixture/connection block-split-tx)
        (let [page          (common-db/get-block @@fixture/connection [:block/uid page-uid])
              parent        (common-db/get-block @@fixture/connection [:block/uid parent-uid])
              child-exists? (common-db/e-by-av @@fixture/connection :block/uid child-2-uid)
              first-child  (common-db/get-block @@fixture/connection [:block/uid child-1-uid])]
          (t/is (not child-exists?)
                "After `:block/remove` block should be gone for good")
          (t/is (= 1 (-> page :block/children count))
                "Page should have 1 child after block split")
          (t/is (= 1 (-> parent :block/children count))
                "Parent should not have 1 child after `:block/remove`")
          (t/is (= 0 (-> first-child :block/order)))))))

  (t/testing "removing middle child"
    (let [page-uid    "page-3-uid"
          parent-uid  "parent-3-uid"
          child-1-uid "child-3-1-uid"
          child-2-uid "child-3-2-uid"
          child-3-uid "child-3-3-uid"
          setup-txs   [{:block/uid      page-uid
                        :node/title     "test page 1"
                        :block/children {:block/uid      parent-uid
                                         :block/string   ""
                                         :block/order    0
                                         :block/children [{:block/uid      child-1-uid
                                                           :block/string   ""
                                                           :block/order    0
                                                           :block/children []}
                                                          {:block/uid      child-2-uid
                                                           :block/string   ""
                                                           :block/order    1
                                                           :block/children []}
                                                          {:block/uid      child-3-uid
                                                           :block/string   ""
                                                           :block/order    2
                                                           :block/children []}]}}]]
      (fixture/transact-with-middleware setup-txs)
      (t/is (= 1 (-> (common-db/get-block @@fixture/connection [:block/uid page-uid])
                     :block/children
                     count))
            "Page should have only 1 child block after setup.")
      (t/is (= 3 (-> (common-db/get-block @@fixture/connection [:block/uid parent-uid])
                     :block/children
                     count))
            "Parent should have only 3 child block after setup.")
      (t/is (= 0 (-> (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
                     :block/children
                     count)))
      (t/is (= 0 (-> (common-db/get-block @@fixture/connection [:block/uid child-2-uid])
                     :block/children
                     count)))
      (t/is (= 0 (-> (common-db/get-block @@fixture/connection [:block/uid child-3-uid])
                     :block/children
                     count)))
      (let [block-remove-op (graph-ops/build-block-remove-op @@fixture/connection child-2-uid)
            block-split-tx  (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection block-remove-op)]
        (d/transact! @fixture/connection block-split-tx)
        (let [page          (common-db/get-block @@fixture/connection [:block/uid page-uid])
              parent        (common-db/get-block @@fixture/connection [:block/uid parent-uid])
              child-exists? (common-db/e-by-av @@fixture/connection :block/uid child-2-uid)
              first-child  (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
              last-child  (common-db/get-block @@fixture/connection [:block/uid child-3-uid])]
          (t/is (not child-exists?)
                "After `:block/remove` block should be gone for good")
          (t/is (= 1 (-> page :block/children count))
                "Page should have 1 child after block split")
          (t/is (= 2 (-> parent :block/children count))
                "Parent should not have 2 child after `:block/remove`")
          (t/is (= 0 (-> first-child :block/order)))
          (t/is (= 2 (-> last-child :block/order))))))))


(t/deftest block-remove-parent
  (t/testing "Make sure we remove subtree"
    )
  (t/testing "Make sure we remove subtree, event if there was a block ref below"
    ))
