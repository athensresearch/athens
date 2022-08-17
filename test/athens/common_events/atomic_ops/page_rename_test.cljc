(ns athens.common-events.atomic-ops.page-rename-test
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.fixture         :as fixture]
    [athens.common-events.graph.atomic    :as atomic-graph-ops]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [clojure.test                         :as t]
    [datascript.core                      :as d])
  #?(:clj
     (:import
       (clojure.lang
         ExceptionInfo))))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(t/deftest page-rename-atomic-test
  (t/testing "simple case, no string representations need updating"
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
      (fixture/transact-with-middleware setup-txs)
      (let [uid-by-title    (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)
            rename-page-txs (atomic-resolver/resolve-to-tx @@fixture/connection
                                                           (graph-ops/build-page-rename-op @@fixture/connection test-title-from test-title-to))]

        (t/is (= test-page-uid uid-by-title))
        (d/transact! @fixture/connection rename-page-txs)
        (let [uid-by-title (common-db/v-by-ea @@fixture/connection [:node/title test-title-to] :block/uid)]
          (t/is (= test-page-uid uid-by-title))))))

  (t/testing "complex case, where we need to update string representations as well"
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
      (fixture/transact-with-middleware setup-txs)
      (let [uid-by-title    (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)
            rename-page-txs (atomic-resolver/resolve-to-tx @@fixture/connection
                                                           (graph-ops/build-page-rename-op @@fixture/connection test-title-from test-title-to))]
        (t/is (= test-page-uid uid-by-title))
        (d/transact! @fixture/connection rename-page-txs)
        (let [uid-by-title (common-db/v-by-ea @@fixture/connection [:node/title test-title-to] :block/uid)
              block-string (common-db/v-by-ea @@fixture/connection [:block/uid test-block-uid] :block/string)]
          (t/is (nil? (common-db/v-by-ea @@fixture/connection [:node/title test-title-from] :block/uid)))
          (t/is (= test-page-uid uid-by-title))
          (t/is (= test-string-to block-string)))))))


(t/deftest page-rename-resulting-in-page-creation
  (let [title-from   "abc"
        nested-title "def"
        title-to     (str "[[" nested-title "]] " title-from)
        setup-repr   [{:page/title     title-from
                       :block/children []}]]
    (fixture/setup! setup-repr)
    (t/is (empty? (common-db/get-page-document @@fixture/connection [:node/title nested-title])))
    (t/is (seq (common-db/get-page-document @@fixture/connection [:node/title title-from])))
    (let [rename-op (graph-ops/build-page-rename-op @@fixture/connection title-from title-to)]
      (fixture/op-resolve-transact! rename-op)
      (t/is (empty? (common-db/get-page-document @@fixture/connection [:node/title title-from])))
      (t/is (seq (common-db/get-page-document @@fixture/connection [:node/title title-to])))
      (t/is (seq (common-db/get-page-document @@fixture/connection [:node/title nested-title]))))
    (fixture/teardown! setup-repr)))


(t/deftest page-rename-resulting-in-page-creation-and-link-renames
  (let [title-from   "abc"
        nested-title "def"
        title-to     (str "[[" nested-title "]] " title-from)
        block-uid    "random-block-uid"
        block-start  (str "[[" title-from "]]")
        block-end    (str "[[" title-to "]]")
        setup-repr   [{:page/title     title-from
                       :block/children [{:block/uid    block-uid
                                         :block/string block-start}]}]]
    (fixture/setup! setup-repr)
    (t/is (empty? (common-db/get-page-document @@fixture/connection [:node/title nested-title])))
    (t/is (= block-start (common-db/get-block-string @@fixture/connection block-uid)))
    (let [rename-op (graph-ops/build-page-rename-op @@fixture/connection title-from title-to)]
      (fixture/op-resolve-transact! rename-op)
      (t/is (seq (common-db/get-page-document @@fixture/connection [:node/title nested-title])))
      (t/is (seq (common-db/get-page-document @@fixture/connection [:node/title title-to])))
      (t/is (= block-end (common-db/get-block-string @@fixture/connection block-uid))))
    (fixture/teardown! setup-repr)))


