(ns athens.self-hosted.web
  (:require [athens.self-hosted.web.graph    :as graph]
            [athens.self-hosted.web.presence :as presence]
            [clojure.tools.logging           :as log]
            [com.stuartsierra.component      :as component]
            [compojure.core                  :as compojure]
            [org.httpkit.server              :as http]))


(compojure/defroutes health-check-route
  (compojure/GET "/health-check" [] "ok"))

(defn make-handler [datahike]
  (compojure/routes health-check-route
                    presence/presence-routes
                    ;; TODO pass `datahike` to graph-routes
                    graph/graph-routes))

(defrecord WebServer [config httpkit datahike]
  component/Lifecycle
  (start [component]
    (if httpkit
      (do
        (log/warn "Server already started, it's ok. Though it means we're not managing it properly.")
        component)
      (let [http-conf (get-in config [:config :http])]
        (log/info "Starting WebServer with config: " http-conf)
        (assoc component :httpkit
               (http/run-server (make-handler datahike) http-conf)))))
  (stop [component]
    (log/info "Stopping WebServer")
    (when httpkit
      (httpkit :timeout 100)
      (assoc component :httpkit nil))))

(defn new-web-server [config]
  (map->WebServer config))
