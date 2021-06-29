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


(test/deftest page-shortcut
  (test/testing "Add page shortcut"
    (let [test-uid           "test-page-uid-1"
          test-title         "test page title 1"
          create-page-event  (common-events/build-page-create-event -1 test-uid test-title)
          create-page-txs    (resolver/resolve-event-to-tx @@fixture/connection
                                                           create-page-event)
          add-shortcut-event (common-events/build-page-add-shortcut -1 test-uid)
          add-shortcut-txs   (resolver/resolve-event-to-tx @@fixture/connection add-shortcut-event)]

      ;; create a new page
      (d/transact @fixture/connection create-page-txs)
      ;; add the new page to the left sidebar
      (d/transact @fixture/connection add-shortcut-txs)

      (test/is
        (not-empty (d/q '[:find (pull ?e [:page/sidebar])
                          :in $ ?uid
                          :where
                          [?e :block/uid ?uid]]
                        @@fixture/connection test-uid)))))

  (test/testing "Remove page shortcut"
    (let [test-uid              "test-page-uid-1"
          test-title            "test page title 1"
          create-page-event     (common-events/build-page-create-event -1 test-uid test-title)
          create-page-txs       (resolver/resolve-event-to-tx @@fixture/connection
                                                              create-page-event)
          add-shortcut-event    (common-events/build-page-add-shortcut -1 test-uid)
          add-shortcut-txs      (resolver/resolve-event-to-tx @@fixture/connection add-shortcut-event)
          remove-shortcut-event (common-events/build-page-remove-shortcut -1 test-uid)
          remove-shortcut-txs   (resolver/resolve-event-to-tx @@fixture/connection remove-shortcut-event)]

      ;; create a new page
      (d/transact @fixture/connection create-page-txs)
      ;; add the new page to the left sidebar
      (d/transact @fixture/connection add-shortcut-txs)
      ;; remove the new page from the left sidebar
      (d/transact @fixture/connection remove-shortcut-txs)

      (test/is
        (nil? (ffirst (d/q '[:find (pull ?e [:page/sidebar])
                             :in $ ?uid
                             :where
                             [?e :block/uid ?uid]]
                           @@fixture/connection test-uid))))))

  (test/testing "Reindex page shortcut"
    (let [test-uid-1   "test-page-uid-1"
          test-title-1 "test page title 1"
          test-uid-2   "test-page-uid-2"
          test-title-2 "test page title 2"
          test-uid-3   "test-page-uid-3"
          test-title-3 "test page title 3"]

      (test/testing "Reindex page shortcut after adding page shortcut"
        (->> (common-events/build-page-create-event -1 test-uid-1 test-title-1)
             (resolver/resolve-event-to-tx @@fixture/connection)
             (d/transact @fixture/connection))
        (->> (common-events/build-page-create-event -1 test-uid-2 test-title-2)
             (resolver/resolve-event-to-tx @@fixture/connection)
             (d/transact @fixture/connection))
        (->> (common-events/build-page-create-event -1 test-uid-3 test-title-3)
             (resolver/resolve-event-to-tx @@fixture/connection)
             (d/transact @fixture/connection))

        ;; add page 1 to the left sidebar and reindex
        (->> (common-events/build-page-add-shortcut -1 test-uid-1)
             (resolver/resolve-event-to-tx @@fixture/connection)
             (d/transact @fixture/connection))

        (->> (common-events/build-page-reindex-left-sidebar -1)
             (resolver/resolve-event-to-tx @@fixture/connection)
             (d/transact @fixture/connection))

        ;; add page 2 to the left sidebar and reindex
        (->> (common-events/build-page-add-shortcut -1 test-uid-2)
             (resolver/resolve-event-to-tx @@fixture/connection)
             (d/transact @fixture/connection))

        (->> (common-events/build-page-reindex-left-sidebar -1)
             (resolver/resolve-event-to-tx @@fixture/connection)
             (d/transact @fixture/connection))

        ;; add page 3 to the left sidebar and reindex
        (->> (common-events/build-page-add-shortcut -1 test-uid-3)
             (resolver/resolve-event-to-tx @@fixture/connection)
             (d/transact @fixture/connection))

        (->> (common-events/build-page-reindex-left-sidebar -1)
             (resolver/resolve-event-to-tx @@fixture/connection)
             (d/transact @fixture/connection))

        (let [page-sidebar (->> (d/q '[:find (pull ?e [:page/sidebar :block/uid])
                                       :where
                                       [?e :page/sidebar _]]
                                     @@fixture/connection)
                                (sort-by (comp :page/sidebar first))
                                (into []))]
          (test/is (= test-uid-1 (get-in page-sidebar [1 0 :block/uid])) "test-uid-1 should be in index 1")
          (test/is (= test-uid-2 (get-in page-sidebar [2 0 :block/uid])) "test-uid-1 should be in index 2")
          (test/is (= test-uid-3 (get-in page-sidebar [3 0 :block/uid])) "test-uid-1 should be in index 3")))

      (test/testing "Reindex page shortcut after removing a shortcut"
        (->> (common-events/build-page-remove-shortcut -1 test-uid-2)
             (resolver/resolve-event-to-tx @@fixture/connection)
             (d/transact @fixture/connection))

        (let [page-sidebar (->> (d/q '[:find (pull ?e [:page/sidebar :block/uid])
                                       :where
                                       [?e :page/sidebar _]]
                                     @@fixture/connection)
                                (sort-by (comp :page/sidebar first))
                                (into []))]
          (test/is (= test-uid-1 (get-in page-sidebar [1 0 :block/uid])) "test-uid-1 should be in index 1")
          (test/is (= test-uid-3 (get-in page-sidebar [2 0 :block/uid])) "test-uid-3 should be in index 2")
          (test/is (empty? (filter #(= (comp :block/uid first %) test-uid-2) page-sidebar)) "test-uid-2 should not be in the vector"))))))

