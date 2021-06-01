(ns athens.self-hosted.components.config
  "Athens Self-Hosted Configuration management"
  (:require
    [clojure.tools.logging      :as log]
    [com.stuartsierra.component :as component]
    [config.core                :as cfg]))


(defrecord Configuration
  []

  component/Lifecycle

  (start
    [component]
    (log/info "Starting configuration component")
    (assoc component :config (cfg/reload-env)))


  (stop
    [component]
    (log/info "Stopping configuration component")
    (assoc component :config nil)))


(defn new-config
  []
  (map->Configuration {}))

