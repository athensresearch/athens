(ns athens.devcards.db
  (:require
    [athens.db :as db]
    [athens.devcards.buttons :refer [button-primary]]
    [cljs-http.client :as http]
    [cljs.core.async :refer [go <!]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [datascript.core :as d]
    [devcards.core :refer [defcard defcard-rg]]
    [posh.reagent :refer [posh! transact!]]
    [reagent.core :as r]))


(defn load-real-db!
  [conn]
  (go
    (let [res (<! (http/get db/athens-url {:with-credentials? false}))
          {:keys [success body]} res]
      (if success
        (transact! conn (db/str-to-db-tx body))
        (js/alert "Failed to retrieve data from GitHub")))))


(defn load-real-db-button
  [conn]
  (let [pressed? (r/atom false)
        handler (fn []
                  (swap! pressed? not)
                  (load-real-db! conn))]
    (fn []
      [button-primary {:disabled @pressed?
                       :on-click-fn handler
                       :label "Load Real Data"}])))


(defcard-rg Load-Real-DB
  "Downloads the ego db. Takes a few seconds."
  [load-real-db-button db/dsdb])


(defcard athens-dsdb
  "The main Athens dsdb:"
  db/dsdb)


(defn new-conn
  []
  (d/create-conn db/schema))


(defn posh-conn!
  [conn]
  (posh! conn))

