(ns athens.electron.db-menu.db-icon
  (:require
    ["@chakra-ui/react" :refer [Box]]
    [athens.electron.db-menu.status-indicator :refer [status-indicator]]))


(defn db-icon
  [{:keys [db status]}]
  [:> Box {:class "icon"
           :position "relative"
           :flex "0 0 auto"
           :width "1.75em"
           :height "1.75em"
           :sx {"text" {:fontSize "16px"}}}
   [:> Box {:as "svg"
            :viewBox "0 0 24 24"
            :margin 0}
    [:> Box
     {:as "rect"
      :fill "var(--link-color)"
      :height "100%"
      :width "100%"
      :rx "4"
      :x "0"
      :y "0"}]
    [:> Box
     {:as "text"
      :fill "white"
      :fontSize "100%"
      :fontWeight "bold"
      :textAnchor "middle"
      :vectorEffect "non-scaling-stroke"
      :style {:text-transform "uppercase"}
      :x "50%"
      :y "75%"}
     (nth (:name db) 0)]]
   (when (and status (not= status :running)) [status-indicator {:status status}])])
