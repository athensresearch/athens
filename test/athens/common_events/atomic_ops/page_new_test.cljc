(ns athens.common-events.atomic-ops.page-new-test
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.graph.ops       :as graph-ops]
    [clojure.test                         :as t]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(t/deftest page-new-atomic-test
  (t/testing "page/new when page didn't exist yet"
    (let [test-title "test page title"
          save!      #(->> (atomic-graph-ops/make-page-new-op test-title)
                           fixture/op-resolve-transact!)]
      (t/is (nil? (common-db/get-page-document @@fixture/connection [:node/title test-title])))
      (save!)
      (t/is (common-db/get-page-document @@fixture/connection [:node/title test-title]))))

  (t/testing "page/new daily page resolves to special uid"
    (let [title "October 22, 2021"
          uid   "10-22-2021"]
      (->> (atomic-graph-ops/make-page-new-op title)
           fixture/op-resolve-transact!)
      (t/is (= uid (common-db/get-page-uid @@fixture/connection title))))))


(t/deftest page-new-composite-test
  (t/testing "that `:page/new` with block uid generates composite ops when page doesn't exist."
    (let [page-title  "page 1 title"
          page-new-op (graph-ops/build-page-new-op @@fixture/connection page-title "uid")]
      (t/is (= :composite/consequence (:op/type page-new-op))))))


(t/deftest undo-page-new-atomic
  (t/testing "page/new when page didn't exist yet"
    (let [test-title "test page title"
          save!      #(->> (atomic-graph-ops/make-page-new-op test-title)
                           (fixture/op-resolve-transact!))]
      ;; setup
      (t/is (nil? (common-db/get-page-document @@fixture/connection [:node/title test-title])))
      (let [[db evt] (save!)]
        ;; new page
        (t/is (common-db/get-page-document @@fixture/connection [:node/title test-title]))
        ;; undo (remove page)
        (let [[db' evt'] (fixture/undo! db evt)]
          (t/is (nil? (common-db/get-page-document @@fixture/connection [:node/title test-title])))
          (fixture/undo! db' evt')
          (t/is (common-db/get-page-document @@fixture/connection [:node/title test-title])))))))


(t/deftest undo-page-new-composite
  (t/testing "page/new when page didn't exist yet"
    (let [test-title   "test-title"
          block-uid    "block-uid"
          save!        #(->> (graph-ops/build-page-new-op @@fixture/connection test-title block-uid)
                             (fixture/op-resolve-transact!))]
      ;; setup
      (t/is (nil? (common-db/get-page-document @@fixture/connection [:node/title test-title])))
      (t/is (nil? (common-db/get-block @@fixture/connection [:block/uid block-uid])))
      (let [[db evt] (save!)]
        ;; new page
        (t/is (common-db/get-page-document @@fixture/connection [:node/title test-title]))
        (t/is (common-db/get-block @@fixture/connection [:block/uid block-uid]))
        ;; undo (remove page)
        (let [[db' evt'] (fixture/undo! db evt)]
          (t/is (nil? (common-db/get-page-document @@fixture/connection [:node/title test-title])))
          (t/is (nil? (common-db/get-block @@fixture/connection [:block/uid block-uid])))
          ;; redo
          (fixture/undo! db' evt')
          (t/is (common-db/get-page-document @@fixture/connection [:node/title test-title]))
          (t/is (common-db/get-block @@fixture/connection [:block/uid block-uid])))))))
