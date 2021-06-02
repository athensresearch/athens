(ns athens.common-events
  "Event as Verbs executed on Knowledge Graph"
  (:require
    [clojure.string :as string]))


(defn event-accepted
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
