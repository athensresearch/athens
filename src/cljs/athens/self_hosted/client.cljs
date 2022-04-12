(ns athens.self-hosted.client
  "Self-Hosted Mode connector."
  (:require
    [athens.common-events              :as common-events]
    [athens.common-events.graph.atomic :as atomic-graph-ops]
    [athens.common-events.schema       :as schema]
    [athens.common.logging             :as log]
    [com.stuartsierra.component        :as component]
    [re-frame.core                     :as rf]))


(defonce ^:private ws-connection (atom nil))


(declare open-handler)
(declare message-handler)
(declare close-handler)
(declare forwarded-events)


(defn- connect-to-self-hosted!
  [url]
  (log/info "WSClient Connecting to:" (pr-str url))
  (when url
    (doto (js/WebSocket. url)
      (.addEventListener "open" open-handler)
      (.addEventListener "message" message-handler)
      (.addEventListener "close" close-handler))))


(def ^:private send-queue (atom []))


(def ^:private reconnect-timer (atom nil))
(def ^:private MAX_RECONNECT_TRY 2)
(def ^:private reconnect-counter (atom -1))


(defn- reconnecting?
  "Checks if WebSocket is awaiting reconnection."
  []
  (some? @reconnect-timer))


(defn- delayed-reconnect!
  ([url]
   (delayed-reconnect! url 3000))
  ([url delay-ms]
   (swap! reconnect-counter inc)
   (log/info "WSClient scheduling reconnect in" (pr-str delay-ms) "ms to" (pr-str url))
   (if (< @reconnect-counter MAX_RECONNECT_TRY)
     (let [timer-id (js/setTimeout (fn []
                                     (reset! reconnect-timer nil)
                                     (connect-to-self-hosted! url))
                                   delay-ms)]
       (reset! reconnect-timer timer-id))
     (do
       (log/warn "Reconnect max tries" (pr-str @reconnect-counter) "reached.")
       (rf/dispatch [:remote/connection-failed])))))


(defn- close-reconnect-timer!
  []
  (when-let [timer-id @reconnect-timer]
    (js/clearTimeout timer-id)
    (reset! reconnect-timer nil)
    (reset! reconnect-counter -1)))


(defn open?
  "Checks if `connection` is open.
  If no args version called, `ws-connection` connection is checked.

  To close the connection stop the component."

  ([]
   (open? @ws-connection))

  ([connection]
   (and (not (nil? connection))
        (= (.-OPEN js/WebSocket)
           (.-readyState connection)))))


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
         (log/debug "event-id:" (pr-str (:event/id data))
                    ", type:" (pr-str (:event/type data))
                    "WSClient sending to server")
         (let [serialized-event (common-events/serialize data)
               errors           (common-events/validate-serialized-event serialized-event)]
           (if errors
             (do (log/warn "Tried sending invalid serialized event:" (pr-str errors))
                 {:result :rejected
                  :reason :invalid-event-schema})
             (do (.send connection serialized-event)
                 {:result :sent}))))
       (do
         (log/warn "event-id:" (pr-str (:event/id data))
                   ", type:" (pr-str (:event/type data))
                   "Can't send: WSClient not open")
         (if (reconnecting?)
           (do
             (log/info "event-id:" (pr-str (:event/id data))
                       ", type:" (pr-str (:event/type data))
                       "WSClient already reconnecting, queued.")
             (swap! send-queue (fnil conj []) data)
             {:result :queued
              :reason :client-already-reconnecting})
           (do
             (log/warn "event-id:" (pr-str (:event/id data))
                       ", type:" (pr-str (:event/type data))
                       "WSClient closed & not reconnecting. Reconnecting & queued.")
             (delayed-reconnect! (.-url connection) 0)
             (swap! send-queue (fnil conj []) data)
             {:result :queued
              :reason :client-started-reconnecting}))))
     (let [explanation (schema/explain-event data)]
       (log/warn "event-id:" (pr-str (:event/id data))
                 ", type:" (pr-str (:event/type data))
                 "Client tried to send invalid event. Explanation: " (pr-str explanation))
       {:result :rejected
        :reason :invalid-event-schema}))))


(def ^:private await-open-event-id (atom nil))


(defn- open-handler
  [event]
  (log/info "WSClient Connected")
  (let [connection             (.-target event)
        username               @(rf/subscribe [:username])
        color                  @(rf/subscribe [:color])
        password               @(rf/subscribe [:password])
        session-intro          {:username username
                                :color    color}
        {event-id :event/id
         :as      hello-event} (common-events/build-presence-hello-event session-intro password)]
    (reset! ws-connection connection)
    (reset! reconnect-timer nil)
    (reset! reconnect-counter -1)
    (reset! await-open-event-id event-id)
    (send! connection hello-event)))


(declare remove-listeners!)


(defn- finished-open-handler
  [{:event/keys [status] :as event}]
  (if (= :accepted status)
    (do
      (log/info "Successfully connected to Lan-Party.")
      (reset! await-open-event-id nil)
      (when (seq @send-queue)
        (log/info "WSClient sending queued packets #" (pr-str (count @send-queue)))
        (doseq [data @send-queue]
          (send! @ws-connection data))
        (log/info "WSClient sent queued packets.")
        (reset! send-queue [])))

    (do
      (log/warn "Server rejected login attempt!")

      (remove-listeners! @ws-connection)
      (close-reconnect-timer!)
      (.close @ws-connection)
      (reset! ws-connection nil)

      (rf/dispatch [:remote/connection-failed])
      (rf/dispatch [:alert/js (str "Server rejected your login attempt.\n"
                                   "Your password simply ain't right.\n"
                                   (pr-str event))]))))


