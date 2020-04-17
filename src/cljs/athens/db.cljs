(ns athens.db
  (:require [datascript.core :as d]
            [clojure.edn :as edn]
            [re-posh.core :as re-posh]
            [re-frame.core :as re-frame]
            ;[reitit.core :as reitit]
            ))

; ajax deserializes JSON string into Clojure vector for us
(defn str-to-db-tx [json-str]
  (js-debugger)
  (as-> json-str x
    (js/JSON.parse json-str)
    (js->clj x)
    (partition 3 x)             ; chunk into 3-tuples
    (rest x)                    ; drop first tuple which is (?e ?a ?v)
    (map #(map edn/read-string %) x)
    (map #(cons :db/add %) x)))

;; (def dsdb-help "https://raw.githubusercontent.com/tangjeff0/athens/master/data/help-db.json")
;; (def dsdb-ego "https://raw.githubusercontent.com/tangjeff0/athens/master/data/ego-db.json")

(def schema {:block/uid      {:db/unique :db.unique/identity}
             :node/title     {:db/unique :db.unique/identity}
             :attrs/lookup   {:db/cardinality :db.cardinality/many}
             :block/children {:db/cardinality :db.cardinality/many
                              :db/valueType :db.type/ref}
             })
             

(def init-rfdb
  {:user/name "Jeff"
   :user/email "tangj12@gmail.com"
   :current-route nil
   :loading {}
   :errors {}})

(def init-dsdb
  [[:db/add 315 :node/title "NUT"]])

(def dsdb (d/create-conn schema))
(re-posh/connect! dsdb)

