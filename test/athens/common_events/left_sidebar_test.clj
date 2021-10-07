(ns athens.common-events.left-sidebar-test
  (:require
    [athens.common-events          :as common-events]
    [athens.common-events.fixture  :as fixture]
    [athens.common-events.resolver :as resolver]
    [clojure.test                  :as test]
    [datahike.api                  :as d]))


(test/use-fixtures :each fixture/integration-test-fixture)


(test/deftest left-sidebar-drop-above
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
      #(d/transact @fixture/connection {:block/uid      (first %)
                                        :node/title     (nth % 2)
                                        :block/children [{:block/uid      (second %)
                                                          :block/string   ""
                                                          :block/order    0
                                                          :block/children []}]})
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

    ;; left-sidebar-drop-above-event
    (->> (common-events/build-left-sidebar-drop-above -1 2 0)
         (resolver/resolve-event-to-tx @@fixture/connection)
         (d/transact @fixture/connection))

    (let [page-shortcut (->> (d/q '[:find (pull ?e [*])
                                    :where
                                    [?e :page/sidebar]]
                                  @@fixture/connection)
                             (sort-by (comp :page/sidebar first)))]

      (test/is
        (->> (map-indexed (fn [i title]
                            (= title (-> page-shortcut
                                         (nth i)
                                         first
                                         :node/title)))
                          [test-title-2 test-title-0 test-title-1])
             (every? true?))
        "check if the page-shortcut is correctly dropped above and become the first item"))

    ;; left-sidebar-drop-above-event
    (->> (common-events/build-left-sidebar-drop-above -1 2 1)
         (resolver/resolve-event-to-tx @@fixture/connection)
         (d/transact @fixture/connection))

    (let [page-shortcut (->> (d/q '[:find (pull ?e [*])
                                    :where
                                    [?e :page/sidebar]]
                                  @@fixture/connection)
                             (sort-by (comp :page/sidebar first)))]

      (test/is
        (->> (map-indexed (fn [i title]
                            (= title (-> page-shortcut
                                         (nth i)
                                         first
                                         :node/title)))
                          [test-title-2 test-title-1 test-title-0])
             (every? true?))
        "check if the page-shortcut is correctly dropped above and become the second item"))))


(test/deftest left-sidebar-drop-below
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
      #(d/transact @fixture/connection {:block/uid      (first %)
                                        :node/title     (nth % 2)
                                        :block/children [{:block/uid      (second %)
                                                          :block/string   ""
                                                          :block/order    0
                                                          :block/children []}]})
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

    ;; left-sidebar-drop-below-event
    (->> (common-events/build-left-sidebar-drop-below -1 0 2)
         (resolver/resolve-event-to-tx @@fixture/connection)
         (d/transact @fixture/connection))

    (let [page-shortcut (->> (d/q '[:find (pull ?e [*])
                                    :where
                                    [?e :page/sidebar]]
                                  @@fixture/connection)
                             (sort-by (comp :page/sidebar first)))]

      (test/is
        (->> (map-indexed (fn [i title]
                            (= title (-> page-shortcut
                                         (nth i)
                                         first
                                         :node/title)))
                          [test-title-1 test-title-2 test-title-0])
             (every? true?))
        "check if the page-shortcut is correctly dropped below and become the last item"))

    (let [_left-sidebar-drop-below-event (->> (common-events/build-left-sidebar-drop-below -1 0 1)
                                              (resolver/resolve-event-to-tx @@fixture/connection)
                                              (d/transact @fixture/connection))
          page-shortcut                  (->> (d/q '[:find (pull ?e [*])
                                                     :where
                                                     [?e :page/sidebar]]
                                                   @@fixture/connection)
                                              (sort-by (comp :page/sidebar first)))]

      (test/is
        (->> (map-indexed (fn [i title]
                            (= title (-> page-shortcut
                                         (nth i)
                                         first
                                         :node/title)))
                          [test-title-2 test-title-1 test-title-0])
             (every? true?))
        "check if the page-shortcut is correctly dropped below and become the second item"))))
