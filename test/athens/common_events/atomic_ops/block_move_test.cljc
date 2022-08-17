(ns athens.common-events.atomic-ops.block-move-test
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.atomic    :as atomic-ops]
    [athens.common-events.graph.composite :as composite-ops]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.common.logging                :as log]
    [clojure.pprint                       :as pp]
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
        (let [block-move-op (atomic-ops/make-block-move-op child-4-uid {:block/uid child-2-uid :relation :before})]
          (atomic-resolver/resolve-transact! @fixture/connection block-move-op false)
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
        (let [block-move-op (atomic-ops/make-block-move-op child-4-uid {:block/uid child-1-uid :relation :after})]
          (atomic-resolver/resolve-transact! @fixture/connection block-move-op false)
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
        (let [block-move-op (atomic-ops/make-block-move-op child-2-uid {:block/uid child-4-uid :relation :after})]
          (atomic-resolver/resolve-transact! @fixture/connection block-move-op false)
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
        (let [block-move-op (atomic-ops/make-block-move-op child-2-uid {:block/uid child-5-uid :relation :before})]
          (atomic-resolver/resolve-transact! @fixture/connection block-move-op false)
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
      (let [block-move-op (atomic-ops/make-block-move-op child-2-2-uid {:block/uid child-1-1-uid :relation :before})]
        (atomic-resolver/resolve-transact! @fixture/connection block-move-op false)
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
          (t/is (= 0 (:block/order child-2-1-block)))))))

  ;; (atomic-ops/make-block-move-op child-2-uid parent-uid :first)


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
      (let [block-move-op  (atomic-ops/make-block-move-op child-1-2-uid {:block/uid child-1-1-uid :relation :first})]
        (atomic-resolver/resolve-transact! @fixture/connection block-move-op false)
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
                                                            [(atomic-ops/make-block-move-op child-2-uid {:block/uid child-1-uid :relation :first})
                                                             (atomic-ops/make-block-move-op child-3-uid {:block/uid child-2-uid :relation :after})])]
        (atomic-resolver/resolve-transact! @fixture/connection chained-move false)
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
                                                            [(atomic-ops/make-block-move-op child-2-uid {:block/uid child-1-uid :relation :first})
                                                             (atomic-ops/make-block-move-op child-3-uid {:block/uid child-2-uid :relation :after})])]
        (atomic-resolver/resolve-transact! @fixture/connection chained-move false)
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
          (t/is (= 1 (:block/order child-3-block)))))))

  (t/testing "different parents selection"
    (let [parent-1-uid "parent-3-uid"
          child-1-uid  "child-3-1-uid"
          child-2-uid  "child-3-2-uid"
          child-3-uid  "child-3-3-uid"
          child-4-uid  "child-3-4-uid"
          setup-tx     [{:block/uid      parent-1-uid
                         :block/string   ""
                         :block/order    0
                         :block/children [{:block/uid      child-1-uid
                                           :block/string   ""
                                           :block/order    0
                                           :block/children [{:block/uid      child-2-uid ; <- selected
                                                             :block/string   ""
                                                             :block/order    0
                                                             :block/children []}]}
                                          {:block/uid      child-3-uid                   ; <- selected
                                           :block/string   ""
                                           :block/order    1
                                           :block/children []}
                                          {:block/uid      child-4-uid
                                           :block/string   ""
                                           :block/order    2
                                           :block/children []}]}]]
      (fixture/transact-with-middleware setup-tx)
      (let [chained-move (composite-ops/make-consequence-op {:op/type :block/move-chained}
                                                            [(atomic-ops/make-block-move-op child-2-uid {:block/uid child-4-uid :relation :after})
                                                             (atomic-ops/make-block-move-op child-3-uid {:block/uid child-2-uid :relation :after})])]
        (atomic-resolver/resolve-transact! @fixture/connection chained-move false)
        (let [parent-block  (common-db/get-block @@fixture/connection
                                                 [:block/uid parent-1-uid])
              child-1-block (common-db/get-block @@fixture/connection
                                                 [:block/uid child-1-uid])
              child-2-block (common-db/get-block @@fixture/connection
                                                 [:block/uid child-2-uid])
              child-3-block (common-db/get-block @@fixture/connection
                                                 [:block/uid child-3-uid])
              child-4-block (common-db/get-block @@fixture/connection
                                                 [:block/uid child-4-uid])]
          (t/is (= 4 (-> parent-block :block/children count)))
          (t/is (= 0 (-> child-1-block :block/children count)))
          (t/is (= 0 (:block/order child-1-block)))
          (t/is (= 1 (:block/order child-4-block)))
          (t/is (= 2 (:block/order child-2-block)))
          (t/is (= 3 (:block/order child-3-block)))))))

  (t/testing "different parents selection"
    (let [parent-1-uid "parent-4-uid"
          child-1-uid  "child-4-1-uid"
          child-2-uid  "child-4-2-uid"
          child-3-uid  "child-4-3-uid"
          child-4-uid  "child-4-4-uid"
          child-5-uid  "child-4-5-uid"
          setup-tx     [{:block/uid      parent-1-uid
                         :block/string   ""
                         :block/order    0
                         :block/children [{:block/uid      child-1-uid
                                           :block/string   ""
                                           :block/order    0
                                           :block/children [{:block/uid      child-2-uid ; <- selected
                                                             :block/string   ""
                                                             :block/order    0
                                                             :block/children []}]}
                                          {:block/uid      child-3-uid                   ; <- selected
                                           :block/string   ""
                                           :block/order    1
                                           :block/children []}
                                          {:block/uid      child-4-uid
                                           :block/string   ""
                                           :block/order    2
                                           :block/children [{:block/uid      child-5-uid
                                                             :block/string   ""
                                                             :block/order    0
                                                             :block/children []}]}]}]] ; <- move after this block
      (fixture/transact-with-middleware setup-tx)
      (let [chained-move (composite-ops/make-consequence-op {:op/type :block/move-chained}
                                                            [(atomic-ops/make-block-move-op child-2-uid {:block/uid child-5-uid :relation :after})
                                                             (atomic-ops/make-block-move-op child-3-uid {:block/uid child-2-uid :relation :after})])]
        (atomic-resolver/resolve-transact! @fixture/connection chained-move false)
        (let [parent-block  (common-db/get-block @@fixture/connection
                                                 [:block/uid parent-1-uid])
              child-1-block (common-db/get-block @@fixture/connection
                                                 [:block/uid child-1-uid])
              child-2-block (common-db/get-block @@fixture/connection
                                                 [:block/uid child-2-uid])
              child-3-block (common-db/get-block @@fixture/connection
                                                 [:block/uid child-3-uid])
              child-4-block (common-db/get-block @@fixture/connection
                                                 [:block/uid child-4-uid])
              child-5-block (common-db/get-block @@fixture/connection
                                                 [:block/uid child-5-uid])]
          (t/is (= 2 (-> parent-block :block/children count)))
          (t/is (= 0 (-> child-1-block :block/children count)))
          (t/is (= 3 (-> child-4-block :block/children count)))
          (t/is (= 0 (:block/order child-1-block)))
          (t/is (= 1 (:block/order child-4-block)))
          (t/is (= 0 (:block/order child-5-block)))
          (t/is (= 1 (:block/order child-2-block)))
          (t/is (= 2 (:block/order child-3-block))))))))


