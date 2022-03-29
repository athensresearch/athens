(ns athens.electron.db-menu.status-indicator
  (:require
   ["@chakra-ui/react" :refer [Box Tooltip]]
   ["@material-ui/icons/CheckCircle" :default CheckCircle]
   ["@material-ui/icons/Error" :default Error]
   ["@material-ui/icons/Sync" :default Sync]))


(defn status-indicator
  [{:keys [status]}]
  [:> Box {:p 0
           :m 0
           :color (cond
                    (:closed status) "error"
                    (:running status) "foreground.primary"
                    :else "foreground.secondary")
           :fontSize "1em"
           :height "1em"
           :width "1em"
           :transform "translate(25%, 25%)"
           :position "absolute"
           :bottom 0
           :right 0
           :borderRadius "full"
           :sx {"svg" {:fontSize "1em"
                       :background "background.floor"
                       :borderRadius "full"}}}
   (cond
     (= status :closed) [:> Tooltip
                         {:label "Disconnected"}
                         [:> Error]]
     (= status :running) [:> Tooltip
                          {:label "Synced"}
                          [:> CheckCircle]]
     :else [:> Tooltip
            {:label "Synchronizing..."}
            [:> Sync]])])
