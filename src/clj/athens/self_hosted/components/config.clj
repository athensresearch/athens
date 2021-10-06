(ns athens.self-hosted.components.config
  "Athens Self-Hosted Configuration management"
  (:require
    [athens.common.logging      :as log]
    [clojure.pprint             :as pp]
    [com.stuartsierra.component :as component]
    [config.core                :as cfg]))


(defrecord Configuration
  []

  component/Lifecycle

  (start
    [component]
    (let [config        (cfg/reload-env)
          merged-config (cfg/merge-maps config (:config-edn config))]
      (log/info "Starting configuration component")
      (log/debug "Merged configuration:" (with-out-str
                                           (pp/pprint merged-config)))
      (assoc component :config merged-config)))


  (stop
    [component]
    (log/info "Stopping configuration component")
    (assoc component :config nil)))


(defn new-config
  []
  (map->Configuration {}))

