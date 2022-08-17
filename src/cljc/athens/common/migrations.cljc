(ns athens.common.migrations
  " Migrations should be interruptible and resumable, so that crashes and mistakes
  will not leave the DB in a bad state that can be recovered from.
  If a migration fails, it should throw an error.
  A good way to make something interruptible is to ensure its idempotent."
  (:require
    [athens.common.logging :as log]))


(defn run-migration!
  [conn set-version! [migration-version migration-f]]
  (log/debug "Running migration version" migration-version)
  (migration-f conn)
  (set-version! conn migration-version)
  (log/debug "Finished migration version" migration-version)
  nil)


(defn- migrate-bootstrap!
  "Similar to migrate!, but for the migrator itself.
  Doesn't keep a separate version table because that's a recursive problem,
  and instead always runs all the migrations.
  They should be idempotent, cheap, and don't log anything, so it's ok to always do this."
  [conn bootstrap-migrations]
  (run! (fn [[_ f]] (f conn)) bootstrap-migrations))


(defn migrate!
  "Migrate conn to latest (or up-to).
   Interrupted migrations should resume gracefully next time migrate! runs. "
  [conn migrations bootstrap-migrations version set-version! & {:keys [up-to] :or {up-to ##Inf}}]
  (migrate-bootstrap! conn bootstrap-migrations)
  (let [current-v  (version conn)
        v-filter   (fn [[v]] (and (< current-v v) (<= v up-to)))
        migrations (filter v-filter migrations)]
    (when (seq migrations)
      (log/debug "Running" (count migrations) "migrations")
      (run! (partial run-migration! conn set-version!) migrations)
      (log/debug "Ledger migrated to version" (-> migrations last first)))))
