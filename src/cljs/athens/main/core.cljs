(ns athens.main.core
  (:require
    [athens.menu :refer [menu-template]]
    [athens.util :refer [ipcMainChannels]]
    ["electron" :refer [app BrowserWindow Menu ipcMain shell]]
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


(defn init-electron-handlers
  []
  (doto ipcMain
    (.handle (:toggle-max-or-min-win-channel ipcMainChannels)
             (fn [_ toggle-min?]
               (when-let [active-win (.getFocusedWindow BrowserWindow)]
                 (if toggle-min?
                   (if (.isMinimized active-win)
                     (.restore active-win)
                     (.minimize active-win))
                   (if (.isMaximized active-win)
                     (.unmaximize active-win)
                     (.maximize active-win))))))
    (.handle (:close-win-channel ipcMainChannels)
             (fn []
               (.quit app)))
    (.handle (:exit-fullscreen-win-channel ipcMainChannels)
             (fn []
               (when-let [active-win (.getFocusedWindow BrowserWindow)]
                 (.setFullScreen active-win false)))))

    ;; Future intent to refactor statup to use startup and teardown effects
    ;; Below is an example of the teardown effect for this init fn
    ;; #(doall (.removeHandler ipcMain toggle-max-or-min-win-channel)
    ;;         (.removeHandler ipcMain close-win-channel)
    ;;         (.removeHandler ipcMain exit-fullscreen-win-channel))
  )


(defn init-browser
  []
  (reset! main-window (BrowserWindow.
                        (clj->js {:width 800
                                  :height 600
                                  :minWidth 400
                                  :minHeight 300
                                  :backgroundColor "#1A1A1A"
                                  :autoHideMenuBar true
                                  :frame false
                                  :titleBarStyle "hidden"
                                  :trafficLightPosition {:x 19, :y 36}
                                  :webPreferences {:contextIsolation false
                                                   :nodeIntegration true
                                                   :worldSafeExecuteJavaScript true
                                                   :enableRemoteModule true
                                                   ;; Remove OverlayScrollbars and instances of `overflow-y: overlay`
                                                   ;; after `scollbar-gutter` is implemented in browsers.
                                                   :enableBlinkFeatures 'OverlayScrollbars'
                                                   :nodeIntegrationWorker true}})))
  ;; Path is relative to the compiled js file (main.js in our case)
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


(defn init-menu
  []
  (.setApplicationMenu Menu (.buildFromTemplate Menu menu-template)))


(defn main
  []
  (.on app "window-all-closed" #(when-not (= js/process.platform "darwin")
                                  (.quit app)))
  (.on app "activate" (fn []
                        (when (nil? @main-window)
                          (init-browser))))
  (.on app "ready" (fn []
                     (init-ipcMain)
                     (init-menu)
                     (init-browser)
                     (init-electron-handlers)
                     (init-updater)
                     (.. autoUpdater checkForUpdates))))
