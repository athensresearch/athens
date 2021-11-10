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


(def event-type-graph
  [:enum
   :datascript/delete-page
   :datascript/block-save
   :datascript/new-block
   :datascript/add-child
   :datascript/split-block
   :datascript/page-add-shortcut
   :datascript/page-remove-shortcut
   :datascript/left-sidebar-drop-above
   :datascript/left-sidebar-drop-below
   :datascript/selected-delete
   :datascript/delete-only-child
   :datascript/delete-merge-block
   :datascript/paste-internal])


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
                 event-type-graph
                 event-type-atomic]]])


(def event-common-server
  [:map
   [:event/id uuid?]
   [:event/type [:or
                 event-type-graph
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


(def datascript-delete-page
  [:map
   [:event/args
    [:map
     [:uid string?]]]])


(def datascript-block-save
  [:map
   [:event/args
    [:map
     [:uid string?]
     [:new-string string?]
     [:add-time?  boolean?]]]])


(def datascript-new-block
  [:map
   [:event/args
    [:map
     [:parent-uid string?]
     [:block-order int?]
     [:new-uid string?]]]])


(def datascript-add-child
  [:map
   [:event/args
    [:map
     [:parent-uid string?]
     [:new-uid   string?]
     [:add-time? boolean?]]]])


(def datascript-split-block
  [:map
   [:event/args
    [:map
     [:uid string?]
     [:value string?]
     [:index int?]
     [:new-uid string?]]]])


(def datascript-paste-internal
  [:map
   [:event/args
    [:map
     [:uid string?]
     [:internal-representation [:vector map?]]]]])


(def datascript-page-add-shortcut
  [:map
   [:event/args
    [:map
     [:uid string?]]]])


(def datascript-page-remove-shortcut
  [:map
   [:event/args
    [:map
     [:uid string?]]]])


(def datascript-left-sidebar-drop-above
  [:map
   [:event/args
    [:map
     [:source-order int?]
     [:target-order int?]]]])


(def datascript-left-sidebar-drop-below
  [:map
   [:event/args
    [:map
     [:source-order int?]
     [:target-order int?]]]])


(def datascript-selected-delete
  [:map
   [:event/args
    [:map
     [:uids [:vector string?]]]]])


(def datascript-block-open
  [:map
   [:event/args
    [:map
     [:block-uid string?]
     [:open?     boolean?]]]])


(def datascript-delete-only-child
  [:map
   [:event/args
    [:map
     [:uid string?]]]])


(def datascript-delete-merge-block
  [:map
   [:event/args
    [:map
     [:uid string?]
     [:value string?]]]])


(def graph-ops-atomic
  [:map
   [:event/op graph-schema/atomic-op]])


(def event
  [:multi {:dispatch :event/type}
   (dispatch :presence/hello presence-hello)
   (dispatch :presence/update presence-update)
   (dispatch :datascript/delete-page datascript-delete-page)
   (dispatch :datascript/block-save datascript-block-save)
   (dispatch :datascript/new-block datascript-new-block)
   (dispatch :datascript/add-child datascript-add-child)
   ;; Same args as `datascript-add-child`
   (dispatch :datascript/split-block datascript-split-block)
   ;; same args as `datascript-split-block`
   (dispatch :datascript/paste-internal datascript-paste-internal)
   (dispatch :datascript/page-add-shortcut datascript-page-add-shortcut)
   (dispatch :datascript/page-remove-shortcut datascript-page-remove-shortcut)
   (dispatch :datascript/left-sidebar-drop-above datascript-left-sidebar-drop-above)
   (dispatch :datascript/left-sidebar-drop-below datascript-left-sidebar-drop-below)
   (dispatch :datascript/delete-only-child datascript-delete-only-child)
   (dispatch :datascript/delete-merge-block datascript-delete-merge-block)
   (dispatch :datascript/selected-delete datascript-selected-delete)
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
   ;; client forwardable events
   (dispatch :datascript/delete-page datascript-delete-page true)
   (dispatch :datascript/block-save datascript-block-save true)
   (dispatch :datascript/new-block datascript-new-block true)
   (dispatch :datascript/add-child datascript-add-child true)
   ;; Same args as `datascript-add-child`
   (dispatch :datascript/split-block datascript-split-block true)
   ;; same args as `datascript-split-block`
   (dispatch :datascript/paste-internal datascript-paste-internal true)
   (dispatch :datascript/page-add-shortcut datascript-page-add-shortcut true)
   (dispatch :datascript/page-remove-shortcut datascript-page-remove-shortcut true)
   (dispatch :datascript/left-sidebar-drop-above datascript-left-sidebar-drop-above true)
   (dispatch :datascript/left-sidebar-drop-below datascript-left-sidebar-drop-below true)
   (dispatch :datascript/delete-only-child datascript-delete-only-child true)
   (dispatch :datascript/delete-merge-block datascript-delete-merge-block true)
   (dispatch :datascript/selected-delete datascript-selected-delete true)

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
