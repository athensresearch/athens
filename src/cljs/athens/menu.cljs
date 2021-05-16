(ns athens.menu
  (:require
    ["electron" :refer [shell]]))


(def isMac (= (.-platform js/process) "darwin"))


(def menu-template
  (clj->js (into [] (concat
                      (when isMac [{:label "Athens"
                                    :submenu [{:role "about"}
                                              {:type "separator"}
                                              {:role "services"}
                                              {:type "separator"}
                                              {:role "hide"}
                                              {:role "hideothers"}
                                              {:role "unhide"}
                                              {:type "separator"}
                                              {:role "quit"}]}])
                      [{:label "File"
                        :submenu [(if isMac
                                    {:role "close"}
                                    {:role "quit"})]}
                       {:label "Edit"
                        :submenu (concat [{:role "undo"}
                                          {:role "redo"}
                                          {:type "separator"}
                                          {:role "cut"}
                                          {:role "copy"}
                                          {:role "paste"}]
                                         (if isMac
                                           [{:role "pasteAndMatchStyle"}
                                            {:role "delete"}
                                            {:role "selectAll"}
                                            {:type "separator"}
                                            {:label "Speech"
                                             :submenu [{:role "startSpeaking"}
                                                       {:role "stopSpeaking"}]}]
                                           [{:role "delete"}
                                            {:type "separator"}
                                            {:role "selectAll"}]))}
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
                       {:label "Window"
                        :submenu (concat [{:role "minimize"}
                                          {:role "zoom"}]
                                         (if isMac
                                           [{:type "separator"}
                                            {:role "front"}
                                            {:type "separator"}
                                            {:role "window"}]
                                           [{:role "close"}]))}]
                      [(if isMac
                         {:role "help"
                          :submenu [{:label "Learn More"
                                     :click #(.openExternal shell "https://github.com/athensresearch/athens")}]}
                         {:label "Help"
                          :submenu [{:label "Learn More"
                                     :click #(.openExternal shell "https://github.com/athensresearch/athens")}]})]))))
