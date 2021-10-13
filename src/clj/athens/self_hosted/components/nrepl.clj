(ns athens.self-hosted.components.nrepl
  (:require
    [athens.common.logging      :as log]
    [com.stuartsierra.component :as component]
    [nrepl.server               :as nrepl]))


(defrecord nReplServer
  [config server]

  component/Lifecycle

  (start
    [component]
    (if-let [nrepl-conf (get-in config [:config :nrepl])]
      (let [port          (:port nrepl-conf)
            nrepl-handler #(do (require 'cider.nrepl)
                               (ns-resolve 'cider.nrepl 'cider-nrepl-handler))
            handler       (nrepl-handler)]
        (log/info "Starting NREPL server with config:" (pr-str nrepl-conf)) 
        (assoc component :server (nrepl/start-server :port port :handler handler)))
      component))


  (stop
    [component]
    (when server
      (log/info "Stopping NREPL server.")
      (nrepl/stop-server server))
    (dissoc component :server)))


(defn new-nrepl-server
  []
  (map->nReplServer {}))
