(ns athens.main.core
  (:require
    ["electron" :refer [app BrowserWindow]]
    ["electron-updater" :refer [autoUpdater]]
    ["fs" :as fs]
    ["path" :as path]))

;; can't use :require expression for some reason
(def log (js/require "electron-log"))

;; declare doesn't work
(defn settings-filepath
  []
  (let [DOC-PATH (.getPath app "documents")
        filepath (.resolve path DOC-PATH "athens" "settings.json")]
    filepath))


(def main-window (atom nil))
(def settings (atom {}))
(def settings-file (settings-filepath))


(defn load-settings!
  "If ~/Documents/athens/settings.json exists, read it and reset settings atom.
  Else write that file with beta settings."
  []
  (if (.. fs (existsSync settings-file))
    (let [read-settings (-> (.. fs (readFileSync settings-file "utf8"))
                            js/JSON.parse
                            (js->clj :keywordize-keys true))]
      (reset! settings read-settings))
    (let [new-settings {:channel "beta"}]
      (reset! settings new-settings)
      (.. log (info "Settings not found, creating settings.json."))
      (.. fs (writeFileSync settings-file (-> new-settings clj->js js/JSON.stringify))))))


(defn set-channel!
  "Defaults to \"beta\" for now."
  []
  (let [channel (:channel @settings)]
    (cond
      (#{"alpha" "beta" "latest"} channel) (set! (.. autoUpdater -channel) channel)
      :else (do (.. log (warn "Settings have an invalid channel:" channel ". Defaulting to beta channel."))
                (set! (.. autoUpdater -channel) "beta")))))


(defn send-status-to-window
  [text]
  (.. log (info text))
  (.. ^js @main-window -webContents (send text)))


(defn init-browser
  []
  (reset! main-window (BrowserWindow.
                        (clj->js {:width 800
                                  :height 600
                                  :autoHideMenuBar true
                                  :enableRemoteModule true
                                  :webPreferences {:nodeIntegration true
                                                   :worldSafeExecuteJavaScript true
                                                   :enableRemoteModule true
                                                   :nodeIntegrationWorker true}})))
  ; Path is relative to the compiled js file (main.js in our case)
  (.loadURL ^js @main-window (str "file://" js/__dirname "/public/index.html"))
  (.on ^js @main-window "closed" #(reset! main-window nil)))


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
         (send-status-to-window "Update downloaded.")
         (.. autoUpdater quitAndInstall))))


(set! (.. autoUpdater -logger) log)
(set! (.. autoUpdater -logger -transports -file -level) "info")
(.. log (info (str "Athens starting... "  "version=" (.getVersion app))))
(load-settings!)
(set-channel!)


(defn main
  []
  (.on app "window-all-closed" #(when-not (= js/process.platform "darwin")
                                  (.quit app)))
  (.on app "ready" init-browser)
  (.on app "ready" (fn []
                     (init-updater)
                     (.. autoUpdater checkForUpdatesAndNotify))))
