(ns athens.common-events.atomic-ops.block-move-test
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.atomic    :as atomic-ops]
    [athens.common-events.graph.composite :as composite-ops]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.common.logging :as log]
    [clojure.test                         :as t]
    [datascript.core                      :as d]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(t/deftest block-move-atomic-op-same-parent-cases

  (t/testing "Moving block up"
    (t/testing "using :before"
      (log/info "same parent move up :before")
      (let [parent-uid  "parent-1-uid"
            child-1-uid "child-1-1-uid"
            child-2-uid "child-1-2-uid"
            child-3-uid "child-1-3-uid"
            child-4-uid "child-1-4-uid"
            child-5-uid "child-1-5-uid"
            setup-tx    [{:block/uid      parent-uid
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
                                            :block/children []}
                                           {:block/uid      child-4-uid
                                            :block/string   ""
                                            :block/order    3
                                            :block/children []}
                                           {:block/uid      child-5-uid
                                            :block/string   ""
                                            :block/order    4
                                            :block/children []}]}]]
        (fixture/transact-with-middleware setup-tx)
        (let [block-move-op  (atomic-ops/make-block-move-op child-4-uid child-2-uid :before)
              block-move-txs (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection block-move-op)]
          (d/transact! @fixture/connection block-move-txs)
          (let [child-1-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-1-uid])
                child-2-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-2-uid])
                child-3-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-3-uid])
                child-4-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-4-uid])
                child-5-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-5-uid])]
            (t/is (= 0 (:block/order child-1-block)))
            (t/is (= 1 (:block/order child-4-block)))
            (t/is (= 2 (:block/order child-2-block)))
            (t/is (= 3 (:block/order child-3-block)))
            (t/is (= 4 (:block/order child-5-block)))))))

    (t/testing "using :after"
      (log/info "same parent move up :after")
      (let [parent-uid  "parent-11-uid"
            child-1-uid "child-11-1-uid"
            child-2-uid "child-11-2-uid"
            child-3-uid "child-11-3-uid"
            child-4-uid "child-11-4-uid"
            child-5-uid "child-11-5-uid"
            setup-tx    [{:block/uid      parent-uid
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
                                            :block/children []}
                                           {:block/uid      child-4-uid
                                            :block/string   ""
                                            :block/order    3
                                            :block/children []}
                                           {:block/uid      child-5-uid
                                            :block/string   ""
                                            :block/order    4
                                            :block/children []}]}]]
        (fixture/transact-with-middleware setup-tx)
        (let [block-move-op  (atomic-ops/make-block-move-op child-4-uid child-1-uid :after)
              block-move-txs (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection block-move-op)]
          (d/transact! @fixture/connection block-move-txs)
          (let [child-1-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-1-uid])
                child-2-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-2-uid])
                child-3-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-3-uid])
                child-4-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-4-uid])
                child-5-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-5-uid])]
            (t/is (= 0 (:block/order child-1-block)))
            (t/is (= 1 (:block/order child-4-block)))
            (t/is (= 2 (:block/order child-2-block)))
            (t/is (= 3 (:block/order child-3-block)))
            (t/is (= 4 (:block/order child-5-block))))))))

  (t/testing "Moving block down"
    (t/testing "using :after"
      (log/info "same parent move down :after")
      (let [parent-uid  "parent-2-uid"
            child-1-uid "child-2-1-uid"
            child-2-uid "child-2-2-uid"
            child-3-uid "child-2-3-uid"
            child-4-uid "child-2-4-uid"
            child-5-uid "child-2-5-uid"
            setup-tx    [{:block/uid      parent-uid
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
                                            :block/children []}
                                           {:block/uid      child-4-uid
                                            :block/string   ""
                                            :block/order    3
                                            :block/children []}
                                           {:block/uid      child-5-uid
                                            :block/string   ""
                                            :block/order    4
                                            :block/children []}]}]]
        (fixture/transact-with-middleware setup-tx)
        (let [block-move-op  (atomic-ops/make-block-move-op child-2-uid child-4-uid :after)
              block-move-txs (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection block-move-op)]
          (d/transact! @fixture/connection block-move-txs)
          (let [child-1-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-1-uid])
                child-2-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-2-uid])
                child-3-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-3-uid])
                child-4-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-4-uid])
                child-5-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-5-uid])]
            (t/is (= 0 (:block/order child-1-block)))
            (t/is (= 1 (:block/order child-3-block)))
            (t/is (= 2 (:block/order child-4-block)))
            (t/is (= 3 (:block/order child-2-block)))
            (t/is (= 4 (:block/order child-5-block)))))))

    (t/testing "using :before"
      (log/info "same parent move down :before")
      (let [parent-uid  "parent-22-uid"
            child-1-uid "child-22-1-uid"
            child-2-uid "child-22-2-uid"
            child-3-uid "child-22-3-uid"
            child-4-uid "child-22-4-uid"
            child-5-uid "child-22-5-uid"
            setup-tx    [{:block/uid      parent-uid
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
                                            :block/children []}
                                           {:block/uid      child-4-uid
                                            :block/string   ""
                                            :block/order    3
                                            :block/children []}
                                           {:block/uid      child-5-uid
                                            :block/string   ""
                                            :block/order    4
                                            :block/children []}]}]]
        (fixture/transact-with-middleware setup-tx)
        (let [block-move-op  (atomic-ops/make-block-move-op child-2-uid child-5-uid :before)
              block-move-txs (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection block-move-op)]
          (d/transact! @fixture/connection block-move-txs)
          (let [child-1-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-1-uid])
                child-2-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-2-uid])
                child-3-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-3-uid])
                child-4-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-4-uid])
                child-5-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-5-uid])]
            (t/is (= 0 (:block/order child-1-block)))
            (t/is (= 1 (:block/order child-3-block)))
            (t/is (= 2 (:block/order child-4-block)))
            (t/is (= 3 (:block/order child-2-block)))
            (t/is (= 4 (:block/order child-5-block)))))))))


