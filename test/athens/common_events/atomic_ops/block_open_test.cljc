(ns athens.common-events.atomic-ops.block-open-test
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.bfs             :as bfs]
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.common-events.resolver.undo   :as undo]
    [clojure.test                         :as t]
    [datascript.core                      :as d]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(t/deftest close-opened-block
  (let [page-1-uid  "page-1-uid"
        block-1-uid "block-1-1-uid"
        child-1-uid "child-1-1-1-uid"
        setup-txs   [{:block/uid      page-1-uid
                      :node/title     "close opened block testing page"
                      :block/children [{:block/uid      block-1-uid
                                        :block/string   ""
                                        :block/order    0
                                        :block/open     true
                                        :block/children [{:block/uid      child-1-uid
                                                          :block/string   ""
                                                          :block/order    0
                                                          :block/open     false
                                                          :block/children []}]}]}]]
    (fixture/transact-with-middleware setup-txs)
    (let [atomic-close (atomic-graph-ops/make-block-open-op block-1-uid false)
          atomic-txs   (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection atomic-close)]
      (d/transact! @fixture/connection atomic-txs)
      (t/is (false? (common-db/v-by-ea @@fixture/connection [:block/uid block-1-uid] :block/open))))))


(t/deftest open-closed-block
  (let [page-1-uid  "page-1-uid"
        block-1-uid "block-1-1-uid"
        child-1-uid "child-1-1-1-uid"
        setup-txs   [{:block/uid      page-1-uid
                      :node/title     "close opened block testing page"
                      :block/children [{:block/uid      block-1-uid
                                        :block/string   ""
                                        :block/order    0
                                        :block/open     false
                                        :block/children [{:block/uid      child-1-uid
                                                          :block/string   ""
                                                          :block/order    0
                                                          :block/open     false
                                                          :block/children []}]}]}]]
    (fixture/transact-with-middleware setup-txs)
    (let [atomic-close (atomic-graph-ops/make-block-open-op block-1-uid true)
          atomic-txs   (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection atomic-close)]
      (d/transact! @fixture/connection atomic-txs)
      (t/is (true? (common-db/v-by-ea @@fixture/connection [:block/uid block-1-uid] :block/open))))))


(t/deftest open-opened-block
  (let [page-1-uid  "page-1-uid"
        block-1-uid "block-1-1-uid"
        child-1-uid "child-1-1-1-uid"
        setup-txs   [{:block/uid      page-1-uid
                      :node/title     "close opened block testing page"
                      :block/children [{:block/uid      block-1-uid
                                        :block/string   ""
                                        :block/order    0
                                        :block/open     true
                                        :block/children [{:block/uid      child-1-uid
                                                          :block/string   ""
                                                          :block/order    0
                                                          :block/open     false
                                                          :block/children []}]}]}]]
    (fixture/transact-with-middleware setup-txs)
    (let [atomic-close (atomic-graph-ops/make-block-open-op block-1-uid true)
          atomic-txs   (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection atomic-close)]
      (d/transact! @fixture/connection atomic-txs)
      (t/is (empty? atomic-txs))
      (t/is (true? (common-db/v-by-ea @@fixture/connection [:block/uid block-1-uid] :block/open))))))


(t/deftest close-closed-block
  (let [page-1-uid  "page-1-uid"
        block-1-uid "block-1-1-uid"
        child-1-uid "child-1-1-1-uid"
        setup-txs   [{:block/uid      page-1-uid
                      :node/title     "close opened block testing page"
                      :block/children [{:block/uid      block-1-uid
                                        :block/string   ""
                                        :block/order    0
                                        :block/open     false
                                        :block/children [{:block/uid      child-1-uid
                                                          :block/string   ""
                                                          :block/order    0
                                                          :block/open     false
                                                          :block/children []}]}]}]]
    (fixture/transact-with-middleware setup-txs)
    (let [atomic-close (atomic-graph-ops/make-block-open-op block-1-uid false)
          atomic-txs   (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection atomic-close)]
      (d/transact! @fixture/connection atomic-txs)
      (t/is (empty? atomic-txs))
      (t/is (false? (common-db/v-by-ea @@fixture/connection [:block/uid block-1-uid] :block/open))))))


(t/deftest undo
  (let [test-uid "test-uid"
        setup-repr (fn [open?]
                     [{:page/title     "test-page"
                       :block/children [{:block/uid    test-uid
                                         :block/string "test-str"
                                         :block/open?  open?}]}])
        get-open #(->> [:block/uid test-uid]
                       (common-db/get-internal-representation @@fixture/connection)
                       :block/open?)
        save! #(-> (atomic-graph-ops/make-block-open-op test-uid %)
                   fixture/op-resolve-transact!)]


    (t/testing "undo initializing block to open"
      (fixture/setup! (setup-repr true))
      (t/is (true? (get-open)) "Setup initialized block to open")
      (let [[db evt] (save! false)]
        (t/is (false? (get-open)) "Changed block to close")
        (fixture/undo! db evt)
        (t/is (true? (get-open)) "Undo block back to open")))

    (t/testing "undo initializing block to closed"
      (fixture/setup! (setup-repr false))
      (t/is (false? (get-open)) "Setup initialized block to closed")
      (let [[db evt] (save! true)]
        (t/is (true? (get-open)) "Changed block to open")
        (fixture/undo! db evt)
        (t/is (false? (get-open)) "Undo block back to closed")))

    (t/testing "redo"
      (fixture/setup! (setup-repr true))
      (t/is (true? (get-open)) "Setup initialized block to open")
      (let [[db evt] (save! false)]
        (t/is (false? (get-open)) "Changed block to close")
        (let [[db' evt'] (fixture/undo! db evt)]
          (t/is (true? (get-open)) "Undo block back to open")
          (fixture/undo! db' evt')
          (t/is (false? (get-open)) "Redo block back to closed"))))))
