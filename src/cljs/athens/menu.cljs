(ns athens.menu
  (:require
    ["electron" :refer [shell]]))


(def isMac (= (.-platform js/process) "darwin"))


(def menu-template
  (clj->js (into [] (concat
                      (when isMac [{:role "appMenu"}])
                      [{:role "fileMenu"}
                       {:role "editMenu"}
                       {:label "View"
                        :submenu [{:role "reload"}
                                  {:role "forceReload"}
                                  {:role "toggleDevTools"}
                                  {:type "separator"}
                                  ;; Default zoom tools disabled so we can own
                                  ;; zoom control internally. It would be better
                                  ;; to remap these items to commands which
                                  ;; perform the described action, using our
                                  ;; internal zoom events.
                                  ;;  {:role "resetZoom"}
                                  ;;  {:role "zoomIn"}
                                  ;;  {:role "zoomOut"}
                                  {:type "separator"}
                                  {:role "togglefullscreen"}]}
                       {:role "windowMenu"}]
                      [(if isMac
                         {:role "help"
                          :submenu [{:label "Learn More"
                                     :click #(.openExternal shell "https://github.com/athensresearch/athens")}]}
                         {:label "Help"
                          :submenu [{:label "Learn More"
                                     :click #(.openExternal shell "https://github.com/athensresearch/athens")}]})]))))
