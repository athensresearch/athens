(ns athens.common-events
  "Event as Verbs executed on Knowledge Graph"
  (:require
    [athens.common.utils :as utils]))


;; building events

;; - confirmation events

(defn build-event-accepted
  "Builds ACK Event Response accepting this event."
  [id]
  {:event/id     id
   :event/status :accepted})


(defn build-event-rejected
  "Builds Rejection Event Response with `:reject/reason & :reject/data`."
  [id message data]
  {:event/id      id
   :event/status  :rejected
   :reject/reason message
   :reject/data   data})


;; - datascript events

(defn build-db-dump-event
  "Builds `:datascript/db-dump` events with `datoms`."
  [datoms]
  (let [event-id (utils/gen-event-id)]
    {:event/id   event-id
     :event/type :datascript/db-dump
     :event/args {:datoms datoms}}))


;; undo-redo events

(defn build-undo-redo-event
  "Builds `:datascript/undo-redo`"
  [redo?]
  (let [event-id (utils/gen-event-id)]
    {:event/id   event-id
     :event/type :datascript/undo-redo
     :event/args {:redo? redo?}}))


;; TODO (RTC): profesh cleaning required
(defn build-split-block-event
  "Builds `:datascript/split-block` event with:
  - `uid`: `:block/uid` of block we're splitting
  - `value`: Current `:block/string` of block splitted
  - `index`: index of the split
  - `new-uid`: `:block/uid` of new block"
  [uid value index new-uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id   event-id
     :event/type :datascript/split-block
     :event/args {:uid     uid
                  :value   value
                  :index   index
                  :new-uid new-uid}}))


;; - presence events

(defn build-presence-hello-event
  "Builds `:presence/hello` event with `session-intro` and `password` (optional)."
  ([session-intro]
   (build-presence-hello-event session-intro nil))
  ([session-intro password]
   (let [event-id (utils/gen-event-id)]
     {:event/id   event-id
      :event/type :presence/hello
      :event/args (cond-> {:session-intro session-intro}
                    password (merge {:password password}))})))


(defn build-presence-session-id-event
  "Builds `:presence/session-id` event with `session-id` for the client."
  [session-id]
  (let [event-id (utils/gen-event-id)]
    {:event/id   event-id
     :event/type :presence/session-id
     :event/args {:session-id session-id}}))


(defn build-presence-online-event
  "Builds `:presence/online` event with `session` that went online."
  [session]
  (let [event-id (utils/gen-event-id)]
    {:event/id   event-id
     :event/type :presence/online
     :event/args session}))


(defn build-presence-all-online-event
  "Builds `:presence/all-online` event with all active users."
  [sessions]
  (let [event-id (utils/gen-event-id)]
    {:event/id   event-id
     :event/type :presence/all-online
     :event/args (vec sessions)}))


(defn build-presence-offline-event
  [session]
  (let [event (build-presence-online-event session)]
    (assoc event :event/type :presence/offline)))


(defn build-presence-update-event
  "Builds `:presence/update` event with `session-id` and map of session props to update."
  [session-id updates]
  (let [event-id (utils/gen-event-id)]
    {:event/id   event-id
     :event/type :presence/update
     :event/args (merge {:session-id session-id}
                        updates)}))


(defn build-atomic-event
  "Builds atomic graph operation"
  [atomic-op]
  (let [event-id (utils/gen-event-id)]
    {:event/id   event-id
     :event/type :op/atomic
     :event/op   atomic-op}))
