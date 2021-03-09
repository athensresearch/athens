(ns athens.dbrx
  (:require
   [datascript.core :as d]
   [reagent.core :as reagent]
   [reagent.ratom :as ratom]))


(defn create-conn [schema]
  (reagent/atom
   (d/empty-db schema)
   :meta {:listeners (atom {})}))


(defn pull [conn selector eid]
  (ratom/make-reaction
    #(d/pull @conn selector eid)))


(defn pull-many [conn selector eids]
  (ratom/make-reaction
    #(d/pull-many @conn selector eids)))


(defn q [query conn & args]
  (ratom/make-reaction
    #(apply d/q query @conn args)))


(defn transact! [conn tx-data]
  (d/transact! conn tx-data))
