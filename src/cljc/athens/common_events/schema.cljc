(ns athens.common-events.schema
  (:require
    [malli.core  :as m]
    [malli.error :as me]
    [malli.util  :as mu]))


(def event-type
  [:enum
   :presence/hello
   :presence/editing
   :datascript/create-page
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
   :datascript/indent-multi
   :datascript/unindent-multi
   :datascript/page-add-shortcut
   :datascript/page-remove-shortcut])


(def event-common
  [:map
   [:event/id string?]
   [:event/last-tx int?]
   [:event/type event-type]])


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
     [:uid string?]
     [:title string?]]]])


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

(def datascript-indent-multi
  [:map
   [:event/args
    [:map
     [:uids   vector?
      :blocks seq?]]]])



(def datascript-unindent
  [:map
   [:event/args
    [:map
     [:uid string?]
     [:value string?]]]])


(def datascript-unindent-multi
  [:map
   [:event/args
    [:map
     [:uids  vector?
      :f-uid string?]]]])



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


(def event
  [:multi {:dispatch :event/type}
   [:presence/hello
    (mu/merge event-common
              presence-hello-args)]
   [:presence/editing
    (mu/merge event-common
              presence-editing)]
   [:datascript/create-page
    (mu/merge event-common
              datascript-create-page)]
   [:datascript/delete-page
    (mu/merge event-common
              datascript-delete-page)]
   [:datascript/block-save
    (mu/merge event-common
              datascript-block-save)]
   [:datascript/new-block
    (mu/merge event-common
              datascript-new-block)]
   [:datascript/add-child
    (mu/merge event-common
              datascript-add-child)]
   [:datascript/open-block-add-child
    (mu/merge event-common
              datascript-add-child)] ; Same args as `datascript-add-child`
   [:datascript/split-block
    (mu/merge event-common
              datascript-split-block)]
   [:datascript/split-block-to-children
    (mu/merge event-common
              datascript-split-block)] ; same args as `datascript-split-block`
   [:datascript/unindent
    (mu/merge event-common
              datascript-unindent)]
   [:datascript/paste-verbatim
    (mu/merge event-common
              datascript-paste-verbatim)]
   [:datascript/page-add-shortcut
    (mu/merge event-common
              datascript-page-add-shortcut)]
   [:datascript/page-remove-shortcut
    (mu/merge event-common
              datascript-page-remove-shortcut)]])


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
   [:event/id string?]
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


(def server-event-types
  [:enum
   :datascript/tx-log
   :datascript/db-dump
   :presence/online
   :presence/all-online
   :presence/offline
   :presence/broadcast-editing])


(def server-event-common
  [:map
   [:event/id string?]
   [:event/last-tx int?]
   [:event/type server-event-types]])


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
      [:vector datom]]
     [:tempids map?]]]])


(def db-dump
  [:map
   [:event/args
    [:map
     [:datoms
      [:sequential datom]]]]])


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
   [:datascript/tx-log (mu/merge server-event-common
                                 tx-log)]
   [:datascript/db-dump (mu/merge server-event-common
                                  db-dump)]
   [:presence/online (mu/merge server-event-common
                               presence-online)]
   [:presence/all-online (mu/merge server-event-common
                                   presence-all-online)]
   [:presence/offline (mu/merge server-event-common
                                presence-offline)]
   [:presence/broadcast-editing (mu/merge server-event-common
                                          presence-broadcast-editing)]])


(def valid-server-event?
  (m/validator server-event))


(defn explain-server-event
  [data]
  (-> server-event
      (m/explain data)
      (me/humanize)))
