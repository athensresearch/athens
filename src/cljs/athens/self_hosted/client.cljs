(ns athens.self-hosted.client
  "Self-Hosted Mode connector."
  (:require
    [athens.common-db                  :as common-db]
    [athens.common-events              :as common-events]
    [athens.common-events.graph.atomic :as atomic-graph-ops]
    [athens.common-events.schema       :as schema]
    [athens.common.logging             :as log]
    [cognitect.transit                 :as transit]
    [com.cognitect.transit.types       :as ty]
    [com.stuartsierra.component        :as component]
    [datascript.core                   :as d]
    [re-frame.core                     :as rf]))


(extend-type ty/UUID IUUID)


(defonce ^:private ws-connection (atom nil))


(declare open-handler)
(declare message-handler)
(declare close-handler)
(declare forwarded-events)


(defn- connect-to-self-hosted!
  [url]
  (log/info "WSClient Connecting to:" url)
  (when url
    (doto (js/WebSocket. url)
      (.addEventListener "open" open-handler)
      (.addEventListener "message" message-handler)
      (.addEventListener "close" close-handler))))


(def ^:private send-queue (atom []))


(def ^:private reconnect-timer (atom nil))
(def ^:private MAX_RECONNECT_TRY 2)
(def ^:private reconnect-counter (atom -1))


(defn- await-response!
  [{:event/keys [id]}]
  (log/debug "event-id:" (pr-str id) "WSClient awaiting response:"))


(defn- reconnecting?
  "Checks if WebSocket is awaiting reconnection."
  []
  (some? @reconnect-timer))


(defn- delayed-reconnect!
  ([url]
   (delayed-reconnect! url 3000))
  ([url delay-ms]
   (swap! reconnect-counter inc)
   (log/info "WSClient scheduling reconnect in" delay-ms "ms to" url)
   (if (< @reconnect-counter MAX_RECONNECT_TRY)
     (let [timer-id (js/setTimeout (fn []
                                     (reset! reconnect-timer nil)
                                     (connect-to-self-hosted! url))
                                   delay-ms)]
       (reset! reconnect-timer timer-id))
     (do
       (log/warn "Reconnect max tries" @reconnect-counter)
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
         (await-response! data)
         (.send connection (transit/write (transit/writer :json) data))
         {:result :sent})
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
  (log/info "WSClient Connected:" event)
  (let [connection             (.-target event)
        last-tx                @(rf/subscribe [:remote/last-seen-tx])
        username               @(rf/subscribe [:username])
        password               @(rf/subscribe [:password])
        {event-id :event/id
         :as      hello-event} (common-events/build-presence-hello-event last-tx
                                                                         username
                                                                         password)]
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
        (log/info "WSClient sending queued packets #" (count @send-queue))
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
  (log/info "event-id:" (pr-str id)
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
            (log/info "event-id:" id "accepted in tx" tx-id))
          :rejected
          (let [{:reject/keys [reason data]} packet]
            (log/warn "event-id:" (pr-str id)
                      "rejected, reason:" reason
                      ", rejection-data:" (pr-str data))
            (rf/dispatch [:remote/reject-forwarded-event packet]))))
      (let [explanation (schema/explain-event-response packet)]
        (log/warn "Received invalid response:" (pr-str explanation))))))


(defn datom->tx-entry
  [[e a v]]
  [:db/add e a v])


(defn- db-dump-handler
  [last-tx {:keys [datoms]}]
  (log/debug "Received DB Dump")
  (rf/dispatch [:reset-conn (d/empty-db common-db/schema)])
  (rf/dispatch [:transact (into [] (map datom->tx-entry) datoms)])
  (rf/dispatch [:remote/snapshot-dsdb])
  (rf/dispatch [:remote/start-event-sync])
  (rf/dispatch [:remote/last-seen-tx! last-tx])
  (rf/dispatch [:db/sync])
  (rf/dispatch [:remote/connected])
  (log/info "✅ Transacted DB dump. last-seen-tx" last-tx))


(defn- presence-online-handler
  [args]
  (let [username (:username args)]
    (log/info "User online:" username)
    (rf/dispatch [:presence/add-user args])))


(defn- presence-all-online-handler
  "args is a vector of users, e.g. [{:username \"Zeus\"}] "
  [args]
  (rf/dispatch [:presence/all-online args]))


(defn- presence-offline-handler
  [args]
  (let [username (:username args)]
    (log/info "User offine:" username)
    (rf/dispatch [:presence/remove-user args])))