(t/deftest page-rename-not-allowing-for-regex-injection
  (t/testing "nested page name"
    (let [title-from    "abc"
          title-nested1 "def"
          title-nested2 "ghi"
          title-full    (str "[[" title-nested1 "]] " title-from)
          title-to      (str "[[" title-nested2 "]] " title-from)
          setup-repr    [{:page/title     title-nested1
                          :block/children []}
                         {:page/title     title-nested2
                          :block/children []}
                         {:page/title     title-full
                          :block/children []}]]
      (fixture/setup! setup-repr)
      (t/is (seq (common-db/get-page-document @@fixture/connection [:node/title title-full])))
      (t/is (empty? (common-db/get-page-document @@fixture/connection [:node/title title-to])))
      (let [rename-op (graph-ops/build-page-rename-op @@fixture/connection title-full title-to)]
        (fixture/op-resolve-transact! rename-op)
        (t/is (empty? (common-db/get-page-document @@fixture/connection [:node/title title-full])))
        (t/is (seq (common-db/get-page-document @@fixture/connection [:node/title title-to]))))
      (fixture/teardown! setup-repr)))

  (t/testing "nested parentheses in page name"
    (let [title-from "(abc) def"
          title-to   "(123) def"
          setup-repr [{:page/title     title-from
                       :block/children []}]]
      (fixture/setup! setup-repr)
      (t/is (seq (common-db/get-page-document @@fixture/connection [:node/title title-from])))
      (t/is (empty? (common-db/get-page-document @@fixture/connection [:node/title title-to])))
      (let [rename-op (graph-ops/build-page-rename-op @@fixture/connection title-from title-to)]
        (fixture/op-resolve-transact! rename-op)
        (t/is (empty? (common-db/get-page-document @@fixture/connection [:node/title title-from])))
        (t/is (seq (common-db/get-page-document @@fixture/connection [:node/title title-to])))
        (fixture/teardown! setup-repr)))))


