(ns athens.style (:require [garden.core :refer [css]]))

(defn loading-css
  []
  [:style (css
           [:body {:font-family "sans-serif"
                   :font-size "1.3rem"}])])

(defn main-css
  []
  [:style
   (css [:body {:font-family "sans-serif"}]
        [:.pages-table [:th {:font-weight "bold"
                             :min-width "11em"}]]
        [:.unknown-date {:color "#595959"}])])
