(ns athens.views.db-switcher.status-indicator
  (:require
   ["@material-ui/icons/CheckCircle" :default CheckCircle]
   ["@material-ui/icons/Error" :default Error]
   ["@material-ui/icons/Sync" :default Sync]
   [athens.style :refer [color]]
   [stylefy.core :as stylefy :refer [use-style]]))



(def status-icon-style
  {:background (color :background-minus-2)
   :border-radius "100%"
   :padding 0
   :margin 0
   :height "12px !important"
   :width "12px !important"
   :position "absolute"
   :bottom "0"
   :right "0"
   ::stylefy/manual [[".:running"]]})

(defn status-indicator
  [{:keys [status]}]
  [:div.status-indicator (use-style status-icon-style
                                    {:class (str status)})
   (cond
     (= status :closed)
     [:> Error (merge {:style {:color (color :error-color)}
                       :title "Disconnected"})]
     (= status :running)
     [:> CheckCircle (merge (use-style status-icon-style)
                            {:style {:color (color :confirmation-color)}
                             :title "Synced"})]
     :else [:> Sync (merge (use-style status-icon-style)
                           {:style {:color (color :highlight-color)}
                            :title "Synchronizing..."})])])