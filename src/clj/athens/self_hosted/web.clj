(ns athens.self-hosted.web
  (:require [athens.self-hosted.web.graph    :as graph]
            [athens.self-hosted.web.presence :as presence]
            [clojure.data.json               :as json]
            [clojure.tools.logging           :as log]
            [com.stuartsierra.component      :as component]
            [compojure.core                  :as compojure]
            [org.httpkit.server              :as http]))


;; Internal state
(defonce clients (atom {}))

;; WebSocket handlers
(defn open-handler [ch]
  (log/info ch "connected")
  (swap! clients assoc ch true))

(defn close-handler
  [ch status]
  (let [ch-username         (get @clients ch)
        presence-disconnect {:presence {:username   ch-username
                                        :disconnect true}}]
    (swap! clients dissoc ch)
    (log/info ch ch-username "closed, status" status)
    (when ch-username
      (doseq [client (keys @clients)]
        (http/send! client (json/json-str presence-disconnect))))))

(defn receive-handler [ch msg]
  (log/debug ch "<-" msg)
  (let [username (get @clients ch)
        data     (json/read-json msg)]
    (log/debug ch "decoded event" (pr-str data))
    (if (and (nil? username)
             (not= :presence/hello (:event/type data)))
      (do
        (log/warn ch "Message out of order, didn't say :presence/hello.")
        (http/send! ch {:event/error :introduce-yourself}))
      (let [event-type (:event/type data)]
        (cond
          (contains? presence/supported-event-types event-type)
          (presence/presence-handler clients ch event-type)

          ;; TODO use same approach for graph events
          ;; 1. check if event type supported
          ;; 2. delegate to graph handler
          )))))

(defn websocket-handler [request]
  (http/as-channel request
                   {:on-open    #'open-handler
                    :on-close   #'close-handler
                    :on-receive #'receive-handler}))

(compojure/defroutes ws-route
  (compojure/GET "/ws" [] websocket-handler))

(compojure/defroutes health-check-route
  (compojure/GET "/health-check" [] "ok"))

(defn make-handler [_datahike]
  (compojure/routes health-check-route
                    ws-route
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
