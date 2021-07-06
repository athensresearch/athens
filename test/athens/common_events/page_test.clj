(ns athens.common-events.page-test
  (:require
    [athens.common-db              :as common-db]
    [athens.common-events          :as common-events]
    [athens.common-events.fixture  :as fixture]
    [athens.common-events.resolver :as resolver]
    [clojure.string                :as string]
    [clojure.test                  :as test]
    [datahike.api                  :as d])
  (:import
    (clojure.lang
      ExceptionInfo)))


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


(test/deftest rename-page
  (test/testing "simple case, no string representations need updating"
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
      (d/transact @fixture/connection (common-db/linkmaker @@fixture/connection setup-txs))
      (let [uid-by-title    (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)
            rename-page-txs (resolver/resolve-event-to-tx @@fixture/connection
                                                          (common-events/build-page-rename-event -1
                                                                                                 test-page-uid
                                                                                                 test-title-from
                                                                                                 test-title-to))]
        (test/is (= test-page-uid uid-by-title))
        (d/transact @fixture/connection rename-page-txs)
        (let [uid-by-title (common-db/v-by-ea @@fixture/connection [:node/title test-title-to] :block/uid)]
          (test/is (= test-page-uid uid-by-title))))))

  (test/testing "complex case, where we need to update string representations as well"
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
      (d/transact @fixture/connection (common-db/linkmaker @@fixture/connection setup-txs))
      (let [uid-by-title    (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)
            rename-page-txs (resolver/resolve-event-to-tx @@fixture/connection
                                                          (common-events/build-page-rename-event -1
                                                                                                 test-page-uid
                                                                                                 test-title-from
                                                                                                 test-title-to))]
        (test/is (= test-page-uid uid-by-title))
        (d/transact @fixture/connection rename-page-txs)
        (let [uid-by-title (common-db/v-by-ea @@fixture/connection [:node/title test-title-to] :block/uid)
              block-string (common-db/v-by-ea @@fixture/connection [:block/uid test-block-uid] :block/string)]
          (test/is (thrown-with-msg? ExceptionInfo #"Nothing found for entity id"
                     (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)))
          (test/is (= test-page-uid uid-by-title))
          (test/is (= test-string-to block-string)))))))


(test/deftest merge-page
  (test/testing "simple case, no string representations need updating"
    (let [test-page-from-uid "test-page-1-1-uid"
          test-title-from    "test page 1 title from"
          test-page-to-uid   "test-page-1-2-uid"
          test-title-to      "test page 1 title to"
          test-block-1-uid   "test-block-1-1-uid"
          test-block-2-uid   "test-block-1-2-uid"
          setup-txs          [{:db/id          -1
                               :node/title     test-title-from
                               :block/uid      test-page-from-uid
                               :block/children [{:db/id          -2
                                                 :block/uid      test-block-1-uid
                                                 :block/string   ""
                                                 :block/order    0
                                                 :block/children []}]}
                              {:db/id          -3
                               :node/title     test-title-to
                               :block/uid      test-page-to-uid
                               :block/children [{:db/id          -4
                                                 :block/uid      test-block-2-uid
                                                 :block/string   ""
                                                 :block/order    0
                                                 :block/children []}]}]]
      ;; need to apply linkmaker, so resolving page-rename event can follow references for :block/string changes
      (d/transact @fixture/connection (common-db/linkmaker @@fixture/connection setup-txs))
      (let [uid-by-title   (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)
            merge-page-txs (resolver/resolve-event-to-tx @@fixture/connection
                                                         (common-events/build-page-merge-event -1
                                                                                               test-page-from-uid
                                                                                               test-title-from
                                                                                               test-title-to))]
        (test/is (= test-page-from-uid uid-by-title))
        (d/transact @fixture/connection merge-page-txs)
        (let [{kids :block/children} (common-db/get-page-document @@fixture/connection [:node/title test-title-to])]
          (test/is (thrown-with-msg? ExceptionInfo #"Nothing found for entity id"
                     (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)))
          (test/is (= 2 (count kids)))
          (test/is (= test-page-from-uid uid-by-title))))))

  (test/testing "complex case, where we need to update string representations as well"
    (let [test-page-from-uid "test-page-2-1-uid"
          test-title-from    "test page 2 title from"
          test-page-to-uid   "test-page-2-2-uid"
          test-title-to      "test page 2 title to"
          test-block-1-uid   "test-block-2-1-uid"
          test-block-2-uid   "test-block-2-2-uid"
          test-string-from   (str "[[" test-title-from "]]")
          test-string-to     (str "[[" test-title-to "]]")
          setup-txs          [{:db/id          -1
                               :node/title     test-title-from
                               :block/uid      test-page-from-uid
                               :block/children [{:db/id          -2
                                                 :block/uid      test-block-1-uid
                                                 :block/string   test-string-from
                                                 :block/order    0
                                                 :block/children []}]}
                              {:db/id          -3
                               :node/title     test-title-to
                               :block/uid      test-page-to-uid
                               :block/children [{:db/id          -4
                                                 :block/uid      test-block-2-uid
                                                 :block/string   test-string-from
                                                 :block/order    0
                                                 :block/children []}]}]]
      ;; need to apply linkmaker, so resolving page-rename event can follow references for :block/string changes
      (d/transact @fixture/connection (common-db/linkmaker @@fixture/connection setup-txs))
      (let [uid-by-title   (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)
            merge-page-txs (resolver/resolve-event-to-tx @@fixture/connection
                                                         (common-events/build-page-merge-event -1
                                                                                               test-page-from-uid
                                                                                               test-title-from
                                                                                               test-title-to))]
        (test/is (= test-page-from-uid uid-by-title))
        (d/transact @fixture/connection merge-page-txs)
        (let [{kids :block/children} (common-db/get-page-document @@fixture/connection [:node/title test-title-to])
              uid-by-title           (common-db/v-by-ea @@fixture/connection [:node/title test-title-to] :block/uid)
              block-string           (common-db/v-by-ea @@fixture/connection [:block/uid test-block-1-uid] :block/string)]
          (test/is (thrown-with-msg? ExceptionInfo #"Nothing found for entity id"
                     (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)))
          (test/is (= 2 (count kids)))
          (test/is (= test-page-to-uid uid-by-title))
          (test/is (= test-string-to block-string)))))))


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


