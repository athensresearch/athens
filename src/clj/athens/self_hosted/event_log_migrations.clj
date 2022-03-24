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


;; Migration #2
;; Enforce immutability of event/id and event/data.
;; Ensure all events have id, a few early 2.0.0-beta versions were missing event ids for initial events.

(def migration-2-fn
  [{:_id :_fn$immutable
    :_fn/name :immutable
    :_fn/doc "Checks whether the proposed object is changing existing data."
    :_fn/code "(nil? (?pO))"}
   {:_id [:_predicate/name :event/id]
    :_predicate/spec [:_fn$immutable]}
   {:_id [:_predicate/name :event/data]
    :_predicate/spec [:_fn$immutable]}])


(defn sid+id-page
  "Returns {:next-page ... :items ...}, where items is a vector of [subject-id event-id] in
  page-number for all events that have event/data in db split by page-size.
  Events without event/id will return nil as event-id. For use with `iteration`."
  ([db page-size page-number]
   {:next-page (inc page-number)
    :items     @(fdb/query db {:select ["?event" "?id"]
                               :where  [["?event" "event/data", "?data"]
                                        {:optional [["?event" "event/id" "?id"]]}]
                               :opts   {:limit  page-size
                                        :offset (* page-size page-number)}})}))


(defn add-missing-uuid!
  [conn ledger sid]
  @(fdb/transact conn ledger [{:_id sid :event/id (str (random-uuid))}]))


(defn sids-with-missing-uids
  [conn ledger]
  (let [db   (fdb/db conn ledger)
        step (partial sid+id-page db 1)]
    (->> (iteration step
                    :kf :next-page
                    :vf :items
                    :somef #(-> % :items seq)
                    :initk 0)
         (sequence cat)
         (remove second)
         (map first))))


(defn migrate-to-2
  [conn ledger]
  (when-not ((migrate/functions conn ledger) "immutable")
    @(fdb/transact conn ledger migration-2-fn))
  (run! (partial add-missing-uuid! conn ledger)
        (sids-with-missing-uids conn ledger)))


;; TODO: Migration #3
;; Adds a order number for fast partial event queries via a filter in a where-triple.
;; Existing events are updated to contain the right order number.


(def migrations
  [[1 migrate-to-1]
   [2 migrate-to-2]
   ;; [3 migrate-to-3]
   ;; [4 migrate-to-4]
   ])


(comment
  (def conn (fdb/connect "http://localhost:8090"))
  (def ledger "events/log")
  @(fdb/new-ledger conn ledger)

  (defn all-events []
    (-> conn
        (fdb/db ledger)
        (fdb/query {:select ["*"]
                    :from "event"})
        deref))

  ;; Migration 2
  ;; init and add some events, one without id
  (migrate/migrate-ledger! conn ledger migrations :up-to 1)
  (def ids (repeatedly 4 #(str (random-uuid))))
  @(fdb/transact conn ledger [{:_id :event :event/id (nth ids 0) :event/data "0"}
                              {:_id :event :event/id (nth ids 1) :event/data "1"}
                              {:_id :event :event/data "4"}
                              {:_id :event :event/id (nth ids 3) :event/data "3"}])
  (all-events)

  ;; I can modify id and data
  @(fdb/transact conn ledger [{:_id [:event/id (nth ids 3)]
                               :event/id (str (random-uuid))
                               :event/data "10"}])

  (all-events)

  ;; After migration the event with data 4 should have an id, and we can't change id and data
  (migrate-to-2 conn ledger)
  (all-events)
  @(fdb/transact conn ledger [{:_id [:event/id (nth ids 1)]
                               :event/id (str (random-uuid))
                               :event/data "10"}])
  ;
  )
