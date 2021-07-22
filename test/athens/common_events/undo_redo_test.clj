(ns athens.common-events.undo-redo-test
  (:require
    [athens.common-db              :as common-db]
    [athens.common-events          :as common-events]
    [athens.common-events.fixture  :as fixture]
    [athens.common-events.resolver :as resolver]
    [clojure.test                  :as test]
    [datahike.api                  :as d]))


;; history

(def history (atom '()))


;; this gives us customization options
;; now if there is a pattern for a tx then the datoms can be
;; easily modified(mind the order of datoms) to add a custom undo/redo strategy
;; Not seeing a use case now, but there is an option to do it
(defn listen!
  [test-fn]
  (d/listen @fixture/connection :history
            (fn [tx-report]
              (when-not (or (->> tx-report :tx-data (some (fn [datom]
                                                            (= (nth datom 1)
                                                               :from-undo-redo))))
                            (->> tx-report :tx-data empty?))

                (swap! history (fn [buff]
                                 (->> buff (remove (fn [[_ applied? _]]
                                                     (not applied?)))
                                      doall)))

                (swap! history (fn [cur-his]
                                 (cons [(-> tx-report :tx-data first (nth 3)) ; removed the `vec` function here because its causing an error
                                        true
                                        (:tx-data tx-report)]
                                       cur-his))))))
  (test-fn)
  (reset! history (atom '()))
  (d/unlisten @fixture/connection :history))


(test/use-fixtures :each fixture/integration-test-fixture listen!)


(test/deftest undo-redo
  (let [test-title        "test page title"
        test-uid          "test-page-uid-1"
        test-block-uid    "test-block-uid-1"
        create-page-event (common-events/build-page-create-event -1 test-uid test-block-uid test-title)
        create-page-tx    (resolver/resolve-event-to-tx @@fixture/connection
                                                        create-page-event)
        block-uid         "test-block-uid"
        string-init       "start test string"
        new-block-tx      [{:db/id          -1
                            :block/uid      block-uid
                            :block/string   string-init
                            :block/order    0
                            :block/children []}]]
    ;; create a new page
    (d/transact @fixture/connection create-page-tx)
    (let [e-by-title (d/q '[:find ?e
                            :where [?e :node/title ?title]
                            :in $ ?title]
                          @@fixture/connection test-title)
          e-by-uid (d/q '[:find ?e
                          :where [?e :block/uid ?uid]
                          :in $ ?uid]
                        @@fixture/connection test-uid)]
      (test/is (seq e-by-title))
      (test/is (= e-by-title e-by-uid)))

    ;; create a new block
    (d/transact @fixture/connection new-block-tx)
    (let [{block-string :block/string} (common-db/get-block @@fixture/connection
                                                            [:block/uid  block-uid])]
      (test/is (= string-init block-string)))

    ;; undo and test the creation of the new block
    ;; also check if the new page is still in db
    (let [undo-event (common-events/build-undo-redo-event -1 false)
          tx-data    (resolver/resolve-event-to-tx history undo-event)]
      (d/transact @fixture/connection tx-data))

    (let [block (d/q '[:find ?e
                       :in $ ?uid
                       :where
                       [?e :block/uid ?uid]]
                     @@fixture/connection
                     block-uid)]
      (test/is (empty? block)))

    (let [e-by-title (d/q '[:find ?e
                            :where [?e :node/title ?title]
                            :in $ ?title]
                          @@fixture/connection test-title)
          e-by-uid (d/q '[:find ?e
                          :where [?e :block/uid ?uid]
                          :in $ ?uid]
                        @@fixture/connection test-uid)]
      (test/is (seq e-by-title))
      (test/is (= e-by-title e-by-uid)))

    ;; undo and test the creation of the new page
    (let [undo-event (common-events/build-undo-redo-event -1 false)
          tx-data    (resolver/resolve-event-to-tx history undo-event)]
      (d/transact @fixture/connection tx-data))

    (let [e-by-title (d/q '[:find ?e
                            :where [?e :node/title ?title]
                            :in $ ?title]
                          @@fixture/connection test-title)
          e-by-uid (d/q '[:find ?e
                          :where [?e :block/uid ?uid]
                          :in $ ?uid]
                        @@fixture/connection test-uid)]
      (test/is (empty? e-by-title))
      (test/is (empty? e-by-uid)))

    ;; redo and test the creation of the new page
    (let [undo-event (common-events/build-undo-redo-event -1 true)
          tx-data    (resolver/resolve-event-to-tx history undo-event)]
      (d/transact @fixture/connection tx-data))

    (let [e-by-title (d/q '[:find ?e
                            :where [?e :node/title ?title]
                            :in $ ?title]
                          @@fixture/connection test-title)
          e-by-uid (d/q '[:find ?e
                          :where [?e :block/uid ?uid]
                          :in $ ?uid]
                        @@fixture/connection test-uid)]
      (test/is (seq e-by-title))
      (test/is (= e-by-title e-by-uid)))

    ;; redo and test the creation of the new block
    (let [undo-event (common-events/build-undo-redo-event -1 true)
          tx-data    (resolver/resolve-event-to-tx history undo-event)]
      (d/transact @fixture/connection tx-data))

    (let [{block-string :block/string} (common-db/get-block @@fixture/connection
                                                            [:block/uid  block-uid])]
      (test/is (= string-init block-string)))))