(test/deftest add-page-shortcut
  (let [test-uid-0    "0"
        test-title-0  "Welcome"
        test-uid-1    "test-uid-1"
        test-title-1  "test-title-1"
        test-uid-2    "test-uid-2"
        test-title-2  "test-title-2"]

    ;; create new pages
    (run!
      #(->> (common-events/build-page-create-event -1 (first %) (second %))
            (resolver/resolve-event-to-tx @@fixture/connection)
            (d/transact @fixture/connection))
      [[test-uid-1 test-title-1]
       [test-uid-2 test-title-2]])

    (let [pages (->> (d/q '[:find ?b
                            :where
                            [?e :block/uid ?b]]
                          @@fixture/connection))]
      (test/is
        (-> (map first pages)
            set
            (every? #{test-uid-0 test-uid-1 test-uid-2}))
        "check if every test-uid-* is added to db"))

    ;; add the pages to the page shortcut
    (run!
      #(->> (common-events/build-page-add-shortcut -1 %)
            (resolver/resolve-event-to-tx @@fixture/connection)
            (d/transact @fixture/connection))
      [test-uid-0 test-uid-1 test-uid-2])

    (let [page-shortcut (->> (d/q '[:find (pull ?e [*])
                                    :where
                                    [?e :page/sidebar]]
                                  @@fixture/connection)
                             (sort-by (comp :page/sidebar first)))]

      (test/is
        (->> (map (comp :block/uid first) page-shortcut)
             (every? #{test-uid-0 test-uid-1 test-uid-2}))
        "check if every test-uid-* is added to page-shortcut")

      (test/is
        (->> (map-indexed (fn [i title]
                            (= title (-> page-shortcut
                                         (nth i)
                                         first
                                         :node/title)))
                          [test-title-0 test-title-1 test-title-2])
             (every? true?))
        "check if the page-shortcuts are added based on the sequence of the moment they're added"))))


