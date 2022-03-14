(ns athens.common-db-test
  (:require
    [athens.common-db :as common-db]
    [clojure.test     :as t]
    [datascript.core  :as d])
  #?(:clj
     (:import
       (clojure.lang
         ExceptionInfo))))


(t/deftest get-internal-representation
  (let [gir (fn [tx-data eid]
              (-> common-db/empty-db
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
        db      (-> common-db/empty-db
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


(t/deftest get-position
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
        db      (-> common-db/empty-db
                    (d/db-with tx-data))]

    (t/testing "page parent"
      (t/testing "first block"
        (t/is (= (common-db/get-position db "1")
                 {:page/title "a page"
                  :relation   :first})))

      (t/testing "non-first block"
        (t/is (= (common-db/get-position db "2")
                 {:block/uid "1"
                  :relation  :after}))))

    (t/testing "block parent"
      (t/testing "first block"
        (t/is (= (common-db/get-position db "2-1")
                 {:block/uid "2"
                  :relation  :first})))

      (t/testing "non-first block"
        (t/is (= (common-db/get-position db "2-2")
                 {:block/uid "2-1"
                  :relation  :after}))))))


(t/deftest position->uid+parent
  (let [tx-data [{:node/title     "a page"
                  :block/uid      "page-uid"
                  :block/children [{:block/uid   "1"
                                    :block/order 0}
                                   {:block/uid      "2"
                                    :block/order    1
                                    :block/children [{:block/uid   "2-1"
                                                      :block/order 0}
                                                     {:block/uid   "2-2"
                                                      :block/order 1}]}]}
                 {:block/uid "no-parent"}]
        db      (-> common-db/empty-db
                    (d/db-with tx-data))]

    (t/testing "page parent"
      (t/testing "first block"
        (t/is (= (common-db/position->uid+parent db {:page/title "a page"
                                                     :relation   :first})
                 ["page-uid" "page-uid"])))

      (t/testing "non-first block"
        (t/is (= (common-db/position->uid+parent db {:block/uid "1"
                                                     :relation  :after})
                 ["1" "page-uid"]))))

    (t/testing "block parent"
      (t/testing "first block"
        (t/is (= (common-db/position->uid+parent db {:block/uid "2"
                                                     :relation  :first})
                 ["2" "2"])))

      (t/testing "non-first block"
        (t/is (= (common-db/position->uid+parent db {:block/uid "2-1"
                                                     :relation  :after})
                 ["2-1" "2"]))))

    (t/testing "throws"
      (t/testing "missing title"
        (t/is (thrown-with-msg? #?(:cljs js/Error
                                   :clj ExceptionInfo)
                                #"Location title does not exist"
                (common-db/position->uid+parent db {:page/title "missing-title"
                                                    :relation   :first}))))
      (t/testing "missing uid"
        (t/is (thrown-with-msg? #?(:cljs js/Error
                                   :clj ExceptionInfo)
                                #"Location uid does not exist"
                (common-db/position->uid+parent db {:block/uid "missing-uid"

                                                    :relation :first}))))
      (t/testing "uid instead of page"
        (t/is (thrown-with-msg? #?(:cljs js/Error
                                   :clj ExceptionInfo)
                                #"Location uid is a page"
                (common-db/position->uid+parent db {:block/uid "page-uid"
                                                    :relation  :first}))))
      (t/testing "block has no parent"
        (t/is (thrown-with-msg? #?(:cljs js/Error
                                   :clj ExceptionInfo)
                                #"Ref block does not have parent"
                (common-db/position->uid+parent db {:block/uid "no-parent"
                                                    :relation  :after})))))))


(t/deftest get-shortcut-neighbors
  (let [db-0 (-> common-db/empty-db
                 (d/db-with [{:node/title "page 1"
                              :block/uid  "page-uid-1"}]))
        db-1 (-> common-db/empty-db
                 (d/db-with [{:node/title   "page 1"
                              :block/uid    "page-uid-1"
                              :page/sidebar 0}]))
        db-3 (-> common-db/empty-db
                 (d/db-with [{:node/title   "page 1"
                              :block/uid    "page-uid-1"
                              :page/sidebar 0}
                             {:node/title   "page 2"
                              :block/uid    "page-uid-2"
                              :page/sidebar 1}
                             {:node/title   "page 3"
                              :block/uid    "page-uid-3"
                              :page/sidebar 2}]))]

    (t/testing "no shortcuts, no neighbhors"
      (t/is (= (common-db/get-shortcut-neighbors db-0 "page 1")
               {:before nil
                :after  nil})))

    (t/testing "only one shortcut, no neighbhors"
      (t/is (= (common-db/get-shortcut-neighbors db-1 "page 1")
               {:before nil
                :after  nil})))

    (t/testing "3 shortcuts"
      (t/is (= (common-db/get-shortcut-neighbors db-3 "page 1")
               {:before nil
                :after  "page 2"}) "Only neighbor after.")

      (t/is (= (common-db/get-shortcut-neighbors db-3 "page 3")
               {:before "page 2"
                :after nil}) "Only neighbor before.")

      (t/is (= (common-db/get-shortcut-neighbors db-3 "page 2")
               {:before "page 1"
                :after "page 3"}) "Neighbor before and after."))))

