(ns athens.self-hosted.components.fluree
  (:require
    [athens.self-hosted.event-log :as event-log]
    [clojure.tools.logging        :as log]
    [com.stuartsierra.component   :as component]
    [fluree.db.api                :as fdb]))


(defn create-fluree-comp
  [address]
  ;; Wrap conn in an atom so we can reconnect without restarting component.
  (let [conn-atom    (atom nil)
        reconnect-fn (fn []
                       (when-some [conn @conn-atom]
                         (fdb/close conn))
                       (reset! conn-atom (fdb/connect address)))
        comp         {:conn-atom    conn-atom
                      :reconnect-fn reconnect-fn}]
    (reconnect-fn)
    comp))


(defrecord Fluree
  [config conn-atom reconnect-fn]

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
          (let [comp         (create-fluree-comp servers)]
            ;; Initialize event log.
            (event-log/init! comp)
            (merge component comp))))))


  (stop
    [component]
    (log/info "Closing Fluree connection")
    (when-some [conn @conn-atom]
      (fdb/close conn))
    (dissoc component :conn-atom :reconnect-fn)))


(defn new-fluree
  []
  (map->Fluree {}))


