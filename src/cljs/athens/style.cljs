(ns athens.style (:require [garden.core :refer [css]]
                           [garden.selectors :refer [nth-child]]))

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
                             :min-width "11em"}]
         [:td {:padding "2px"}]
         [:tr
          [:&
           [(garden.selectors/& (nth-child :even)) {:background-color "#e8e8e8"}]]]
         [:& {:border-spacing "0"}]]
        [:.unknown-date {:color "#595959"}]
        [:.left-sidebar [:li {:padding-top "0.27em" :padding-bottom "0.27em"}]
         {:padding 0
          :margin 0
          :list-style-type "none"}])])