(t/deftest block-move-atomic-op-diff-parent-cases
  (t/testing "Move block to new parent using :before"
    (let [parent-1-uid  "parent-1-uid"
          parent-2-uid  "parent-2-uid"
          child-1-1-uid "child-1-1-uid"
          child-1-2-uid "child-1-2-uid"
          child-2-1-uid "child-2-1-uid"
          child-2-2-uid "child-2-2-uid"
          setup-tx      [{:block/uid      parent-1-uid
                          :block/string   ""
                          :block/order    0
                          :block/children [{:block/uid      child-1-1-uid
                                            :block/string   ""
                                            :block/order    0
                                            :block/children []}
                                           {:block/uid      child-1-2-uid
                                            :block/string   ""
                                            :block/order    1
                                            :block/children []}]}
                         {:block/uid      parent-2-uid
                          :block/string   ""
                          :block/order    0
                          :block/children [{:block/uid      child-2-1-uid
                                            :block/string   ""
                                            :block/order    0
                                            :block/children []}
                                           {:block/uid      child-2-2-uid
                                            :block/string   ""
                                            :block/order    1
                                            :block/children []}]}]]
      (fixture/transact-with-middleware setup-tx)
      ;; from:
      (let [block-move-op  (atomic-ops/make-block-move-op child-2-2-uid child-1-1-uid :before)
            block-move-txs (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection block-move-op)]
        (d/transact! @fixture/connection block-move-txs)
        (let [child-1-1-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-1-1-uid])
              child-1-2-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-1-2-uid])
              child-2-1-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-2-1-uid])
              child-2-2-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-2-2-uid])]
          (t/is (= 0 (:block/order child-2-2-block)))
          (t/is (= 1 (:block/order child-1-1-block)))
          (t/is (= 2 (:block/order child-1-2-block)))
          (t/is (= 0 (:block/order child-2-1-block)))))

      ;; (atomic-ops/make-block-move-op child-2-uid parent-uid :first)
      ))

  (t/testing "Move block to new parent using :first"
    (let [parent-1-uid  "parent-11-uid"
          child-1-1-uid "child-11-1-uid"
          child-1-2-uid "child-11-2-uid"
          setup-tx      [{:block/uid      parent-1-uid
                          :block/string   ""
                          :block/order    0
                          :block/children [{:block/uid      child-1-1-uid
                                            :block/string   ""
                                            :block/order    0
                                            :block/children []}
                                           {:block/uid      child-1-2-uid
                                            :block/string   ""
                                            :block/order    1
                                            :block/children []}]}]]
      (fixture/transact-with-middleware setup-tx)
      (let [block-move-op  (atomic-ops/make-block-move-op child-1-2-uid child-1-1-uid :first)
            block-move-txs (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection block-move-op)]
        (d/transact! @fixture/connection block-move-txs)
        (let [parent-block    (common-db/get-block @@fixture/connection
                                                   [:block/uid parent-1-uid])
              child-1-1-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-1-1-uid])
              child-1-2-block (common-db/get-block @@fixture/connection
                                                   [:block/uid child-1-2-uid])]
          (t/is (= 1 (-> parent-block :block/children count)))
          (t/is (= 0 (:block/order child-1-1-block)))
          (t/is (= 1 (-> child-1-1-block :block/children count)))
          (t/is (= 0 (:block/order child-1-2-block))))))))


