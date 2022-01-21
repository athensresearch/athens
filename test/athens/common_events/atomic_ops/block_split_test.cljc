(ns athens.common-events.atomic-ops.block-split-test
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [clojure.test                         :as t]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(t/deftest block-split-tests

  (t/testing "simple :block/split - split one block into two"
    (let [page-title  "page 1"
          alice-uid   "alice-uid"
          bob-uid     "bob-uid"
          start-str   "abc123"
          split-index 3
          end-str-1   (subs start-str 0 split-index)
          end-str-2   (subs start-str split-index)
          setup-repr  [{:page/title     page-title
                        :block/children [#:block{:uid    alice-uid
                                                 :string start-str}]}]
          exp-repr    [{:page/title     page-title
                        :block/children [#:block {:uid    alice-uid
                                                  :string end-str-1}
                                         #:block {:uid    bob-uid
                                                  :string end-str-2}]}]
          run!        (fn []
                        (let [atomic-txs (->> (graph-ops/build-block-split-op @@fixture/connection
                                                                              {:old-block-uid alice-uid
                                                                               :new-block-uid bob-uid
                                                                               :string        start-str
                                                                               :index         split-index
                                                                               :relation      :after})
                                              (graph-ops/extract-atomics)
                                              (map #(atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection %)))]
                          (doseq [atomic-tx atomic-txs]
                            (fixture/transact-with-middleware atomic-tx))))]
      ;; setup
      (fixture/setup! setup-repr)
      (t/is (= setup-repr [(fixture/get-repr [:node/title page-title])]))
      ;; run
      (run!)
      (t/is (= exp-repr [(fixture/get-repr [:node/title page-title])]))
      (fixture/teardown! setup-repr)))

  (t/testing "`:block/split` with siblings - re-index after the split"
    (let [page-title  "test page"
          alice-uid   "alice-uid"
          bob-uid     "bob-uid"
          charlie-uid "charlie-uid"
          start-str   "abc123"
          split-index 3
          end-str-1   (subs start-str 0 split-index)
          end-str-2   (subs start-str split-index)
          bob-str     "bob was here"
          setup-repr  [{:page/title     page-title
                        :block/children [#:block{:uid    alice-uid
                                                 :string start-str}
                                         #:block {:uid    bob-uid
                                                  :string bob-str}]}]
          exp-repr    [{:page/title     page-title
                        :block/children [#:block {:uid    alice-uid
                                                  :string end-str-1}
                                         #:block {:uid    charlie-uid
                                                  :string end-str-2}
                                         #:block {:uid    bob-uid
                                                  :string bob-str}]}]
          run!        (fn []
                        (let [atomic-txs (->> (graph-ops/build-block-split-op @@fixture/connection
                                                                              {:old-block-uid alice-uid
                                                                               :new-block-uid charlie-uid
                                                                               :string        start-str
                                                                               :index         split-index
                                                                               :relation      :after})
                                              (graph-ops/extract-atomics)
                                              (map #(atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection %)))]
                          (doseq [atomic-tx atomic-txs]
                            (fixture/transact-with-middleware atomic-tx))))]
      ;; setup
      (fixture/setup! setup-repr)
      (t/is (= setup-repr [(fixture/get-repr [:node/title page-title])]))
      ;; run
      (run!)
      (t/is (= exp-repr [(fixture/get-repr [:node/title page-title])]))
      (fixture/teardown! setup-repr)))

  (t/testing ":block/split after writing without save (temp-string) leads to `:block/save` and link creation."
    (let [page-title        "test page 1"
          alice-uid         "alice-uid"
          bob-uid           "bob-uid"
          start-str         "asd123"
          new-page          "123"
          end-str-1         "asd"
          split-index       3
          end-str-2         (str "[[" new-page "]]")
          new-tmp-string    (str end-str-1 end-str-2)
          setup-repr        [{:page/title     page-title
                              :block/children [#:block{:uid    alice-uid
                                                       :string start-str}]}]
          exp-repr-page-1   [{:page/title     page-title
                              :block/children [#:block{:string end-str-1
                                                       :uid    alice-uid}
                                               #:block{:string end-str-2
                                                       :uid    bob-uid}]}]
          exp-repr-new-page [{:page/title new-page}]
          run!              (fn []
                              (let [atomic-txs (->> (graph-ops/build-block-split-op @@fixture/connection
                                                                                    {:old-block-uid alice-uid
                                                                                     :new-block-uid bob-uid
                                                                                     :string        new-tmp-string
                                                                                     :index         split-index
                                                                                     :relation      :after})
                                                    (graph-ops/extract-atomics)
                                                    (map #(atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection %)))]
                                (doseq [atomic-tx atomic-txs]
                                  (fixture/transact-with-middleware atomic-tx))))]
      ;; setup
      (fixture/setup! setup-repr)
      (t/is (= [(fixture/get-repr [:node/title page-title])]
               setup-repr))
      ;; run block split
      (run!)
      ;; test
      (t/is (= exp-repr-new-page
               [(fixture/get-repr [:node/title new-page])])
            "New page exists.")
      (t/is (= [(fixture/get-repr [:node/title page-title])]
               exp-repr-page-1)
            "First page has new blocks.")
      (fixture/teardown! setup-repr)
      (fixture/teardown! exp-repr-page-1))))


