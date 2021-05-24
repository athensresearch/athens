(ns athens.self-hosted.web.graph
  (:require [clojure.data.json     :as json]
            [clojure.tools.logging :as log]
            [compojure.core        :as compojure]
            [datahike.api          :as d]
            [org.httpkit.server    :as http]))

(defonce clients (atom {}))

(defn open-handler [channel]
  (log/info channel "connected")
  (swap! clients assoc channel true))

(defn receive-handler [channel message]
  (log/info channel "Received message: " message)
  ;; TODO add meat and potatoes
  )

(defn close-handler [channel status]
  (log/info channel " closed, status " status)
  ;; TODO add meat and potatoes
  )

(defn graph-handler [req]
  (http/as-channel req
                   {:on-open    #'open-handler
                    :on-receive #'receive-handler
                    :on-close   #'close-handler}))

(compojure/defroutes graph-routes
  (compojure/GET "/ws-graph" [] graph-handler))
