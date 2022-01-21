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
          alice-start-str   "asd123"
          new-page          "123"
          alice-end-str         "asd"
          split-index       3
          end-str-2         (str "[[" new-page "]]")
          new-tmp-string    (str alice-end-str end-str-2)
          setup-repr        [{:page/title     page-title
                              :block/children [#:block{:uid    alice-uid
                                                       :string alice-start-str}]}]
          exp-repr-page-1   [{:page/title     page-title
                              :block/children [#:block{:string alice-end-str
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
    (let [page-title      "test page"
          alice-uid       "alice-uid"
          bob-uid         "bob-uid"
          charlie-uid     "charlie-uid"
          alice-start-str "a-o-k"
          split-index     3
          alice-end-str   (subs alice-start-str 0 split-index)
          charlie-end-str (subs alice-start-str split-index)
          bob-str         "bob was here"
          setup-repr      [{:page/title     page-title
                            :block/children [#:block {:uid    alice-uid
                                                      :string alice-start-str}
                                             #:block {:uid    bob-uid
                                                      :string bob-str}]}]
          exp-repr        [{:page/title     page-title
                            :block/children [#:block {:uid      alice-uid
                                                      :string   alice-end-str
                                                      :children [#:block{:string charlie-end-str
                                                                         :uid    charlie-uid}]}
                                             #:block {:uid    bob-uid
                                                      :string bob-str}]}]
          run!            (fn []
                            (let [atomic-txs (->> (graph-ops/build-block-split-op @@fixture/connection
                                                                                  {:old-block-uid alice-uid
                                                                                   :new-block-uid charlie-uid
                                                                                   :string        alice-start-str
                                                                                   :index         split-index
                                                                                   :relation      :first})
                                                  (graph-ops/extract-atomics)
                                                  (map #(atomic-resolver/resolve-atomic-op-to-tx @@fixture/connection %)))]
                              (doseq [atomic-tx atomic-txs]
                                (fixture/transact-with-middleware atomic-tx))))]
      ;; setup
      (fixture/setup! setup-repr)
      (t/is (= setup-repr
               [(fixture/get-repr [:node/title page-title])]))
      ;; run
      (run!)
      ;; test
      (t/is (= exp-repr
               [(fixture/get-repr [:node/title page-title])])))))




{:block/uid "page-3-uid",
 :db/id 1,
 :node/title "test page 2"
 :block/children [{:block/order 0,
                    :block/string "a-o-k",
                    :block/uid "child-3-1-uid",
                    :db/id 2}
                  {:block/order 1,
                   :block/string "",
                   :block/uid "child-3-2-uid",
                   :db/id 3}]}

{:block/children [{:block/children [{:block/open true,
                                     :block/order 0,
                                     :block/string "o-k",
                                     :block/uid "child-3-3-uid",
                                     :db/id 4}],
                   :block/order 0,
                   :block/string "a-",
                   :block/uid "child-3-1-uid",
                   :db/id 2}
                  {:block/order 1,
                   :block/string "",
                   :block/uid "child-3-2-uid",
                   :db/id 3}],
 :block/uid "page-3-uid",
 :db/id 1,
 :node/title "test page 2"}