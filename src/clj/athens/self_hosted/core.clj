(ns athens.self-hosted.core
  "Athens Self Hosted Backend entry point."
  (:gen-class)
  (:require
    [athens.self-hosted.components.config      :as cfg]
    [athens.self-hosted.components.datahike    :as datahike]
    [athens.self-hosted.components.nrepl       :as nrepl]
    [athens.self-hosted.components.web         :as web]
    [clojure.tools.logging                     :as log]
    [com.stuartsierra.component                :as component]))


(defn new-system
  "Creates new system map"
  []
  (log/debug "Building new system map")
  (component/system-map
    :config      (cfg/new-config)
    :datahike    (component/using (datahike/new-datahike)
                                  [:config])
    :webserver   (component/using (web/new-web-server)
                                  [:config :datahike])
    :nrepl       (component/using (nrepl/new-nrepl-server)
                                  [:config])))


(def system (new-system))


(defn -main
  [& _args]
  (log/info "Athens Self-Hosted Starting")
  (alter-var-root #'system component/start)
  (log/info "Athens Self-Hosted ready to do thy bidding"))
