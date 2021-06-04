(ns athens.self-hosted.web.presence
  (:require
    [athens.common-events  :as common-events]
    [clojure.data.json     :as json]
    [clojure.tools.logging :as log]
    [org.httpkit.server    :as http]))


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
  [clients ch {:event/keys [id args last-tx]}]
  (let [username (:username args)]
    (log/info ch "New Client Intro:" username)
    (swap! clients assoc ch username)
    ;; TODO broadcast new presence

    ;; TODO send client updated entities

    ;; confirm
    (common-events/build-event-accepted id -1)))


(defn editing-handler
  [clients ch {:event/keys [args]}]
  (let [username (get clients ch)]
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
      (doseq [client (keys @clients)]
        (http/send! client (json/json-str (last @all-presence)))))))


(defn viewing-handler
  [_clients _ch _event]
  ;; TODO new viewing presence
  )


(defn presence-handler
  [clients ch {:event/keys [type] :as event}]
  (condp = type
    :presence/hello   (hello-handler clients ch event)
    :presence/editing (editing-handler clients ch event)
    :presence/viewing (viewing-handler clients ch event)))
