(ns athens.self-hosted.components.fluree
  (:require
    [athens.self-hosted.event-log :as event-log]
    [clojure.tools.logging        :as log]
    [com.stuartsierra.component   :as component]
    [fluree.db.api                :as fdb]))


(defrecord Fluree
  [config conn]

  component/Lifecycle

  (start
    [component]
    (let [servers    (get-in config [:config :fluree :servers])
          in-memory? (-> config :config :in-memory?)]
      (if in-memory?
        (do
          (log/warn "Athens configuration is set to use in-memory, skipping Fluree initialization.")
          component)
        (do
          (log/info "Starting Fluree connection, servers" servers)
          (let [conn (fdb/connect servers)]
            ;; Initialize event log.
            (event-log/ensure-ledger! conn)
            (assoc component :conn conn))))))


  (stop
    [component]
    (log/info "Closing Fluree connection")
    (when conn
      (fdb/close conn))
    (dissoc component :conn)))


(defn new-fluree
  []
  (map->Fluree {}))


