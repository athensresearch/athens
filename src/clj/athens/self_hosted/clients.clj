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
;; channel -> session info
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


;; Client management API

(defn get-client-session
  [channel]
  (get @clients channel))


(defn get-client-sessions
  []
  (vals @clients))


(defn get-client-username
  [channel]
  (or (:username (get-client-session channel))
      "<unknown>"))


(defn add-client!
  [channel session]
  (log/debug "add-client! username:" (:username session))
  (swap! clients assoc channel session))


(defn remove-client!
  [channel]
  (log/debug "remove-client! username:" (get-client-username channel))
  (swap! clients dissoc channel))


;; Public send API
(defn send!
  "Send data to a client via `channel`"
  [channel data]
  (let [username              (get-client-username channel)
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
