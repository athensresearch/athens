(ns athens.common-events.orderkeeper-test
  (:require
    [athens.common-db              :as common-db]
    #_[athens.common-events          :as common-events]
    [athens.common-events.fixture  :as fixture]
    #_[athens.common-events.resolver :as resolver]
    [clojure.test                  :as t]
    [datahike.api                  :as d]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(defn transact-with-orderkeeper
  [tx-data]
  (d/transact @fixture/connection (common-db/orderkeeper @@fixture/connection tx-data)))


(t/deftest order-change-needed
  (let [target-page-title "target-page-1-title"
        target-page-uid   "target-page-1-uid"
        block-uid-1       "target-block-1-uid"
        setup-tx          [{:node/title     target-page-title
                            :block/uid      target-page-uid
                            :block/children [{:block/uid    block-uid-1
                                              :block/order  1
                                              :block/string "Lalala"}]}]
        orderkeeper-txs   (common-db/orderkeeper @@fixture/connection setup-tx)]
    (t/is (= (inc (count setup-tx)) (count orderkeeper-txs)))
    (t/is (= {:block/uid   block-uid-1
              :block/order 0}
             (last orderkeeper-txs)))))


(t/deftest order-change-needed-with-tie-resolution
  (t/testing "missing `:block/order` 0"
    (let [target-page-title "target-page-1-title"
          target-page-uid   "target-page-1-uid"
          block-uid-1       "target-block-1-uid"
          block-uid-2       "target-block-2-uid"
          setup-tx          [{:node/title     target-page-title
                              :block/uid      target-page-uid
                              :block/children [{:block/uid    block-uid-1
                                                :block/order  1
                                                :block/string "Lalala"}
                                               {:block/uid    block-uid-2
                                                :block/order  1
                                                :block/string "Ulalala"}]}]
          orderkeeper-txs   (common-db/orderkeeper @@fixture/connection setup-tx)]
      (t/is (= (inc (count setup-tx)) (count orderkeeper-txs)))
      (t/is (= {:block/uid   block-uid-1
                :block/order 0}
               (last orderkeeper-txs)))))

  (t/testing "double `:block/order` 0"
    (let [target-page-title "target-page-1-title"
          target-page-uid   "target-page-1-uid"
          block-uid-1       "target-block-1-uid"
          block-uid-2       "target-block-2-uid"
          setup-tx          [{:node/title     target-page-title
                              :block/uid      target-page-uid
                              :block/children [{:block/uid    block-uid-1
                                                :block/order  0
                                                :block/string "Lalala"}
                                               {:block/uid    block-uid-2
                                                :block/order  0
                                                :block/string "Ulalala"}]}]
          orderkeeper-txs   (common-db/orderkeeper @@fixture/connection setup-tx)]
      (t/is (= (inc (count setup-tx)) (count orderkeeper-txs)))
      (t/is (= {:block/uid   block-uid-2
                :block/order 1}
               (last orderkeeper-txs)))))

  (t/testing "that we're not just sorting by `:block/uid`, and actually `:block/order` is our primary sort"
    (let [target-page-title "target-page-1-title"
          target-page-uid   "target-page-1-uid"
          block-uid-1       "target-block-1-uid"
          block-uid-2       "target-block-2-uid"
          setup-tx          [{:node/title     target-page-title
                              :block/uid      target-page-uid
                              :block/children [{:block/uid    block-uid-2
                                                :block/order  2
                                                :block/string "Lalala"}
                                               {:block/uid    block-uid-1
                                                :block/order  3
                                                :block/string "Ulalala"}]}]
          orderkeeper-txs   (common-db/orderkeeper @@fixture/connection setup-tx)]
      (t/is (= (+ 2 (count setup-tx)) (count orderkeeper-txs)))
      (t/is (= #{{:block/uid   block-uid-2
                  :block/order 0}
                 {:block/uid   block-uid-1
                  :block/order 1}}
               (set
                 (drop (count setup-tx)
                       orderkeeper-txs)))))))


(t/deftest no-change-needed
  (let [target-page-title "target-page-1-title"
        target-page-uid   "target-page-1-uid"
        block-uid-1       "target-block-1-uid"
        setup-tx          [{:node/title     target-page-title
                            :block/uid      target-page-uid
                            :block/children [{:block/uid    block-uid-1
                                              :block/order  0
                                              :block/string "Lalala"}]}]
        orderkeeper-txs   (common-db/orderkeeper @@fixture/connection setup-tx)]
    (t/is (= (count setup-tx) (count orderkeeper-txs)))))
