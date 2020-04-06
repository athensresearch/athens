(ns athens.core
  (:require
   [athens.events]
   [athens.subs]
   [athens.views :as views]
   [athens.config :as config]
   [athens.db :as db]
   [athens.router :as router]
   [athens.parser :refer [parser]]
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [re-posh.core :as rp]
   ))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (router/init-routes!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn init []
  (rf/dispatch-sync [:init-rfdb])
  (rf/dispatch-sync [:init-dsdb])
  (rf/dispatch [:boot-async])
  (dev-setup)
  (mount-root))
