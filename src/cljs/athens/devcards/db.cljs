(ns athens.devcards.db
  (:require
    [athens.db :as db]
    [cljs-http.client :as http]
    [cljs.core.async :refer [go <!]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer [defcard]]
    [posh.reagent :refer [posh! transact!]]))


(defcard athens-dsdb
  "Indices are omitted because they include all the datoms
  Could probably use this and `load-db` as generic helpers for any componenets that use posh."
  (dissoc @db/dsdb :eavt :aevt :avet))


(defn load-db
  []
  (go
    (let [res (<! (http/get db/athens-url {:with-credentials? false}))
          {:keys [success body]} res]
      (if success
        (transact! db/dsdb (db/str-to-db-tx body))
        (js/alert "Failed to retrieve data from GitHub")))))


;; TODO: Use mount to manage state?
(defn -main
  []
  (posh! db/dsdb)
  (load-db))
