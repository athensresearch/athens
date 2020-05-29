(ns athens.devcards.left-sidebar
  (:require
    [athens.db :as db]
    [cljsjs.react]
    [cljsjs.react.dom]
    [cljs.core.async :refer [go <!]]
    [devcards.core :refer [defcard defcard-rg]]
    [reagent.core :as rg]
    [posh.reagent :refer [pull q posh! transact!]]
    [datascript.core :as d]
    [cljs-http.client :as http]
    [athens.style :as style]))

(def log js/console.log)

(defn trace
  [x]
  (log x) x)

(posh! db/dsdb)

(defn left-sidebar
  "TODO: Don't know how we want to handle global subscriptions or dispatches (i.e. routing) now that we are in reagent"
  []
  (let [favorites (q '[:find ?order ?title ?bid
                       :where
                       [?e :page/sidebar ?order]
                       [?e :node/title ?title]
                       [?e :block/uid ?bid]]
                     db/dsdb)
        sorted-favorites (->> @favorites
                              (into [])
                              (sort-by first))
        ;;current-route (subscribe [:current-route])
        ]
    [:div {:style {:margin "0 10px" :max-width 250}}
     ;;[:div [:a {:href (rfee/href :pages)} "All /pages"]]
     ;;[:div [:span {:style {}} "Current Route: " [:b (-> @current-route :path)]]]
     [:div {:style {:border-bottom "1px solid gray" :margin "10px 0"}}]
     [:ul (style/+left-sidebar {})
      (for [[_order title bid] sorted-favorites]
        ;;^{:key bid} [:li [:a {:href (rfee/href :page {:id bid})} title]]
        ^{:key bid} [:li [:span title]])
      ]]))

(defcard-rg left-sidebar
  [left-sidebar])

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


(defn -main []
  (load-db))
(-main)