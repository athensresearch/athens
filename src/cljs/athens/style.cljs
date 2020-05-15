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
                         {:font-family "sans-serif"}
                         [:.linked-reference
                          {:background-color "lightblue" :margin "15px 0px" :padding 5}]
                         [:.unlinked-reference
                          {:background-color "lightblue" :margin "15px 0px" :padding 5}]])
                   (css :.pages-table
                        [:th {:font-weight "bold"
                              :min-width "11em"
                              }])])]))
