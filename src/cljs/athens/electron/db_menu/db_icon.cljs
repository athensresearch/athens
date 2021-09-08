(ns athens.electron.db-menu.db-icon
  (:require
    [athens.electron.db-menu.status-indicator :refer [status-indicator]]
    [stylefy.core :as stylefy :refer [use-style]]))


(def db-icon-style
  {:position "relative"
   :width "1.75em"
   :height "1.75em"
   ::stylefy/manual [[:text {:font-size "16px"}]]})


(defn db-icon
  [{:keys [db status]}]
  [:div.icon (use-style db-icon-style)
   [:svg {:viewBox "0 0 24 24"}
    [:rect
     {:fill "var(--link-color)"
      :height "24"
      :rx "4"
      :width "24"
      :x "0"
      :y "0"}]
    [:text
     {:fill "white"
      :fontSize "100%"
      :fontWeight "bold"
      :textAnchor "middle"
      :vectorEffect "non-scaling-stroke"
      :style {:text-transform "uppercase"}
      :x "50%"
      :y "75%"}
     (nth (:name db) 0)]]
   (when status [status-indicator {:status status}])])
