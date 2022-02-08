(ns athens.reactive
  (:require
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


(comment
  (-> (ratoms) utils/spy)
  ;;
  )
