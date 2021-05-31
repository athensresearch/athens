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
   [:event/last-tx string?]
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


(defn explain
  [data]
  (-> event
      (m/explain data)
      (me/humanize)))
