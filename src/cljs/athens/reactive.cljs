(ns athens.reactive
  (:require
    [athens.common.sentry :refer-macros [defntrace]]
    [athens.common.utils :as utils]
    [athens.db :as db]
    [datascript.core :as d]
    [posh.reagent :as p]))


(defn watch!
  "Watch the global datascript database."
  []
  (p/posh! db/dsdb))


(defn unwatch!
  "Unwatch the global datascript database."
  []
  ;; Watching a new conn will remove all old watchers.
  ;; You can verify this by calling print-posh-state.
  (p/posh! (d/create-conn)))


(defn watch-state
  []
  (try
    (-> (p/get-posh-atom db/dsdb)
        deref
        ;; all keys
        ;; (:schema :filters :return :retrieve :txs :cache :dbs
        ;; :schemas :ratoms :changed :graph :dcfg :reactions :conns)

        ;; These keys don't matter much.
        (dissoc :schema :filters :dbs :conns :schemas :dcfg))
    ;; get-posh-atom will throw if not watching.
    (catch :default _)))


(defn ratoms
  "Returns current reactive atoms."
  []
  (-> (watch-state) :ratoms))


;; Ratoms

(defn get-reactive-linked-references
  "For node and block page references UI."
  [eid]
  (->> @(p/pull db/dsdb '[:block/_refs] eid)
       :block/_refs
       (mapv :db/id)
       db/merge-parents-and-block
       db/group-by-parent
       (sort-by #(-> % first second))
       (map #(vector (ffirst %) (second %)))
       vec
       rseq))


(def node-document-pull-vector
  '[:db/id :block/uid :node/title :page/sidebar
    {:block/children [:block/uid :block/order]}])


(defntrace get-reactive-node-document
  [id]
  (->> @(p/pull db/dsdb node-document-pull-vector id)
       db/sort-block-children))


(def block-document-pull-vector
  '[:db/id :block/uid :block/string :block/open :block/_refs
    {:block/children [:block/uid :block/order]}])


(defntrace get-reactive-block-document
  [id]
  (->> @(p/pull db/dsdb block-document-pull-vector id)
       db/sort-block-children))


(defntrace get-reactive-parents-recursively
  [id]
  (->> @(p/pull db/dsdb '[:db/id :node/title :block/uid :block/string {:block/_children ...}] id)
       db/shape-parent-query))


(defntrace get-reactive-shortcuts
  []
  (->> @(p/q '[:find ?order ?title
               :where
               [?e :page/sidebar ?order]
               [?e :node/title ?title]] db/dsdb)
       seq
       (sort-by first)))


(defntrace get-reactive-block-or-page-by-uid
  [uid]
  @(p/pull db/dsdb '[:node/title :block/string :db/id] [:block/uid uid]))


(comment
  ;; Print what ratoms are active.
  (-> (ratoms) utils/spy)
  ;;
  )
