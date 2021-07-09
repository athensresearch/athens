(ns athens.common-events.schema
  (:require
    #?(:clj
       [datahike.datom :as datom])
    [malli.core        :as m]
    [malli.error       :as me]
    [malli.util        :as mu]))


(def event-type-presence
  [:enum
   :presence/hello
   :presence/editing])


(def event-type-presence-server
  [:enum
   :presence/online
   :presence/all-online
   :presence/offline
   :presence/broadcast-editing])


(def event-type-graph
  [:enum
   :datascript/create-page
   :datascript/rename-page
   :datascript/merge-page
   :datascript/delete-page
   :datascript/block-save
   :datascript/new-block
   :datascript/add-child
   :datascript/open-block-add-child
   :datascript/split-block
   :datascript/split-block-to-children
   :datascript/unindent
   :datascript/paste-verbatim
   :datascript/indent
   :datascript/page-add-shortcut
   :datascript/page-remove-shortcut
   :datascript/left-sidebar-drop-above
   :datascript/left-sidebar-drop-below])


(def event-type-graph-server
  [:enum
   :datascript/tx-log
   :datascript/db-dump])


(def event-common
  [:map
   [:event/id uuid?]
   [:event/last-tx int?]
   [:event/type [:or
                 event-type-presence
                 event-type-graph]]])


(def event-common-server
  [:map
   [:event/id uuid?]
   [:event/last-tx int?]
   [:event/type [:or
                 event-type-graph
                 event-type-graph-server
                 event-type-presence-server]]])


(defn dispatch
  ([type args]
   (dispatch type args false))
  ([type args server?]
   [type (mu/merge
           (if server?
             event-common-server
             event-common)
           args)]))


(def presence-hello-args
  [:map
   [:event/args
    [:map
     [:username string?]]]])


