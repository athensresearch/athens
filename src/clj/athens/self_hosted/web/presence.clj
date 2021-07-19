(ns athens.self-hosted.web.presence
  (:require
    [athens.common-events       :as common-events]
    [athens.self-hosted.clients :as clients]
    [clojure.tools.logging      :as log]
    [datahike.api               :as d]))


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
    :presence/goodbye})


(defn hello-handler
  [datahike channel {:event/keys [id args _last-tx]}]
  (let [username (:username args)
        max-tx   (:max-tx @datahike)]
    (log/info channel "New Client Intro:" username)
    (clients/add-client! channel username)

    (let [datoms (d/datoms @datahike :eavt) #_(map (fn [{:keys [e a v tx added]}]
                        [e a v tx added])
                      (d/datoms @datahike :eavt))]
      (log/debug channel "Sending" (count datoms) "eavt")
      (clients/send! channel
                     (common-events/build-db-dump-event max-tx datoms))
      (clients/send! channel
                     (common-events/build-presence-all-online-event max-tx
                                                                    (clients/get-clients-usernames))))

    ;; TODO Recipe for diff/patch updating client
    ;; 1. query for tx-ids since `last-tx`
    ;; 2. query for all eavt touples from 1.
    ;; 3. send! to client

    ;; confirm
    (common-events/build-event-accepted id max-tx)))


(defn editing-handler
  [channel {:event/keys [args]}]
  (let [username (clients/get-client-username channel)
        {:keys [block/uid]} args]
    (when uid
      (let [broadcast-presence-editing-event (common-events/build-presence-broadcast-editing-event 42 username uid)]
        (clients/broadcast! broadcast-presence-editing-event)
        #_(dosync
            (let [all-presence* (conj @all-presence presence)
                  total (count all-presence*)]
              ;; NOTE: better way of cleanup, time based maybe? hold presence for 1 minute?
              (if (> total 100)
                (ref-set all-presence (vec (drop (- total 100) all-presence*)))
                (ref-set all-presence all-presence*)))))
      #_(clients/broadcast! (last @all-presence)))))


(defn goodbye-handler
  [channel _event]
  (let [username (clients/get-client-username channel)]
    (prn _event)))


(defn presence-handler
  [datahike channel {:event/keys [type] :as event}]
  (condp = type
    :presence/hello   (hello-handler datahike channel event)
    :presence/editing (editing-handler channel event)
    #_#_:presence/goodbye (goodbye-handler channel event)))
