(ns athens.common-events.schema
  (:require
    [malli.core  :as m]
    [malli.error :as me]
    [malli.util  :as mu]))


(def event-type
  [:enum
   :presence/hello
   :datascript/paste-verbatim])


(def event-common
  [:map
   [:event/id string?]
   [:event/last-tx pos-int?]
   [:event/type event-type]])


(def presence-hello-args
  [:map
   [:event/args
    [:map
     [:username string?]
     #_[:last-tx string?]]]])


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
   [:accepted/tx-id pos-int?]])


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
