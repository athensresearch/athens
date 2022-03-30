(ns athens.common-events.graph.ops-test
  (:require
   [athens.common-events.fixture :as fixture]
   [athens.common-events.graph.ops :as ops]
   [clojure.test :as t]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))

(t/deftest extract-new-titles
  (let [test-uid   "test-uid"
        setup-repr [{:page/title     "test-page"
                     :block/children [{:block/uid    test-uid
                                       :block/string "testing rocks"}]}]]
    (t/testing "from single page new operation"
      (fixture/setup! setup-repr)
      (let [new-title    "new-test-page"
            block-string (str "[[" new-title "]]")
            save-ops     (ops/build-block-save-op @@fixture/connection test-uid block-string)
            new-titles   (ops/ops->new-page-titles save-ops)]
        (t/is (= #{new-title} new-titles)))
      (fixture/teardown! setup-repr))

    (t/testing "from nested page new operation"
      (fixture/setup! setup-repr)
      (let [new-t-1      "abc"
            new-t-2      (str "[[" new-t-1 "]] 123")
            block-string (str "[[" new-t-2 "]]")
            save-ops     (ops/build-block-save-op @@fixture/connection test-uid block-string)
            new-titles   (ops/ops->new-page-titles save-ops)]
        (t/is (= #{new-t-1 new-t-2} new-titles)))
      (fixture/teardown! setup-repr))

    (t/testing "from multiple pages"
      (fixture/setup! setup-repr)
      (let [new-t-1      "abc"
            new-t-2      "123"
            block-string (str "[[" new-t-1 "]][[" new-t-2 "]]")
            save-ops     (ops/build-block-save-op @@fixture/connection test-uid block-string)
            new-titles   (ops/ops->new-page-titles save-ops)]
        (t/is (= #{new-t-1 new-t-2} new-titles)))
      (fixture/teardown! setup-repr))

    (t/testing "from multiple pages, no duplicates"
      (fixture/setup! setup-repr)
      (let [new-t-1      "abc"
            new-t-2      "123"
            block-string (str "[[" new-t-1 "]][[" new-t-2 "]][[" new-t-1 "]]")
            save-ops     (ops/build-block-save-op @@fixture/connection test-uid block-string)
            new-titles   (ops/ops->new-page-titles save-ops)]
        (t/is (= #{new-t-1 new-t-2} new-titles)))
      (fixture/teardown! setup-repr))))


(t/deftest compute-structural-diff
  (let [test-t-1   "abc"
        test-t-2   "def"
        test-uid-1 "test-uid-1"
        test-uid-2 "test-uid-2"
        test-uid-3 "test-uid-3"
        setup-repr [{:page/title     "test-page"
                     :block/children [{:block/uid    test-uid-1
                                       :block/string (str "[[" test-t-1 "]] ((" test-uid-2 "))")}]}
                    {:page/title     test-t-1
                     :block/children [{:block/uid    test-uid-2
                                       :block/string "more of a formality"}]}
                    {:page/title     test-t-2
                     :block/children [{:block/uid    test-uid-3
                                       :block/string "unimportant"}]}]]
    (t/testing "new page link & block ref, removed block ref"
      (fixture/setup! setup-repr)
      (let [block-string    (str "[[" test-t-1 "]] #[[" test-t-2 "]] ((" test-uid-3 "))")
            save-ops        (ops/build-block-save-op @@fixture/connection test-uid-1 block-string)
            [removed added] (ops/structural-diff @@fixture/connection save-ops)]
        (t/is (= #{[:block-ref test-uid-2]} removed))
        (t/is (= #{[:page-link "def"] [:block-ref "test-uid-3"]} added)))
      (fixture/teardown! setup-repr))))