(t/deftest block-move-under-page
  (let [block-0-uid     "block-0-uid"
        block-1-uid     "block-1-uid"
        block-2-uid     "block-2-uid"
        child-1-1-uid   "child-1-1-uid"
        child-2-1-uid   "child-2-1-uid"
        setup-repr     [{:page/title     "test-page"
                         :block/children [{:block/uid    block-0-uid
                                           :block/string "zero"
                                           :block/order  0}
                                          {:block/uid    block-1-uid
                                           :block/string "one"
                                           :block/order  1
                                           :block/children [{:block/uid      child-1-1-uid
                                                             :block/string   ""
                                                             :block/order    0
                                                             :block/children []}]}
                                          {:block/uid    block-2-uid
                                           :block/string "two"
                                           :block/order  2
                                           :block/children [{:block/uid      child-2-1-uid
                                                             :block/string   ""
                                                             :block/order    0
                                                             :block/children []}]}]}]
        get-block-order    #(->> [:block/uid %]
                                 (common-db/get-block @@fixture/connection)
                                 :block/order)
        move!              #(-> (atomic-ops/make-block-move-op %1 {:relation %2
                                                                   :page/title %3})
                                fixture/op-resolve-transact!)]

    (t/testing "Block move under a page: Move source block :after target block, when both source and target block are under **same** parent"
      (fixture/setup! setup-repr)
      (t/is (= 0 (get-block-order block-0-uid)) "Move block 0 after block 1")
      (t/is (= 1 (get-block-order block-1-uid)) "Move block 0 after block 1")
      (move! block-1-uid :first "test-page")
      (clojure.pprint/pprint (common-db/get-block @@fixture/connection [:node/title "test-page"]))
      (t/is (= 1 (get-block-order block-0-uid)) "Move source uid (block-0-uid) after target uid (block-1-uid)")
      (t/is (= 0 (get-block-order block-1-uid)) "Move source uid (block-0-uid) after target uid (block-1-uid)")
      (fixture/teardown! setup-repr))))


