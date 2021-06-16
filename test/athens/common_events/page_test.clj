(ns athens.common-events.page-test
  (:require
    [athens.common-events          :as common-events]
    [athens.common-events.fixture  :as fixture]
    [athens.common-events.resolver :as resolver]
    [clojure.test                  :as test]
    [datahike.api                  :as d]))


(test/use-fixtures :each fixture/integration-test-fixture)


(test/deftest create-page
  (let [test-title        "test page title"
        test-uid          "test-page-uid-1"
        create-page-event (common-events/build-page-create-event -1 test-uid test-title)
        txs               (resolver/resolve-event-to-tx @@fixture/connection
                                                        create-page-event)]
    (d/transact @fixture/connection txs)
    (let [e-by-title (d/q '[:find ?e
                            :where [?e :node/title ?title]
                            :in $ ?title]
                          @@fixture/connection test-title)
          e-by-uid (d/q '[:find ?e
                          :where [?e :block/uid ?uid]
                          :in $ ?uid]
                        @@fixture/connection test-uid)]
      (test/is (seq e-by-title))
      (test/is (= e-by-title e-by-uid)))))
