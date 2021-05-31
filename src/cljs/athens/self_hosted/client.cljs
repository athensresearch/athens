(ns athens.self-hosted.client
  "Self-Hosted Mode connector."
  (:require
    [athens.common-events.schema :as schema]
    [com.stuartsierra.component  :as component]
    [re-frame.core               :as rf]))


;; TODO: make configurable
(def ws-url "ws://localhost:3010/ws")


(defonce ^:private ws-connection (atom nil))


(declare open-handler)
(declare message-handler)
(declare close-handler)


(defn- connect-to-self-hosted!
  [url]
  (js/console.log "WS Client Connecting to:" url)
  (when url
    (doto (js/WebSocket. url)
      (.addEventListener "open" open-handler)
      (.addEventListener "message" message-handler)
      (.addEventListener "close" close-handler))))


(def ^:private send-queue (atom []))


(def ^:private reconnect-timer (atom nil))


(defn- reconnecting?
  "Checks if WebSocket is awaiting reconnection."
  []
  (some? @reconnect-timer))


(defn- delayed-reconnect!
  ([url]
   (delayed-reconnect! url 3000))
  ([url delay-ms]
   (js/console.log "WSClient scheduling reconnect in" delay-ms "ms to" url)
   (let [timer-id (js/setTimeout (fn []
                                   (reset! reconnect-timer nil)
                                   (connect-to-self-hosted! url))
                                 delay-ms)]
     (reset! reconnect-timer timer-id))))


(defn- close-reconnect-timer!
  []
  (when-let [timer-id @reconnect-timer]
    (js/clearTimeout timer-id)))


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
   (if (schema/valid-event? data)
     (if (open? connection)
       (do
         (js/console.debug "WSClient sending to server:" (pr-str data))
         (.send connection
                (-> data
                    clj->js
                    js/JSON.stringify))
         {:result :sent})
       (do
         (js/console.warn "WSClient not open")
         (if (reconnecting?)
           (do
             (js/console.info "WSClient already reconnecting, queued.")
             (swap! send-queue (fnil conj []) data)
             {:result :queued
              :reason :client-already-reconnecting})
           (do
             (js/console.warn "WSClient closed & not reconnecting. Reconnecting & queued.")
             (delayed-reconnect! (.-url connection) 0)
             (swap! send-queue (fnil conj []) data)
             {:result :queued
              :reason :client-started-reconnecting}))))
     (let [explanation (schema/explain data)]
       (js/console.warn "Tried to send invalid event. Explanation: " (pr-str explanation))
       {:result :rejected
        :reason :invalid-event-schema}))))


(defn- open-handler
  [event]
  (js/console.log "WSClient Connected:" event)
  (let [connection (.-target event)]
    (reset! ws-connection connection)
    (send! connection
           {:event/id      (str (gensym))
            :event/last-tx "0" ; TODO: discover last tx
            :event/type    :presence/hello
            :event/args    {:username (:name @(rf/subscribe [:user]))}})
    (when (seq @send-queue)
      (js/console.log "WSClient sending queued packets #" (count @send-queue))
      (doseq [data @send-queue]
        (send! connection data))
      (reset! send-queue []))))


(defn- message-handler
  [event]
  (let [data (-> event
                 .-data
                 js/JSON.parse
                 (js->clj :keywordize-keys true))]
    (js/console.warn "TODO: WSClient Received:" (pr-str data))
    ;; TODO implement message handler for client
    ))


(defn- remove-listeners!
  [connection]
  (doto connection
    (.removeEventListener "close" close-handler)
    (.removeEventListener "message" message-handler)
    (.removeEventListener "open" open-handler)))


(defn- close-handler
  [event]
  (js/console.log "WSClient Disconnected:" event)
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


;; REPL Testing
(comment

  ;; define a client
  (def client (new-ws-client ws-url))

  ;; start a client
  (component/start client)

  ;; check if open?
  (open?)

  ;; try to send an invalid message
  (send! {:some :message})
  ;; => {:result :rejected, :reason :invalid-event-schema}

  ;; send a `:presence/hello` event
  (send! {:event/id "test-id"
          :event/last-tx "0"
          :event/type :presence/hello
          :event/args {:username "Bob's your uncle"}})
  ;; => {:result :sent}
  )
