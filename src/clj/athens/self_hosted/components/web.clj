(ns athens.self-hosted.components.web
  (:require
    [athens.common-events.schema       :as schema]
    [athens.self-hosted.clients        :as clients]
    [athens.self-hosted.web.datascript :as datascript]
    [athens.self-hosted.web.presence   :as presence]
    [clojure.tools.logging             :as log]
    [com.stuartsierra.component        :as component]
    [compojure.core                    :as compojure]
    [org.httpkit.server                :as http]))


;; WebSocket handlers
(defn open-handler
  [channel]
  (clients/add-client! channel))


(defn close-handler
  [channel status]
  (let [username            (clients/get-client-username channel)
        presence-disconnect {:presence {:username   username
                                        :disconnect true}}]
    (clients/remove-client! channel)
    (log/info channel username "closed, status" status)
    (when username
      (clients/broadcast! presence-disconnect))))


(defn- make-receive-handler
  [datahike]
  (fn receive-handler
    [channel msg]
    (log/debug channel "<-" msg)
    (let [username (clients/get-client-username channel)
          data     (clients/<-transit msg)]
      (if-not (schema/valid-event? data)
        (let [explanation (schema/explain-event data)]
          (log/warn channel "invalid event received:" explanation)
          (clients/send! channel {:event/id      (:event/id data)
                                  :event/status  :rejected
                                  :reject/reason explanation}))
        (do
          (log/debug channel "decoded event" (pr-str data))
          (if (and (nil? username)
                   (not= :presence/hello (:event/type data)))
            (do
              (log/warn channel "Message out of order, didn't say :presence/hello.")
              (clients/send! channel {:event/id      (:event/id data)
                                      :event/status  :rejected
                                      :reject/reason :introduce-yourself}))
            (let [event-type (:event/type data)
                  result     (cond
                               (contains? presence/supported-event-types event-type)
                               (presence/presence-handler datahike channel data)

                               (contains? datascript/supported-event-types event-type)
                               (datascript/datascript-handler datahike channel data))]
              (clients/send! channel (merge {:event/id (:event/id data)}
                                            result)))))))))


(defn- make-websocket-handler
  [datahike]
  (fn websocket-handler
    [request]
    (http/as-channel request
                     {:on-open    #'open-handler
                      :on-close   #'close-handler
                      :on-receive (make-receive-handler datahike)})))


(defn- make-ws-route
  [datahike]
  (compojure/routes
    (compojure/GET "/ws" []
                   (make-websocket-handler datahike))))


(compojure/defroutes health-check-route
                     (compojure/GET "/health-check" [] "ok"))


(defn make-handler
  [datahike]
  (compojure/routes health-check-route
                    (make-ws-route datahike)))


(defrecord WebServer
  [config httpkit datahike]

  component/Lifecycle

  (start
    [component]
    (if httpkit
      (do
        (log/warn "Server already started, it's ok. Though it means we're not managing it properly.")
        component)
      (let [http-conf (get-in config [:config :http])]
        (log/info "Starting WebServer with config: " http-conf)
        (assoc component :httpkit
               (http/run-server (make-handler datahike) http-conf)))))


  (stop
    [component]
    (log/info "Stopping WebServer")
    (when httpkit
      (httpkit :timeout 100)
      (assoc component :httpkit nil))))


(defn new-web-server
  []
  (map->WebServer {}))

