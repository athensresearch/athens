(ns athens.self-hosted.web.presence
  (:require
    [athens.common-events       :as common-events]
    [athens.common.logging      :as log]
    [athens.self-hosted.clients :as clients]
    [clojure.string             :as str]
    [datascript.core            :as d]))


(def supported-event-types
  #{:presence/hello
    :presence/update})


(defn- valid-password
  [conn channel id {:keys [session-intro]}]
  (let [username   (:username session-intro)
        session-id (str (random-uuid))
        session    (assoc session-intro :session-id session-id)]
    (log/info "New Client Intro:" session-intro)
    (clients/add-client! channel session)
    (clients/send! channel (common-events/build-presence-session-id-event session-id))
    (let [datoms (map ; Convert Datoms to just vectors.
                  (comp vec seq)
                  (d/datoms @conn :eavt))]
      (log/debug "Sending" (count datoms) "eavt to" (pr-str username))
      (clients/send! channel
                     (common-events/build-db-dump-event datoms)))
    (clients/send! channel
                   (common-events/build-presence-all-online-event (clients/get-client-sessions)))
    (clients/broadcast! (common-events/build-presence-online-event session))

    ;; confirm
    (common-events/build-event-accepted id)))


(defn- invalid-password
  [channel id {:keys [username]}]
  (log/warn channel "Invalid password in hello for username:" username)
  (common-events/build-event-rejected id
                                      "You shall not pass"
                                      {:password-error :invalid-password}))


(defn hello-handler
  [conn server-password channel {:event/keys [id args]}]
  (let [{:keys [password]} args]
    (if (or (str/blank? server-password)
            (= server-password password))
      (valid-password conn channel id args)
      (invalid-password channel id args))))


(defn update-handler
  [channel {:event/keys [id args]}]
  (let [{:keys [session-id]}  (clients/get-client-session channel)
        ;; Always build a new event with the session-id for this channel.
        ;; If the client sends a incorrect/spoofed session-id, it will be ignored.
        presence-update-event (common-events/build-presence-update-event session-id args)]
    (swap! clients/clients update channel merge args)
    (clients/broadcast! presence-update-event)
    (common-events/build-event-accepted id)))


(defn goodbye-handler
  [session]
  (let [presence-offline-event (athens.common-events/build-presence-offline-event session)]
    (clients/broadcast! presence-offline-event)))


(defn presence-handler
  [conn server-password channel {:event/keys [type] :as event}]
  (condp = type
    :presence/hello  (hello-handler conn server-password channel event)
    ;; presence/goodbye is called on client close.
    :presence/update (update-handler channel event)))