(t/deftest block-move-atomic-op-chains-aka-multi-cases
  (t/testing "flat source"
    (let [parent-1-uid "parent-1-uid"
          child-1-uid  "child-1-1-uid"
          child-2-uid  "child-1-2-uid"
          child-3-uid  "child-1-3-uid"
          setup-tx     [{:block/uid      parent-1-uid
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
                                           :block/children []}]}]]
      (fixture/transact-with-middleware setup-tx)
      (let [chained-move (composite-ops/make-consequence-op {:op/type :block/move-chained}
                                                            [(atomic-ops/make-block-move-op child-2-uid child-1-uid :first)
                                                             (atomic-ops/make-block-move-op child-3-uid child-2-uid :after)])]
        ;; in real usage use `resolve-transact!`, here we have to emulate it so we don't use middleware
        (doseq [atomic (graph-ops/extract-atomics chained-move)
                :let   [atomic-txs (atomic-resolver/resolve-to-tx @@fixture/connection atomic)]]
          (d/transact! @fixture/connection atomic-txs))
        (let [parent-block  (common-db/get-block @@fixture/connection
                                                   [:block/uid parent-1-uid])
              child-1-block (common-db/get-block @@fixture/connection
                                                 [:block/uid child-1-uid])
              child-2-block (common-db/get-block @@fixture/connection
                                                 [:block/uid child-2-uid])
              child-3-block (common-db/get-block @@fixture/connection
                                                 [:block/uid child-3-uid])]
          (t/is (= 1 (-> parent-block :block/children count)))
          (t/is (= 2 (-> child-1-block :block/children count)))
          (t/is (= 0 (:block/order child-1-block)))
          (t/is (= 0 (:block/order child-2-block)))
          (t/is (= 1 (:block/order child-3-block)))))))

  (t/testing "flat source, but has children"
    (let [parent-1-uid  "parent-2-uid"
          child-1-uid   "child-2-1-uid"
          child-2-uid   "child-2-2-uid"
          child-2-1-uid "child-2-2-1-uid"
          child-3-uid   "child-2-3-uid"
          setup-tx      [{:block/uid      parent-1-uid
                          :block/string   ""
                          :block/order    0
                          :block/children [{:block/uid      child-1-uid
                                            :block/string   ""
                                            :block/order    0
                                            :block/children []}
                                           {:block/uid      child-2-uid
                                            :block/string   ""
                                            :block/order    1
                                            :block/children [{:block/uid      child-2-1-uid
                                                              :block/string   ""
                                                              :block/order    0
                                                              :block/children []}]}
                                           {:block/uid      child-3-uid
                                            :block/string   ""
                                            :block/order    2
                                            :block/children []}]}]]
      (fixture/transact-with-middleware setup-tx)
      (let [chained-move (composite-ops/make-consequence-op {:op/type :block/move-chained}
                                                            [(atomic-ops/make-block-move-op child-2-uid child-1-uid :first)
                                                             (atomic-ops/make-block-move-op child-3-uid child-2-uid :after)])]
        ;; in real usage use `resolve-transact!`, here we have to emulate it so we don't use middleware
        (doseq [atomic (graph-ops/extract-atomics chained-move)
                :let   [atomic-txs (atomic-resolver/resolve-to-tx @@fixture/connection atomic)]]
          (d/transact! @fixture/connection atomic-txs))
        (let [parent-block  (common-db/get-block @@fixture/connection
                                                   [:block/uid parent-1-uid])
              child-1-block (common-db/get-block @@fixture/connection
                                                 [:block/uid child-1-uid])
              child-2-block (common-db/get-block @@fixture/connection
                                                 [:block/uid child-2-uid])
              child-3-block (common-db/get-block @@fixture/connection
                                                 [:block/uid child-3-uid])]
          (t/is (= 1 (-> parent-block :block/children count)))
          (t/is (= 2 (-> child-1-block :block/children count)))
          (t/is (= 1 (-> child-2-block :block/children count)))
          (t/is (= 0 (:block/order child-1-block)))
          (t/is (= 0 (:block/order child-2-block)))
          (t/is (= 1 (:block/order child-3-block))))))))
