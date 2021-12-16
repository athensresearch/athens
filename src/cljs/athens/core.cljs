(ns athens.core
  (:require
    ["@sentry/integrations" :as integrations]
    ["@sentry/react" :as Sentry]
    ["@sentry/tracing" :as tracing]
    [athens.coeffects]
    [athens.common.logging :as log]
    [athens.components]
    [athens.config :as config]
    [athens.db :refer [dsdb]]
    [athens.effects]
    [athens.electron.core]
    [athens.events]
    [athens.interceptors]
    [athens.listeners :as listeners]
    [athens.router :as router]
    [athens.style :as style]
    [athens.subs]
    [athens.util :as util]
    [athens.views :as views]
    [datalog-console.integrations.datascript :as datalog-console]
    [goog.dom :refer [getElement]]
    [re-frame.core :as rf]
    [reagent.dom :as r-dom]))


(goog-define SENTRY_DSN "")


(defn dev-setup
  []
  (when config/debug?
    (log/info "dev mode")))


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
                                               (new (.. integrations -CaptureConsole)
                                                    (clj->js {:levels ["error" "assert"]}))
                                               (new (.. integrations -ReportingObserver)
                                                    (clj->js {:types ["crash"]}))]
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


(defn init-styles
  []
  (util/add-body-classes (util/app-classes {:os        (util/get-os)
                                            :electron? (util/electron?)})))


(defn boot-evts
  []
  (if (util/electron?)
    [:boot/desktop]
    [:boot/web]))


(rf/reg-event-fx
  :boot
  (fn [_ _]
    {:dispatch (boot-evts)}))


(defn init
  []
  (set-global-alert!)
  (init-sentry)
  (init-ipcRenderer)
  (style/init)
  (init-styles)
  (listeners/init)
  (when config/debug?
    (datalog-console/enable! {:conn dsdb}))
  (rf/dispatch-sync (boot-evts))
  (dev-setup)
  (mount-root))
