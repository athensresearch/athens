(ns athens.common-events.atomic-ops.page-merge-test
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [clojure.test                         :as t]
    [datascript.core                      :as d])
  #?(:clj
     (:import
       (clojure.lang
         ExceptionInfo))))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(t/deftest page-merge-tests
  (t/testing "simple case, no string representations need updating"
    (let [test-page-from-uid "test-page-1-1-uid"
          test-title-from    "test page 1 title from"
          test-page-to-uid   "test-page-1-2-uid"
          test-title-to      "test page 1 title to"
          test-block-1-uid   "test-block-1-1-uid"
          test-block-2-uid   "test-block-1-2-uid"
          setup-txs          [{:node/title     test-title-from
                               :block/uid      test-page-from-uid
                               :block/children [{:block/uid      test-block-1-uid
                                                 :block/string   ""
                                                 :block/order    0
                                                 :block/children []}]}
                              {:node/title     test-title-to
                               :block/uid      test-page-to-uid
                               :block/children [{:block/uid      test-block-2-uid
                                                 :block/string   ""
                                                 :block/order    0
                                                 :block/children []}]}]]
      ;; need to apply linkmaker, so resolving page-rename event can follow references for :block/string changes
      (fixture/transact-with-middleware setup-txs)
      (let [uid-by-title   (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)
            merge-page-txs (atomic-resolver/resolve-to-tx @@fixture/connection
                                                          (atomic-graph-ops/make-page-merge-op test-title-from test-title-to))]
        (t/is (= test-page-from-uid uid-by-title))
        (d/transact! @fixture/connection merge-page-txs)
        (let [{kids :block/children} (common-db/get-page-document @@fixture/connection [:node/title test-title-to])]
          (t/is (nil? (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)))
          (t/is (= 2 (count kids)))
          (t/is (= test-page-from-uid uid-by-title))))))

  (t/testing "complex case, where we need to update string representations as well"
    (let [test-page-from-uid "test-page-2-1-uid"
          test-title-from    "test page 2 title from"
          test-page-to-uid   "test-page-2-2-uid"
          test-title-to      "test page 2 title to"
          test-block-1-uid   "test-block-2-1-uid"
          test-block-2-uid   "test-block-2-2-uid"
          test-string-from   (str "[[" test-title-from "]]")
          test-string-to     (str "[[" test-title-to "]]")
          setup-txs          [{:node/title     test-title-from
                               :block/uid      test-page-from-uid
                               :block/children [{:block/uid      test-block-1-uid
                                                 :block/string   test-string-from
                                                 :block/order    0
                                                 :block/children []}]}
                              {:node/title     test-title-to
                               :block/uid      test-page-to-uid
                               :block/children [{:block/uid      test-block-2-uid
                                                 :block/string   test-string-from
                                                 :block/order    0
                                                 :block/children []}]}]]
      (fixture/transact-with-middleware setup-txs)
      (let [uid-by-title   (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)
            merge-page-txs (atomic-resolver/resolve-to-tx @@fixture/connection
                                                          (atomic-graph-ops/make-page-merge-op test-title-from test-title-to))]
        (t/is (= test-page-from-uid uid-by-title))
        (d/transact! @fixture/connection merge-page-txs)
        (let [{kids :block/children} (common-db/get-page-document @@fixture/connection [:node/title test-title-to])
              uid-by-title           (common-db/v-by-ea @@fixture/connection [:node/title test-title-to] :block/uid)
              block-string           (common-db/v-by-ea @@fixture/connection [:block/uid test-block-1-uid] :block/string)
              block-1                (common-db/get-block @@fixture/connection [:block/uid test-block-1-uid])
              block-2                (common-db/get-block @@fixture/connection [:block/uid test-block-2-uid])]
          (t/is (nil? (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)))
          (t/is (= 2 (count kids)))
          (t/is (= test-page-to-uid uid-by-title))
          (t/is (= test-string-to block-string))
          (t/is (= 0 (:block/order block-2)))
          (t/is (= 1 (:block/order block-1)))))))

  (t/testing "simple case, where we don't need to update string representations but there is a bit more then 1 block per page"
    (let [test-page-from-uid "test-page-3-1-uid"
          test-title-from    "test page 3 title from"
          test-page-to-uid   "test-page-3-2-uid"
          test-title-to      "test page 3 title to"
          test-block-1-uid   "test-block-3-1-uid"
          test-block-2-uid   "test-block-3-2-uid"
          test-block-3-uid   "test-block-3-3-uid"
          test-block-4-uid   "test-block-3-4-uid"
          test-string-from   (str "[[" test-title-from "]]")
          test-string-to     (str "[[" test-title-to "]]")
          setup-txs          [{:node/title     test-title-from
                               :block/uid      test-page-from-uid
                               :block/children [{:block/uid      test-block-1-uid
                                                 :block/string   test-string-from
                                                 :block/order    0
                                                 :block/children []}
                                                {:block/uid      test-block-2-uid
                                                 :block/string   test-string-from
                                                 :block/order    1
                                                 :block/children []}]}
                              {:node/title     test-title-to
                               :block/uid      test-page-to-uid
                               :block/children [{:block/uid      test-block-3-uid
                                                 :block/string   test-string-from
                                                 :block/order    0
                                                 :block/children []}
                                                {:block/uid      test-block-4-uid
                                                 :block/string   test-string-from
                                                 :block/order    1
                                                 :block/children []}]}]]
      (fixture/transact-with-middleware setup-txs)
      (let [uid-by-title   (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)
            merge-page-txs (atomic-resolver/resolve-to-tx @@fixture/connection
                                                          (atomic-graph-ops/make-page-merge-op test-title-from test-title-to))]
        (t/is (= test-page-from-uid uid-by-title))
        (d/transact! @fixture/connection merge-page-txs)
        (let [{kids :block/children} (common-db/get-page-document @@fixture/connection [:node/title test-title-to])
              uid-by-title           (common-db/v-by-ea @@fixture/connection [:node/title test-title-to] :block/uid)
              block-string           (common-db/v-by-ea @@fixture/connection [:block/uid test-block-1-uid] :block/string)
              block-1                (common-db/get-block @@fixture/connection [:block/uid test-block-1-uid])
              block-2                (common-db/get-block @@fixture/connection [:block/uid test-block-2-uid])
              block-3                (common-db/get-block @@fixture/connection [:block/uid test-block-3-uid])
              block-4                (common-db/get-block @@fixture/connection [:block/uid test-block-4-uid])]
          (t/is (nil? (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)))
          (t/is (= 4 (count kids)))
          (t/is (= test-page-to-uid uid-by-title))
          (t/is (= test-string-to block-string))
          (t/is (= 0 (:block/order block-3)))
          (t/is (= 1 (:block/order block-4)))
          (t/is (= 2 (:block/order block-1)))
          (t/is (= 3 (:block/order block-2))))))))


