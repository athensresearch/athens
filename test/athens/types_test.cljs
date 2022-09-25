(ns athens.types-test
  (:require
    [athens.common-events.fixture :as fixture]
    [athens.db                    :as db]
    [athens.reactive              :as reactive]
    [athens.types.core            :as types]
    [athens.types.default.view    :as default]
    [clojure.test                 :as t]
    [re-frame.subs                :as rf-subs]))


(t/use-fixtures :each (partial fixture/integration-test-fixture []))


(defn subscription-stub
  ([query]
   (atom
     (if (= [:feature-flags] query)
       {:comments      true
        :notifications true
        :properties    false
        :cover-photo   true
        :tasks         true
        :reactions     true}
       (do
         (println "what are you subscribing to" (pr-str query))
         {}))))
  ([query _dynvec]
   (subscription-stub query)))


(t/deftest text-view-of-block-ref
  (let [page-1-uid  "page-1-uid"
        block-1-uid "block-1-1-uid"
        block-1     {:block/uid      block-1-uid
                     :block/string   "abc123"
                     :block/order    0
                     :block/open     true
                     :block/children []}
        block-2-uid "block-1-2-uid"
        block-2-str (str "((" block-1-uid "))")
        block-2     {:block/uid      block-2-uid
                     :block/string   block-2-str
                     :block/order    0
                     :block/open     true
                     :block/children []}
        setup-txs   [{:block/uid      page-1-uid
                      :node/title     "testing block-ref text representation"
                      :block/children [block-1
                                       block-2]}]]
    (fixture/transact-with-middleware setup-txs)
    (with-redefs [db/dsdb           @fixture/connection
                  rf-subs/subscribe subscription-stub]
      (reactive/init!)
      (t/is (= "((abc123))"
               (types/text-view (default/DefaultBlockRenderer. nil) block-2 {}))))))
