(ns athens.views.db-switcher.db-icon
  (:require
   [athens.views.db-switcher.sync-indicator :refer [sync-indicator]]))

(defn db-icon
  [{:keys [db status]}]
   [:svg.icon {:viewBox "0 0 24 24"
               :style {:font-size "16px"}}
    [:rect
     {:stroke "var(--body-text-color---opacity-lower)"
      :fill "var(--background-color)"
      :height "23"
      :rx "4"
      :width "23"
      :x "0.5"
      :y "0.5"}]
    [:text
     {:fill "var(--body-text-color)"
      :fontSize "100%"
      :fontWeight "bold"
      :textAnchor "middle"
      :vectorEffect "non-scaling-stroke"
      :style {:text-transform "uppercase"}
      :x "50%"
      :y "75%"}
     (nth (:name db) 0)]
    (when status [sync-indicator {:status status}])])