(let [from-title   "from-title"
      target-title "target-title"
      setup-repr   [{:page/title     from-title
                     :block/children [{:block/uid    "from-block-one"
                                       :block/string ""}
                                      {:block/uid    "from-block-two"
                                       :block/string ""}]}
                    {:page/title     target-title
                     :block/children [{:block/uid    "to-block-one"
                                       :block/string (str "ref to [[" from-title "]]")}
                                      {:block/uid    "to-block-two"
                                       :block/string ""}]}]
      exp-repr     [{:page/title     target-title
                     :block/children [{:block/uid    "to-block-one"
                                       :block/string (str "ref to [[" target-title "]]")}
                                      {:block/uid    "to-block-two"
                                       :block/string ""}
                                      {:block/uid    "from-block-one"
                                       :block/string ""}
                                      {:block/uid    "from-block-two"
                                       :block/string ""}]}]
      ;; We don't have a representation for shortcuts yet.
      setup-ops    [(atomic-graph-ops/make-shortcut-new-op from-title)
                    (atomic-graph-ops/make-shortcut-new-op target-title)]
      lookup       [:node/title target-title]
      merge!       #(-> (atomic-graph-ops/make-page-merge-op from-title target-title)
                        fixture/op-resolve-transact!)]

  (t/deftest undo-merge
    (fixture/setup! setup-repr setup-ops)

    (let [[evt-db evt] (merge!)]
      (t/is (= [(fixture/get-repr lookup)] exp-repr)
            "Merged children into target and replaced ref")
      (t/is (= (common-db/get-sidebar-titles @@fixture/connection)
               ["target-title"])
            "Removed shortcut")
      (fixture/undo! evt-db evt)
      (t/is (= setup-repr [(fixture/get-repr [:node/title from-title])
                           (fixture/get-repr [:node/title target-title])])
            "Undo restored to the original state")
      (t/is (= (common-db/get-sidebar-titles @@fixture/connection)
               ["from-title" "target-title"])
            "Undo restored shortcuts")))


  (t/deftest redo-merge
    (fixture/setup! setup-repr setup-ops)

    (let [[evt-db evt] (merge!)
          [evt-db' evt'] (fixture/undo! evt-db evt)]
      (fixture/undo! evt-db' evt')
      (t/is (= [(fixture/get-repr lookup)] exp-repr)
            "Redo merged children into target and replaced ref")
      (t/is (= (common-db/get-sidebar-titles @@fixture/connection)
               ["target-title"])
            "Redo removed shortcut"))))


(t/deftest merge-props
  (fixture/setup! [{:page/title "title"
                    :block/properties
                    {"key" #:block{:uid    "uid"
                                   :string ""}}}
                   {:page/title "another-title"
                    :block/properties
                    {"another-key" #:block{:uid    "another-uid"
                                           :string ""}}}])
  (fixture/op-resolve-transact! (atomic-graph-ops/make-page-merge-op "title" "another-title"))
  (fixture/is #{{:page/title "key"}
                {:page/title "another-key"}
                {:page/title "another-title"
                 :block/properties
                 {"key"         #:block{:uid    "uid"
                                        :string ""}
                  "another-key" #:block{:uid    "another-uid"
                                        :string ""}}}}))


(t/deftest merge-conflict-page-props
  (fixture/setup! [{:page/title "title"
                    :block/properties
                    {"key" #:block{:uid    "uid"
                                   :string ""}}}
                   {:page/title "another-title"
                    :block/properties
                    {"key" #:block{:uid    "another-uid"
                                   :string ""}}}])
  (fixture/op-resolve-transact! (atomic-graph-ops/make-page-merge-op "title" "another-title"))
  (fixture/is #{{:page/title "key"}
                {:page/title "another-title"
                 :block/properties
                 {"key" #:block{:uid    "another-uid"
                                :string ""}}}}))


(t/deftest merge-conflict-linked-props
  (fixture/setup! [{:page/title "title"
                    :block/properties
                    {"key"         #:block{:uid    "uid"
                                           :string ""}
                     "another-key" #:block{:uid    "another-uid"
                                           :string ""}}}])
  (fixture/op-resolve-transact! (atomic-graph-ops/make-page-merge-op "key" "another-key"))
  (fixture/is #{{:page/title "another-key"}
                {:page/title "title"
                 :block/properties
                 {"another-key" #:block{:uid    "another-uid"
                                        :string ""}}}}))
