(ns athens.self-hosted.web.presence
  (:require
    [athens.common-events       :as common-events]
    [athens.self-hosted.clients :as clients]
    [clojure.tools.logging      :as log]))


(defn- now
  []
  (quot (System/currentTimeMillis) 1000))


(let [max-id (atom 0)]
  (defn next-id
    []
    (swap! max-id inc)))


(defonce all-presence (ref []))


(def supported-event-types
  #{:presence/hello
    :presence/editing
    :presence/viewing})


(defn hello-handler
  [datahike channel {:event/keys [id args last-tx]}]
  (let [username (:username args)
        max-tx   (-> datahike
                     :conn
                     deref
                     :max-tx)]
    (log/info channel "New Client Intro:" username)
    (clients/add-client! channel username)
    (clients/broadcast! (common-events/build-presence-online username max-tx))

    ;; TODO send client updated entities
    ;; 1. query for tx-ids since `last-tx`
    ;; 2. query for all eavt touples from 1.
    ;; 3. send! to client

    ;; confirm
    (common-events/build-event-accepted id -1)))


(defn editing-handler
  [channel {:event/keys [args]}]
  (let [username (clients/get-client-username channel)]
    (when-let [uid (:editing args)]
      (let [presence {:presence {:time     (now)
                                 :id       (next-id)
                                 :editing  uid
                                 :username username}}]
        (dosync
          (let [all-presence* (conj @all-presence presence)
                total         (count all-presence*)]
            ;; NOTE: better way of cleanup, time based maybe? hold presence for 1 minute?
            (if (> total 100)
              (ref-set all-presence (vec (drop (- total 100) all-presence*)))
              (ref-set all-presence all-presence*)))))
      (clients/broadcast! (last @all-presence)))))


(defn viewing-handler
  [_channel _event]
  ;; TODO new viewing presence
  )


(defn presence-handler
  [datahike channel {:event/keys [type] :as event}]
  (condp = type
    :presence/hello   (hello-handler datahike channel event)
    :presence/editing (editing-handler channel event)
    :presence/viewing (viewing-handler channel event)))
