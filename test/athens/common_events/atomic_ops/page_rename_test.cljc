(ns athens.common-events.atomic-ops.page-rename-test
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


(t/deftest page-rename-atomic-test
  (t/testing "simple case, no string representations need updating"
    (let [test-page-uid   "test-page-1-1-uid"
          test-title-from "test page 1 title from"
          test-title-to   "test page 1 title to"
          test-block-uid  "test-block-1-1-uid"
          setup-txs       [{:db/id          -1
                            :node/title     test-title-from
                            :block/uid      test-page-uid
                            :block/children [{:db/id          -2
                                              :block/uid      test-block-uid
                                              :block/string   ""
                                              :block/order    0
                                              :block/children []}]}]]
      ;; need to apply linkmaker, so resolving page-rename event can follow references for :block/string changes
      (d/transact! @fixture/connection (common-db/linkmaker @@fixture/connection setup-txs))
      (let [uid-by-title    (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)
            rename-page-txs (atomic-resolver/resolve-to-tx @@fixture/connection
                                                           (atomic-graph-ops/make-page-rename-op test-title-from test-title-to))]

        (t/is (= test-page-uid uid-by-title))
        (d/transact! @fixture/connection rename-page-txs)
        (let [uid-by-title (common-db/v-by-ea @@fixture/connection [:node/title test-title-to] :block/uid)]
          (t/is (= test-page-uid uid-by-title))))))

  (t/testing "complex case, where we need to update string representations as well"
    (let [test-page-uid    "test-page-2-1-uid"
          test-title-from  "test page 2 title from"
          test-title-to    "test page 2 title to"
          test-block-uid   "test-block-2-1-uid"
          test-string-from (str "[[" test-title-from "]]")
          test-string-to   (str "[[" test-title-to "]]")
          setup-txs        [{:db/id          -1
                             :node/title     test-title-from
                             :block/uid      test-page-uid
                             :block/children [{:db/id          -2
                                               :block/uid      test-block-uid
                                               :block/string   test-string-from
                                               :block/order    0
                                               :block/children []}]}]]
      ;; need to apply linkmaker, so resolving page-rename event can follow references for :block/string changes
      (d/transact! @fixture/connection (common-db/linkmaker @@fixture/connection setup-txs))
      (let [uid-by-title    (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)
            rename-page-txs (atomic-resolver/resolve-to-tx @@fixture/connection
                                                           (atomic-graph-ops/make-page-rename-op test-title-from test-title-to))]
        (t/is (= test-page-uid uid-by-title))
        (d/transact! @fixture/connection rename-page-txs)
        (let [uid-by-title (common-db/v-by-ea @@fixture/connection [:node/title test-title-to] :block/uid)
              block-string (common-db/v-by-ea @@fixture/connection [:block/uid test-block-uid] :block/string)]
          (t/is (thrown-with-msg? #?(:cljs js/Error
                                     :clj ExceptionInfo)
                                  #"Nothing found for entity id"
                  (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)))
          (t/is (= test-page-uid uid-by-title))
          (t/is (= test-string-to block-string)))))))
