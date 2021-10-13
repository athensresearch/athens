(ns athens.self-hosted.event-log
  (:require
    [clojure.edn :as edn]
    [clojure.tools.logging :as log]
    [fluree.db.api :as fdb]))


(def ledger "events/log")


(def schema
  [{:_id :_collection
    :_collection/name :event
    :_collection/doc "Athens semantic events."}
   {:_id :_predicate
    :_predicate/name :event/id
    :_predicate/doc "A globally unique event id."
    :_predicate/unique true
    :_predicate/type :string}
   {:_id :_predicate
    :_predicate/name :event/data
    :_predicate/doc "Event data serialized as an EDN string."
    :_predicate/type :string}])


(def initial-events
  [;; TODO: Convert welcome page etc into events, add them here.
   ])


(defn serialize
  [id data]
  {:_id :event
   :event/id (str id)
   :event/data (pr-str data)})


(defn deserialize
  [{id   "event/id"
    data "event/data"}]
  [id (edn/read-string data)])


(defn all-events
  [conn]
  (map deserialize
       @(fdb/query (fdb/db conn ledger)
                   {:select {"?event" ["*"]}
                    :where  [["?event" "event/id", "?id"]]
                    ;; Subject (?event here) is a monotonically incrementing bigint,
                    ;; so ordering by that gives us insertion order.
                    :opts   {:orderBy ["ASC", "?event"]}})))


(defn add-event!
  [conn id data]
  @(fdb/transact conn ledger [(serialize id data)]))


(defn ensure-ledger!
  [conn]
  (when (empty? @(fdb/ledger-info conn ledger))
    (log/info "Fluree ledger for event-log not found, creating" ledger)
    @(fdb/new-ledger conn ledger)
    (fdb/wait-for-ledger-ready conn ledger)
    @(fdb/transact conn ledger schema)
    (log/info "Populating fresh ledger with initial events...")
    (doseq [[id data] initial-events]
      (add-event! conn id data))
    (log/info "✅ Populated fresh ledger.")
    (log/info "✅ Fluree ledger for event-log created.")))


#_(defn events-since
  "TODO: All events since start-id."
  [_db _start-id]
  ;; convert start-id to subjects
  ;; use same query as all-events but add more where clauses
  )


#_(defn subscribe
  "TODO: Calls f with k, [id data], for each event starting at start-id.
  k is is a subscription key that you can use with unsubscribe."
  [_conn _k _start-id _f]
  ;; Fluree only has fdb/listen that starts at the calling time, but we can use
  ;; a notifier pattern to turn that into an arbitrary subscription since a past block.
  ;; Use events-since for the initial list, store the last id+subject, call f with each of those.
  ;; Make a listener fn that takes the listener data and determines if it's an event, extracts
  ;; the id if so, calls events-since with it, and call f with with block there.
  ;; When calling f, ignore subjects that have already been used, avoiding duplicated.
  )


#_(defn unsubscribe
  "TODO: remove subscription with key k."
  [_conn _k])


(comment
  (def conn (fdb/connect "http://localhost:8090"))

  ;; Create ledger if not present.
  (ensure-ledger! conn)

  ;; What are the current events in the ledger?
  (all-events conn)

  ;; Add a few events.
  (def events [["uuid-1" [1 2 3]]
               ["uuid-2" [4 5 6]]
               ["uuid-3" [7 8 9]]])

  (doseq [[id data] events]
    (add-event! conn id data))

  ;; Check the events again.
  (all-events conn)

  @(fdb/delete-ledger conn ledger))
