(ns athens.reactive
  "Functions that will reactively update Reagent components when their DataScript data changes.
  Also contains functions to start, stop, and inspect the reactive watchers.
  Functions in this namespace should be used very deliberately to avoid performance overheads.
  No other namespace should import posh.reagent."
  (:require
    [athens.common-db :as common-db]
    [athens.common.sentry :refer-macros [defntrace]]
    [athens.common.utils :as utils]
    [athens.dates :as dates]
    [athens.db :as db]
    [datascript.core :as d]
    [posh.reagent :as p]))


(defn watch!
  "Watch the global datascript database."
  []
  (p/posh! db/dsdb))


(defn unwatch!
  "Unwatch the global datascript database.
  While unwatched, all get-reactive-* fns will return non-reactive pulls and queries."
  []
  ;; Watching a new conn will remove all old watchers.
  ;; You can verify this by printing watch-state.
  (p/posh! (d/create-conn)))


(defn init!
  "Initialize the reactive watchers.
  Must be called before watch-state or any of the get-reactive-* fns, or these will throw."
  []
  ;; Add a remove the watcher to the global datascript db once.
  ;; This will leave the posh datom connected to it, even after unwatch.
  (watch!)
  (unwatch!))


(defn watch-state
  []
  (-> (p/get-posh-atom db/dsdb)
      deref
      ;; all keys
      ;; (:schema :filters :return :retrieve :txs :cache :dbs
      ;; :schemas :ratoms :changed :graph :dcfg :reactions :conns)

      ;; These keys don't matter much.
      (dissoc :schema :filters :dbs :conns :schemas :dcfg)))


(defn ratoms
  "Returns current reactive atoms."
  []
  (-> (watch-state) :ratoms))


;; Initialization

(init!)


;; Ratoms
;; NB: p/pull will not throw on missing ident, and will update the ratom when it exists.



(defn get-reactive-linked-references
  "For node and block page references UI."
  [eid]
  (->> @(p/pull db/dsdb '[:block/_refs] eid)
       :block/_refs
       (mapv :db/id)
       db/eids->groups))


(defn get-reactive-linked-properties
  "For node page properties references UI."
  [eid]
  (->> @(p/pull db/dsdb '[:block/_key] eid)
       :block/_key
       (mapv :db/id)
       db/eids->groups))


(defn get-reactive-edited-on-day-blocks
  "For node page edited on references UI."
  [title]
  (let [date     (dates/title-to-date title)
        day      (dates/date-to-day date)
        next-day (dates/get-day date -1)
        start    (-> day :inst inst-ms)
        end      (-> next-day :inst inst-ms)]
    (->> @(p/q '[:find ?b
                 :in $ ?start ?end
                 :where
                 [?t :time/ts ?ts]
                 [(>= ?ts ?start)]
                 [(< ?ts ?end)]
                 [?e :event/time ?t]
                 [?b :block/edits ?e]
                 [?b :block/string _]]
               db/dsdb start end)
         (mapv first)
         db/eids->groups)))


(def recursive-properties-document-pull-vector
  '[{:block/_property-of [:block/uid :block/string :block/order :block/refs
                          {:block/key [:node/title]}
                          {:block/edits [{:event/time [:time/ts]
                                          :event/auth [:presence/id]}]}
                          {:block/children ...}
                          {:block/_property-of ...}]}])


(def node-document-pull-vector
  (vec (concat '[:db/id :block/uid :node/title :page/sidebar
                 {:block/children [:block/uid :block/order]}]
               recursive-properties-document-pull-vector)))


(defntrace get-reactive-node-document
  [id]
  (->> @(p/pull db/dsdb node-document-pull-vector id)
       common-db/sort-block-children
       common-db/add-property-map))


(def block-document-pull-vector
  (vec (concat '[:db/id :block/uid :block/string :block/open :block/_refs
                 {:block/key [:node/title]}
                 {:block/children [:block/uid :block/order]}
                 {:block/create [{:event/time [:time/ts]}
                                 {:event/auth [:presence/id]}]}
                 {:block/edits [{:event/time [:time/ts]}]}]
               recursive-properties-document-pull-vector)))


(defntrace get-reactive-block-document
  [id]
  (->> @(p/pull db/dsdb block-document-pull-vector id)
       common-db/sort-block-children
       common-db/add-property-map))


(defntrace get-reactive-right-sidebar-item
  [id]
  (->> @(p/pull db/dsdb '[:db/id :block/uid :block/string :node/title] id)))


(defntrace get-reactive-parents-recursively
  [id]
  (->> @(p/pull db/dsdb '[:db/id :node/title :block/uid :block/string
                          {:block/edits [{:event/time [:time/ts]}]}
                          {:block/property-of ...}
                          {:block/_children ...}]
                id)
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


(defntrace reactive-get-entity-type
  "Reactive version of athens.common-db/get-entity-type."
  [eid]
  (->> @(p/pull db/dsdb '[{:block/_property-of [:block/string {:block/key [:node/title]}]}] eid)
       :block/_property-of
       (some (fn [e]
               (when (-> e :block/key :node/title (= ":entity/type"))
                 (:block/string e))))))


(defn get-reactive-instances-of-key-value
  "Find all blocks that have key-value matching where
  key is a string and value is a string, then find that property block's parent."
  [k v]
  (->> @(p/q '[:find [?parent ...]
               :in $ ?key ?value
               :where
               [?eid :block/key ?k]
               [?k :node/title ?key]
               [?eid :block/string ?value]
               [?eid :block/property-of ?parent]]
             athens.db/dsdb k v)
       (mapv get-reactive-block-document)))


(comment
  ;; Print what ratoms are active.
  (-> (ratoms) utils/spy))


;;

