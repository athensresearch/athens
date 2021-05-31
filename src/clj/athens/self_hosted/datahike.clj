(ns athens.self-hosted.datahike
  (:require
    [clojure.tools.logging :as log]
    [com.stuartsierra.component :as component]
    [datahike.api :as d]))


(defrecord Datahike
  [config conn]

  component/Lifecycle

  (start
    [component]
    (let [dh-conf (get-in config [:config :datahike])]
      (if (d/database-exists? dh-conf)
        (log/info "Connecting to existing Datahike database")
        (do
          (log/info "Creating new Datahike database")
          (d/create-database dh-conf)))
      (log/info "Starting Datahike connection: " dh-conf)
      (assoc component :conn (d/connect dh-conf))))


  (stop
    [component]
    (log/info "Stopping Datahike")
    (when conn
      (log/info "Releasing conn")
      (d/release conn)
      (assoc component :conn nil))))


(defn new-datahike
  [conf]
  (map->Datahike conf))
