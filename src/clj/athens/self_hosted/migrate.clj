(ns athens.self-hosted.migrate
  "Contains schema and data migrations to the event log.
  Migrations should be interruptible and resumable, so that crashes and mistakes
  will not leave the DB in a bad state that can be recovered from.
  If a migration fails, it should throw an error.
  A good way to make something interruptible is to ensure its idempotent."
  (:require
    [athens.self-hosted.fluree.utils :as fu]
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [fluree.db.api :as fdb]))


;; Migration helpers

(defn get-predicate-values
  [conn ledger predicate]
  (set (fu/query conn ledger {:select "?o"
                              :where  [["?s" predicate "?o"]]})))


(defn internal-name?
  [s]
  (str/starts-with? s "_"))


(defn collections
  [conn ledger]
  (->> (get-predicate-values conn ledger "_collection/name")
       (remove internal-name?)
       set))


(defn predicates
  [conn ledger]
  (->> (get-predicate-values conn ledger "_predicate/name")
       (remove internal-name?)
       set))


(defn functions
  [conn ledger]
  (set (get-predicate-values conn ledger "_fn/name")))


;; Migrations for the migrator itself.
;; It also can change over time.

;; Migration #1
;; First migration. Add the migration table.

(def migration-1-schema
  [{:_id :_collection
    :_collection/name :migrations
    :_collection/doc "Completed migrations for the Athens semantic event log."}
   {:_id :_predicate
    :_predicate/name :migrations/version
    :_predicate/doc "Marker that the matching migration version was applied."
    :_predicate/unique true
    :_predicate/upsert true
    :_predicate/type :int}])


(defn migrate-to-1
  [conn ledger]
  (when-not ((collections conn ledger) "migrations")
    (fu/transact! conn ledger migration-1-schema)))


(def bootstrap-migrations
  [[1 migrate-to-1]
   ;; [2 migrate-to-2]
   ])


;; Migrator

(defn set-migration-version!
  [conn ledger version]
  (fu/transact! conn ledger [{:_id :migrations
                              :migrations/version version}]))


(defn get-current-version
  [conn ledger]
  (let [ret (fu/query conn ledger {:select "(max ?v)"
                                   :where [["?s" "migrations/version" "?v"]]})]
    (if (ex-message ret)
      0
      (or ret 0))))


(defn run-migration!
  [conn ledger [migration-version migration-f]]
  (log/info "Running migration version" migration-version)
  (migration-f conn ledger)
  (set-migration-version! conn ledger migration-version)
  (log/info "Finished migration version" migration-version)
  nil)


(defn- boostrap-migrator!
  "Similar to migrate-ledger!, but for the migrator table itself.
  Doesn't keep a separate version table because that's a recursive problem,
  and instead always runs all the migrations.
  They are idempotent, cheap, and don't log anything, so it's ok to always do this."
  [conn ledger]
  (run! (fn [[_ f]] (f conn ledger)) bootstrap-migrations))


(defn migrate-ledger!
  "Migrate ledger to latest (or up-to) schema and data versions.
   Interrupted migrations should resume gracefully next time the migrate-ledger! runs. "
  [conn ledger migrations & {:keys [up-to] :or {up-to ##Inf}}]
  (boostrap-migrator! conn ledger)
  (let [current    (get-current-version conn ledger)
        v-filter   (fn [[v]] (and (< current v) (<= v up-to)))
        migrations (filter v-filter migrations)]
    (when (seq migrations)
      (log/info "Running" (count migrations) "migrations")
      (run! (partial run-migration! conn ledger) migrations)
      (log/info "Ledger migrated to version" (-> migrations last first)))))


(comment

  (def conn (fdb/connect "http://localhost:8090"))
  (def ledger "events/log")

  ;; The ledger itself needs to exist.
  @(fdb/new-ledger conn ledger)

  (migrate-ledger! conn ledger [])

  (migrate-ledger! conn ledger [] :up-to 2)

  )
