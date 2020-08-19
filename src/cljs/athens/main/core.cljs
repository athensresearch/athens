(ns athens.main.core
  (:require
    ["electron" :refer [app BrowserWindow #_crashReporter]]
    ["electron-updater" :refer [autoUpdater]]))


(def log (js/require "electron-log"))

(set! (.. autoUpdater -logger) log)
(set! (.. autoUpdater -logger -transports -file -level) "info")
(set! (.. autoUpdater -channel) "beta")

;(.. log (info "AHHHHHHHHHHh"))
(.. log (info (str "Athens starting... "  "version=" (.getVersion app))))


(def main-window (atom nil))


(defn send-status-to-window
  [text]
  (.. log (info text))
  (.. @main-window -webContents (send text)))


(defn init-browser
  []
  (reset! main-window (BrowserWindow.
                        (clj->js {:width 800
                                  :height 600
                                  :autoHideMenuBar true
                                  :webPreferences {:nodeIntegration true
                                                   :nodeIntegrationWorker true}})))
  ; Path is relative to the compiled js file (main.js in our case)
  (.loadURL @main-window (str "file://" js/__dirname "/public/index.html"))
  (.on @main-window "closed" #(reset! main-window nil)))


(defn init-updater
  []
  (.on autoUpdater "checking-for-update"
       (fn []
         (send-status-to-window "Checking for update...")))

  (.on autoUpdater "update-available"
       (fn [_]
         (send-status-to-window "Update available.")))

  (.on autoUpdater "update-not-available"
       (fn [_]
         (send-status-to-window "Update not available.")))

  (.on autoUpdater "error"
       (fn [e]
         (send-status-to-window (str "Error in auto-updater. " e))))

  (.on autoUpdater "download-progress"
       (fn [progress-obj]
         (let [progress-clj (js->clj progress-obj)
               {:keys [bytesPerSecond percent transferred total]} progress-clj
               msg (str "Download speed: " bytesPerSecond
                        " - Downloaded " percent "%"
                        " (" transferred "/" total ")")]
           (send-status-to-window msg))))

  (.on autoUpdater "update-downloaded"
       (fn [_]
         (send-status-to-window "Update downloaded."))))


(defn main
  []
  ; CrashReporter can just be omitted
  ;(.start crashReporter
  ;        (clj->js
  ;          {:companyName "MyAwesomeCompany"
  ;           :productName "MyAwesomeApp"
  ;           :submitURL "https://example.com/submit-url"
  ;           :autoSubmit false}))

  (.on app "window-all-closed" #(when-not (= js/process.platform "darwin")
                                  (.quit app)))
  (.on app "ready" init-browser)
  (.on app "ready" (fn []
                     (init-updater)
                     (.. autoUpdater checkForUpdatesAndNotify))))

