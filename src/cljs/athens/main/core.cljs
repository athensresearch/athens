(ns athens.main.core
  (:require
    ["electron" :refer [app BrowserWindow ipcMain shell]]
    ["electron-updater" :refer [autoUpdater]]))


(def log (js/require "electron-log"))

(set! (.. autoUpdater -logger) log)
(set! (.. autoUpdater -logger -transports -file -level) "info")
(set! (.. autoUpdater -channel) "beta")
(set! (.. autoUpdater -autoDownload) false)
(set! (.. autoUpdater -autoInstallOnAppQuit) false)

(.. log (info (str "Athens starting... "  "version=" (.getVersion app))))


(defonce main-window (atom nil))
(defonce update-available? (atom nil))


(defn send-status-to-window
  [text]
  (.. log (info text))
  (when @main-window
    (.. ^js @main-window -webContents (send text))))


(defn init-browser
  []
  (reset! main-window (BrowserWindow.
                        (clj->js {:width 800
                                  :height 600
                                  :backgroundColor "#1A1A1A"
                                  :autoHideMenuBar true
                                  :enableRemoteModule true
                                  :webPreferences {:contextIsolation false
                                                   :nodeIntegration true
                                                   :worldSafeExecuteJavaScript true
                                                   :enableRemoteModule true
                                                   :nodeIntegrationWorker true}})))
  ; Path is relative to the compiled js file (main.js in our case)
  (.loadURL ^js @main-window (str "file://" js/__dirname "/public/index.html"))
  (.on ^js @main-window "closed" #(reset! main-window nil))
  (.. ^js @main-window -webContents (on "new-window" (fn [e url]
                                                       (.. e preventDefault)
                                                       (.. shell (openExternal url))))))


(defn init-updater
  []
  (.on autoUpdater "checking-for-update"
       (fn []
         (send-status-to-window "Checking for update...")))

  (.on autoUpdater "update-available"
       (fn [_]
         (reset! update-available? true)
         (send-status-to-window "Update available.")))

  (.on autoUpdater "update-not-available"
       (fn [_]
         (reset! update-available? false)
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
         (send-status-to-window "Update downloaded.")
         (.. autoUpdater quitAndInstall))))


(defn init-ipcMain
  []
  (.on ipcMain "check-update"
       (fn [e _]
         (set! (.. e -returnValue) @update-available?)))
  (.on ipcMain "confirm-update"
       (fn [_ _]
         (.. autoUpdater downloadUpdate))))


(defn main
  []
  (.on app "window-all-closed" #(when-not (= js/process.platform "darwin")
                                  (.quit app)))
  (.on app "activate" (fn []
                        (when (nil? @main-window)
                          (init-browser))))
  (.on app "ready" (fn []
                     (init-ipcMain)
                     (init-browser)
                     (init-updater)
                     (.. autoUpdater checkForUpdates))))
