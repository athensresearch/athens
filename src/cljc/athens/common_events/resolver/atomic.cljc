(ns athens.common-events.resolver.atomic
  (:require
    [athens.common-db :as common-db]
    [athens.common.utils :as utils]
    #?(:clj  [datahike.api :as d]
       :cljs [datascript.core :as d]))
  #?(:clj
     (:import
       clojure.lang.ExceptionInfo)))


(defmulti resolve-atomic-op-to-tx
  "Resolves ⚛️ Atomic Graph Ops to TXs."
  #(:op/type %2))


(defmethod resolve-atomic-op-to-tx :block/new
  [db {:op/keys [args]}]
  (let [{:keys [parent-uid
                block-uid
                block-order]} args
        now                   (utils/now-ts)
        new-block             {:db/id        -1
                               :block/uid    block-uid
                               :block/string ""
                               :block/order  block-order
                               :block/open   true
                               :create/time  now
                               :edit/time    now}
        reindex               (concat [new-block]
                                      (common-db/inc-after db [:block/uid parent-uid] (dec block-order)))
        tx-data               {:block/uid      parent-uid
                               :block/children reindex
                               :edit/time      now}]
    [tx-data]))


;; This is Atomic Graph Op, there is also composite version of it
(defmethod resolve-atomic-op-to-tx :block/save
  [db {:op/keys [args]}]
  (let [{:keys [block-uid new-string old-string]} args
        {stored-old-string :block/string}         (try
                                                    (d/pull db [:block/string] [:block/uid block-uid])
                                                    (catch #?(:clj ExceptionInfo
                                                              :cljs js/Error) _ex
                                                      {:block/string ""}))]
    (if (= stored-old-string old-string)
      (let [now           (utils/now-ts)
            updated-block {:db/id        [:block/uid block-uid]
                           :block/string new-string
                           :edit/time    now}]
        [updated-block])
      (throw
        (ex-info ":block/save operation started from a stale state."
                 {:op/args           args
                  :actual-old-string stored-old-string})))))


(defmethod resolve-atomic-op-to-tx :page/new
  [_db {:op/keys [args]}]
  (let [{:keys [page-uid
                block-uid
                title]} args
        now             (utils/now-ts)
        child           {:block/string ""
                         :block/uid    block-uid
                         :block/order  0
                         :block/open   true
                         :create/time  now
                         :edit/time    now}
        page            {:node/title     title
                         :block/uid      page-uid
                         :block/children [child]
                         :create/time    now
                         :edit/time      now}]
    [page]))


(defmethod resolve-atomic-op-to-tx :composite/consequence
  [db {:op/keys [consequences] :as _composite}]
  (into []
        (mapcat (fn [consequence]
                  (resolve-atomic-op-to-tx db consequence))
                consequences)))
