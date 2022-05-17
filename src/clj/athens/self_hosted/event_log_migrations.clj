(ns athens.self-hosted.event-log-migrations
  "Contains schema and data migrations to the event log."
  (:require
    [athens.self-hosted.fluree.utils :as fu]
    [athens.self-hosted.migrate :as migrate]
    [clojure.tools.logging :as log]
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
    (fu/transact! conn ledger migration-1-schema)))


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


;; NB: there's no efficient way to query on a non-existing predicate in Fluree,
;; so here (and in sid+order-page) we return all elements.
;; Fluree has a filter map that can be used in `where`, but it's slow.
;; See https://discord.com/channels/896089675511508992/908441256986816533/955881109869187082
(defn sid+id-page
  "Returns {:next-page ... :items ...}, where items is a vector of [subject-id event-id] in
  page-number for all events that have event/data in db, split by page-size.
  Events without event/id will return nil as event-id. For use with `iteration`."
  ([db page-size page-number]
   (log/info "Fetching sid+id offset" (* page-size page-number))
   {:next-page (inc page-number)
    :items     (fu/query db {:select ["?event" "?id"]
                             :where  [["?event" "event/data", "?data"]
                                      {:optional [["?event" "event/id" "?id"]]}]
                             :opts   {:limit  page-size
                                      :offset (* page-size page-number)}})}))


(defn add-missing-uuid!
  [conn ledger sid]
  (log/info "Adding uuid to sid" sid)
  (fu/transact! conn ledger [{:_id sid :event/id (str (random-uuid))}]))


(defn sids-with-missing-uids
  [conn ledger]
  (let [db   (fdb/db conn ledger)
        step (partial sid+id-page db 100)]
    (->> (iteration step
                    :kf :next-page
                    :vf :items
                    :somef #(-> % :items seq)
                    :initk 0)
         (sequence cat)
         ;; Remove items that have a non-nil uid in [sid uid]
         (remove second)
         (map first))))


(defn migrate-to-2
  [conn ledger]
  (when-not ((migrate/functions conn ledger) "immutable")
    (fu/transact! conn ledger migration-2-fn))
  (run! (partial add-missing-uuid! conn ledger)
        (sids-with-missing-uids conn ledger)))


;; TODO: Migration #3
;; Adds a order number for fast partial event queries via a filter in a where-triple.
;; Existing events are updated to contain the right order number.

(def migration-3-schema
  [{:_id :_predicate
    :_predicate/name :event/order
    ;; Note on limits:
    ;; PostgreSQL data types https://www.postgresql.org/docs/current/datatype-numeric.html
    ;; Fluree data types https://developers.flur.ee/docs/overview/schema/predicates/
    ;; PostgreSQL uses `serial` and `bigserial` for auto-incrementing fields.
    ;; Fluree `int` is the same max as PostgreSQL `serial` (32 bits = 4 bytes), and the same
    ;; goes for `long` and `bigserial` (64 bits = 8 bytes).
    ;; So `int` gives us up to 2147483647, and `long` is up to 9223372036854775807.
    ;; This isn't infinite, but it's a lot, and if we hit the limit we should find another
    ;; efficient way of doing the ordered log, and migrate all events there instead.
    ;; I also tried Flurees `bigint` but it made queries and insertions (~0.8s at 45k events) slow.

    ;; Note on how ordering is implemented:
    ;; Fluree's refs are represented in flakes as the subject ID, which seems to be a least a long
    ;; that starts at 351843720888320 and goes up by 1 with each new entity.
    ;; Refs work for ordering even though they are returned as `{:_id 351843720888320}`, since
    ;; on the flake itself it's a number. Unclear if this is supported long term in Fluree though.
    ;; We set a reference to the own entity on transaction, or migration for the ones missing it.
    ;; Using refs for ordering is very efficient because no calculations need to be made on insertion,
    ;; and the only limit it will hit is that of the max entity that fluree supports.
    ;; I tried before using the `max-pred-val` smartfn but that proved to scale with total events,
    ;; which made it unsuitable insertions even on the medium term (95k events was already taking 600ms).

    ;; TODO: the "strictly increasing" condition could be validated via specs:
    ;; - collection spec to ensure order is there
    ;; - predicate spec to ensure the new number is bigger than the max
    ;; This validation isn't happening here, we're just transacting "correct" data.
    :_predicate/doc "Strictly increasing long for event ordering."
    :_predicate/unique true
    :_predicate/type :ref
    :_predicate/spec [[:_fn/name :immutable]]}])


(defn add-order!
  [conn ledger sid]
  (log/info "Adding order to sid" sid)
  (fu/transact! conn ledger [{:_id sid
                              :event/order sid}]))


(defn sid+order-page
  "Returns {:next-page ... :items ...}, where items is a vector of [subject-id order] in
  page-number for all events with an event/id in db, ordered by subject-id, split by page-size.
  Before event/order was added, events were ordered by subject-id as it's a strictly increasing
  bigint that acts as insertion order.
  Events without event/order will return nil as order. For use with `iteration`."
  ([db page-size page-number]
   (log/info "Fetching sid+order offset" (* page-size page-number))
   {:next-page (inc page-number)
    :items     (fu/query db {:select ["?event" "?order"]
                             :where  [["?event" "event/id", "?id"]
                                      {:optional [["?event" "event/order" "?order"]]}]
                             :opts   {:orderBy ["ASC", "?event"]
                                      :limit   page-size
                                      :offset  (* page-size page-number)}})}))


