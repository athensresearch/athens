(ns athens.self-hosted.web
  (:require
    [athens.common-events.schema       :as schema]
    [athens.self-hosted.web.datascript :as datascript]
    [athens.self-hosted.web.presence   :as presence]
    [clojure.tools.logging             :as log]
    [cognitect.transit                 :as transit]
    [com.stuartsierra.component        :as component]
    [compojure.core                    :as compojure]
    [org.httpkit.server                :as http])
  (:import
    (java.io
      ByteArrayInputStream
      ByteArrayOutputStream)))


;; Internal state
(defonce clients (atom {}))


(defn- ->transit
  [data]
  (let [out (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json)]
    (transit/write writer data)
    (.toString out)))


(defn- <-transit
  [transit-str]
  (let [in (ByteArrayInputStream. (.getBytes transit-str))
        reader (transit/reader in :json)]
    (transit/read reader)))


(defn send!
  "Send data to a client via `channel`"
  [channel data]
  (http/send! channel (->transit data)))


;; WebSocket handlers
(defn open-handler
  [ch]
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
        (send! client presence-disconnect)))))


(defn receive-handler
  [ch msg]
  (log/debug ch "<-" msg)
  (let [username (get @clients ch)
        data     (<-transit msg)]
    (if-not (schema/valid-event? data)
      (let [explanation (schema/explain-event data)]
        (log/warn ch "invalid event received:" explanation)
        (send! ch {:event/id      (:event/id data)
                   :event/status  :rejected
                   :reject/reason explanation}))
      (do
        (log/debug ch "decoded event" (pr-str data))
        (if (and (nil? username)
                 (not= :presence/hello (:event/type data)))
          (do
            (log/warn ch "Message out of order, didn't say :presence/hello.")
            (send! ch {:event/id      (:event/id data)
                       :event/status  :rejected
                       :reject/reason :introduce-yourself}))
          (let [event-type (:event/type data)
                result     (cond
                             (contains? presence/supported-event-types event-type)
                             (presence/presence-handler clients ch data)

                             (contains? datascript/supported-event-types event-type)
                             (datascript/datascript-handler ch data))]
            (send! ch (merge {:event/id (:event/id data)}
                             result))))))))


(defn websocket-handler
  [request]
  (http/as-channel request
                   {:on-open    #'open-handler
                    :on-close   #'close-handler
                    :on-receive #'receive-handler}))


(compojure/defroutes ws-route
                     (compojure/GET "/ws" [] websocket-handler))


(compojure/defroutes health-check-route
                     (compojure/GET "/health-check" [] "ok"))


(defn make-handler
  [_datahike]
  (compojure/routes health-check-route
                    ws-route))


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
  [config]
  (map->WebServer config))
