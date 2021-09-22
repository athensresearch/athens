(ns athens.self-hosted.web.presence
  (:require
    [athens.common-events       :as common-events]
    [athens.self-hosted.clients :as clients]
    [clojure.set                :as set]
    [clojure.string             :as str]
    [clojure.tools.logging      :as log]
    [datahike.api               :as d]))


(let [max-id (atom 0)]
  (defn next-id
    []
    (swap! max-id inc)))


(defonce all-presence (atom {}))


(def supported-event-types
  #{:presence/hello
    :presence/editing
    :presence/rename
    :presence/goodbye})


(defn- valid-password
  [datahike channel id {:keys [username]}]
  (let [max-tx (:max-tx @datahike)]
    (log/info channel "New Client Intro:" username)
    (clients/add-client! channel username)

    (let [datoms (remove #(= :dx/dbInstant (:a %))
                         (d/datoms @datahike :eavt))]
      (log/debug channel "Sending" (count datoms) "eavt")
      (clients/send! channel
                     (common-events/build-db-dump-event max-tx datoms))
      (clients/send! channel
                     (common-events/build-presence-all-online-event max-tx
                                                                    (clients/get-clients-usernames)))
      (doseq [{:keys [username block-uid]} (vals @all-presence)]
        (let [broadcast-presence-editing-event
              (common-events/build-presence-broadcast-editing-event max-tx username block-uid)]
          (clients/broadcast! broadcast-presence-editing-event)))
      (clients/broadcast! (common-events/build-presence-online-event max-tx
                                                                     username)))

    ;; TODO Recipe for diff/patch updating client
    ;; 1. query for tx-ids since `last-tx`
    ;; 2. query for all eavt touples from 1.
    ;; 3. send! to client

    ;; confirm
    (common-events/build-event-accepted id max-tx)))


(defn- invalid-password
  [channel id {:keys [username]}]
  (log/warn channel "Invalid password in hello for username:" username)
  (common-events/build-event-rejected id
                                      "You shall not pass"
                                      {:password-error :invalid-password}))


(defn hello-handler
  [datahike server-password channel {:event/keys [id args _last-tx]}]
  (let [{:keys [password]} args]
    (if (or (str/blank? server-password)
            (= server-password password))
      (valid-password datahike channel id args)
      (invalid-password channel id args))))


(defn editing-handler
  [datahike channel {:event/keys [id args]}]
  (let [username            (clients/get-client-username channel)
        {:keys [block-uid]} args
        max-tx              (:max-tx @datahike)]
    (when block-uid
      (let [broadcast-presence-editing-event (common-events/build-presence-broadcast-editing-event max-tx username block-uid)]
        (swap! all-presence assoc username {:username username
                                            :block/uid block-uid})
        (clients/broadcast! broadcast-presence-editing-event)
        (common-events/build-event-accepted id max-tx)))))


(defn rename-handler
  [datahike channel {:event/keys [id args]}]
  (let [{:keys
         [current-username
          new-username]}         args
        max-tx                   (:max-tx @datahike)
        broadcast-rename-event (common-events/build-presence-broadcast-rename-event max-tx
                                                                                    current-username
                                                                                    new-username)]

    (swap! all-presence (fn [all]
                          (-> all
                              (update-in [:presence :users] set/rename-keys {current-username new-username})
                              (update-in [:presence :users new-username] assoc :username new-username))))
    (clients/add-client! channel new-username)
    (clients/broadcast! broadcast-rename-event)
    (common-events/build-event-accepted id max-tx)))


(defn goodbye-handler
  [channel]
  (let [username (clients/get-client-username channel)
        ;; TODO: max-tx shouldn't be 42
        presence-offline-event (athens.common-events/build-presence-offline-event 42 username)]
    (when username
      (swap! all-presence dissoc username)
      (clients/broadcast! presence-offline-event))))


(defn presence-handler
  [datahike server-password channel {:event/keys [type] :as event}]
  (condp = type
    :presence/hello   (hello-handler datahike server-password channel event)
    :presence/editing (editing-handler datahike channel event)
    :presence/rename (rename-handler datahike channel event)
    #_#_:presence/goodbye (goodbye-handler channel event)))
