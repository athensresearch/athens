(ns athens.common-events.schema
  (:require
    [athens.common-events.graph.schema :as graph-schema]
    [malli.core                        :as m]
    [malli.error                       :as me]
    [malli.util                        :as mu]))


(def event-type-presence-client
  [:enum
   :presence/hello
   :presence/update])


(def event-type-presence-server
  [:enum
   :presence/session-id
   :presence/online
   :presence/all-online
   :presence/offline
   :presence/update])


(def event-type-graph-server
  [:enum
   :datascript/db-dump])


(def event-type-atomic
  [:enum
   :op/atomic])


(def event-common
  [:map
   [:event/id uuid?]
   [:event/type [:or
                 event-type-presence-client
                 event-type-atomic]]
   [:event/create-time {:optional true} int?]
   [:event/presence-id {:optional true} string?]])


(def event-common-server
  [:map
   [:event/id uuid?]
   [:event/type [:or
                 event-type-graph-server
                 event-type-presence-server
                 event-type-atomic]]])


(defn dispatch
  ([type args]
   (dispatch type args false))
  ([type args server?]
   [type (mu/merge
           (if server?
             event-common-server
             event-common)
           args)]))


(def session-id
  [:session-id string?])


;; Having all keys optional enables us to have
;; anonymous or third party clients.
;; These are the keys our client uses, if present.
(def session-intro
  [:map
   [:username {:optional true} string?]
   [:color {:optional true} string?]
   [:block-uid {:optional true} string?]])


(def session
  (mu/merge
    session-intro
    [:map
     session-id]))


(def presence-hello
  [:map
   [:event/args
    [:map
     [:session-intro session-intro]
     [:password {:optional true} string?]]]])


(def presence-session-id
  [:map
   [:event/args
    [:map
     session-id]]])


(def presence-update
  [:map
   [:event/args
    session]])


(def presence-online
  [:map
   [:event/args
    session]])


(def presence-all-online
  [:map
   [:event/args
    [:vector
     session]]])


(def presence-offline
  presence-online)


(def graph-ops-atomic
  [:map
   [:event/op graph-schema/atomic-op]])


(def event
  [:multi {:dispatch :event/type}
   (dispatch :presence/hello presence-hello)
   (dispatch :presence/update presence-update)
   (dispatch :op/atomic graph-ops-atomic)])


(def valid-event?
  (m/validator event))


(defn explain-event
  [data]
  (-> event
      (m/explain data)
      (me/humanize)))


(def event-status
  [:enum :rejected :accepted])


(def event-response-common
  [:map
   [:event/id uuid?]
   [:event/status event-status]])


(def rejection-reason
  [:enum :introduce-yourself :stale-client])


(def response-rejected
  [:map
   [:reject/reason [:or string? rejection-reason]]
   [:reject/data {:optional true} map?]])


(def event-response
  [:multi {:dispatch :event/status}
   [:accepted event-response-common]
   [:rejected (mu/merge event-response-common
                        response-rejected)]])


(def valid-event-response?
  (m/validator event-response))


(defn explain-event-response
  [data]
  (-> event-response
      (m/explain data)
      (me/humanize)))


(def datom
  [:vector any?])


(def db-dump
  [:map
   [:event/args
    [:map
     [:datoms
      ;; NOTE: this is because after serialization & deserialization data is represented differently
      [:sequential datom]]]]])


(def server-event
  [:multi {:dispatch :event/type}
   ;; server specific graph events
   (dispatch :datascript/db-dump db-dump true)
   ;; server specific presence events
   (dispatch :presence/session-id presence-session-id true)
   (dispatch :presence/online presence-online true)
   (dispatch :presence/all-online presence-all-online true)
   (dispatch :presence/offline presence-offline true)
   (dispatch :presence/update presence-update true)

   ;; ⚛️ Atomic Graph Ops
   (dispatch :op/atomic graph-ops-atomic true)])


(def valid-server-event?
  (m/validator server-event))


(defn explain-server-event
  [data]
  (-> server-event
      (m/explain data)
      (me/humanize)))
