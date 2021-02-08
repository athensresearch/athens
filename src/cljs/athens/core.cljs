(ns athens.core
  (:require
    [athens.coeffects]
    [athens.config :as config]
    [athens.effects]
    [athens.electron]
    [athens.events]
    [athens.listeners :as listeners]
    [athens.router :as router]
    [athens.style :as style]
    [athens.subs]
    [athens.views :as views]
    [goog.dom :refer [getElement]]
    [re-frame.core :as rf]
    [reagent.dom :as r-dom]
    [stylefy.core :as stylefy]
    ["@sentry/react" :as Sentry]
    ["@sentry/tracing" :refer (Integrations)]))


(goog-define SENTRY_DSN "")


(defn dev-setup
  []
  (when config/debug?
    (println "dev mode")))


(defn ^:dev/after-load mount-root
  []
  (rf/clear-subscription-cache!)
  (router/init-routes!)
  (r-dom/render [views/main-panel]
                (getElement "app")))


(defn init-ipcRenderer
  []
  (let [ipcRenderer       (.. (js/require "electron") -ipcRenderer)
        update-available? (.sendSync ipcRenderer "check-update" "renderer")]
    (when update-available?
      (when (js/window.confirm "Update available. Would you like to update and restart to the latest version?")
        (.sendSync ipcRenderer "confirm-update")))))


(defn init
  []
  (.init Sentry (clj->js {:dsn              SENTRY_DSN
                          :release          (str "athens@" (.. (js/require "electron") -remote -app getVersion))
                          :integrations     [(new (.-BrowserTracing Integrations))]
                          :environment      (if config/debug? "development" "production")
                          :tracesSampleRate 1.0}))
  (init-ipcRenderer)
  (style/init)
  (stylefy/tag "body" style/app-styles)
  (listeners/init)
  (rf/dispatch-sync [:desktop/boot])
  ;(rf/dispatch-sync [:init-rfdb])
  ;(rf/dispatch-sync [:loading/unset])
  ;;(rf/dispatch-sync [:boot])
  (dev-setup)
  (mount-root))