(t/deftest block-split-to-child-test
  (t/testing "`:block/split` add splitted block as first child with re-indexing 🪄"
    (let [page-1-uid  "page-3-uid"
          child-1-uid "child-3-1-uid"
          child-2-uid "child-3-2-uid"
          child-3-uid "child-3-3-uid"
          start-str   "a-o-k"
          new-str     "o-k"
          setup-txs   [{:block/uid      page-1-uid
                        :node/title     "test page 2"
                        :block/children [{:block/uid      child-1-uid
                                          :block/string   start-str
                                          :block/order    0
                                          :block/children []}
                                         {:block/uid      child-2-uid
                                          :block/string   ""
                                          :block/order    1
                                          :block/children []}]}]]
      (fixture/transact-with-middleware setup-txs)
      (let [page (common-db/get-block @@fixture/connection [:block/uid page-1-uid])]
        (t/is (nil? (common-db/e-by-av @@fixture/connection
                                       :block/uid
                                       child-3-uid))
              "Should not have 3rd child before block split.")
        (t/is (= 2 (-> page
                       :block/children
                       count))
              "Page should have only 2 children block after setup.")
        (let [block-split-op      (graph-ops/build-block-split-op @@fixture/connection
                                                                  {:old-block-uid child-1-uid
                                                                   :new-block-uid child-3-uid
                                                                   :string        start-str
                                                                   :index         2
                                                                   :relation      :first})
              block-split-atomics (graph-ops/extract-atomics block-split-op)]
          (doseq [atomic-op block-split-atomics
                  :let      [atomic-txs (atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection atomic-op)]]
            (fixture/transact-with-middleware atomic-txs))
          (let [page        (common-db/get-block @@fixture/connection [:block/uid page-1-uid])
                old-1-block (common-db/get-block @@fixture/connection [:block/uid child-1-uid])
                old-2-block (common-db/get-block @@fixture/connection [:block/uid child-2-uid])
                new-block   (common-db/get-block @@fixture/connection [:block/uid child-3-uid])]
            (t/is (= 2 (-> page :block/children count))
                  "Page should have 2 children after block split")
            (t/is (= 1 (-> old-1-block :block/children count))
                  "old-1-block should have 1 child after block split")
            ;; `:block/string` tests
            (t/is (= "a-" (-> old-1-block :block/string)))
            (t/is (= "" (-> old-2-block :block/string)))
            (t/is (= new-str (-> new-block :block/string)))
            ;; `:block/order' tests`
            (t/is (= 0 (-> old-1-block :block/order)))
            (t/is (= 0 (-> new-block :block/order)))
            (t/is (= 1 (-> old-2-block :block/order)))))))))

