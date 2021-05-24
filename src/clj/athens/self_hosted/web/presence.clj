(ns athens.self-hosted.web.presence
  (:require [clojure.data.json     :as json]
            [clojure.tools.logging :as log]
            [compojure.core        :as compojure]
            [org.httpkit.server    :as http]))

(defonce clients (atom {}))


(defn- now
  []
  (quot (System/currentTimeMillis) 1000))


(let [max-id (atom 0)]
  (defn next-id
    []
    (swap! max-id inc)))


(defonce all-presence (ref []))


(defn open-handler
  [ch]
  (log/info ch "connected")
  (swap! clients assoc ch true))


(defn receive-handler
  [ch msg]
  (let [ch-username (get @clients ch)]
    (log/debug ch "WS Server <-" ch-username ":" msg)
    (let [data (json/read-json msg)]
      (log/debug "decoded msg:" (pr-str data))

      ;; presence
      (when-let [uid (:editing data)]
        (let [data {:presence {:time     (now)
                               :id       (next-id)
                               :editing  uid
                               :username ch-username}}]
          (dosync
            (let [all-presence* (conj @all-presence data)
                  total         (count all-presence*)]
              ;; NOTE: better way of cleanup, time based maybe? hold presence for 1 minutes?
              (if (> total 100)
                (ref-set all-presence (vec (drop (- total 100) all-presence*)))
                (ref-set all-presence all-presence*)))))
        (doseq [client (keys @clients)]
          (http/send! client (json/json-str (last @all-presence)))))

      ;; hello
      (when-let [username (:username data)]
        (log/info "New Client:" username)
        (swap! clients assoc ch username)))))


(defn close-handler
  [ch status]
  (let [ch-username         (get @clients ch)
        presence-disconnect {:presence {:username   ch-username
                                        :disconnect true}}]
    (swap! clients dissoc ch)
    (log/info ch ch-username "closed, status" status)
    (when ch-username
      (doseq [client (keys @clients)]
        (http/send! client (json/json-str presence-disconnect))))))


(defn presence-handler
  [req]
  (http/as-channel req
                   {:on-open    #'open-handler
                    :on-receive #'receive-handler
                    :on-close   #'close-handler}))

(compojure/defroutes presence-routes
  (compojure/GET "/ws-presence" [] presence-handler))
