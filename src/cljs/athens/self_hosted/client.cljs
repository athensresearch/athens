(ns athens.self-hosted.client
  "Self-Hosted Mode connector."
  (:require
    [athens.common-events.schema :as schema]
    [com.stuartsierra.component  :as component]
    [re-frame.core               :as rf]))


;; TODO: make configurable
(def ws-url "ws://localhost:3010/ws")


(defonce ^:private ws-connection (atom nil))


(defn open?
  "Checks if `connection` is open.
  If no args version called, `ws-connection` connection is checked.

  To close the connection stop the component."

  ([]
   (open? @ws-connection))

  ([connection]
   (= (.-OPEN js/WebSocket)
      (.-readyState connection))))


(defn send!
  "Sends data over open WebSocket.
  1st argument `connection` is optional, default is `ws-connection`.
  `data` is expected to be JSON serializable structure."

  ([data]
   (send! @ws-connection data))

  ([connection data]
   (when (open? connection)
     (if (schema/valid-event? data)
       (.send connection
              (-> data
                  clj->js
                  js/JSON.stringify))
       (let [explanation (schema/explain data)]
         (js/console.warn "Tried to send invalid event. Explanation: " (pr-str explanation)))))))


(defn- open-handler
  [event]
  (js/console.log "WS Client Connected:" event)
  (send! (.-target event)
         {:event/type :presence/hello
          :event/args {:username @(rf/subscribe [:user])}}))


(defn- message-handler
  [event]
  (let [data (-> event
                 .-data
                 js/JSON.parse
                 (js->clj :keywordize-keys true))]
    (js/console.warn "TODO: WSClient Received:" (pr-str data))
    ;; TODO implement message handler for client
    ))


(declare close-handler)


(defn- connect-to-self-hosted!
  [url]
  (js/console.log "WS Client Connecting to:" url)
  (when url
    (doto (js/WebSocket. url)
      (.addEventListener "open" open-handler)
      (.addEventListener "message" message-handler)
      (.addEventListener "close" close-handler))))


(def ^:private reconnect-timer (atom nil))


(defn- delayed-reconnect!
  [url]
  (js/console.log "WSClient scheduling reconnect in 3s to" url)
  (let [timer-id (js/setTimeout (fn []
                                  (reset! reconnect-timer nil)
                                  (connect-to-self-hosted! url))
                                3000)]
    (reset! reconnect-timer timer-id)))


(defn- close-reconnect-timer!
  []
  (when-let [timer-id @reconnect-timer]
    (js/clearTimeout timer-id)))


(defn- remove-listeners!
  [connection]
  (doto connection
    (.removeEventListener "close" close-handler)
    (.removeEventListener "message" message-handler)
    (.removeEventListener "open" open-handler)))


(defn- close-handler
  [event]
  (js/console.log "WS Client Disconnected:" event)
  (let [connection (.-target event)
        url        (.-url connection)]
    (remove-listeners! connection)
    (delayed-reconnect! url)))


(defrecord WSClient
  [url]

  component/Lifecycle

  (start
    [component]
    (js/console.log "WSClient starting with url:" url)
    (let [connection (connect-to-self-hosted! url)]
      (js/console.debug "WSClient connection started...")
      (reset! ws-connection connection)
      component))


  (stop
    [component]
    (js/console.log "WSClient stopping for url:" url)
    (when-let [connection @ws-connection]
      (close-reconnect-timer!)
      (remove-listeners! connection)
      (.close connection)
      (js/console.debug "WSClient closed connection")
      (reset! ws-connection nil)
      component)))


(defn new-ws-client
  [url]
  (map->WSClient {:url url}))