(test/deftest remove-page-shortcut
  (let [test-uid-0    "0"
        test-title-0  "Welcome"
        test-uid-1    "test-uid-1"
        test-title-1  "test-title-1"
        test-uid-2    "test-uid-2"
        test-title-2  "test-title-2"]

    ;; create new pages
    (run!
      #(->> (common-events/build-page-create-event -1 (first %) (second %))
            (resolver/resolve-event-to-tx @@fixture/connection)
            (d/transact @fixture/connection))
      [[test-uid-1 test-title-1]
       [test-uid-2 test-title-2]])

    (let [pages (->> (d/q '[:find ?b
                            :where
                            [?e :block/uid ?b]]
                          @@fixture/connection))]
      (test/is
        (-> (map first pages)
            set
            (every? #{test-uid-0 test-uid-1 test-uid-2}))
        "check if every test-uid-* is added to db"))

    ;; add the pages to the page shortcut
    (run!
      #(->> (common-events/build-page-add-shortcut -1 %)
            (resolver/resolve-event-to-tx @@fixture/connection)
            (d/transact @fixture/connection))
      [test-uid-0 test-uid-1 test-uid-2])

    (let [page-shortcut (->> (d/q '[:find (pull ?e [*])
                                    :where
                                    [?e :page/sidebar]]
                                  @@fixture/connection)
                             (sort-by (comp :page/sidebar first)))]

      (test/is
        (->> (map (comp :block/uid first) page-shortcut)
             (every? #{test-uid-0 test-uid-1 test-uid-2}))
        "check if every test-uid-* is added to page-shortcut")

      (test/is
        (->> (map-indexed (fn [i title]
                            (= title (-> page-shortcut
                                         (nth i)
                                         first
                                         :node/title)))
                          [test-title-0 test-title-1 test-title-2])
             (every? true?))
        "check if the page-shortcuts are added based on the sequence of the moment they're added"))

    ;; remove a page from the page-shortcut
    (->> (common-events/build-page-remove-shortcut -1 test-uid-1)
         (resolver/resolve-event-to-tx @@fixture/connection)
         (d/transact @fixture/connection))

    (let [page-shortcut (->> (d/q '[:find (pull ?e [*])
                                    :where
                                    [?e :page/sidebar]]
                                  @@fixture/connection)
                             (sort-by (comp :page/sidebar first)))]
      (test/is
        (->> (map (comp :block/uid first) page-shortcut)
             (not-any? #{test-uid-1}))
        "check if the page is removed from the shortcuts")

      (test/is
        (->> (map-indexed (fn [i title]
                            (= title (-> page-shortcut
                                         (nth i)
                                         first
                                         :node/title)))
                          [test-title-0 test-title-2])
             (every? true?))
        "check if the page shortcuts are still ordered after removing a page"))))


(test/deftest link-unlinked-refs
  (let [page-1-uid "page-1-uid"
        child-1    ["child-1-uid" "test"]
        child-2    ["child-2-uid" "Test"]
        child-3    ["child-3-uid" "attest"]
        child-4    ["child-4-uid" "atTest"]
        setup-txs  [{:db/id          -1
                     :block/uid      page-1-uid
                     :node/title     "Test"
                     :block/children (map-indexed
                                       (fn [i [uid str]]
                                         {:db/id        (- i -2)
                                          :block/uid      uid
                                          :block/string   str
                                          :block/order    i
                                          :block/children []})
                                       [child-1 child-2 child-3 child-4])}]]

    ;; create blocks
    (d/transact @fixture/connection setup-txs)

    (let [page-1-children (->> (d/q '[:find (pull ?c [:block/uid])
                                      :in $ ?uid
                                      :where
                                      [?e :block/uid ?uid]
                                      [?e :block/children ?c]]
                                    @@fixture/connection page-1-uid)
                               flatten
                               (map :block/uid))]

      (test/is
        (every? (set (mapv first [child-1 child-2 child-3 child-4])) page-1-children)
        "check if every blocks are added"))

    ;; unlinked refs transaction
    (run!
      (fn [[uid string]]
        (->> (common-events/build-unlinked-references-link -1 uid string "Test")
             (resolver/resolve-event-to-tx @@fixture/connection)
             (d/transact @fixture/connection)))
      [child-1 child-2 child-3 child-4])

    (let [linked-blocks (->> (d/q '[:find (pull ?c [*])
                                    :in $ ?uid
                                    :where
                                    [?e :block/uid ?uid]
                                    [?e :block/children ?c]]
                                  @@fixture/connection page-1-uid)
                             flatten
                             (mapv :block/string))]
      (test/is
        (= ["at[[Test]]" "[[Test]]" "at[[Test]]" "[[Test]]"] linked-blocks)))))


(test/deftest link-unlinked-refs-all
  (let [page-1-title "Test"
        page-1-uid "page-1-uid"
        child-1    ["child-1-uid" "test"]
        child-2    ["child-2-uid" "Test"]
        child-3    ["child-3-uid" "attest"]
        child-4    ["child-4-uid" "atTest"]
        setup-txs  [{:db/id          -1
                     :block/uid      page-1-uid
                     :node/title     page-1-title
                     :block/children (map-indexed
                                       (fn [i [uid str]]
                                         {:db/id        (- i -2)
                                          :block/uid      uid
                                          :block/string   str
                                          :block/order    i
                                          :block/children []})
                                       [child-1 child-2 child-3 child-4])}]]

    ;; block transaction
    (d/transact @fixture/connection setup-txs)

    (let [page-1-children (->> (d/q '[:find (pull ?c [:block/uid])
                                      :in $ ?uid
                                      :where
                                      [?e :block/uid ?uid]
                                      [?e :block/children ?c]]
                                    @@fixture/connection page-1-uid)
                               flatten
                               (map :block/uid))]

      (test/is
        (every? (set (mapv first [child-1 child-2 child-3 child-4])) page-1-children)
        "check if every blocks are added"))


    (let [unlinked-refs          (common-db/get-unlinked-references
                                   @@fixture/connection
                                   (string/escape page-1-title (let [esc-chars "()*&^%$#![]"]
                                                                 (zipmap esc-chars
                                                                         (map #(str "\\" %) esc-chars)))))] ; same as escape-str in athens.util
      ;; link unlinked refs all transaction
      (->> (common-events/build-unlinked-references-link-all -1 unlinked-refs page-1-title)
           (resolver/resolve-event-to-tx @@fixture/connection)
           (d/transact @fixture/connection)))

    (let [linked-blocks          (->> (d/q '[:find (pull ?c [*])
                                             :in $ ?uid
                                             :where
                                             [?e :block/uid ?uid]
                                             [?e :block/children ?c]]
                                           @@fixture/connection page-1-uid)
                                      flatten
                                      (mapv :block/string))]
      (test/is
        (= ["at[[Test]]" "[[Test]]" "at[[Test]]" "[[Test]]"] linked-blocks)))))
