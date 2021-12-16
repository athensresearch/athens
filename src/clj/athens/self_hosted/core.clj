(ns athens.self-hosted.core
  "Athens Self Hosted Backend entry point."
  (:gen-class)
  (:require
    [athens.common.logging                    :as log]
    [athens.self-hosted.components.config     :as cfg]
    [athens.self-hosted.components.datascript :as datascript]
    [athens.self-hosted.components.fluree     :as fluree]
    [athens.self-hosted.components.nrepl      :as nrepl]
    [athens.self-hosted.components.web        :as web]
    [com.stuartsierra.component               :as component]))


(defn new-system
  "Creates new system map"
  []
  (log/debug "Building new system map")
  (component/system-map
    :config     (cfg/new-config)
    :fluree     (component/using (fluree/new-fluree)
                                 [:config])
    :datascript (component/using (datascript/new-datascript)
                                 [:config :fluree])
    :webserver  (component/using (web/new-web-server)
                                 [:config :datascript :fluree])
    :nrepl      (component/using (nrepl/new-nrepl-server)
                                 [:config])))


(def system (new-system))


(defn -main
  [& _args]
  (log/info "Athens Self-Hosted Starting")
  (alter-var-root #'system component/start)
  (log/info "Athens Self-Hosted ready to do thy bidding"))
