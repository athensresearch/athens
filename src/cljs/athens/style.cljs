(ns athens.style (:require  [garden.core :refer [css]]))

(defn loading-css
  []
  (fn []
    [:style (css [
      :body {:font-family "sans-serif"}
      :#loading-text {:font-size "1.3rem"}
    ])]
  )
)

(defn main-css
  []
  (fn []
    [:style (css [
      :body {:font-family "sans-serif"}
    ])]
  )
)
