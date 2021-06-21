(ns athens.self-hosted.clients
  "Client comms"
  (:require
    [clojure.tools.logging :as log]
    [cognitect.transit                 :as transit]
    [org.httpkit.server                :as http])
  (:import
    (datahike.datom
      Datom)
    (java.io
      ByteArrayInputStream
      ByteArrayOutputStream)))


;; Internal state
(defonce clients (atom {}))


;; Transit reader/writer
(def ^:private datom-writer
  (transit/write-handler
    "datom"
    (fn [{:keys [e a v tx added]}]
      [e a v tx added])))


(defn ->transit
  [data]
  (let [out    (ByteArrayOutputStream. 4096)
        writer (transit/writer out :json {:handlers {Datom datom-writer}})]
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
  (log/debug "->" (get @clients channel) ", data:" (pr-str data))
  (http/send! channel (->transit data)))


(defn broadcast!
  "Broadcasts event to all connected clients"
  [event]
  (log/debug "Broadcasting:" (pr-str event))
  (doseq [client (keys @clients)]
    (send! client event)))


;; Client management API
(defn add-client!
  ([channel]
   (add-client! channel true))
  ([channel username]
   (log/debug channel "add-client!" username)
   (swap! clients assoc channel username)))


(defn get-client-username
  [channel]
  (let [username (get @clients channel)]
    (log/debug channel "get-client-username" username)
    username))

(defn get-clients
  []
  @clients)


(defn remove-client!
  [channel]
  (let [username (get @clients channel)]
    (log/debug channel "remeove-client!" username)
    (swap! clients dissoc channel)))
