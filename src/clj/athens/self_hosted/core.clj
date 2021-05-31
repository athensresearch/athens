(ns athens.self-hosted.core
  "Athens Self Hosted Backend entry point."
  (:gen-class)
  (:require
    [athens.self-hosted.config     :as cfg]
    [athens.self-hosted.datahike   :as datahike]
    [athens.self-hosted.web        :as web]
    [clojure.tools.logging         :as log]
    [com.stuartsierra.component    :as component]
    [system.core                   :as system]
    [system.components.repl-server :as nrepl]))


(defn new-system
  "Creates new system map"
  []
  (log/debug "Building new system map")
  (component/system-map
    :config    (cfg/new-config)
    :datahike  (component/using (datahike/new-datahike {})
                                [:config])
    :webserver (component/using (web/new-web-server {})
                                [:config :datahike])
    ;; TODO move 8877 to configuration, need to wrap new-repl-server so it can use `:config`
    :nrepl     (nrepl/new-repl-server :port 8877)))


(def system (new-system))


(defn -main
  [& _args]
  (log/info "Athens Self-Hosted Starting")
  (component/start system)
  (log/info "Athens Self-Hosted ready to do thy bidding"))
