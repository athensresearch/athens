(ns athens.self-hosted.components.nrepl
  (:require
    [clojure.tools.logging      :as log]
    [com.stuartsierra.component :as component]
    [nrepl.server               :as nrepl]))


(defrecord nReplServer
  [config server]

  component/Lifecycle

  (start
    [component]
    (let [nrepl-conf    (get-in config [:config :nrepl])
          port          (get nrepl-conf :port)
          nrepl-handler #(do (require 'cider.nrepl)
                             (ns-resolve 'cider.nrepl 'cider-nrepl-handler))
          handler       (nrepl-handler)]
      (log/info "Starting NREPL server with config:" (pr-str nrepl-conf))
      (assoc component :server (nrepl/start-server :port port :handler handler))))


  (stop
    [component]
    (log/info "Stopping NREPL server.")
    (when server
      (nrepl/stop-server server)
      (assoc component :server nil))))


(defn new-nrepl-server
  []
  (map->nReplServer {}))
