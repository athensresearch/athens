(ns athens.self-hosted.clients
  "Client comms"
  (:require
    [athens.common-events.schema :as schema]
    [athens.common.logging       :as log]
    [cognitect.transit           :as transit]
    [org.httpkit.server          :as http])
  (:import
    (java.io
      ByteArrayInputStream
      ByteArrayOutputStream)))


;; Internal state
(defonce clients (atom {}))


(defn ->transit
  [data]
  (let [out    (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json)]
    (transit/write writer data)
    (.toString out)))


(defn <-transit
  [transit-str]
  (let [in (ByteArrayInputStream. (.getBytes transit-str))
        reader (transit/reader in :json)]
    (transit/read reader)))


;; Public send API
(defn send!
  "Send data to a client via `channel`"
  [channel data]
  (let [username              (get @clients channel)
        valid-event-response? (schema/valid-event-response? data)
        valid-server-event?   (schema/valid-server-event? data)]
    (if (or valid-event-response?
            valid-server-event?)
      (do
        (log/debug "Sending to username:" username
                   ", event-id:" (:event/id data)
                   ", type:" (:event/type data)
                   ", status:" (:event/status data))
        (http/send! channel (->transit data)))
      ;; TODO internal failure mode, collect in reporting
      (log/error "Not sending invalid event to username:" username
                 ", event-id:" (:event/id data)
                 ", type:" (:event/type data)
                 ", invalid schema:"
                 "event-response take:" (str (schema/explain-event-response data))
                 ", server-event take:" (str (schema/explain-server-event data))))))


(defn broadcast!
  "Broadcasts event to all connected clients"
  [{:event/keys [id type] :as event}]
  (log/debug "Broadcasting event-id:" id "type:" type)
  (doseq [client (keys @clients)]
    (send! client event)))


;; Client management API
(defn add-client!
  ([channel]
   (add-client! channel false))
  ([channel username]
   (log/debug "add-client! username:" username)
   (swap! clients assoc channel username)))


(defn get-client-username
  [channel]
  (get @clients channel))


(defn get-clients
  []
  @clients)


(defn get-clients-usernames
  []
  (vals @clients))


(defn remove-client!
  [channel]
  (let [username (get @clients channel)]
    (log/debug "remove-client! username:" username)
    (swap! clients dissoc channel)))
