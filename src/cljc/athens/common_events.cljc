(ns athens.common-events
  "Event as Verbs executed on Knowledge Graph"
  (:require [clojure.string :as string]))

(defn paste-verbatim->tx [uid text start value]
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
