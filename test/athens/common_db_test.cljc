(ns athens.common-db-test
  (:require
    [athens.common-db :as common-db]
    [athens.common-events.bfs :as bfs]
    [clojure.test :as t]
    [clojure.walk :as walk]
    [datascript.core :as d])
  #?(:clj
     (:import
       (clojure.lang
         ExceptionInfo))))


(t/deftest version
  (let [conn (common-db/create-conn)]
    (t/is (= (common-db/db-versions @conn) #{0 1 2 3}))
    (t/is (= (common-db/version conn) 3))))


(t/deftest migrate-conn
  (let [conn (-> (common-db/create-conn)
                 (common-db/migrate-conn!))]
    (t/is (= (d/schema @conn)
             (merge common-db/v1-bootstrap-schema
                    common-db/v1-schema
                    common-db/v2-schema
                    common-db/v3-schema)))
    (t/is (= (common-db/db-versions @conn)
             #{0 1 2 3}))))


(t/deftest reset-conn
  (let [old-conn (common-db/migrate-conn! (d/create-conn) :up-to 1)
        new-conn (common-db/create-conn)
        block    {:block/uid    "uid"
                  :block/string "string"}]
    (t/is (= (d/schema @old-conn)
             (merge common-db/v1-bootstrap-schema common-db/v1-schema)))
    (t/is (= (common-db/db-versions @old-conn)
             #{0 1}))
    (d/transact! old-conn [block])
    (t/is (= (d/pull @old-conn '[:block/uid :block/string] [:block/uid "uid"]) block))
    (common-db/reset-conn! new-conn @old-conn)
    (t/is (= (d/schema @new-conn)
             (merge common-db/v1-bootstrap-schema
                    common-db/v1-schema
                    common-db/v2-schema
                    common-db/v3-schema)))
    (t/is (= (common-db/db-versions @new-conn)
             #{0 1 2 3}))
    (t/is (= (d/pull @old-conn '[:block/uid :block/string] [:block/uid "uid"]) block))))


(t/deftest migrate-v2-time-to-v3-time
  (let [conn       (common-db/migrate-conn! (d/create-conn) :up-to 2)
        ;; Datoms from part of the welcome page.
        v2-datoms  [[:db/add 3 :block/children 4]
                    [:db/add 3 :block/children 5]
                    [:db/add 3 :block/uid "bde244379"]
                    [:db/add 3 :create/time 1656950111341]
                    [:db/add 3 :edit/time 1656950111350]
                    [:db/add 3 :node/title "Welcome"]
                    [:db/add 3 :page/sidebar 0]
                    [:db/add 4 :block/open true]
                    [:db/add 4 :block/order 0]
                    [:db/add 4 :block/string "Welcome to Athens, Open-Source Networked Thought!"]
                    [:db/add 4 :block/uid "ee770c334"]
                    [:db/add 4 :create/time 1656950111345]
                    [:db/add 4 :edit/time 1656950111356]
                    [:db/add 5 :block/children 6]
                    [:db/add 5 :block/open true]
                    [:db/add 5 :block/order 1]
                    [:db/add 5 :block/string "You can open and close blocks that have children."]
                    [:db/add 5 :block/uid "6aecd4172"]
                    [:db/add 5 :create/time 1656950111350]
                    [:db/add 5 :edit/time 1656950111358]
                    [:db/add 6 :block/open true]
                    [:db/add 6 :block/order 0]
                    [:db/add 6 :block/string "![](https://athens-assets-1.s3.us-east-2.amazonaws.com/welcome.gif)"]
                    [:db/add 6 :block/uid "5f82a48ef"]
                    [:db/add 6 :create/time 1656950111353]
                    [:db/add 6 :edit/time 1656950111360]]
        pull-id    [:node/title "Welcome"]
        v2-pattern '[:create/time :edit/time {:block/children ...}]
        v3-pattern '[:time/ts {:block/children ...} {:block/create ...} {:block/edits ...} {:event/time ...}]]
    (d/transact! conn v2-datoms)
    ;; Starts with v2 times.
    (t/is (= {:block/children
              [{:create/time 1656950111345
                :edit/time   1656950111356}
               {:block/children
                [{:create/time 1656950111353
                  :edit/time   1656950111360}]
                :create/time 1656950111350
                :edit/time   1656950111358}]
              :create/time 1656950111341
              :edit/time   1656950111350}
             (d/pull @conn v2-pattern pull-id)))
    ;; After migration, v2 times are migrated to v3.
    (common-db/migrate-conn! conn :up-to 3)
    (t/is (= nil (d/pull @conn v2-pattern pull-id)))
    (t/is (= {:block/children
              [{:block/create {:event/time {:time/ts 1656950111345}}
                :block/edits  [{:event/time {:time/ts 1656950111356}}]}
               {:block/children
                [{:block/create {:event/time {:time/ts 1656950111353}}
                  :block/edits  [{:event/time {:time/ts 1656950111360}}]}]
                :block/create {:event/time {:time/ts 1656950111350}}
                :block/edits  [{:event/time {:time/ts 1656950111358}}]}]
              :block/create {:event/time {:time/ts 1656950111341}}
              :block/edits  [{:event/time {:time/ts 1656950111350}}]}
             (d/pull @conn v3-pattern pull-id)))))


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


(t/deftest get-block-property-document
  (let [db (bfs/db-from-repr [{:page/title "title"
                               :block/properties
                               {"key" #:block{:uid    "uid1"
                                              :string "one"
                                              :children
                                              [#:block{:uid    "uid2"
                                                       :string "two"}]
                                              :properties
                                              {"another-key"
                                               #:block{:uid    "uid3"
                                                       :string "three"}}}}}])
        remove-create-edits (fn [x]
                              (walk/prewalk (fn [node]
                                              (if (map? node)
                                                (dissoc node :block/create :block/edits)
                                                node)) x))]

    (t/is (= (remove-create-edits (common-db/get-block-property-document db [:node/title "title"]))
             {"key"
              {:block/children
               [{:block/open   true,
                 :block/order  0,
                 :block/string "two",
                 :block/uid    "uid2",
                 :db/id        9}],
               :block/key    #:node{:title "key"},
               :block/open   true,
               :block/string "one",
               :block/uid    "uid1",
               :db/id        8,
               :block/_property-of
               [{:block/key    #:node{:title "another-key"},
                 :block/open   true,
                 :block/string "three",
                 :block/uid    "uid3",
                 :db/id        11}],
               :block/properties
               {"another-key"
                {:block/key    #:node{:title "another-key"},
                 :block/open   true,
                 :block/string "three",
                 :block/uid    "uid3",
                 :db/id        11}}}}))))
