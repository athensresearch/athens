(ns athens.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [re-posh.core :as rp]   
   [athens.events]
   [athens.subs]
   [athens.views :as views]
   [athens.config :as config]
   [athens.db :as db]
   ))

;(def tmp (rp/subscribe [:node/all]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn init []
  (rf/dispatch-sync [:init-rfdb])
  (rf/dispatch-sync [:init-dsdb])
  (rf/dispatch [:boot-async])
  (dev-setup)
  (mount-root))
