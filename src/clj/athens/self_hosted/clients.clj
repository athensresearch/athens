(ns athens.self-hosted.clients
  "Client comms"
  (:require
    [athens.common-events        :as common-events]
    [athens.common-events.schema :as schema]
    [athens.common.logging       :as log]
    [org.httpkit.server          :as http]))


;; Internal state
;; channel -> session info
(defonce clients (atom {}))


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
      (let [type             (common-events/find-event-or-atomic-op-type data)
            status           (:event/status data)
            serialized-event (common-events/serialize data)
            errors           (when-not (common-events/ignore-serialized-event-validation? data)
                               (common-events/validate-serialized-event serialized-event))]
        (if errors
          (log/error "Not sending invalid event to username:" username
                     ", event-id:" (:event/id data)
                     ", type:" (common-events/find-event-or-atomic-op-type data)
                     ", invalid serialized event:"
                     "event-response take:" (str errors))
          (do
            (log/debug "Sending to username:" username
                       ", event-id:" (:event/id data)
                       (if type
                         (str ", type: " type)
                         (str ", status: " status)))
            (http/send! channel serialized-event))))
      ;; TODO internal failure mode, collect in reporting
      (log/error "Not sending invalid event to username:" username
                 ", event-id:" (:event/id data)
                 ", type:" (common-events/find-event-or-atomic-op-type data)
                 ", invalid schema:"
                 "event-response take:" (str (schema/explain-event-response data))
                 ", server-event take:" (str (schema/explain-server-event data))))))


(defn broadcast!
  "Broadcasts event to all connected clients"
  [{:event/keys [id] :as event}]
  (log/debug "Broadcasting event-id:" id "type:" (common-events/find-event-or-atomic-op-type event))
  (doseq [client (keys @clients)]
    (send! client event)))
