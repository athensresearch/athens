(ns athens.common-events.atomic-ops.block-new-test
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    #_[clojure.pprint                       :as pp]
    [clojure.test                         :as t]
    [datascript.core                      :as d]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(t/deftest concurrency-simulations-block-new-v2-1
  (t/testing "just 2 events starting from the same point, but with new concurrency compatible model, wooh!"
    (let [page-1-uid    "page-4-uid"
          block-1-uid   "block-4-1-uid"
          block-2-uid   "block-4-2-uid"
          block-3-1-uid "block-4-3-1-uid"
          block-3-2-uid "block-4-3-2-uid"
          setup-txs     [{:block/uid      page-1-uid
                          :node/title     "test page 4"
                          :block/children [{:block/uid      block-1-uid
                                            :block/string   ""
                                            :block/order    0
                                            :block/children []}
                                           {:block/uid      block-2-uid
                                            :block/string   ""
                                            :block/order    1
                                            :block/children []}]}]]
      (fixture/transact-with-middleware setup-txs)
      ;; we want children to be:
      ;; 0: block-1-uid
      ;; 1: block-3-1-uid
      ;; 2: block-2-uid
      ;; 3: block-3-2-uid
      ;; now, there is no order to resolve and apply these events so we have what was intended
      (t/testing "event-1 before event-2"
        (let [;; intention: add block after `block-1-uid`
              event-1 (atomic-graph-ops/make-block-new-op block-3-1-uid block-1-uid :after)
              ;; intention: add block after `block-2-uid`
              event-2 (atomic-graph-ops/make-block-new-op block-3-2-uid block-2-uid :after)]
          (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection event-1))
          (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection event-2))
          (t/is (= 1 (:block/order (common-db/get-block @@fixture/connection
                                                        [:block/uid block-3-1-uid]))))
          (t/is (= 3 (:block/order (common-db/get-block @@fixture/connection
                                                        [:block/uid block-3-2-uid]))))))))

  (t/testing "just 2 events starting from the same point, but with new concurrency compatible model, wooh!"
    (let [page-1-uid         "page-5-uid"
          block-1-uid        "block-5-1-uid"
          block-2-uid        "block-5-2-uid"
          block-2-order-prev 1
          block-3-1-uid      "block-5-3-1-uid"
          block-3-2-uid      "block-5-3-2-uid"
          setup-txs          [{:block/uid      page-1-uid
                               :node/title     "test page 5"
                               :block/children [{:block/uid      block-1-uid
                                                 :block/string   ""
                                                 :block/order    0
                                                 :block/children []}
                                                {:block/uid      block-2-uid
                                                 :block/string   ""
                                                 :block/order    block-2-order-prev
                                                 :block/children []}]}]]
      (fixture/transact-with-middleware setup-txs)
      ;; we want children to be at the end:
      ;; 0: block-1-uid
      ;; 1: block-3-1-uid
      ;; 2: block-2-uid
      ;; 3: block-3-2-uid
      ;; now, there is no order to resolve and apply these events so we have what was intended
      (t/testing "event-1 after event-2"

        (let [;; intention: add block after `block-1-uid`
              event-1     (atomic-graph-ops/make-block-new-op block-3-1-uid block-1-uid :after)
              ;; intention: add block after `block-2-uid`
              event-2     (atomic-graph-ops/make-block-new-op block-3-2-uid block-2-uid :after)
              event-2-txs (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection event-2)]
          (d/transact! @fixture/connection event-2-txs)
          (let [block-2   (common-db/get-block @@fixture/connection
                                               [:block/uid block-2-uid])
                block-3-2 (common-db/get-block @@fixture/connection
                                               [:block/uid block-3-2-uid])]
            (t/is (= block-2-order-prev (:block/order block-2)))
            (t/is (= 2 (:block/order block-3-2))))
          (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection event-1))
          (let [block-3-1 (common-db/get-block @@fixture/connection
                                               [:block/uid block-3-1-uid])
                block-3-2 (common-db/get-block @@fixture/connection
                                               [:block/uid block-3-2-uid])]
            (t/is (= 1 (:block/order block-3-1)))
            (t/is (= 3 (:block/order block-3-2)))))))))


