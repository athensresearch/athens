(ns athens.common-events.atomic-ops.page-remove-test
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


(t/deftest page-remove-test
  (t/testing "Removing page with no references"
    (let [test-uid        "test-page-uid-1"
          test-block-uid  "test-block-uid-1"
          test-title      "test page title 1"
          create-page-txs [{:block/uid      test-uid
                            :node/title     test-title
                            :block/children [{:block/uid      test-block-uid
                                              :block/order    0
                                              :block/string   ""
                                              :block/children []}]}]]
      (fixture/transact-with-middleware create-page-txs)
      (let [e-by-title (d/q '[:find ?e
                              :where [?e :node/title ?title]
                              :in $ ?title]
                            @@fixture/connection test-title)
            e-by-uid   (d/q '[:find ?e
                              :where [?e :block/uid ?uid]
                              :in $ ?uid]
                            @@fixture/connection test-uid)]
        (t/is (seq e-by-title))
        (t/is (= e-by-title e-by-uid)))

      (let [remove-page-op  (atomic-graph-ops/make-page-remove-op test-title)
            remove-page-txs (atomic-resolver/resolve-to-tx @@fixture/connection remove-page-op)]

        (d/transact! @fixture/connection remove-page-txs)
        (let [e-by-title (d/q '[:find ?e
                                :where [?e :node/title ?title]
                                :in $ ?title]
                              @@fixture/connection test-title)
              e-by-uid   (d/q '[:find ?e
                                :where [?e :block/uid ?uid]
                                :in $ ?uid]
                              @@fixture/connection test-uid)]
          (t/is (empty? e-by-title))
          (t/is (= e-by-title e-by-uid))))))

  (t/testing "Remove page with references"
    (let [test-page-1-title "test page 1 title"
          test-page-1-uid   "test-page-1-uid"
          test-page-2-title "test page 2 title"
          test-page-2-uid   "test-page-2-uid"
          block-text        (str "[[" test-page-1-title "]]")
          block-uid         "test-block-uid"
          setup-txs         [{:db/id          -1
                              :node/title     test-page-1-title
                              :block/uid      test-page-1-uid
                              :block/children [{:db/id          -2
                                                :block/uid      "test-block-1-uid"
                                                :block/string   ""
                                                :block/children []}]}
                             {:db/id          -3
                              :node/title     test-page-2-title
                              :block/uid      test-page-2-uid
                              :block/children [{:db/id        -4
                                                :block/uid    block-uid
                                                :block/string block-text}]}]
          query             '[:find ?text
                              :where
                              [?e :block/string ?text]
                              [?e :block/uid ?uid]
                              :in $ ?uid]]
      (fixture/transact-with-middleware setup-txs)
      (t/is (= #{[block-text]}
               (d/q query
                    @@fixture/connection
                    block-uid)))

      ;; remove page 1
      (d/transact! @fixture/connection
                   (->> test-page-1-title
                        (atomic-graph-ops/make-page-remove-op)
                        (atomic-resolver/resolve-to-tx @@fixture/connection)))
      ;; check if page reference was cleaned
      (t/is (= #{[test-page-1-title]}
               (d/q query
                    @@fixture/connection
                    block-uid))))))