(t/deftest undo
  (let [block-0-uid     "block-0-uid"
        block-1-uid     "block-1-uid"
        block-2-uid     "block-2-uid"
        child-1-1-uid   "child-1-1-uid"
        child-2-1-uid   "child-2-1-uid"
        setup-repr     [{:page/title     "test-page"
                         :block/children [{:block/uid    block-0-uid
                                           :block/string "zero"
                                           :block/order  0}
                                          {:block/uid    block-1-uid
                                           :block/string "one"
                                           :block/order  1
                                           :block/children [{:block/uid      child-1-1-uid
                                                             :block/string   ""
                                                             :block/order    0
                                                             :block/children []}]}
                                          {:block/uid    block-2-uid
                                           :block/string "two"
                                           :block/order  2
                                           :block/children [{:block/uid      child-2-1-uid
                                                             :block/string   ""
                                                             :block/order    0
                                                             :block/children []}]}]}]
        get-block-order    #(->> [:block/uid %]
                                 (common-db/get-block @@fixture/connection)
                                 :block/order)
        get-children-count #(->> [:block/uid %]
                                 (common-db/get-block @@fixture/connection)
                                 :block/children
                                 count)
        move!              #(-> (atomic-ops/make-block-move-op %1 {:relation %2
                                                                   :block/uid %3})
                                fixture/op-resolve-transact!)]

    (t/testing "Undo Block move: Move source block :after target block, when both source and target block are under **same** parent"
      (fixture/setup! setup-repr)
      (t/is (= 1 (get-block-order block-1-uid)) "Move block 1 after block 2")
      (t/is (= 2 (get-block-order block-2-uid)) "Move block 1 after block 2")
      (let [[evt-db evt] (move! block-1-uid :after block-2-uid)]
        (t/is (= 2 (get-block-order block-1-uid)) "Move source uid (block-1-uid) after target uid (block-2-uid)")
        (t/is (= 1 (get-block-order block-2-uid)) "Move source uid (block-1-uid) after target uid (block-2-uid)")
        (fixture/undo! evt-db evt)
        (t/is (= 1 (get-block-order block-1-uid)) "Undo: Move source uid (block-1-uid) and target uid (block-2-uid) to original position ")
        (t/is (= 2 (get-block-order block-2-uid))  "Undo: Move source uid (block-1-uid) and target uid (block-2-uid) to original position "))
      (fixture/teardown! setup-repr))

    (t/testing "Redo Block move: Move source block :after target block, when both source and target block are under **same** parent"
      (fixture/setup! setup-repr)
      (t/is (= 1 (get-block-order block-1-uid)) "Move block 1 after block 2")
      (t/is (= 2 (get-block-order block-2-uid)) "Move block 1 after block 2")
      (let [[evt-db evt] (move! block-1-uid :after block-2-uid)]
        (t/is (= 2 (get-block-order block-1-uid)) "Move source uid (block-1-uid) after target uid (block-2-uid)")
        (t/is (= 1 (get-block-order block-2-uid)) "Move source uid (block-1-uid) after target uid (block-2-uid)")
        (let [[evt-db' evt']  (fixture/undo! evt-db evt)]
          (t/is (= 1 (get-block-order block-1-uid)) "Undo: Move source uid (block-1-uid) and target uid (block-2-uid) to original position ")
          (t/is (= 2 (get-block-order block-2-uid))  "Undo: Move source uid (block-1-uid) and target uid (block-2-uid) to original position ")
          (fixture/undo! evt-db' evt')
          (t/is (= 2 (get-block-order block-1-uid)) "Move block 1 after block 2")
          (t/is (= 1 (get-block-order block-2-uid)) "Move block 1 after block 2")))
      (fixture/teardown! setup-repr))

    (t/testing "Undo: Block move under a page: Move source block :after target block, when both source and target block are under **same** parent"
      (fixture/setup! setup-repr)
      (t/is (= 0 (get-block-order block-0-uid)) "Move block 0 after block 1")
      (t/is (= 1 (get-block-order block-1-uid)) "Move block 0 after block 1")
      (let [[evt-db evt] (move! block-0-uid :after block-1-uid)]
        (t/is (= 1 (get-block-order block-0-uid)) "Move source uid (block-0-uid) after target uid (block-1-uid)")
        (t/is (= 0 (get-block-order block-1-uid)) "Move source uid (block-0-uid) after target uid (block-1-uid)")
        (fixture/undo! evt-db evt)
        (t/is (= 0 (get-block-order block-0-uid)) "Undo: Move source uid (block-0-uid) and target uid (block-1-uid) to original position ")
        (t/is (= 1 (get-block-order block-1-uid))  "Undo: Move source uid (block-0-uid) and target uid (block-1-uid) to original position "))
      (fixture/teardown! setup-repr))

    (t/testing "Redo: Block move under a page: Move source block :after target block, when both source and target block are under **same** parent"
      (fixture/setup! setup-repr)
      (t/is (= 0 (get-block-order block-0-uid)) "Move block 0 after block 1")
      (t/is (= 1 (get-block-order block-1-uid)) "Move block 0 after block 1")
      (let [[evt-db evt] (move! block-0-uid :after block-1-uid)]
        (t/is (= 1 (get-block-order block-0-uid)) "Move source uid (block-0-uid) after target uid (block-1-uid)")
        (t/is (= 0 (get-block-order block-1-uid)) "Move source uid (block-0-uid) after target uid (block-1-uid)")
        (let [[evt-db' evt']  (fixture/undo! evt-db evt)]
          (t/is (= 0 (get-block-order block-0-uid)) "Undo: Move source uid (block-0-uid) and target uid (block-1-uid) to original position ")
          (t/is (= 1 (get-block-order block-1-uid))  "Undo: Move source uid (block-0-uid) and target uid (block-1-uid) to original position ")
          (fixture/undo! evt-db' evt')
          (t/is (= 1 (get-block-order block-0-uid)) "Move source uid (block-0-uid) after target uid (block-1-uid)")
          (t/is (= 0 (get-block-order block-1-uid))) "Move source uid (block-0-uid) after target uid (block-1-uid)"))
      (fixture/teardown! setup-repr))

    (t/testing "Undo move: Move source block :before target block, when both source and target block are under **same** parent"
      (fixture/setup! setup-repr)
      (t/is (= 0 (get-block-order block-0-uid)) "Move block 1 :before block 0")
      (t/is (= 1 (get-block-order block-1-uid)) "Move block 1 :before block 0")
      (let [[evt-db evt] (move! block-1-uid :before block-0-uid)]
        (t/is (= 1 (get-block-order block-0-uid)) "Move source uid (block-1-uid) :before target uid (block-0-uid)")
        (t/is (= 0 (get-block-order block-1-uid)) "Move source uid (block-1-uid) :before target uid (block-0-uid)")
        (fixture/undo! evt-db evt)
        (t/is (= 0 (get-block-order block-0-uid)) "Undo: Move source uid (block-1-uid) and target uid (block-0-uid) to original position ")
        (t/is (= 1 (get-block-order block-1-uid)) "Undo: Move source uid (block-1-uid) and target uid (block-0-uid) to original position "))
      (fixture/teardown! setup-repr))

    (t/testing "Redo move: Move source block :before target block, when both source and target block are under **same** parent"
      (fixture/setup! setup-repr)
      (t/is (= 0 (get-block-order block-0-uid)) "Move block 1 :before block 0")
      (t/is (= 1 (get-block-order block-1-uid)) "Move block 1 :before block 0")
      (let [[evt-db evt] (move! block-1-uid :before block-0-uid)]
        (t/is (= 1 (get-block-order block-0-uid)) "Move source uid (block-1-uid) :before target uid (block-0-uid)")
        (t/is (= 0 (get-block-order block-1-uid)) "Move source uid (block-1-uid) :before target uid (block-0-uid)")
        (let [[evt-db' evt'] (fixture/undo! evt-db evt)]
          (t/is (= 0 (get-block-order block-0-uid)) "Undo: Move source uid (block-1-uid) and target uid (block-0-uid) to original position ")
          (t/is (= 1 (get-block-order block-1-uid)) "Undo: Move source uid (block-1-uid) and target uid (block-0-uid) to original position ")
          (fixture/undo! evt-db' evt')
          (t/is (= 1 (get-block-order block-0-uid)) "Move source uid (block-1-uid) :before target uid (block-0-uid)")
          (t/is (= 0 (get-block-order block-1-uid)) "Move source uid (block-1-uid) :before target uid (block-0-uid)")))
      (fixture/teardown! setup-repr))

    (t/testing "Undo move: Move source block :after target block, when both source and target block are under **Different** parent"
      (fixture/setup! setup-repr)
      (t/is (= 0 (get-block-order child-1-1-uid)) "Move child-1-1 :after child-2-1")
      (t/is (= 1 (get-children-count block-1-uid)) "Block 1 should have 1 child")
      (t/is (= 0 (get-block-order child-2-1-uid)) "Move child-1-1 :after child-2-1")
      (t/is (= 1 (get-children-count block-2-uid)) "Block 2 should have 1 child")
      (let [[evt-db evt] (move! child-1-1-uid :after child-2-1-uid)]
        (t/is (= 1 (get-block-order child-1-1-uid)) "Move source uid (child-1-1-uid) :after target uid (child-2-1-uid)")
        (t/is (= 0 (get-children-count block-1-uid)) "Block 1 should have 0 child")
        (t/is (= 0 (get-block-order child-2-1-uid)) "Move source uid (child-1-1-uid) :after target uid (child-2-1-uid)")
        (t/is (= 2 (get-children-count block-2-uid)) "Block 2 should have 2 child")
        (fixture/undo! evt-db evt)
        (t/is (= 0 (get-block-order child-1-1-uid)) "Undo: Move source uid (block-1-uid) and target uid (block-0-uid) to original position ")
        (t/is (= 1 (get-children-count block-1-uid)) "Block 1 should have 1 child")
        (t/is (= 0 (get-block-order child-2-1-uid)) "Undo: Move source uid (block-1-uid) and target uid (block-0-uid) to original position ")
        (t/is (= 1 (get-children-count block-2-uid)) "Block 2 should have 1 child"))
      (fixture/teardown! setup-repr))

    (t/testing "Redo move: Move source block :after target block, when both source and target block are under **Different** parent"
      (fixture/setup! setup-repr)
      (t/is (= 0 (get-block-order child-1-1-uid)) "Move child-1-1 :after child-2-1")
      (t/is (= 1 (get-children-count block-1-uid)) "Block 1 should have 1 child")
      (t/is (= 0 (get-block-order child-2-1-uid)) "Move child-1-1 :after child-2-1")
      (t/is (= 1 (get-children-count block-2-uid)) "Block 2 should have 1 child")
      (let [[evt-db evt] (move! child-1-1-uid :after child-2-1-uid)]
        (t/is (= 1 (get-block-order child-1-1-uid)) "Move source uid (child-1-1-uid) :after target uid (child-2-1-uid)")
        (t/is (= 0 (get-children-count block-1-uid)) "Block 1 should have 0 child")
        (t/is (= 0 (get-block-order child-2-1-uid)) "Move source uid (child-1-1-uid) :after target uid (child-2-1-uid)")
        (t/is (= 2 (get-children-count block-2-uid)) "Block 2 should have 2 child")
        (let [[evt-db' evt'] (fixture/undo! evt-db evt)]
          (t/is (= 0 (get-block-order child-1-1-uid)) "Undo: Move source uid (block-1-uid) and target uid (block-0-uid) to original position ")
          (t/is (= 1 (get-children-count block-1-uid)) "Block 1 should have 1 child")
          (t/is (= 0 (get-block-order child-2-1-uid)) "Undo: Move source uid (block-1-uid) and target uid (block-0-uid) to original position ")
          (t/is (= 1 (get-children-count block-2-uid)) "Block 2 should have 1 child")
          (fixture/undo! evt-db' evt')
          (t/is (= 1 (get-block-order child-1-1-uid)) "Move source uid (child-1-1-uid) :after target uid (child-2-1-uid)")
          (t/is (= 0 (get-children-count block-1-uid)) "Block 1 should have 0 child")
          (t/is (= 0 (get-block-order child-2-1-uid)) "Move source uid (child-1-1-uid) :after target uid (child-2-1-uid)")
          (t/is (= 2 (get-children-count block-2-uid))) "Block 2 should have 2 child"))
      (fixture/teardown! setup-repr))

    (t/testing "Undo move: Move source block :before target block, when both source and target block are under **Different** parent"
      (fixture/setup! setup-repr)
      (t/is (= 0 (get-block-order child-1-1-uid)) "Move child-1-1 :before child-2-1")
      (t/is (= 1 (get-children-count block-1-uid)) "Block 1 should have 1 child")
      (t/is (= 0 (get-block-order child-2-1-uid)) "Move child-1-1 :before child-2-1")
      (t/is (= 1 (get-children-count block-2-uid)) "Block 2 should have 1 child")
      (let [[evt-db evt] (move! child-1-1-uid :before child-2-1-uid)]
        (t/is (= 0 (get-block-order child-1-1-uid)) "Move source uid (child-1-1-uid) :before target uid (child-2-1-uid)")
        (t/is (= 0 (get-children-count block-1-uid)) "Block 1 should have 0 child")
        (t/is (= 1 (get-block-order child-2-1-uid)) "Move source uid (child-1-1-uid) :before target uid (child-2-1-uid)")
        (t/is (= 2 (get-children-count block-2-uid)) "Block 2 should have 2 child")
        (fixture/undo! evt-db evt)
        (t/is (= 0 (get-block-order child-1-1-uid)) "Undo: Move source uid (block-1-uid) and target uid (block-0-uid) to original position ")
        (t/is (= 1 (get-children-count block-1-uid)) "Block 1 should have 1 child")
        (t/is (= 0 (get-block-order child-2-1-uid)) "Undo: Move source uid (block-1-uid) and target uid (block-0-uid) to original position ")
        (t/is (= 1 (get-children-count block-2-uid)) "Block 2 should have 1 child"))
      (fixture/teardown! setup-repr))

    (t/testing "Redo move: Move source block :before target block, when both source and target block are under **Different** parent"
      (fixture/setup! setup-repr)
      (t/is (= 0 (get-block-order child-1-1-uid)) "Move child-1-1 :before child-2-1")
      (t/is (= 1 (get-children-count block-1-uid)) "Block 1 should have 1 child")
      (t/is (= 0 (get-block-order child-2-1-uid)) "Move child-1-1 :before child-2-1")
      (t/is (= 1 (get-children-count block-2-uid)) "Block 2 should have 1 child")
      (let [[evt-db evt] (move! child-1-1-uid :before child-2-1-uid)]
        (t/is (= 0 (get-block-order child-1-1-uid)) "Move source uid (child-1-1-uid) :before target uid (child-2-1-uid)")
        (t/is (= 0 (get-children-count block-1-uid)) "Block 1 should have 0 child")
        (t/is (= 1 (get-block-order child-2-1-uid)) "Move source uid (child-1-1-uid) :before target uid (child-2-1-uid)")
        (t/is (= 2 (get-children-count block-2-uid)) "Block 2 should have 2 child")
        (let [[evt-db' evt'] (fixture/undo! evt-db evt)]
          (t/is (= 0 (get-block-order child-1-1-uid)) "Undo: Move source uid (block-1-uid) and target uid (block-0-uid) to original position ")
          (t/is (= 1 (get-children-count block-1-uid)) "Block 1 should have 1 child")
          (t/is (= 0 (get-block-order child-2-1-uid)) "Undo: Move source uid (block-1-uid) and target uid (block-0-uid) to original position ")
          (t/is (= 1 (get-children-count block-2-uid)) "Block 2 should have 1 child")
          (fixture/undo! evt-db' evt')
          (t/is (= 0 (get-block-order child-1-1-uid)) "Move source uid (child-1-1-uid) :before target uid (child-2-1-uid)")
          (t/is (= 0 (get-children-count block-1-uid)) "Block 1 should have 0 child")
          (t/is (= 1 (get-block-order child-2-1-uid)) "Move source uid (child-1-1-uid) :before target uid (child-2-1-uid)")
          (t/is (= 2 (get-children-count block-2-uid))) "Block 2 should have 2 child"))
      (fixture/teardown! setup-repr))))


