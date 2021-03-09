(ns athens.posh
  (:require
   [athens.dbrx :as dbrx]
   [datascript.core :as d]
   [posh.reagent :as posh]
   [taoensso.tufte :as tufte]))


(def version
  ;; :dbrx or :posh
  :dbrx)


(defn create-conn [schema]
  (tufte/p ::create-conn
     (case version
       :posh (let [conn (d/create-conn schema)]
               (posh/posh! conn)
               conn)
       :dbrx (dbrx/create-conn schema))))


(defn pull [conn selector eid]
  (tufte/p ::pull
     (case version
       :posh (posh/pull conn selector eid)
       :dbrx (dbrx/pull conn selector eid))))


(defn pull-many [conn selector eids]
  (tufte/p ::pull-many
     (case version
       :posh (posh/pull-many conn selector eids)
       :dbrx (dbrx/pull-many conn selector eids))))


(defn q [query conn & args]
  (tufte/p ::q
     (case version
       :posh (apply posh/q query conn args)
       :dbrx (apply dbrx/q query conn args))))


(defn transact! [conn tx-data]
  (tufte/p ::transact!
     (case version
       :posh (posh/transact! conn tx-data)
       :dbrx (dbrx/transact! conn tx-data))))
