(ns athens.self-hosted.components.tx-listener
  "Datahike transaction log listener.

  Component depends on `:datahike` for connection to listen on
  and `:webserver` for broadcasting to connected clients"
  (:require
    [clojure.tools.logging      :as log]
    [com.stuartsierra.component :as component]))


(defrecord TxListener
  [datahike webserver listener-key]

  component/Lifecycle

  (start
    [component]
    (log/warn "TODO: Implement TxListener start")
    component)


  (stop
    [component]
    (log/warn "TODO: Implement TxListener stop")
    component))


(defn new-tx-listener
  []
  (map->TxListener {}))