(def presence-editing
  [:map
   [:event/args
    [:map
     [:username string?]
     ;; how to make block/uid string? or nil?
     ;; why would it be `nil?`
     #_[:block/uid  string?]]]])


(def datascript-create-page
  [:map
   [:event/args
    [:map
     [:page-uid string?]
     [:block-uid string?]
     [:title string?]]]])


(def datascript-delete-page
  [:map
   [:event/args
    [:map
     [:uid string?]]]])


(def datascript-rename-page
  [:map
   [:event/args
    [:map
     [:uid string?]
     [:old-name string?]
     [:new-name string?]]]])


(def datascript-block-save
  [:map
   [:event/args
    [:map
     [:uid string?]
     [:new-string string?]]]])


(def datascript-new-block
  [:map
   [:event/args
    [:map
     [:parent-eid int?]
     [:block-order int?]
     [:new-uid string?]]]])


(def datascript-add-child
  [:map
   [:event/args
    [:map
     [:eid int?]
     [:new-uid string?]]]])


(def datascript-split-block
  [:map
   [:event/args
    [:map
     [:uid string?]
     [:value string?]
     [:index int?]
     [:new-uid string?]]]])


(def datascript-indent
  [:map
   [:event/args
    [:map
     [:uid string?]
     [:value string?]]]])


(def datascript-unindent
  [:map
   [:event/args
    [:map
     [:uid string?]
     [:value string?]]]])


(def datascript-paste-verbatim
  [:map
   [:event/args
    [:map
     [:uid string?]
     [:text string?]
     [:start nat-int?]
     [:value string?]]]])


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


(def event
  [:multi {:dispatch :event/type}
   (dispatch :presence/hello presence-hello-args)
   (dispatch :presence/editing presence-editing)
   (dispatch :datascript/create-page datascript-create-page)
   (dispatch :datascript/rename-page datascript-rename-page)
   ;; Same args as `datascript-rename-page`
   (dispatch :datascript/merge-page datascript-rename-page)
   (dispatch :datascript/delete-page datascript-delete-page)
   (dispatch :datascript/block-save datascript-block-save)
   (dispatch :datascript/new-block datascript-new-block)
   (dispatch :datascript/add-child datascript-add-child)
   ;; Same args as `datascript-add-child`
   (dispatch :datascript/open-block-add-child datascript-add-child)
   (dispatch :datascript/split-block datascript-split-block)
   ;; same args as `datascript-split-block`
   (dispatch :datascript/split-block-to-children datascript-split-block)
   (dispatch :datascript/indent datascript-indent)
   (dispatch :datascript/unindent datascript-unindent)
   (dispatch :datascript/paste-verbatim datascript-paste-verbatim)
   (dispatch :datascript/page-add-shortcut datascript-page-add-shortcut)
   (dispatch :datascript/page-remove-shortcut datascript-page-remove-shortcut)
   (dispatch :datascript/left-sidebar-drop-above datascript-left-sidebar-drop-above)
   (dispatch :datascript/left-sidebar-drop-below datascript-left-sidebar-drop-below)])


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


(def response-accepted
  [:map
   [:accepted/tx-id int?]])


(def rejection-reason
  [:enum :introduce-yourself :stale-client])


(def response-rejected
  [:map
   [:reject/reason [:or string? rejection-reason]]
   [:reject/data {:optional true} map?]])


(def event-response
  [:multi {:dispatch :event/status}
   [:accepted (mu/merge event-response-common
                        response-accepted)]
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
  [:map
   [:e pos-int?]
   [:a keyword?]
   [:v any?]
   [:tx int?]
   [:added {:optional true} boolean?]])


(def tx-log
  [:map
   [:event/args
    [:map
     [:tx-data
      [:vector #?(:clj  [:fn datom/datom?]
                  :cljs datom)]]
     [:tempids map?]]]])


(def db-dump
  [:map
   [:event/args
    [:map
     [:datoms
      ;; NOTE: this is because after serialization & deserialization data is represented differently
      [:sequential #?(:clj  [:fn datom/datom?]
                      :cljs datom)]]]]])


(def user
  [:map
   [:username string?]])


(def presence-online
  [:map
   [:event/args
    user]])


(def presence-all-online
  [:map
   [:event/args
    [:vector
     user]]])


(def presence-offline
  presence-online)


(def presence-broadcast-editing
  [:map
   [:event/args
    [:map
     [:username string?]
     [:block/uid string?]]]])


(def server-event
  [:multi {:dispatch :event/type}
   ;; client forwardable events
   (dispatch :datascript/create-page datascript-create-page true)
   (dispatch :datascript/rename-page datascript-rename-page true)
   ;; Same args as `datascript-rename-page`
   (dispatch :datascript/merge-page datascript-rename-page true)
   (dispatch :datascript/delete-page datascript-delete-page true)
   (dispatch :datascript/block-save datascript-block-save true)
   (dispatch :datascript/new-block datascript-new-block true)
   (dispatch :datascript/add-child datascript-add-child true)
   ;; Same args as `datascript-add-child`
   (dispatch :datascript/open-block-add-child datascript-add-child true)
   (dispatch :datascript/split-block datascript-split-block true)
   ;; same args as `datascript-split-block`
   (dispatch :datascript/split-block-to-children datascript-split-block true)
   (dispatch :datascript/indent datascript-indent true)
   (dispatch :datascript/unindent datascript-unindent true)
   (dispatch :datascript/paste-verbatim datascript-paste-verbatim true)
   (dispatch :datascript/page-add-shortcut datascript-page-add-shortcut true)
   (dispatch :datascript/page-remove-shortcut datascript-page-remove-shortcut true)
   (dispatch :datascript/left-sidebar-drop-above datascript-left-sidebar-drop-above true)
   (dispatch :datascript/left-sidebar-drop-below datascript-left-sidebar-drop-below true)

   ;; server specific graph events
   (dispatch :datascript/tx-log tx-log true)
   (dispatch :datascript/db-dump db-dump true)
   ;; server specific presence events
   (dispatch :presence/online presence-online true)
   (dispatch :presence/all-online presence-all-online true)
   (dispatch :presence/offline presence-offline true)
   (dispatch :presence/broadcast-editing presence-broadcast-editing true)])


(def valid-server-event?
  (m/validator server-event))


(defn explain-server-event
  [data]
  (-> server-event
      (m/explain data)
      (me/humanize)))
