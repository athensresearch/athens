(ns athens.common-events.page-test
  (:require
    [athens.common-events          :as common-events]
    [athens.common-events.fixture  :as fixture]
    [athens.common-events.resolver :as resolver]
    [clojure.test                  :as test]
    [datahike.api                  :as d]))


(test/use-fixtures :each fixture/integration-test-fixture)


(test/deftest create-page
  (let [test-title        "test page title"
        test-uid          "test-page-uid-1"
        create-page-event (common-events/build-page-create-event -1 test-uid test-title)
        txs               (resolver/resolve-event-to-tx @@fixture/connection
                                                        create-page-event)]
    (d/transact @fixture/connection txs)
    (let [e-by-title (d/q '[:find ?e
                            :where [?e :node/title ?title]
                            :in $ ?title]
                          @@fixture/connection test-title)
          e-by-uid (d/q '[:find ?e
                          :where [?e :block/uid ?uid]
                          :in $ ?uid]
                        @@fixture/connection test-uid)]
      (test/is (seq e-by-title))
      (test/is (= e-by-title e-by-uid)))))


(test/deftest delete-page
  (test/testing "Deleting page with no references"
    (let [test-uid          "test-page-uid-1"
          test-title        "test page title 1"
          create-page-event (common-events/build-page-create-event -1 test-uid test-title)
          create-page-txs   (resolver/resolve-event-to-tx @@fixture/connection
                                                          create-page-event)]

      (d/transact @fixture/connection create-page-txs)
      (let [e-by-title (d/q '[:find ?e
                              :where [?e :node/title ?title]
                              :in $ ?title]
                            @@fixture/connection test-title)
            e-by-uid   (d/q '[:find ?e
                              :where [?e :block/uid ?uid]
                              :in $ ?uid]
                            @@fixture/connection test-uid)]
        (test/is (seq e-by-title))
        (test/is (= e-by-title e-by-uid)))

      (let [delete-page-event (common-events/build-page-delete-event -1 test-uid)
            delete-page-txs   (resolver/resolve-event-to-tx @@fixture/connection
                                                            delete-page-event)]

        (d/transact @fixture/connection delete-page-txs)
        (let [e-by-title (d/q '[:find ?e
                                :where [?e :node/title ?title]
                                :in $ ?title]
                              @@fixture/connection test-title)
              e-by-uid   (d/q '[:find ?e
                                :where [?e :block/uid ?uid]
                                :in $ ?uid]
                              @@fixture/connection test-uid)]
          (test/is (empty? e-by-title))
          (test/is (= e-by-title e-by-uid))))))

  (test/testing "Delete page with references"
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
      (d/transact @fixture/connection setup-txs)
      (println "Delete page:" @@fixture/connection)
      (test/is (= #{[block-text]}
                  (d/q query
                       @@fixture/connection
                       block-uid)))

      ;; delete page 1
      (d/transact @fixture/connection
                  (->> test-page-1-uid
                       (common-events/build-page-delete-event -1)
                       (resolver/resolve-event-to-tx @@fixture/connection)))
      ;; check if page reference was cleaned
      (test/is (= #{[test-page-1-title]}
                  (d/q query
                       @@fixture/connection
                       block-uid))))))
