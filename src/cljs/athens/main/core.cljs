^:cljstyle/ignore
(ns athens.main.core
  (:require
    ["electron" :refer [app BrowserWindow Menu ipcMain shell]]
    ["electron-updater" :refer [autoUpdater]]
    ["electron-window-state" :as electron-window-state]
    [athens.electron.utils :as electron.utils]
    [athens.menu :refer [menu-template]]))


;; This flag controls whether we check for updates on startup.
;; We use electron-updater with github releases for updates.
;; This setup does not support the use of electron-updater release channels,
;; and instead supports only the release/prerelease distinction.
;;
;; If AUTO_UPDATE is false, the app will never check for updates.
;; If AUTO_UPDATE is true:
;; - if the app version contains prerelease components (e.g. 1.0.0-beta.20, where beta.20
;;   is the prerelease component), electron-updater will check github releases+prereleases,
;;   otherwise it will just check releases.
;; - releases without latest*.yml files are ignored (these files are controlled via the
;;   electron-builder -c.publish.publishAutoUpdate=true arg).
;; - if there's a bigger (according to semver) release, show an update prompt.
;;
;; These flags can be set on .github/workflows/build.yml:
;; - AUTO_UPDATE can be set on the build-app job together with other defines
;; - -c.publish.publishAutoUpdate=true can be set on the release-electron job on the
;;   action-electron-builder args
(goog-define AUTO_UPDATE true)

(set! (.. autoUpdater -logger) (electron.utils/log))
(set! (.. autoUpdater -logger -transports -file -level) "info")
(set! (.. autoUpdater -autoDownload) false)
(set! (.. autoUpdater -autoInstallOnAppQuit) false)

(.. (electron.utils/log) (info (str "Athens starting... "  "version=" (.getVersion app))))


(defonce main-window (atom nil))
(defonce update-available? (atom nil))


(defn send-status-to-window
  [text]
  (.. (electron.utils/log) (info text))
  (when @main-window
    (.. ^js @main-window -webContents (send text))))


(defn init-electron-handlers
  []
  (doto ipcMain
    (.handle (:toggle-max-or-min-win-channel electron.utils/ipcMainChannels)
             (fn [_ toggle-min?]
               (when-let [active-win (.getFocusedWindow BrowserWindow)]
                 (if toggle-min?
                   (if (.isMinimized active-win)
                     (.restore active-win)
                     (.minimize active-win))
                   (if (.isMaximized active-win)
                     (.unmaximize active-win)
                     (.maximize active-win))))))
    (.handle (:close-win-channel electron.utils/ipcMainChannels)
             (fn []
               (.quit app)))
    (.handle (:exit-fullscreen-win-channel electron.utils/ipcMainChannels)
             (fn []
               (when-let [active-win (.getFocusedWindow BrowserWindow)]
                 (.setFullScreen active-win false)))))

  ;; Future intent to refactor statup to use startup and teardown effects
  ;; Below is an example of the teardown effect for this init fn
  ;; #(doall (.removeHandler ipcMain toggle-max-or-min-win-channel)
  ;;         (.removeHandler ipcMain close-win-channel)
  ;;         (.removeHandler ipcMain exit-fullscreen-win-channel))
  )


(def quitting (atom false))


(defn init-browser
  []
  (let [main-window-state (electron-window-state #js {:defaultWidth 800
                                                      :defaultHeight 600})]
    (reset! main-window (BrowserWindow.
                          (clj->js {:x (.-x main-window-state)
                                    :y (.-y main-window-state)
                                    :width (.-width main-window-state)
                                    :height (.-height main-window-state)
                                    :minWidth 650 ; Minimum width before clipping in toolbar
                                    :minHeight 300
                                    :backgroundColor "#1A1A1A"
                                    :autoHideMenuBar true
                                    :frame false
                                    :titleBarStyle "hidden"
                                    :trafficLightPosition {:x 19, :y 36}
                                    :webPreferences {:contextIsolation false
                                                     :nodeIntegration true
                                                     :worldSafeExecuteJavaScript true
                                                     ;; Using the remote module is slow can can lead to suble race conditions.
                                                     ;; https://nornagon.medium.com/electrons-remote-module-considered-harmful-70d69500f31
                                                     ;; If we're seeing weird race conditions on node modules, check this article.
                                                     :enableRemoteModule true
                                                     ;; Remove OverlayScrollbars and instances of `overflow-y: overlay`
                                                     ;; after `scollbar-gutter` is implemented in browsers.
                                                     :enableBlinkFeatures 'OverlayScrollbars'
                                                     :nodeIntegrationWorker true}})))
    (.manage main-window-state @main-window)
    ;; Path is relative to the compiled js file (main.js in our case)
    (.loadURL ^js @main-window (str "file://" js/__dirname "/public/index.html"))
    (.on ^js @main-window "closed" #(reset! main-window nil))
    ;; On mac, hide the window instead of closing to keep transient state.
    ;; https://stackoverflow.com/a/45156004/2116927
    ;; Also see remaining code from the example in the `main` fn below.
    (.on ^js @main-window "close" (fn [e]
                                    (when (and (= js/process.platform "darwin")
                                               (not @quitting))
                                      (.. e preventDefault)
                                      (.. ^js @main-window hide))))
    (.. ^js @main-window -webContents (on "new-window" (fn [e url]
                                                         (.. e preventDefault)
                                                         (.. shell (openExternal url)))))))


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
  (.on app "before-quit" #(reset! quitting true))
  (.on app "activate" (fn []
                        (if (nil? @main-window)
                          (init-browser)
                          (.show @main-window))))
  (.on app "ready" (fn []
                     (init-ipcMain)
                     (init-menu)
                     (init-browser)
                     (init-electron-handlers)
                     (when AUTO_UPDATE
                       (init-updater)
                       (.. autoUpdater checkForUpdates)))))
