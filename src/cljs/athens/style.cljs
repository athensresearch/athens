(ns athens.style (:require [clojure.string :as str]
                           [garden.core :refer [css]]))

(defn loading-css []
  (fn []
    [:style (css [:body {:font-family "sans-serif"
                         :font-size "1.3rem"}])]))

(defn main-css []
  (fn []
    [:style
     (str/join "" [(css [:body
                         {:font-family "sans-serif"}])
                   (css [:.arrow-down
                         {:width 0
                          :height 0
                          :border-left  "5px solid transparent"
                          :border-right "5px solid transparent"
                          :border-top   "5px solid black"
                          :cursor "pointer"
                          :margin-top 4}])
                   (css [:.arrow-right
                         {:width 0
                          :height 0
                          :border-top  "5px solid transparent"
                          :border-bottom "5px solid transparent"
                          :border-left   "5px solid black"
                          :cursor "pointer"
                          :margin-right 4}])
                   (css :.lnk-refs-wrap
                        :.lnk-refs
                        [:.lnk-ref
                         {:background-color "lightblue"
                          :margin "15px 0px"
                          :padding 5}])
                   (css :.unl-refs-wrap
                        :.unl-refs
                        [:.unl-ref
                         {:background-color "lightblue"
                          :margin "15px 0px"
                          :padding 5}])
                   (css :.pages-table
                        [:th {:font-weight "bold"
                              :min-width "11em"}])])]))
