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
    [stylefy.core :as stylefy]))


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


(defn set-global-alert!
  "Alerts user if there's an uncaught error.
  https://developer.mozilla.org/en-US/docs/Web/API/GlobalEventHandlers/onerror "
  []
  (set! js/window.onerror (fn [message, source, lineno, colno, error]
                            (js/alert (str "message="message "\nsource="source "\nlineno=" lineno "\ncolno=" colno "\nerror="error)))))


(defn init
  []
  (set-global-alert!)
  (style/init)
  (stylefy/tag "body" style/app-styles)
  (listeners/init)
  (rf/dispatch-sync [:desktop/boot])
  ;(rf/dispatch-sync [:init-rfdb])
  ;(rf/dispatch-sync [:loading/unset])
  ;;(rf/dispatch-sync [:boot])
  (dev-setup)
  (mount-root))
