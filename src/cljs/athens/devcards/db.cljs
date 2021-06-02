(ns athens.devcards.db
  (:require
    [athens.db :as db]
    [athens.views.buttons :refer [button]]
    [cljs-http.client :as http]
    [cljs.core.async :refer [go <!]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [datascript.core :as d]
    [devcards.core :refer [defcard defcard-rg]]
    [posh.reagent :refer [transact!]]
    [reagent.core :as r]))


;;; Components


(defn load-real-db!
  []
  (go
    (let [res (<! (http/get db/athens-url {:with-credentials? false}))
          {:keys [success body]} res]
      (if success
        (transact! db/dsdb (db/str-to-db-tx body))
        (js/alert "Failed to retrieve data from GitHub")))))


(defn load-real-db-button
  []
  (let [pressed? (r/atom false)
        handler (fn []
                  (swap! pressed? not)
                  (load-real-db!))]
    (fn []
      [button {:disabled @pressed?
               :on-click handler} "Load Real Data"])))


(defn reset-db-button
  []
  (fn []
    [button {:on-click #(d/reset-conn! db/dsdb (d/empty-db db/schema))} "Reset DB"]))


;;; Devcards


(defcard-rg Load-Real-DB
  "Downloads the ego db. Takes a few seconds. Can only press once."
  [load-real-db-button])


(defcard-rg Reset-DB
  [reset-db-button])


(defcard athens-dsdb
  "The main Athens dsdb:"
  db/dsdb)