(defn- awaited-response-handler
  [{:event/keys [id status] :as packet}]
  (log/debug "event-id:" (pr-str id)
             "WSClient: response status:" (pr-str status))
  ;; is it hello confirmation?
  (if (= @await-open-event-id id)
    (finished-open-handler packet)
    ;; is valid response?
    (if (schema/valid-event-response? packet)
      (do
        (log/debug "event-id:" (pr-str id)
                   "Received valid response.")
        (condp = status
          :accepted
          (let [{:accepted/keys [tx-id]} packet]
            (log/debug "event-id:" (pr-str id) "accepted in tx" tx-id))
          :rejected
          (let [{:reject/keys [reason data]} packet]
            (log/warn "event-id:" (pr-str id)
                      "rejected, reason:" (pr-str reason)
                      ", rejection-data:" (pr-str data))
            (rf/dispatch [:remote/reject-forwarded-event packet]))))
      (let [explanation (schema/explain-event-response packet)]
        (log/warn "Received invalid response:" (pr-str explanation))))))


(defn- db-dump-handler
  [{:keys [datoms]}]
  (log/debug "Received DB Dump")
  (rf/dispatch [:db-dump-handler datoms]))


(defn- presence-session-id-handler
  [{:keys [session-id]}]
  (log/info "Session id:" (pr-str session-id))
  (rf/dispatch [:presence/add-session-id session-id]))


(defn- presence-online-handler
  [args]
  (let [username (:username args)]
    (log/info "User online:" (pr-str username))
    (rf/dispatch [:presence/add-user args])))


(defn- presence-all-online-handler
  "args is a vector of users, e.g. [{:username \"Zeus\"}] "
  [args]
  (rf/dispatch [:presence/all-online args]))


(defn- presence-offline-handler
  [args]
  (let [username (:username args)]
    (log/info "User offine:" (pr-str username))
    (rf/dispatch [:presence/remove-user args])))


(defn- presence-update
  [args]
  (log/debug "User update:" (pr-str args))
  (rf/dispatch [:presence/update args]))


(defn- forwarded-event-handler
  [args]
  (log/debug "Forwarded event-id:" (pr-str (:event/id args)))
  (rf/dispatch [:remote/apply-forwarded-event args]))


(def forwarded-events
  #{:op/atomic})


(defn- server-event-handler
  [{:event/keys [id type args] :as packet}]
  (log/debug "WSClient received from server."
             "event-id:" (pr-str id) ", type:" (pr-str type))
  (if (schema/valid-server-event? packet)

    (condp contains? type
      #{:datascript/db-dump}  (db-dump-handler args)
      #{:presence/session-id} (presence-session-id-handler args)
      #{:presence/online}     (presence-online-handler args)
      #{:presence/all-online} (presence-all-online-handler args)
      #{:presence/offline}    (presence-offline-handler args)
      #{:presence/update}     (presence-update args)
      forwarded-events        (forwarded-event-handler packet))

    (log/warn "event-id:" (pr-str id)
              ", type:" (pr-str type)
              "WSClient Received invalid server event, explanation:" (pr-str (schema/explain-server-event packet)))))


(defn- message-handler
  [event]
  (let [serialized-event (.-data event)
        data             (common-events/deserialize serialized-event)
        errors           (when-not (common-events/ignore-serialized-event-validation? data)
                           (common-events/validate-serialized-event serialized-event))]
    (cond
      errors                              (log/warn "Received invalid serialized event:" (pr-str errors))
      (schema/valid-event-response? data) (awaited-response-handler data)
      :else                               (server-event-handler data))))


(defn- remove-listeners!
  [connection]
  (doto connection
    (.removeEventListener "close" close-handler)
    (.removeEventListener "message" message-handler)
    (.removeEventListener "open" open-handler)))


(defn- close-handler
  [event]
  (log/info "WSClient Disconnected unexpectedly, reconnecting:" (pr-str event))
  (let [connection (.-target event)
        url        (.-url connection)]
    (rf/dispatch [:loading/set])
    (rf/dispatch [:presence/clear])
    (rf/dispatch [:conn-status :reconnecting])
    (remove-listeners! connection)
    (delayed-reconnect! url)))


(defrecord WSClient
  [url]

  component/Lifecycle

  (start
    [component]
    (log/info "WSClient starting with url:" url)
    (let [connection (connect-to-self-hosted! url)]
      (log/debug "WSClient connection started...")
      (reset! ws-connection connection)
      component))


  (stop
    [component]
    (log/info "WSClient stopping for url:" url)
    (when-let [connection @ws-connection]
      (close-reconnect-timer!)
      (remove-listeners! connection)
      (.close connection)
      (log/info "WSClient closed connection")
      (reset! ws-connection nil)
      (rf/dispatch [:conn-status :disconnected])
      component)))


(defn new-ws-client
  [url]
  (map->WSClient {:url url}))


;; REPL Testing
(comment

  (def ws-url "ws://localhost:3010/ws")

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
  (send! {:event/id      "test-id"
          :event/type    :presence/hello
          :event/args    {:username "Bob's your uncle"}})
  ;; => {:result :sent}

  ;; test atomic op
  (send! {:event/id (random-uuid)
          :event/type :op/atomic
          :event/args #:op{:type :block/new,
                           :atomic? true,
                           :args {:parent-uid "test1", :block-uid "test2", :block-order 2}}})

  (send! (common-events/build-atomic-event
          (atomic-graph-ops/make-page-new-op "test title"))))
