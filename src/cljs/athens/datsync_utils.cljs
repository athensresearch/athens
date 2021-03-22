(ns athens.datsync-utils
  (:require [athens.db :as db]
            [dat.sync.client]
            [datascript.core :as d]))


(defn apply-remote-tx!
  [tx-data]
  (let [remote-tx-meta {:dat.sync.prov/agent :dat.sync/remote}]
    (dat.sync.client/transact-with-middleware!
      db/dsdb dat.sync.client/wrap-remote-tx
      tx-data remote-tx-meta)))


(defn remote-tx
  "Copied from datsync, fixed minor bug in implementation"
  [db tx]
  (let [tx (->> (dat.sync.client/normalize-tx tx)
                (remove
                  (fn [[_ _ a]]
                    (#{:dat.sync.remote.db/id :db/id} a))))
        translated-tx (d/q '[:find ?op ?dat-e ?a ?dat-v
                             :in % $ [[?op ?e ?a ?v]]
                             :where [(get-else $ ?e :dat.sync.remote.db/id ?e) ?dat-e]
                             (remote-value-trans ?v ?a ?dat-v)]
                           '[[(attr-type-ident ?attr-ident ?type-ident)
                              [?attr :db/ident ?attr-ident]
                              [?attr :db/valueType ?vt]
                              [?vt :db/ident ?type-ident]]
                             [(is-ref ?attr-ident)
                              (attr-type-ident ?attr-ident :db.type/ref)]
                             [(remote-value-trans ?ds-v ?attr-ident ?remote-v)
                              (is-ref ?attr-ident)
                              [(> ?ds-v 0)]
                              [?ds-v :dat.sync.remote.db/id ?remote-v]]
                             [(remote-value-trans ?ds-v ?attr-ident ?remote-v)
                              (is-ref ?attr-ident)
                              [(< ?ds-v 0)]
                              [(ground ?ds-v) ?remote-v]]
                             ;; Shit... really want to be able to use (not ...) here...
                             [(remote-value-trans ?ds-v ?atrr-ident ?remote-v)
                              (attr-type-ident ?attr-ident ?vt-ident)
                              [(not= ?vt-ident :db.type/ref)]
                              [(ground ?ds-v) ?remote-v]]]
                           db tx)]
    (vec translated-tx)))