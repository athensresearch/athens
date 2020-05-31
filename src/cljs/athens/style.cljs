(ns athens.style
  (:require
    [athens.lib.dom.attributes :refer [with-classes with-style]]
    [garden.core :refer [css]]
    [garden.selectors :refer [nth-child]]))

;; Styles for the loading screen
(defn loading-css
  []
  [:style (css
            [:body {:font-family "sans-serif"
                    :font-size "1.3rem"}])])

;; Styles for the main app.
(defn main-css
  []
  [:style
   (css [:body {:font-family "sans-serif"}]
        [:.pages-table
         {:width "60%" :margin-top 20}
         [:th {:font-weight "bold"
               :min-width "11em"}]
         [:td {:padding "2px"}]
         [:tr
          [:&
           [(garden.selectors/& (nth-child :even)) {:background-color "#e8e8e8"}]]]
         [:& {:border-spacing "0"}]]
        [:.left-sidebar [:li {:padding-top "0.27em" :padding-bottom "0.27em"}]
         {:padding 0
          :margin 0
          :list-style-type "none"}])])


;; Functions that add styles to an element. Prefer to directly add styles when possible, otherwise
;; use classes, and style above.


(def +left-sidebar
  (with-classes "left-sidebar"))


(def +pages-table
  (with-classes "pages-table"))


(def +unknown-date
  (with-style {:color "#595959"}))