(t/deftest block-new-v2-test
  (t/testing "`:block/new-v2` block creation tests"

    (t/testing "rel `:before`"
      (t/testing "inserts into 1st position"
        (let [parent-block-uid "1-before-test-parent-uid"
              block-1-uid      "1-before-test-block-1-uid"
              block-2-uid      "1-before-test-block-2-uid"
              setup-txs        [{:block/uid      parent-block-uid
                                 :block/string   ""
                                 :block/order    0
                                 :block/children [{:block/uid    block-1-uid
                                                   :block/string ""
                                                   :block/order  0}]}]]
          (fixture/transact-with-middleware setup-txs)
          (let [block-new-v2-op (atomic-graph-ops/make-block-new-op block-2-uid block-1-uid :before)]
            (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                                      block-new-v2-op))
            (let [parent  (common-db/get-block @@fixture/connection [:block/uid parent-block-uid])
                  block-1 (common-db/get-block @@fixture/connection [:block/uid block-1-uid])
                  block-2 (common-db/get-block @@fixture/connection [:block/uid block-2-uid])]
              (t/is (= 2 (-> parent :block/children count)))
              (t/is (= 0 (-> block-2 :block/order)))
              (t/is (= 1 (-> block-1 :block/order)))))))

      (t/testing "inserts between 2 blocks"
        (let [parent-block-uid "2-before-test-parent-uid"
              block-1-uid      "2-before-test-block-1-uid"
              block-2-uid      "2-before-test-block-2-uid"
              block-3-uid      "2-before-test-block-3-uid"
              setup-txs        [{:block/uid      parent-block-uid
                                 :block/string   ""
                                 :block/order    0
                                 :block/children [{:block/uid    block-1-uid
                                                   :block/string ""
                                                   :block/order  0}
                                                  {:block/uid    block-2-uid
                                                   :block/string ""
                                                   :block/order  1}]}]]
          (fixture/transact-with-middleware setup-txs)
          (let [block-new-v2-op (atomic-graph-ops/make-block-new-op block-3-uid block-2-uid :before)]
            (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                                      block-new-v2-op))
            (let [parent  (common-db/get-block @@fixture/connection [:block/uid parent-block-uid])
                  block-1 (common-db/get-block @@fixture/connection [:block/uid block-1-uid])
                  block-2 (common-db/get-block @@fixture/connection [:block/uid block-2-uid])
                  block-3 (common-db/get-block @@fixture/connection [:block/uid block-3-uid])]
              (t/is (= 3 (-> parent :block/children count)))
              (t/is (= 0 (-> block-1 :block/order)))
              (t/is (= 1 (-> block-3 :block/order)))
              (t/is (= 2 (-> block-2 :block/order))))))))

    (t/testing "rel `:after`"
      (t/testing "inserts at last position"
        (let [parent-block-uid "1-after-test-parent-uid"
              block-1-uid      "1-after-test-block-1-uid"
              block-2-uid      "1-after-test-block-2-uid"
              setup-txs        [{:block/uid      parent-block-uid
                                 :block/string   ""
                                 :block/order    0
                                 :block/children [{:block/uid    block-1-uid
                                                   :block/string ""
                                                   :block/order  0}]}]]
          (fixture/transact-with-middleware setup-txs)
          (let [block-new-v2-op (atomic-graph-ops/make-block-new-op block-2-uid block-1-uid :after)]
            (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                                      block-new-v2-op))
            (let [parent  (common-db/get-block @@fixture/connection [:block/uid parent-block-uid])
                  block-1 (common-db/get-block @@fixture/connection [:block/uid block-1-uid])
                  block-2 (common-db/get-block @@fixture/connection [:block/uid block-2-uid])]
              (t/is (= 2 (-> parent :block/children count)))
              (t/is (= 0 (-> block-1 :block/order)))
              (t/is (= 1 (-> block-2 :block/order)))))))

      (t/testing "inserts between 2 blocks"
        (let [parent-block-uid "2-after-test-parent-uid"
              block-1-uid      "2-after-test-block-1-uid"
              block-2-uid      "2-after-test-block-2-uid"
              block-3-uid      "2-after-test-block-3-uid"
              setup-txs        [{:block/uid      parent-block-uid
                                 :block/string   ""
                                 :block/order    0
                                 :block/children [{:block/uid    block-1-uid
                                                   :block/string ""
                                                   :block/order  0}
                                                  {:block/uid    block-2-uid
                                                   :block/string ""
                                                   :block/order  1}]}]]
          (fixture/transact-with-middleware setup-txs)
          (let [block-new-v2-op (atomic-graph-ops/make-block-new-op block-3-uid block-1-uid :after)]
            (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                                      block-new-v2-op))
            (let [parent  (common-db/get-block @@fixture/connection [:block/uid parent-block-uid])
                  block-1 (common-db/get-block @@fixture/connection [:block/uid block-1-uid])
                  block-2 (common-db/get-block @@fixture/connection [:block/uid block-2-uid])
                  block-3 (common-db/get-block @@fixture/connection [:block/uid block-3-uid])]
              (t/is (= 3 (-> parent :block/children count)))
              (t/is (= 0 (-> block-1 :block/order)))
              (t/is (= 1 (-> block-3 :block/order)))
              (t/is (= 2 (-> block-2 :block/order))))))))

    (t/testing "rel `:first`"
      (let [parent-block-uid "1-first-test-parent-uid"
            block-1-uid      "1-first-test-block-1-uid"
            block-2-uid      "1-first-test-block-2-uid"
            setup-txs        [{:block/uid      parent-block-uid
                               :block/string   ""
                               :block/order    0
                               :block/children [{:block/uid    block-1-uid
                                                 :block/string ""
                                                 :block/order  0}]}]]
        (fixture/transact-with-middleware setup-txs)
        (let [block-new-v2-op (atomic-graph-ops/make-block-new-op block-2-uid parent-block-uid :first)]
          (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                                    block-new-v2-op))
          (let [parent  (common-db/get-block @@fixture/connection [:block/uid parent-block-uid])
                block-1 (common-db/get-block @@fixture/connection [:block/uid block-1-uid])
                block-2 (common-db/get-block @@fixture/connection [:block/uid block-2-uid])]
            (t/is (= 2 (-> parent :block/children count)))
            (t/is (= 0 (-> block-2 :block/order)))
            (t/is (= 1 (-> block-1 :block/order)))))))

    (t/testing "rel `:last`"
      (let [parent-block-uid "1-last-test-parent-uid"
            block-1-uid      "1-last-test-block-1-uid"
            block-2-uid      "1-last-test-block-2-uid"
            setup-txs        [{:block/uid      parent-block-uid
                               :block/string   ""
                               :block/order    0
                               :block/children [{:block/uid    block-1-uid
                                                 :block/string ""
                                                 :block/order  0}]}]]
        (fixture/transact-with-middleware setup-txs)
        (let [block-new-v2-op  (atomic-graph-ops/make-block-new-op block-2-uid parent-block-uid :last)
              block-new-v2-txs (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection block-new-v2-op)]
          (d/transact! @fixture/connection block-new-v2-txs)
          (let [parent  (common-db/get-block @@fixture/connection [:block/uid parent-block-uid])
                block-1 (common-db/get-block @@fixture/connection [:block/uid block-1-uid])
                block-2 (common-db/get-block @@fixture/connection [:block/uid block-2-uid])]
            (t/is (= 2 (-> parent :block/children count)))
            (t/is (= 0 (-> block-1 :block/order)))
            (t/is (= 1 (-> block-2 :block/order)))))))

    (t/testing "rel absolute ordering, please don't use it"
      (let [parent-block-uid "1-abs-test-parent-uid"
            block-1-uid      "1-abs-test-block-1-uid"
            block-2-uid      "1-abs-test-block-2-uid"
            block-3-uid      "1-abs-test-block-3-uid"
            setup-txs        [{:block/uid      parent-block-uid
                               :block/string   ""
                               :block/order    0
                               :block/children [{:block/uid    block-1-uid
                                                 :block/string ""
                                                 :block/order  0}
                                                {:block/uid    block-2-uid
                                                 :block/string ""
                                                 :block/order  1}]}]]
        (fixture/transact-with-middleware setup-txs)
        (let [block-new-v2-op (atomic-graph-ops/make-block-new-op block-3-uid parent-block-uid 1)]
          (d/transact! @fixture/connection (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection
                                                                                    block-new-v2-op))
          (let [parent  (common-db/get-block @@fixture/connection [:block/uid parent-block-uid])
                block-1 (common-db/get-block @@fixture/connection [:block/uid block-1-uid])
                block-2 (common-db/get-block @@fixture/connection [:block/uid block-2-uid])
                block-3 (common-db/get-block @@fixture/connection [:block/uid block-3-uid])]
            (t/is (= 3 (-> parent :block/children count)))
            (t/is (= 0 (-> block-1 :block/order)))
            (t/is (= 1 (-> block-2 :block/order)))
            (t/is (= 2 (-> block-3 :block/order)))))))))
