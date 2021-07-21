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


(test/deftest undo
  (let [block-uid   "test-block-uid"
        string-init "start test string"
        string-new  "new test string"
        setup-tx    [{:db/id          -1
                      :block/uid      block-uid
                      :block/string   string-init
                      :block/order    0
                      :block/children []}]]

    (d/transact @fixture/connection setup-tx)
    (let [block-save-event             (common-events/build-block-save-event -1
                                                                             block-uid
                                                                             string-new)
          block-save-txs               (resolver/resolve-event-to-tx @@fixture/connection
                                                                     block-save-event)
          {block-string :block/string} (common-db/get-block @@fixture/connection
                                                            [:block/uid  block-uid])]
      (test/is (= string-init block-string))
      (d/transact @fixture/connection block-save-txs)
      (let [{new-block-string :block/string} (common-db/get-block @@fixture/connection
                                                                  [:block/uid  block-uid])]
        (test/is (= string-new new-block-string))))

    ;; undo the previous event
    (let [undo-event (common-events/build-undo-redo-event -1 false)
          tx-data    (resolver/resolve-event-to-tx history undo-event)]
      (d/transact @fixture/connection tx-data))

    (let [{new-block-string :block/string} (common-db/get-block @@fixture/connection
                                                                [:block/uid  block-uid])]
      (test/is (nil? new-block-string)))))
