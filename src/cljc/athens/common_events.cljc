(ns athens.common-events
  "Event as Verbs executed on Knowledge Graph"
  (:require
    [clojure.string :as string])
  #?(:clj
     (:import
       (java.util
         Date
         UUID))))


(defn build-event-accepted
  "Builds ACK Event Response."
  [id tx-id]
  {:event/id       id
   :event/status   :accepted
   :accepted/tx-id tx-id})


(defn event-rejected
  "Builds Rejection Event Response."
  [id message data]
  {:event/id      id
   :event/status  :rejected
   :reject/reason message
   :reject/data   data})


(defn- now-ts
  []
  #?(:clj  (.getTime (Date.))
     :cljs (.getTime (js/Date.))))


(defn- gen-block-uid
  []
  #?(:clj (subs (.toString (UUID/randomUUID)) 27)
     :cljs (subs (str (random-uuid)) 27)))


(defn- gen-event-id
  []
  (str (gensym "eid-")))


(defn build-page-create-event
  [last-tx uid title]
  (let [event-id (gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/create-page
     :event/args    {:uid   uid
                     :title title}}))


(defn page-create->tx
  "Creates Transactions to create a page with `title` & `uid`."
  [uid title]
  (let [now       (now-ts)
        child-uid (gen-block-uid)
        child     {:db/id        -2
                   :block/string ""
                   :block/uid    child-uid
                   :block/order  0
                   :block/open   true
                   :create/time  now
                   :edit/time    now}
        page-tx {:db/id -1
                 :node/title title
                 :block/uid uid
                 :block/children [child]
                 :create/time now
                 :edit/time now}]
    [page-tx]))


(defn build-paste-verbatim-event
  [last-tx uid text start value]
  (let [event-id (gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/paste-verbatim
     :event/args    {:uid   uid
                     :text  text
                     :start start
                     :value value}}))


(defn paste-verbatim->tx
  [uid text start value]
  (let [block-empty? (string/blank? value)
        block-start? (zero? start)
        new-string   (cond
                       block-empty?       text
                       (and (not block-empty?)
                            block-start?) (str text value)
                       :else              (str (subs value 0 start)
                                               text
                                               (subs value start)))
        tx-data      [{:db/id        [:block/uid uid]
                       :block/string new-string}]]
    tx-data))


(defn build-presence-hello
  [username last-tx]
  (let [event-id (gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :presence/hello
     :event/args    {:username username}}))


(defn build-presence-online
  [username last-tx]
  (let [event-id (gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :presence/online
     :event/args    {:username username}}))


(defn build-tx-log-event
  [tx-report]
  (let [event-id          (gen-event-id)
        {:keys [tx-data
                tempids]} tx-report
        last-tx           (:db/current-tx tempids)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/tx-log
     :event/args    {:tx-data tx-data
                     :tempids tempids}}))
