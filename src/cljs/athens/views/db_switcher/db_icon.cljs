(ns athens.views.db-switcher.db-icon
  (:require
   [athens.views.db-switcher.sync-indicator :refer [sync-indicator]]))

(defn db-icon
  [{:keys [db status]}]
   [:svg.icon {:viewBox "0 0 24 24"
               :style {:font-size "16px"
                       ;;:overflow "visible"
                       }}
    [:rect
     {:stroke "var(--body-text-color---opacity-low)"
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
      ;; :paintOrder "stroke fill"
      ;; :stroke "black"
      ;; :strokeOpacity 0.25
      ;; :strokeWidth "2"
      :textAnchor "middle"
      :vectorEffect "non-scaling-stroke"
      :style {:text-transform "uppercase"}
      :x "50%"
      :y "75%"}
     (nth (:name db) 0)]
    (when status [sync-indicator {:status status}])])
