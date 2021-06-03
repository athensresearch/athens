(ns athens.self-hosted.components.tx-listener
  "Datahike transaction log listener.

  Component depends on `:datahike` for connection to listen on
  and `:webserver` for broadcasting to connected clients"
  (:require
    [athens.common-events              :as common-events]
    [athens.self-hosted.components.web :as web]
    [clojure.tools.logging             :as log]
    [com.stuartsierra.component        :as component]
    [datahike.api                      :as d]))


(defn- tx-report-handler
  [tx-report]
  (log/info "tx-report-handler" (pr-str tx-report))
  ;; TODO provide transit encoding for datoms
  (web/broadcast! (common-events/build-tx-log-event tx-report)))


(defn- start-listener!
  "Connects tx log listener to Datahike connection.
  Returns listener-key, to be used in unlisten."
  [dh-conn]
  (d/listen dh-conn tx-report-handler))


(defrecord TxListener
  [datahike webserver listener-key]

  component/Lifecycle

  (start
    [component]
    (log/info "TxListener start")
    (assoc component :listener-key (start-listener! (:conn datahike))))


  (stop
    [component]
    (log/warn "TxListener stop")
    (d/unlisten (:conn datahike) listener-key)
    (assoc component :listener-key nil)))


(defn new-tx-listener
  []
  (map->TxListener {}))