(t/deftest page-rename-undo-test
  (t/testing "just rename it back already"
    (let [from-title        "test-rename-undo-title-from"
          to-title          "test-rename-undo-title-to"
          test-uid          "block-1-uid"
          setup-repr        [{:page/title     from-title
                              :block/children [{:block/uid    test-uid
                                                :block/string ""}]}]
          get-page-by-title #(common-db/get-page-document @@fixture/connection [:node/title %])]
      (fixture/setup! setup-repr)
      (t/is (seq (get-page-by-title from-title)))
      (t/is (nil? (get-page-by-title to-title)))
      (let [[rename-db rename-event] (fixture/op-resolve-transact! (graph-ops/build-page-rename-op @@fixture/connection from-title to-title))]
        (t/is (nil? (get-page-by-title from-title)))
        (t/is (seq (get-page-by-title to-title)))
        (fixture/undo! rename-db rename-event)
        (t/is (seq (get-page-by-title from-title)))
        (t/is (nil? (get-page-by-title to-title)))))))


(t/deftest page-rename-undo-test-2
  (t/testing "Rename and change refs"
    (let [from-title        "test-rename-undo-title-from"
          to-title          "test-rename-undo-title-to"
          test-uid          "block-1-uid"
          start-str         (str "[[" from-title "]]")
          end-str           (str "[[" to-title "]]")
          setup-repr        [{:page/title     from-title
                              :block/children [{:block/uid    "doesn't really matter"
                                                :block/string ""}]}
                             {:page/title     "this page holds block with ref to renamed page"
                              :block/children [{:block/uid    test-uid
                                                :block/string start-str}]}]
          get-page-by-title #(common-db/get-page-document @@fixture/connection [:node/title %])
          get-str           #(common-db/v-by-ea @@fixture/connection [:block/uid test-uid] :block/string)]
      (fixture/setup! setup-repr)
      (t/is (seq (get-page-by-title from-title)))
      (t/is (nil? (get-page-by-title to-title)))
      (t/is (= start-str (get-str)))
      (let [[rename-db rename-event] (fixture/op-resolve-transact! (graph-ops/build-page-rename-op @@fixture/connection from-title to-title))]
        (t/is (nil? (get-page-by-title from-title)))
        (t/is (seq (get-page-by-title to-title)))
        (t/is (= end-str (get-str)))
        (fixture/undo! rename-db rename-event)
        (t/is (seq (get-page-by-title from-title)))
        (t/is (nil? (get-page-by-title to-title)))
        (t/is (= start-str (get-str)))))))


(t/deftest page-rename-undo-redo-test
  (t/testing "Rename and change refs"
    (let [from-title        "test-rename-undo-title-from"
          to-title          "test-rename-undo-title-to"
          test-uid          "block-1-uid"
          start-str         (str "[[" from-title "]]")
          end-str           (str "[[" to-title "]]")
          setup-repr        [{:page/title     from-title
                              :block/children [{:block/uid    "doesn't really matter"
                                                :block/string ""}]}
                             {:page/title     "this page holds block with ref to renamed page"
                              :block/children [{:block/uid    test-uid
                                                :block/string start-str}]}]
          get-page-by-title #(common-db/get-page-document @@fixture/connection [:node/title %])
          get-str           #(common-db/v-by-ea @@fixture/connection [:block/uid test-uid] :block/string)]
      (fixture/setup! setup-repr)
      (t/is (seq (get-page-by-title from-title)))
      (t/is (nil? (get-page-by-title to-title)))
      (t/is (= start-str (get-str)))
      (let [[rename-db rename-event] (fixture/op-resolve-transact! (graph-ops/build-page-rename-op @@fixture/connection from-title to-title))]
        (t/is (nil? (get-page-by-title from-title)))
        (t/is (seq (get-page-by-title to-title)))
        (t/is (= end-str (get-str)))
        (let [[undo-db undo-event] (fixture/undo! rename-db rename-event)]
          (t/is (seq (get-page-by-title from-title)))
          (t/is (nil? (get-page-by-title to-title)))
          (t/is (= start-str (get-str)))
          ;; redo
          (fixture/undo! undo-db undo-event)
          (t/is (nil? (get-page-by-title from-title)))
          (t/is (seq (get-page-by-title to-title)))
          (t/is (= end-str (get-str))))))))


(t/deftest page-rename-also-hashtag-test
  (t/testing "when naked hashtag stays naked"
    (let [title-from "abc"
          title-to   "def"
          b1-uid     "b1"
          b2-uid     "b2"
          b3-uid     "b3"
          b1-from    (str "#" title-from)
          b2-from    (str "[[" title-from "]]")
          b3-from    (str "#[[" title-from "]]")
          b1-to      (str "#" title-to)
          b2-to      (str "[[" title-to "]]")
          b3-to      (str "#[[" title-to "]]")
          setup-repr [{:page/title     title-from
                       :block/children [{:block/uid    b1-uid
                                         :block/string b1-from}
                                        {:block/uid    b2-uid
                                         :block/string b2-from}
                                        {:block/uid    b3-uid
                                         :block/string b3-from}]}]]
      (fixture/setup! setup-repr)
      (t/is (= b1-from (common-db/get-block-string @@fixture/connection b1-uid)))
      (t/is (= b2-from (common-db/get-block-string @@fixture/connection b2-uid)))
      (t/is (= b3-from (common-db/get-block-string @@fixture/connection b3-uid)))
      (fixture/op-resolve-transact! (graph-ops/build-page-rename-op @@fixture/connection title-from title-to))
      (t/is (= b1-to (common-db/get-block-string @@fixture/connection b1-uid)))
      (t/is (= b2-to (common-db/get-block-string @@fixture/connection b2-uid)))
      (t/is (= b3-to (common-db/get-block-string @@fixture/connection b3-uid)))
      (fixture/teardown! setup-repr)))

  (t/testing "when naked hashtag is wrapped"
    (let [title-from "abc"
          title-to   "def ghi"
          b1-uid     "b1"
          b2-uid     "b2"
          b3-uid     "b3"
          b1-from    (str "#" title-from)
          b2-from    (str "[[" title-from "]]")
          b3-from    (str "#[[" title-from "]]")
          b1-to      (str "#[[" title-to "]]")
          b2-to      (str "[[" title-to "]]")
          b3-to      (str "#[[" title-to "]]")
          setup-repr [{:page/title     title-from
                       :block/children [{:block/uid    b1-uid
                                         :block/string b1-from}
                                        {:block/uid    b2-uid
                                         :block/string b2-from}
                                        {:block/uid    b3-uid
                                         :block/string b3-from}]}]]
      (fixture/setup! setup-repr)
      (t/is (= b1-from (common-db/get-block-string @@fixture/connection b1-uid)))
      (t/is (= b2-from (common-db/get-block-string @@fixture/connection b2-uid)))
      (t/is (= b3-from (common-db/get-block-string @@fixture/connection b3-uid)))
      (fixture/op-resolve-transact! (graph-ops/build-page-rename-op @@fixture/connection title-from title-to))
      (t/is (= b1-to (common-db/get-block-string @@fixture/connection b1-uid)))
      (t/is (= b2-to (common-db/get-block-string @@fixture/connection b2-uid)))
      (t/is (= b3-to (common-db/get-block-string @@fixture/connection b3-uid)))
      (fixture/teardown! setup-repr))))


(t/deftest rename-prop
  (fixture/setup! [{:page/title "title"
                    :block/properties
                    {"key" #:block{:uid    "uid"
                                   :string ""}}}])
  (fixture/op-resolve-transact! (graph-ops/build-page-rename-op @@fixture/connection "key" "another-key"))
  (fixture/is #{{:page/title "another-key"}
                {:page/title "title"
                 :block/properties
                 {"another-key" #:block{:uid    "uid"
                                        :string ""}}}}))
