(ns athens.common-events.atomic-ops.page-merge-test
  (:require
    [athens.common-db                     :as common-db]
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
          (t/is (thrown-with-msg? #?(:cljs js/Error
                                     :clj ExceptionInfo)
                                  #"Nothing found for entity id"
                  (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)))
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
              block-string           (common-db/v-by-ea @@fixture/connection [:block/uid test-block-1-uid] :block/string)]
          (t/is (thrown-with-msg? #?(:cljs js/Error
                                     :clj ExceptionInfo)
                                  #"Nothing found for entity id"
                  (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)))
          (t/is (= 2 (count kids)))
          (t/is (= test-page-to-uid uid-by-title))
          (t/is (= test-string-to block-string)))))))