(t/deftest child-to-prop
  (fixture/setup! [{:page/title "title"
                    :block/children
                    [#:block{:uid    "uid"
                             :string ""}]}])
  (fixture/op-resolve-transact!
    (graph-ops/build-block-move-op @@fixture/connection "uid" {:page/title "title"
                                                               :relation   {:page/title "key"}}))
  (fixture/is #{{:page/title "key"}
                {:page/title "title"
                 :block/properties
                 {"key" #:block{:uid    "uid"
                                :string ""}}}}))


(t/deftest prop-to-child
  (fixture/setup! [{:page/title "title"
                    :block/properties
                    {"key" #:block{:uid    "uid"
                                   :string ""}}}])
  (fixture/op-resolve-transact!
    (graph-ops/build-block-move-op @@fixture/connection "uid" {:page/title "title"
                                                               :relation   :first}))
  (fixture/is #{{:page/title "key"}
                {:page/title "title"
                 :block/children
                 [#:block{:uid    "uid"
                          :string ""}]}}))


(t/deftest prop-to-prop
  (fixture/setup! [{:page/title "title"
                    :block/properties
                    {"key" #:block{:uid    "uid"
                                   :string ""}}}])
  (fixture/op-resolve-transact!
    (graph-ops/build-block-move-op @@fixture/connection "uid" {:page/title "title"
                                                               :relation   {:page/title "key2"}}))
  (fixture/is #{{:page/title "key"}
                {:page/title "key2"}
                {:page/title "title"
                 :block/properties
                 {"key2" #:block{:uid    "uid"
                                 :string ""}}}}))
