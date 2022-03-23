(ns athens.self-hosted.event-log-migrations
  "Contains schema and data migrations to the event log.
  Each migration is interruptible and can be resumed automatically."
  (:require
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [fluree.db.api :as fdb]))


;; Helpers

(defn collections
  [conn ledger]
  (let [res (-> conn
                (fdb/db ledger)
                (fdb/query {:select "?name"
                            :where [["?s" "_collection/name" "?name"]]})
                deref)]
    (->> res
         (remove #(str/starts-with? % "_")) ; encode this in the query?
         set)))


(defn predicates
  [conn ledger]
  (let [res (-> conn
                (fdb/db ledger)
                (fdb/query {:select "?name"
                            :where [["?s" "_predicate/name" "?name"]]})
                deref)]
    (->> res
         (remove #(str/starts-with? % "_"))
         set)))


;; Migration #1
;; First migration. Add collection and base event data.
;; At this time we didn't yet have migrations versions.

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
  (when-not (and ((collections conn ledger) "event")
                 (every? (predicates conn ledger) ["event/id" "event/data"]))
    @(fdb/transact conn ledger migration-1-schema))
  ;; This migration doesn't set the migration version because those didn't exist yet.
  ::skip-version)


;; Migration #2
;; Adds a migration version marker to enable future incremental migrations.

(def migration-2-schema
  [{:_id :_collection
    :_collection/name :migrations
    :_collection/doc "Completed migrations for the Athens semantic event log."}
   {:_id :_predicate
    :_predicate/name :migrations/version
    :_predicate/doc "Marker that the matching migration version was applied."
    :_predicate/unique true
    :_predicate/type :int}])


(defn migrate-to-2
  [conn ledger]
  (when-not ((collections conn ledger) "migrations")
    @(fdb/transact conn ledger migration-2-schema)))


;; TODO: Migration # 3
;; Ensure all events have id (string) and data (string) using spec fns.
;; Backfills missing id or data.


;; TODO: Migration #4
;; Adds a order number for fast partial event queries via a filter in a where-triple.
;; Existing events are updated to contain the right order number.


;; Migrator

(defn set-migration-version!
  [conn ledger version]
  @(fdb/transact conn ledger [{:_id :migrations
                               :migrations/version version}]))


(defn get-current-version
  [conn ledger]
  (if-not ((collections conn ledger) "migrations")
    0
    (-> conn
        (fdb/db ledger)
        (fdb/query {:select "(max ?v)"
                    :where [["?s" "migrations/version" "?v"]]})
        deref)))


(defn run-migration!
  [conn ledger [migration-version migration-f]]
  (log/info "Running migration version" migration-version)
  (condp = (migration-f conn ledger)
    ;; Interrupt the migration by throwing in case any migration fails.
    ::failed       (throw (ex-info (str "Migration " migration-version " failed, see above for details") {}))
    ::skip-version nil
    (set-migration-version! conn ledger migration-version))
  nil)


(def all-migrations
  [[1 migrate-to-1]
   [2 migrate-to-2]
   ;; [3 migrate-to-3]
   ;; [4 migrate-to-4]
   ])


(defn migrate-ledger!
  "Migrate ledger to latest (or up-to) schema and data versions.
   Interrupted migrations should resume gracefully next time the migrate-ledger! runs. "
  [conn ledger & {:keys [up-to] :or {up-to ##Inf}}]
  (let [current    (get-current-version conn ledger)
        v-filter   (fn [[v]] (and (< current v) (<= v up-to)))
        migrations (filter v-filter all-migrations)]
    (when (seq migrations)
      (log/info "Running" (count migrations) "migrations")
      (run! (partial run-migration! conn ledger) migrations)
      (log/info "Ledger migrated to version" (-> migrations last first)))))


(comment

  (def conn (fdb/connect "http://localhost:8090"))
  (def ledger "events/log")

  (migrate-ledger! conn ledger)

  (migrate-ledger! conn ledger :up-to 2)

  )
