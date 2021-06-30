(ns athens.common-events.linkmaker-test
  (:require
    [athens.common-db              :as common-db]
    [athens.common-events          :as common-events]
    [athens.common-events.fixture  :as fixture]
    [athens.common-events.resolver :as resolver]
    [clojure.test                  :as t]
    [datahike.api                  :as d]))


(t/use-fixtures :each fixture/integration-test-fixture)


(t/deftest p1-page-created
  (t/testing "New page created, nothing referring to it")

  (t/testing "New page created, references found and updated"
             ;; This actually is very unlikely in current setup,
             ;; because when page link (to not existing page) is encountered in updated block
             ;; we're creating page.
             ))


;; 

(t/deftest b1-block-with-new-page-ref
  (t/testing "New page reference to existing page in block"
    (let [target-page-uid   "target-page-1-1-uid"
          target-page-title "Target Page Title 1 1"
          source-page-uid   "source-page-1-1-uid"
          source-page-title "Source Page Title 1 1"
          testing-block-uid "testing-block-1-1-uid"
          setup-tx          [{:db/id          -1
                              :node/title     target-page-title
                              :block/uid      target-page-uid
                              :block/children [{:db/id        -2
                                                :block/uid    "irrelevant-1-1"
                                                :block/string ""
                                                :block/order  0}]}
                             {:db/id          -3
                              :node/title     source-page-title
                              :block/uid      source-page-uid
                              :block/children [{:db/id        -4
                                                :block/uid    testing-block-uid
                                                :block/string ""
                                                :block/order  0}]}]]
      (d/transact @fixture/connection setup-tx)
      ;; assert that target page has no `:block/refs` to start with
      (let [target-page (common-db/get-page-document @@fixture/connection [:block/uid target-page-uid])
            add-link-tx [{:db/id        [:block/uid testing-block-uid]
                          :block/string (str "[[" target-page-title "]]")}]]
        (t/is (empty? (:block/refs target-page)))

        (d/transact @fixture/connection (common-db/linkmaker @@fixture/connection add-link-tx))
        (let [{block-refs :block/refs} (common-db/get-page-document @@fixture/connection [:block/uid target-page-uid])]
          ;; assert that we do have new ref
          (t/is (seq block-refs))
          (t/is (= #{testing-block-uid} block-refs))))))

  (t/testing "New page reference to not existing page in block")

  (t/testing "We're splitting block so 1st Page link stays in 1st block, and 2nd Page link goes to a new block"
    (let [target-page-1-uid   "target-page-3-1-uid"
          target-page-1-title "Target Page Title 3 1"
          target-page-2-uid   "target-page-3-2-uid"
          target-page-2-title "Target Page Title 3 2"

          source-page-uid        "source-page-3-1-uid"
          source-page-title      "Source Page Title 3 1"
          testing-block-1-uid    "testing-block-3-1-uid"
          testing-block-1-string (str "[[" target-page-1-title "]]"
                                      "[[" target-page-2-title "]]")
          split-index            (count (str "[[" target-page-1-title "]]"))
          testing-block-2-uid    "testing-block-3-2-uid"
          setup-tx               [{:db/id          -1
                                   :node/title     target-page-1-title
                                   :block/uid      target-page-1-uid
                                   :block/children [{:db/id        -2
                                                     :block/uid    "irrelevant-1"
                                                     :block/string ""
                                                     :block/order  0}]}
                                  {:db/id          -3
                                   :node/title     target-page-2-title
                                   :block/uid      target-page-2-uid
                                   :block/children [{:db/id        -4
                                                     :block/uid    "irrelevant-2"
                                                     :block/string ""
                                                     :block/order  0}]}
                                  {:db/id          -5
                                   :node/title     source-page-title
                                   :block/uid      source-page-uid
                                   :block/children [{:db/id        -6
                                                     :block/uid    testing-block-1-uid
                                                     :block/string testing-block-1-string
                                                     :block/order  0}]}]]

      (d/transact @fixture/connection (common-db/linkmaker @@fixture/connection setup-tx))

      (let [{target-page-1-refs :block/refs} (common-db/get-page-document @@fixture/connection [:block/uid target-page-1-uid])
            {target-page-2-refs :block/refs} (common-db/get-page-document @@fixture/connection [:block/uid target-page-2-uid])
            split-block-event                (common-events/build-split-block-event -1
                                                                                    testing-block-1-uid
                                                                                    testing-block-1-string
                                                                                    split-index
                                                                                    testing-block-2-uid)
            split-block-tx                   (resolver/resolve-event-to-tx @@fixture/connection split-block-event)]
        ;; assert that target pages has no `:block/refs` to start with
        (t/is (= #{testing-block-1-uid} target-page-1-refs))
        (t/is (= #{testing-block-1-uid} target-page-2-refs))

        ;; apply split-block
        (d/transact @fixture/connection (common-db/linkmaker @@fixture/connection split-block-tx))
        (let [{target-page-1-refs :block/refs} (common-db/get-page-document @@fixture/connection [:block/uid target-page-1-uid])
              {target-page-2-refs :block/refs} (common-db/get-page-document @@fixture/connection [:block/uid target-page-2-uid])]
          ;; assert that we do have new ref
          (t/is (= #{testing-block-1-uid} target-page-1-refs))
          (t/is (= #{testing-block-2-uid} target-page-2-refs)))))))
