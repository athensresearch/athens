(ns athens.common-events.linkmaker-test
  (:require
    [athens.common-db              :as common-db]
    [athens.common-events          :as common-events]
    [athens.common-events.fixture  :as fixture]
    [athens.common-events.resolver :as resolver]
    [clojure.test                  :as t]
    [datahike.api                  :as d]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(t/deftest string->lookup-refs
  (t/testing "Doesn't find refs if none are there"
    (t/are [x] (empty? (common-db/string->lookup-refs x))
      ""
      "one"
      "[one]"
      "[ [one]]"
      "[[one"
      "one]]"))
  (t/testing "Finds page refs"
    (t/are [x y] (= (common-db/string->lookup-refs x) y)
      "[[one]]"            #{[:node/title "one"]}
      "[[one]] two"        #{[:node/title "one"]}
      "one [[two three]]"  #{[:node/title "two three"]}
      "#one"               #{[:node/title "one"]}
      "#[[one]]"           #{[:node/title "one"]}
      "one #[[two three]]" #{[:node/title "two three"]}
      "[[one]] #two [[three four]] #[[five six]]"
      #{[:node/title "one"] [:node/title "two"] [:node/title "three four"] [:node/title "five six"]}))
  (t/testing "Finds block refs"
    (t/are [x y] (= (common-db/string->lookup-refs x) y)
      "((one))"            #{[:block/uid "one"]}
      "one ((two))"        #{[:block/uid "two"]}
      "((one)) two"        #{[:block/uid "one"]}
      "((one)) ((two))"    #{[:block/uid "one"] [:block/uid "two"]}))
  (t/testing "Finds mixed page and block refs"
    (t/is (= (common-db/string->lookup-refs "((one)) [[two]] ((three)) #[[four]]")
             #{[:block/uid "one"] [:node/title "two"] [:block/uid "three"] [:node/title "four"]})))
  ;; broken, need improved parser
  #_(t/testing "Finds nested refs inside page refs"
    (t/are [x y] (= (common-db/string->lookup-refs x) y)
      "[[one [[two]]]]"     #{[:node/title "one [[two]]"] [:node/title "two"]}
      "#[[one #two three]]" #{[:node/title "one #two #three"] [:node/title "two"] [:node/title "three"]}
      "one [[#two #three]]" #{[:node/title "#two #three"] [:node/title "two"] [:node/title "three"]}
      "[[truly [[madly [[deeply [[nested]]]]]]]]"
      #{[:node/title "truly [[madly [[deeply [[nested]]]]]]"]
        [:node/title "madly [[deeply [[nested]]]]"]
        [:node/title "deeply [[nested]]"]
        [:node/title "nested"]})))

(t/deftest eid->lookup-ref
  (let [page      [{:db/id 101 :block/uid "page" :node/title "the page"}]
        block     [{:db/id 102 :block/uid "block" :block/string "the block"}]
        pageblock [{:db/id 103 :block/uid "pageblock" :node/title "the pageblock" :block/string "the pageblock"}]
        neither   [{:db/id 104 :create/time 1}]
        _         (d/transact @fixture/connection (concat page block pageblock neither))
        db        @@fixture/connection]
    (d/transact @fixture/connection (concat page block pageblock neither))
    (t/testing "Returns nil if the entity doesn't exist"
      (t/is (nil? (common-db/eid->lookup-ref db 200))))
    (t/testing "Returns nil if the entity doesn't have :node/title or :block/uid"
      (t/is (nil? (common-db/eid->lookup-ref db 104))))
    (t/testing "Returns :node/title lookup if present"
      (t/is (= [:node/title "the page"] (common-db/eid->lookup-ref db 101))))
    (t/testing "Returns :block/uid lookup if present"
      (t/is (= [:block/uid "block"] (common-db/eid->lookup-ref db 102))))
    (t/testing "Returns :node/title lookup if both :node/title and :block/uid are present"
      (t/is (= [:node/title "the pageblock"] (common-db/eid->lookup-ref db 103))))))

(t/deftest update-refs-tx
  (t/testing "Returns empty tx if the sets are empty")
  (t/testing "Returns empty tx if the nothing changes between the sets")
  (t/testing "Adds refs")
  (t/testing "Removes refs")
  (t/testing "Adds and removes refs in the same tx"))

(comment
  (string->lookup-refs)
  (t/test-vars [#'athens.common-events.linkmaker-test/eid->lookup-ref])
  )


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
      (d/transact @fixture/connection (common-db/linkmaker @@fixture/connection setup-tx))
      ;; assert that target page has no `:block/refs` to start with
      (let [target-page (common-db/get-page-document @@fixture/connection [:block/uid target-page-uid])
            add-link-tx [{:db/id        [:block/uid testing-block-uid]
                          :block/string (str "[[" target-page-title "]]")}]]
        (t/is (empty? (:block/_refs target-page)))

        (d/transact @fixture/connection (common-db/linkmaker @@fixture/connection add-link-tx))
        (let [{testing-block-eid :db/id
               block-refs        :block/refs} (common-db/get-block @@fixture/connection [:block/uid testing-block-uid])
              {target-page-eid :db/id
               page-refs       :block/_refs}  (common-db/get-page-document @@fixture/connection [:block/uid target-page-uid])]
          ;; assert that we do have new ref
          (t/is (seq page-refs))
          (t/is (seq block-refs))
          (t/is (= [{:db/id testing-block-eid}] page-refs))
          (t/is (= [{:db/id target-page-eid}] block-refs))))))

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

      (let [{testing-block-1-eid :db/id}      (common-db/get-block @@fixture/connection [:block/uid testing-block-1-uid])
            {target-page-1-refs :block/_refs} (common-db/get-page-document @@fixture/connection [:block/uid target-page-1-uid])
            {target-page-2-refs :block/_refs} (common-db/get-page-document @@fixture/connection [:block/uid target-page-2-uid])
            split-block-event                 (common-events/build-split-block-event -1
                                                                                     testing-block-1-uid
                                                                                     testing-block-1-string
                                                                                     split-index
                                                                                     testing-block-2-uid)
            split-block-tx                    (resolver/resolve-event-to-tx @@fixture/connection split-block-event)]
        ;; assert that target pages has no `:block/refs` to start with
        (t/is (= [{:db/id testing-block-1-eid}] target-page-1-refs))
        (t/is (= [{:db/id testing-block-1-eid}] target-page-2-refs))

        ;; apply split-block
        (d/transact @fixture/connection (common-db/linkmaker @@fixture/connection split-block-tx))
        (let [{testing-block-2-eid :db/id}     (common-db/get-block @@fixture/connection [:block/uid testing-block-2-uid])
              {target-page-1-refs :block/_refs} (common-db/get-page-document @@fixture/connection [:block/uid target-page-1-uid])
              {target-page-2-refs :block/_refs} (common-db/get-page-document @@fixture/connection [:block/uid target-page-2-uid])]
          ;; assert that we do have new ref
          (t/is (= [{:db/id testing-block-1-eid}] target-page-1-refs))
          (t/is (= [{:db/id testing-block-2-eid}] target-page-2-refs)))))))
