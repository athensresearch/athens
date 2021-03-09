(ns athens.posh
  (:require
   [athens.dbrx :as dbrx]
   [datascript.core :as d]
   [posh.reagent :as posh]))


(def version
  ;; :dbrx or :posh
  :dbrx)


(defn create-conn [schema]
  (case version
    :posh (let [conn (d/create-conn schema)]
            (posh/posh! conn)
            conn)
    :dbrx (dbrx/create-conn schema)))


(defn pull [conn selector eid]
  (case version
    :posh (posh/pull conn selector eid)
    :dbrx (dbrx/pull conn selector eid)))


(defn pull-many [conn selector eids]
  (case version
    :posh (posh/pull-many conn selector eids)
    :dbrx (dbrx/pull-many conn selector eids)))


(defn q [query conn & args]
  (case version
    :posh (apply posh/q query conn args)
    :dbrx (apply dbrx/q query conn args)))


(defn transact! [conn tx-data]
  (case version
    :posh (posh/transact! conn tx-data)
    :dbrx (dbrx/transact! conn tx-data)))
