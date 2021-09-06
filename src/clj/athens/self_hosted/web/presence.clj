(ns athens.self-hosted.web.presence
  (:require
    [athens.common-events       :as common-events]
    [athens.self-hosted.clients :as clients]
    [clojure.tools.logging      :as log]
    [datahike.api               :as d]))


(let [max-id (atom 0)]
  (defn next-id
    []
    (swap! max-id inc)))


(defonce all-presence (ref []))


(def supported-event-types
  #{:presence/hello
    :presence/editing
    :presence/rename
    :presence/goodbye})


(defn hello-handler
  [datahike channel {:event/keys [id args _last-tx]}]
  (let [username (:username args)
        max-tx   (:max-tx @datahike)]
    (log/info channel "New Client Intro:" username)
    (clients/add-client! channel username)

    (let [datoms (d/datoms @datahike :eavt)]
      (log/debug channel "Sending" (count datoms) "eavt")
      (clients/send! channel
                     (common-events/build-db-dump-event max-tx datoms))
      (clients/send! channel
                     (common-events/build-presence-all-online-event max-tx
                                                                    (clients/get-clients-usernames)))
      (clients/broadcast! (common-events/build-presence-online-event max-tx
                                                                     username)))

    ;; TODO Recipe for diff/patch updating client
    ;; 1. query for tx-ids since `last-tx`
    ;; 2. query for all eavt touples from 1.
    ;; 3. send! to client

    ;; confirm
    (common-events/build-event-accepted id max-tx)))


(defn editing-handler
  [datahike channel {:event/keys [id args]}]
  (let [username            (clients/get-client-username channel)
        {:keys [block-uid]} args
        max-tx              (:max-tx @datahike)]
    (when block-uid
      (let [broadcast-presence-editing-event (common-events/build-presence-broadcast-editing-event max-tx username block-uid)]
        (clients/broadcast! broadcast-presence-editing-event)
        (common-events/build-event-accepted id max-tx)))))


(defn rename-handler
  [datahike channel {:event/keys [id args]}]
  (let [{:keys
         [current-username
          new-username]}         args
        max-tx                   (:max-tx @datahike)
        broadcast-username-event (common-events/build-presence-broadcast-rename-event max-tx
                                                                                      current-username
                                                                                      new-username)]

    (clients/add-client! channel new-username)
    (clients/broadcast! broadcast-username-event)
    (common-events/build-event-accepted id max-tx)))


(defn goodbye-handler
  [channel _event]
  (let [_username (clients/get-client-username channel)]
    (prn _event)))


(defn presence-handler
  [datahike channel {:event/keys [type] :as event}]
  (condp = type
    :presence/hello   (hello-handler datahike channel event)
    :presence/editing (editing-handler datahike channel event)
    :presence/rename (rename-handler datahike channel event)
    #_#_:presence/goodbye (goodbye-handler channel event)))
