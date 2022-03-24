(ns athens.self-hosted.event-log-migrations
  "Contains schema and data migrations to the event log."
  (:require
    [athens.self-hosted.migrate :as migrate]
    [fluree.db.api :as fdb]))


;; Migration #1
;; First migration. Add collection and base event data.

(def migration-1-schema
  [{:_id :_collection
    :_collection/name :event
    :_collection/doc "Athens semantic event log."}
   {:_id :_predicate
    :_predicate/name :event/id
    :_predicate/doc "A globally unique event id."
    :_predicate/unique true
    :_predicate/type :string}
   {:_id :_predicate
    :_predicate/name :event/data
    :_predicate/doc "Event data serialized as an EDN string."
    :_predicate/type :string}])


(defn migrate-to-1
  [conn ledger]
  (when-not (and ((migrate/collections conn ledger) "event")
                 (every? (migrate/predicates conn ledger) ["event/id" "event/data"]))
    @(fdb/transact conn ledger migration-1-schema)))


;; TODO: Migration #2
;; Enforce immutability of event/id and event/data.
;; Ensure all events have id, a few early 2.0.0-beta versions were missing event ids for initial events.



;; TODO: Migration #3
;; Adds a order number for fast partial event queries via a filter in a where-triple.
;; Existing events are updated to contain the right order number.



(def migrations
  [[1 migrate-to-1]
   ;; [2 migrate-to-2]
   ;; [3 migrate-to-3]
   ;; [4 migrate-to-4]
   ])

