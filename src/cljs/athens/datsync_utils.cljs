(ns athens.datsync-utils
  (:require
    [athens.db :as db]
    [dat.sync.client]
    [datascript.core :as d]))


(defn apply-remote-tx!
  [tx-data]
  (let [remote-tx-meta {:dat.sync.prov/agent :dat.sync/remote}]
    (dat.sync.client/transact-with-middleware!
      db/dsdb dat.sync.client/wrap-remote-tx
      tx-data remote-tx-meta)))


(defn is-ref?
  [attr]
  (= (get-in @db/dsdb [:schema attr :db/valueType])
     :db.type/ref))


(defn new-remote-idx
  [id]
  (str "new-" id))


(defn remote-tx
  "Inspired from datsync. datsync implementation is insufficient currently
   This checks for remote ids for datoms entity and refs(if attr is ref)
   Hacks in place due to limitations of datascript
   Leverages https://docs.datomic.com/on-prem/query/query.html"
  [db tx]
  (let [tx (->> (dat.sync.client/normalize-tx tx)
                (remove
                  (fn [[_ _ a]]
                    (#{:dat.sync.remote.db/id :db/id} a))))
        translated-tx (d/q '[:find ?op ?dat-e ?a ?dat-v
                             :in % ?is-ref ?c-is-ref ?new-id $ [[?op ?e ?a ?v]]
                             :where
                             [(?new-id ?e) ?new]
                             [(get-else $ ?e :dat.sync.remote.db/id ?new) ?dat-e]
                             (remote-value-trans ?is-ref ?c-is-ref ?new-id ?v ?a ?dat-v)]

                           '[[(remote-value-trans ?is-ref ?c-is-ref ?new-id ?ds-v ?attr-ident ?remote-v)
                              [(?is-ref ?attr-ident)]
                              [?ds-v :dat.sync.remote.db/id ?remote-v]]
                             [(remote-value-trans ?is-ref ?c-is-ref ?new-id ?ds-v ?attr-ident ?remote-v)
                              [(?is-ref ?attr-ident)]
                              (not [?ds-v :dat.sync.remote.db/id ?remote-v])
                              [(?new-id ?ds-v) ?remote-v]]
                             [(remote-value-trans ?is-ref ?c-is-ref ?new-id ?ds-v ?attr-ident ?remote-v)
                              [(?c-is-ref ?attr-ident)]
                              [(ground ?ds-v) ?remote-v]]]
                           ;; use fns' due to lack of resolve in cljs
                           ;; without which non directly ref fns' work
                           ;; interface difference in https://github.com/tonsky/datascript
                           is-ref?
                           (complement is-ref?)
                           ;; remote-id is made into a string to force datahike to resolve
                           ;; it as new
                           new-remote-idx
                           db tx)]
    (vec translated-tx)))
