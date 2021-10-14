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
