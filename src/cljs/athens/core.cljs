(ns athens.core
  (:require
    ["@sentry/integrations" :as integrations]
    ["@sentry/react" :as Sentry]
    ["@sentry/tracing" :as tracing]
    [athens.coeffects]
    [athens.components]
    [athens.config :as config]
    [athens.effects]
    [athens.electron]
    [athens.events]
    [athens.listeners :as listeners]
    [athens.router :as router]
    [athens.style :as style]
    [athens.subs]
    [athens.util :as util]
    [athens.views :as views]
    [goog.dom :refer [getElement]]
    [re-frame.core :as rf]
    [reagent.dom :as r-dom]
    [stylefy.core :as stylefy]))


(goog-define SENTRY_DSN "")


(defn dev-setup
  []
  (when config/debug?
    (println "dev mode")))


(defn ^:dev/after-load mount-root
  []
  (rf/clear-subscription-cache!)
  (router/init-routes!)
  (r-dom/render [views/main]
                (getElement "app")))


(defn sentry-on?
  "Checks localStorage to see if sentry is on. Sentry is disabled/enabled in settings along with Posthog."
  []
  (not= "off" (js/localStorage.getItem "sentry")))


(defn init-sentry
  "Two checks for sentry: once on init and once on beforeSend."
  []
  (when (sentry-on?)
    (.init Sentry (clj->js {:dsn SENTRY_DSN
                            :release          (str "athens@" (util/athens-version))
                            :integrations     [(new (.. tracing -Integrations -BrowserTracing))
                                               (new (.. integrations -CaptureConsole) (clj->js {:levels ["warn" "error" "debug" "assert"]}))]
                            :environment      (if config/debug? "development" "production")
                            :beforeSend       #(when (sentry-on?) %)
                            :tracesSampleRate 1.0}))))


(defn set-global-alert!
  "Alerts user if there's an uncaught error.
  https://developer.mozilla.org/en-US/docs/Web/API/GlobalEventHandlers/onerror "
  []
  (set! js/window.onerror (fn [message, source, lineno, colno, error]
                            (js/alert (str "message=" message "\nsource=" source "\nlineno=" lineno "\ncolno=" colno "\nerror=" error)))))


(defn init-ipcRenderer
  []
  (when (util/electron?)
    (let [ipcRenderer       (.. (js/require "electron") -ipcRenderer)
          update-available? (.sendSync ipcRenderer "check-update" "renderer")]
      (when update-available?
        (when (js/window.confirm "Update available. Would you like to update and restart to the latest version?")
          (.sendSync ipcRenderer "confirm-update"))))))


(defn init-windowsize
  "When the app is initialized, check if we should use the last window size and if so, set the current window size to that value"
  []
  (when (util/electron?)
    (let [curWindow        (.getCurrentWindow athens.electron/remote)
          [lastx lasty]    (util/get-window-size)]
      (.setSize curWindow lastx lasty)
      (.center curWindow)
      (.on ^js curWindow "close" (fn [e]
                                   (let [sender (.-sender e)
                                         [x y] (.getSize ^js sender)]
                                     (rf/dispatch [:window/set-size [x y]])))))))


(defn init
  []
  (set-global-alert!)
  (init-sentry)
  (init-ipcRenderer)
  (init-windowsize)
  (style/init)
  (stylefy/tag "body" style/app-styles)
  (listeners/init)
  (if (util/electron?)
    (rf/dispatch-sync [:boot/desktop])
    (rf/dispatch-sync [:boot/web]))
  (dev-setup)
  (mount-root))
