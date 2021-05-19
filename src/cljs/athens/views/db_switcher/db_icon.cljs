(ns athens.views.db-switcher.db-icon
  (:require [athens.views.db-switcher.sync-indicator :refer [sync-indicator]]))

(defn db-icon
  [{:keys [db status]}]
   [:svg.icon {:viewBox "0 0 24 24"}
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
      :paintOrder "stroke fill"
      :stroke "black"
      :strokeOpacity 0.25
      :strokeWidth "2"
      :textAnchor "middle"
      :vectorEffect "non-scaling-stroke"
      :x "50%"
      :y "75%"}
     (nth (:name db) 0)]
    (when status [sync-indicator {:status status}])])