(defn sids-with-missing-order
  [conn ledger]
  (let [db   (fdb/db conn ledger)
        step (partial sid+order-page db 100)]
    (->> (iteration step
                    :kf :next-page
                    :vf :items
                    :somef #(-> % :items seq)
                    :initk 0)
         (sequence cat)
         ;; Remove items that have a non-nil order in [sid order]
         (remove second)
         (map first))))


(defn migrate-to-3
  [conn ledger]
  (when-not ((migrate/predicates conn ledger) "event/order")
    (->> (fu/transact! conn ledger migration-3-schema)
         :block
         ;; Force sync to ensure query recognizes the new schema predicate.
         (fu/wait-for-block conn ledger)))
  (run! (partial add-order! conn ledger)
        (sids-with-missing-order conn ledger)))


(def migrations
  [[1 migrate-to-1]
   [2 migrate-to-2]
   [3 migrate-to-3]
   ;; [4 migrate-to-4]
   ])


(comment
  (def conn (fdb/connect "http://localhost:8090"))
  (def ledger "events/log")
  @(fdb/new-ledger conn ledger)


  @(fdb/transact conn ledger migration-1-schema)

  (defn all-events []
    (-> conn
        (fdb/db ledger)
        (fu/query {:select ["*"]
                   :from   "event"})))

  ;; Migration 2
  ;; init and add some events, one without id
  (migrate/migrate-ledger! conn ledger migrations :up-to 1)
  (def ids (repeatedly 4 #(str (random-uuid))))
  (fu/transact! conn ledger [{:_id :event :event/id (nth ids 0) :event/data "0"}
                                   {:_id :event :event/id (nth ids 1) :event/data "1"}
                                   {:_id :event :event/data "4"}
                                   {:_id :event :event/id (nth ids 3) :event/data "3"}])
  (all-events)

  ;; I can modify id and data
  (fu/transact! conn ledger [{:_id [:event/id (nth ids 3)]
                                    :event/id (str (random-uuid))
                                    :event/data "10"}])

  (all-events)

  ;; After migration the event with data 4 should have an id, and we can't change id and data
  (migrate-to-2 conn ledger)
  (all-events)
  (fu/transact! conn ledger [{:_id [:event/id (nth ids 1)]
                              :event/id (str (random-uuid))
                              :event/data "10"}])

  ;; Migration 3
  ;; The events inserted above don't have an order id
  (all-events)

  ;; Running the migration should add it.
  (migrate-to-3 conn ledger)
  (all-events)

  ;;
  )


(comment
  (def conn (fdb/connect "http://localhost:8090"))
  (def ledger "events/log")
  @(fdb/new-ledger conn ledger)

  (def schema
    [{:_id              :_collection
      :_collection/name :event}
     {:_id               :_predicate
      :_predicate/name   :event/id
      :_predicate/unique true
      :_predicate/type   :string}
     {:_id               :_predicate
      :_predicate/name   :event/order
      :_predicate/unique true
      :_predicate/type   :ref}])

  @(fdb/transact conn ledger schema)


  (defn new-event [id]
    (let [str-id      (str id)
          self-tempid (str "event$self-" str-id)]
      {:_id         self-tempid
       :event/id    str-id
       :event/order self-tempid}))

  @(fdb/transact conn ledger [(new-event 1)
                              (new-event 2)
                              (new-event 3)
                              (new-event 4)])

  ;; descending order

  (-> conn
      (fdb/db ledger)
      (fdb/query {:select {"?event" ["*"]}
                  :where  [["?event" "event/order", "?order"]]
                  :opts   {:orderBy ["DESC", "?order"]}})
      deref)
  ;; => [{:_id 351843720888323, "event/id" "4", "event/order" {:_id 351843720888323}}
  ;;     {:_id 351843720888322, "event/id" "3", "event/order" {:_id 351843720888322}}
  ;;     {:_id 351843720888321, "event/id" "2", "event/order" {:_id 351843720888321}}
  ;;     {:_id 351843720888320, "event/id" "1", "event/order" {:_id 351843720888320}}]

  ;; ascending order

  (-> conn
      (fdb/db ledger)
      (fdb/query {:select {"?event" ["*"]}
                  :where  [["?event" "event/order", "?order"]]
                  :opts   {:orderBy ["ASC", "event/order"]}})
      deref)
  ;; => [{:_id 351843720888320, "event/id" "1", "event/order" {:_id 351843720888320}}
  ;;     {:_id 351843720888321, "event/id" "2", "event/order" {:_id 351843720888321}}
  ;;     {:_id 351843720888322, "event/id" "3", "event/order" {:_id 351843720888322}}
  ;;     {:_id 351843720888323, "event/id" "4", "event/order" {:_id 351843720888323}}]

  ;; Filtered

  (-> conn
      (fdb/db ledger)
      (fdb/query {:select {"?event" ["*"]}
                  :where  [["?event" "event/order" (str "#(> ?order " 351843720888321 ")")]]
                  :opts   {:orderBy ["ASC", "?order"]}})
      deref)
  ;; => [{:_id 351843720888323, "event/id" "4", "event/order" {:_id 351843720888323}}
  ;;     {:_id 351843720888322, "event/id" "3", "event/order" {:_id 351843720888322}}]
)
