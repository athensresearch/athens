(ns athens.common-events.page-test
  (:require
    [athens.common-events          :as common-events]
    [athens.common-events.fixture  :as fixture]
    [athens.common-events.resolver :as resolver]
    [clojure.test                  :as test]
    [datascript.core               :as d]))


(test/use-fixtures :each fixture/integration-test-fixture)


(test/deftest add-page-shortcut
  (let [test-uid-0       "0"
        test-title-0     "Welcome"
        test-uid-1       "test-uid-1"
        test-block-uid-1 "test-block-uid-1"
        test-title-1     "test-title-1"
        test-uid-2       "test-uid-2"
        test-block-uid-2 "test-block-uid-2"
        test-title-2     "test-title-2"]

    ;; create new pages
    (run!
      #(d/transact! @fixture/connection [{:block/uid      (first %)
                                          :node/title     (nth % 2)
                                          :block/children [{:block/uid      (second %)
                                                            :block/string   ""
                                                            :block/order    0
                                                            :block/children []}]}])
      [[test-uid-1 test-block-uid-1 test-title-1]
       [test-uid-2 test-block-uid-2 test-title-2]])

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
      #(->> (common-events/build-page-add-shortcut %)
            (resolver/resolve-event-to-tx @@fixture/connection)
            (d/transact! @fixture/connection))
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
  (let [test-uid-0       "0"
        test-title-0     "Welcome"
        test-uid-1       "test-uid-1"
        test-block-uid-1 "test-block-uid-1"
        test-title-1     "test-title-1"
        test-uid-2       "test-uid-2"
        test-block-uid-2 "test-block-uid-2"
        test-title-2     "test-title-2"]

    ;; create new pages
    (run!
      #(d/transact! @fixture/connection [{:block/uid      (first %)
                                          :node/title     (nth % 2)
                                          :block/children [{:block/uid      (second %)
                                                            :block/string   ""
                                                            :block/order    0
                                                            :block/children []}]}])
      [[test-uid-1 test-block-uid-1 test-title-1]
       [test-uid-2 test-block-uid-2 test-title-2]])

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
      #(->> (common-events/build-page-add-shortcut %)
            (resolver/resolve-event-to-tx @@fixture/connection)
            (d/transact! @fixture/connection))
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
    (->> (common-events/build-page-remove-shortcut test-uid-1)
         (resolver/resolve-event-to-tx @@fixture/connection)
         (d/transact! @fixture/connection))

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
