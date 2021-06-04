(ns athens.common-events.schema
  (:require
    [malli.core  :as m]
    [malli.error :as me]
    [malli.util  :as mu]))


(def event-type
  [:enum
   :presence/hello
   :datascript/create-page
   :datascript/paste-verbatim])


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


(def datascript-create-page
  [:map
   [:event/args
    [:map
     [:uid string?]
     [:title string?]]]])


(def datascript-paste-verbatim
  [:map
   [:event/args
    [:map
     [:uid string?]
     [:text string?]
     [:start nat-int?]
     [:value string?]]]])


(def event
  [:multi {:dispatch :event/type}
   [:presence/hello
    (mu/merge event-common
              presence-hello-args)]
   [:datascript/create-page
    (mu/merge event-common
              datascript-create-page)]
   [:datascript/paste-verbatim
    (mu/merge event-common
              datascript-paste-verbatim)]])


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
  [:enum :datascript/tx-log])


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
   [:added boolean?]])


(def tx-log
  [:map
   [:event/args
    [:map
     [:tx-data
      [:vector datom]]
     [:tempids map?]]]])


(def server-event
  [:multi {:dispatch :event/type}
   [:datascript/tx-log (mu/merge server-event-common
                                 tx-log)]])


(def valid-server-event?
  (m/validator server-event))


(defn explain-server-event
  [data]
  (-> server-event
      (m/explain data)
      (me/humanize)))
