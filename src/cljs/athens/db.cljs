(ns athens.db
  (:require [clojure.edn :as edn]
            [datascript.core :as d]
            #_[re-frame.core :as re-frame]
            [re-posh.core :as re-posh]
            ))

(def str-kw-mappings
  "Maps attributes from \"Export All as JSON\" to original datascript attributes."
  {"children" :block/children
   "create-email" :create/email
   "create-time" :create/time
   "edit-email" :edit/email
   "edit-time" :edit/time
   "email" :user/email
   "emoji" :ent/emoji
   "emojis" :ent/emojis
   "props" :block/props
   "string" :block/string
   "text-align" :block/text-align
   "time" nil
   "title" :node/title
   "uid" :block/uid
   "users" nil
   "heading" :block/heading})

(defn convert-key [k]
  (get str-kw-mappings k k))

(defn parse-hms
  "Parses JSON retrieved from Roam's \"Export all as JSON\". Not fully functional."
  [hms]
  (if (not (coll? hms))
    hms
    (map #(reduce (fn [acc [k v]]
                    (assoc acc (convert-key k) (parse-hms v)))
                  {}
                  %)
         hms)))

(defn parse-tuples
  "Parse tuples exported via method specified in https://roamresearch.com/#/app/ego/page/eJ14YtH2G."
  [tuples]
  (->> tuples
       (partition 3)            ; chunk into 3-tuples
       rest                     ; drop first tuple which is (?e ?a ?v)
       (map #(map edn/read-string %))
       (map #(cons :db/add %))))

(defn json-str-to-vector
  "Convert a JSON str to a clojure vector. May receive JSON through an HTTP request or file upload."
  [json-str]
  (->> json-str
       (js/JSON.parse)
       (js->clj)))

(defn str-to-db-tx
  "Deserializes a JSON string into EDN and then Datoms."
  [json-str]
  (let [-vector (json-str-to-vector json-str)]
    (if (coll? (first -vector))
      (parse-hms -vector)
      (parse-tuples -vector))))

(def athens-url "https://raw.githubusercontent.com/athensresearch/athens/master/data/athens.datoms")
(def help-url   "https://raw.githubusercontent.com/athensresearch/athens/master/data/help.datoms")
(def ego-url    "https://raw.githubusercontent.com/athensresearch/athens/master/data/ego.datoms")

(def schema {:block/uid      {:db/unique :db.unique/identity}
             :node/title     {:db/unique :db.unique/identity}
             :attrs/lookup   {:db/cardinality :db.cardinality/many}
             :block/children {:db/cardinality :db.cardinality/many
                              :db/valueType :db.type/ref}})

(defonce rfdb {:user "Jeff"
               :current-route nil
               :loading true
               :errors {}})

(defonce dsdb (d/create-conn schema))
(re-posh/connect! dsdb)
