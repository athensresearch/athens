(ns athens.common-events
  "Event as Verbs executed on Knowledge Graph"
  (:require
    [athens.common.utils :as utils]
    [cognitect.transit :as transit]
    #?(:cljs [com.cognitect.transit.types :as ty]))
  #?(:clj
     (:import
       (java.io
         ByteArrayInputStream
         ByteArrayOutputStream))))


;; Limits

;; Fluree default max size over websocket is ~2mb.
;; There doesn't seem to be a max for nginx
;; https://serverfault.com/questions/1034906/can-nginx-limit-incoming-websocket-message-size
;; Was able to transmit 500mb over websocket from the server to client.
;; Let's settle on a nice sensible 1MB limit for now.
(def max-event-size-in-bytes (* 1 1000 1000))


(defn valid-serialized-event?
  [serialized-event]
  (< (count serialized-event) max-event-size-in-bytes))


(defn validate-serialized-event
  [serialized-event]
  (when-not (valid-serialized-event? serialized-event)
    (ex-info "Serialized event is larger than 1 MB" {})))


(defn ignore-serialized-event-validation?
  [event]
  (-> event :event/type
      ;; db-dump is sending the whole database and can (easily) go over max-event-size-in-bytes.
      ;; Only real solution for this is to break down the db-dump into smaller pieces,
      ;; possibly transitioning to partial loading by default in the future.
      #{:datascript/db-dump}))


;; serialization and limits

;; Really shouldn't need these UUID and datom-reader, but we still send datoms via db-dump.
#?(:cljs
   ;; see https://github.com/cognitect/transit-cljs/issues/41#issuecomment-503287258
   (extend-type ty/UUID IUUID))


(def ^:private datom-reader
  (transit/read-handler
    (fn [[e a v tx added]]
      {:e     e
       :a     a
       :v     v
       :tx    tx
       :added added})))


(def serialization-type :json)
(def serialization-opts {:handlers {:datom datom-reader}})


(defn serialize
  [event]
  #?(:cljs (-> (transit/writer serialization-type)
               (transit/write event))
     :clj  (let [out    (ByteArrayOutputStream. 4096)
                 writer (transit/writer out serialization-type)]
             (transit/write writer event)
             (.toString out))))


(defn deserialize
  [serialized-event]
  #?(:cljs (-> (transit/reader serialization-type serialization-opts)
               (transit/read serialized-event))
     :clj  (let [in     (ByteArrayInputStream. (.getBytes serialized-event))
                 reader (transit/reader in serialization-type serialization-opts)]
             (transit/read reader))))


;; building events

;; - confirmation events

(defn build-event-accepted
  "Builds ACK Event Response accepting this event."
  [id]
  {:event/id     id
   :event/status :accepted})


(defn build-event-rejected
  "Builds Rejection Event Response with `:reject/reason & :reject/data`."
  [id message data]
  {:event/id      id
   :event/status  :rejected
   :reject/reason message
   :reject/data   data})


;; - datascript events

(defn build-db-dump-event
  "Builds `:datascript/db-dump` events with `datoms`."
  [datoms]
  (let [event-id (utils/gen-event-id)]
    {:event/id   event-id
     :event/type :datascript/db-dump
     :event/args {:datoms datoms}}))


;; - presence events

(defn build-presence-hello-event
  "Builds `:presence/hello` event with `session-intro` and `password` (optional)."
  ([session-intro]
   (build-presence-hello-event session-intro nil))
  ([session-intro password]
   (let [event-id (utils/gen-event-id)]
     {:event/id   event-id
      :event/type :presence/hello
      :event/args (cond-> {:session-intro session-intro}
                    password (merge {:password password}))})))


(defn build-presence-session-id-event
  "Builds `:presence/session-id` event with `session-id` for the client."
  [session-id]
  (let [event-id (utils/gen-event-id)]
    {:event/id   event-id
     :event/type :presence/session-id
     :event/args {:session-id session-id}}))


(defn build-presence-online-event
  "Builds `:presence/online` event with `session` that went online."
  [session]
  (let [event-id (utils/gen-event-id)]
    {:event/id   event-id
     :event/type :presence/online
     :event/args session}))


(defn build-presence-all-online-event
  "Builds `:presence/all-online` event with all active users."
  [sessions]
  (let [event-id (utils/gen-event-id)]
    {:event/id   event-id
     :event/type :presence/all-online
     :event/args (vec sessions)}))


(defn build-presence-offline-event
  [session]
  (let [event (build-presence-online-event session)]
    (assoc event :event/type :presence/offline)))


(defn build-presence-update-event
  "Builds `:presence/update` event with `session-id` and map of session props to update."
  [session-id updates]
  (let [event-id (utils/gen-event-id)]
    {:event/id   event-id
     :event/type :presence/update
     :event/args (merge {:session-id session-id}
                        updates)}))


(defn build-atomic-event
  "Builds atomic graph operation"
  [atomic-op]
  (let [event-id (utils/gen-event-id)]
    {:event/id   event-id
     :event/type :op/atomic
     :event/op   atomic-op}))


(defn find-event-or-atomic-op-type
  "Finds `:event/type` or type of atomic op"
  [event-or-op]
  (let [event? (and (contains? event-or-op :event/type)
                    (not= :op/atomic (:event/type event-or-op)))]
    (if event?
      (:event/type event-or-op)
      (let [op      (or (:event/op event-or-op)
                        event-or-op)
            atomic? (:op/atomic? op)]
        (if atomic?
          (:op/type op)
          (let [trigger (:op/trigger op)]
            (or (:op/type trigger)
                trigger)))))))
