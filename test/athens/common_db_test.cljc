(ns athens.common-db-test
  (:require
    [athens.common-db :as common-db]
    [clojure.test     :as t]
    [datascript.core  :as d]))


(t/deftest get-internal-representation
  (let [gir (fn [tx-data eid]
              (-> (d/empty-db common-db/schema)
                  (d/db-with tx-data)
                  (common-db/get-internal-representation eid)))]

    (t/testing "rename :node/title to :page/title"
      (t/is (= (gir [{:node/title "a page"}]
                    [:node/title "a page"])
               {:page/title "a page"})))

    (t/testing "rename :block/open to :block/open?"
      (t/is (= (gir [{:block/uid  "1"
                      :block/open false}]
                    [:block/uid "1"])
               {:block/uid   "1"
                :block/open? false})))

    (t/testing "remove :block/order"
      (t/is (= (gir [{:block/uid   "1"
                      :block/order 0}]
                    [:block/uid "1"])
               {:block/uid "1"})))

    (t/testing "remove :block/open? if true"
      (t/is (= (gir [{:block/uid  "1"
                      :block/open true}]
                    [:block/uid "1"])
               {:block/uid "1"})))

    (t/testing "remove :block/uid if map has :page/title"
      (t/is (= (gir [{:node/title "a page"
                      :block/uid  "1"}]
                    [:node/title "a page"])
               {:page/title "a page"})))))


(t/deftest compat-position
  (let [tx-data [{:node/title     "a page"
                  :block/uid      "page-uid"
                  :block/children [{:block/uid   "1"
                                    :block/order 0}
                                   {:block/uid      "2"
                                    :block/order    1
                                    :block/children [{:block/uid   "2-1"
                                                      :block/order 0}
                                                     {:block/uid   "2-2"
                                                      :block/order 1}]}]}]
        db      (-> (d/empty-db common-db/schema)
                    (d/db-with tx-data))]

    (t/testing "coerce uid to title"
      (t/is (= (common-db/compat-position db {:block/uid "page-uid"
                                              :relation  :first})
               {:page/title "a page"
                :relation   :first})))

    (t/testing "coerce integer relation"
      (t/testing "first block"
        (t/is (= (common-db/compat-position db {:block/uid "2"
                                                :relation  0})
                 {:block/uid "2"
                  :relation  :first})))

      (t/testing "non-first block"
        (t/is (= (common-db/compat-position db {:block/uid "2"
                                                :relation  1})
                 {:block/uid "2-1"
                  :relation  :after}))))))
