(ns athens.core
  (:require
    [athens.coeffects]
    [athens.config :as config]
    [athens.effects]
    [athens.events]
    [athens.listeners :as listeners]
    [athens.router :as router]
    [athens.style :refer [app-styles]]
    [athens.subs]
    [athens.views :as views]
    [goog.dom :refer [getElement]]
    [re-frame.core :as rf]
    [reagent.core :as reagent]
    [stylefy.core :as stylefy]))


(defn dev-setup
  []
  (when config/debug?
    (println "dev mode")))


(defn ^:dev/after-load mount-root
  []
  (rf/clear-subscription-cache!)
  (router/init-routes!)
  (reagent/render [views/main-panel]
                  (getElement "app")))


(defn init
  []
  (stylefy/tag "body" app-styles)
  (stylefy/init)
  (listeners/init)
  (rf/dispatch-sync [:init-rfdb])
  (rf/dispatch-sync [:loading/unset])
  ;;(rf/dispatch-sync [:boot])
  (dev-setup)
  (mount-root))
