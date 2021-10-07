(ns athens.self-hosted.components.web
  (:require
    [athens.common-events              :as common-events]
    [athens.common-events.schema       :as schema]
    [athens.common.logging             :as log]
    [athens.self-hosted.clients        :as clients]
    [athens.self-hosted.web.datascript :as datascript]
    [athens.self-hosted.web.presence   :as presence]
    [clojure.set                       :as set]
    [com.stuartsierra.component        :as component]
    [compojure.core                    :as compojure]
    [org.httpkit.server                :as http]))


;; WebSocket handlers
(defn open-handler
  [channel]
  (clients/add-client! channel))


(defn close-handler
  [datahike channel status]
  (let [username (clients/get-client-username channel)]
    (clients/remove-client! channel)
    ;; Notify clients after removing the one that left.
    (presence/goodbye-handler datahike username)
    (log/info "username:" username "!! closed connection, status:" status)))


(defn- valid-event-handler
  "Processes valid event received from the client."
  [datahike server-password channel username {:event/keys [id type] :as data}]
  (if (and (false? username)
           (not= :presence/hello type))
    (do
      (log/warn "Message out of order, didn't say :presence/hello.")
      (clients/send! channel (common-events/build-event-rejected id
                                                                 :introduce-yourself
                                                                 {:protocol-error :client-not-introduced})))
    (if-let [result (cond
                      (contains? presence/supported-event-types type)
                      (presence/presence-handler (:conn datahike) server-password channel data)

                      (contains? datascript/supported-event-types type)
                      (datascript/datascript-handler (:conn datahike) channel data)

                      (= :op/atomic type)
                      (datascript/atomic-op-handler (:conn datahike) channel data)

                      :else
                      (do
                        (log/error username "-> receive-handler, unsupported event:" (pr-str type))
                        (common-events/build-event-rejected id
                                                            (str "Unsupported event: " type)
                                                            {:unsupported-type type})))]
      (merge {:event/id id}
             result)
      (log/error "username:" username ", event-id:" id ", type:" type "No result for `valid-event-handler`"))))


(def ^:private forwardable-events
  (set/union
    datascript/supported-event-types
    #{:op/atomic}))


(defn- make-receive-handler
  [datahike server-password]
  (fn receive-handler
    [channel msg]
    (let [username (clients/get-client-username channel)
          data     (clients/<-transit msg)]
      (if-not (schema/valid-event? data)
        (let [explanation (schema/explain-event data)]
          (log/warn "username:" username "Invalid event received, explanation:" explanation)
          (clients/send! channel (common-events/build-event-rejected (:event/id data)
                                                                     (str "Invalid event: " (pr-str data))
                                                                     explanation)))
        (let [{:event/keys [id type]} data]
          (log/debug "username:" username ", event-id:" id ", type:" type "received valid event")
          (let [{:event/keys [status]
                 :as         result} (valid-event-handler datahike server-password channel username data)]
            (log/debug "username:" username ", event-id:" id ", processed with status:" status)
            ;; forward to everyone if accepted
            (when (and (= :accepted status)
                       (contains? forwardable-events type))
              (log/debug "Forwarding accepted event, event-id:" id)
              (clients/broadcast! data))
            ;; acknowledge
            (clients/send! channel result)))))))


(defn- make-websocket-handler
  [datahike server-password]
  (fn websocket-handler
    [request]
    (http/as-channel request
                     {:on-open    #'open-handler
                      :on-close   (partial close-handler (:conn datahike))
                      :on-receive (make-receive-handler datahike server-password)})))


(defn- make-ws-route
  [datahike server-password]
  (compojure/routes
    (compojure/GET "/ws" []
                   (make-websocket-handler datahike server-password))))


(compojure/defroutes health-check-route
                     (compojure/GET "/health-check" [] "ok"))


(defn make-handler
  [datahike server-password]
  (compojure/routes health-check-route
                    (make-ws-route datahike server-password)))


(defrecord WebServer
  [config httpkit datahike]

  component/Lifecycle

  (start
    [component]
    (if httpkit
      (do
        (log/warn "Server already started, it's ok. Though it means we're not managing it properly.")
        component)
      (let [http-conf       (get-in config [:config :http])
            server-password (get-in config [:config :password])]
        (log/info "Starting WebServer with config: " http-conf)
        (assoc component :httpkit
               (http/run-server (make-handler datahike server-password) http-conf)))))


  (stop
    [component]
    (log/info "Stopping WebServer")
    (when httpkit
      (httpkit :timeout 100)
      (assoc component :httpkit nil))))


(defn new-web-server
  []
  (map->WebServer {}))