(defn- presence-receive-editing
  [args]
  (log/info "User editing:" (pr-str args))
  (rf/dispatch [:presence/update-editing args]))


(defn- presence-receive-rename
  [args]
  (log/info "User rename:" (pr-str args))
  (rf/dispatch [:presence/update-rename args]))


(defn- forwarded-event-handler
  [args]
  (log/info "Forwarded event:" (pr-str args))
  (rf/dispatch [:remote/apply-forwarded-event args]))


(def forwarded-events
  #{:datascript/rename-page
    :datascript/merge-page
    :datascript/delete-page
    :datascript/block-save
    :datascript/new-block
    :datascript/add-child
    :datascript/open-block-add-child
    :datascript/split-block
    :datascript/split-block-to-children
    :datascript/unindent
    :datascript/indent
    :datascript/indent-multi
    :datascript/unindent-multi
    :datascript/page-add-shortcut
    :datascript/page-remove-shortcut
    :datascript/drop-child
    :datascript/drop-multi-child
    :datascript/drop-link-child
    :datascript/drop-diff-parent
    :datascript/drop-multi-diff-source-same-parents
    :datascript/drop-multi-diff-source-diff-parents
    :datascript/drop-link-diff-parent
    :datascript/drop-same
    :datascript/drop-multi-same-source
    :datascript/drop-multi-same-all
    :datascript/drop-link-same-parent
    :datascript/left-sidebar-drop-above
    :datascript/left-sidebar-drop-below
    :datascript/unlinked-references-link
    :datascript/unlinked-references-link-all
    :datascript/selected-delete
    :datascript/block-open
    :datascript/paste
    :datascript/paste-verbatim
    :datascript/delete-only-child
    :datascript/delete-merge-block
    :datascript/bump-up
    :datascript/paste-internal

    :op/atomic})


(defn- server-event-handler
  [{:event/keys [id last-tx type args] :as packet}]
  (log/debug "event-id:" (pr-str id)
             ", type:" type
             "WSClient received from server")
  (if (schema/valid-server-event? packet)

    (condp contains? type
      #{:datascript/db-dump} (db-dump-handler last-tx args)
      #{:presence/online} (presence-online-handler args)
      #{:presence/all-online} (presence-all-online-handler args)
      #{:presence/offline} (presence-offline-handler args)
      #{:presence/broadcast-editing} (presence-receive-editing args)
      #{:presence/broadcast-rename} (presence-receive-rename args)
      forwarded-events (forwarded-event-handler packet))

    (log/warn "event-id:" (pr-str id)
              ", type:" type
              "WSClient Received invalid server event, explanation:" (pr-str (schema/explain-server-event packet)))))


(def ^:private datom-reader
  (transit/read-handler
    (fn [[e a v tx added]]
      {:e     e
       :a     a
       :v     v
       :tx    tx
       :added added})))


(defn- message-handler
  [event]
  (let [packet (->> event
                    .-data
                    (transit/read
                      (transit/reader
                        :json
                        {:handlers
                         {:datom datom-reader}})))]

    (if (schema/valid-event-response? packet)
      (awaited-response-handler packet)
      (server-event-handler packet))))


(defn- remove-listeners!
  [connection]
  (doto connection
    (.removeEventListener "close" close-handler)
    (.removeEventListener "message" message-handler)
    (.removeEventListener "open" open-handler)))


(defn- close-handler
  [event]
  (log/info "WSClient Disconnected:" event)
  (let [connection (.-target event)
        url        (.-url connection)]
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
          :event/last-tx 0
          :event/type    :presence/hello
          :event/args    {:username "Bob's your uncle"}})
  ;; => {:result :sent}

  ;; send a `:datascript/paste-verbatim` event
  (send! {:event/id      "test-id2"
          :event/last-tx 1
          :event/type    :datascript/paste-verbatim
          :event/args    {:uid   "invalid-uid"
                          :text  "pasted text"
                          :start 0
                          :value ""}})

  ;; test atomic op
  (send! {:event/id (random-uuid)
          :event/last-tx 1
          :event/type :op/atomic
          :event/args #:op{:type :block/new,
                           :atomic? true,
                           :args {:parent-uid "test1", :block-uid "test2", :block-order 2}}})

  (send! (common-events/build-atomic-event
          1
          (atomic-graph-ops/make-page-new-op "test title"
                                             "page-uid-1"))))


