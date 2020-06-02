(ns athens.style
  (:require
    [athens.lib.dom.attributes :refer [with-classes with-style with-styles]]
    [garden.color :refer [opacify hex->hsl]]
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


(def COLORS
  {:link-color         "#0075E1"
   :highlight-color    "#F9A132"
   :warning-color      "#D20000"
   :confirmation-color "#009E23"
   :header-text-color  "#322F38"
   :body-text-color    "#433F38"
   :panel-color        "#EFEDEB"
   :app-bg-color       "#FFFFFF"})


(def OPACITIES [0.1 0.25 0.5 0.75 1])


;; Functions that add styles to an element. Prefer to directly add styles when possible, otherwise
;; use classes, and style above.

;; Color Functions

(def +link-bg
  (with-styles {:background-color (:link-color COLORS)}))

;; Shadow Functions

(def +text-shadow
  (with-styles {:text-shadow "0px 8px 20px rgba(0, 0, 0, 0.1)"}))


(def +box-shadow
  (with-styles {:box-shadow "0px 8px 20px rgba(0, 0, 0, 0.1)"}))

;; Flex Functions

(def +flex-center
  (with-styles {:display "flex" :justify-content "center" :align-items "center"}))


(def +flex-space-between
  (with-styles {:display "flex" :justify-content "space-between" :align-items "center"}))


(def +flex-space-around
  (with-styles {:display "flex" :justify-content "space-around" :align-items "center"}))


(def +flex-wrap
  (with-styles {:display "flex" :flex-wrap "wrap"}))


(def +flex-column
  (with-styles {:display "flex" :flex-direction "column"}))

;; Class Functions

(def +left-sidebar
  (with-classes "left-sidebar"))


(def +pages-table
  (with-classes "pages-table"))


(def +unknown-date
  (with-style {:color "#595959"}))


;; Style Guide


(defn style-guide-css
  []
  [:style (css
            [:* {:font-family "IBM Plex Sans, Sans-Serif"}]
            [:h1 :h2 :h3 :h4 :h5 :h6 {:margin "0.2em"}]
            [:h1 {:font-size "50px"
                  :font-weight 600
                  :line-height "65px"
                  :line-spacing "-0.03em"}]
            [:h2 {:font-size "38px"
                  :font-weight 500
                  :line-height "49px"
                  :line-spacing "-0.03em"}]
            [:h3 {:font-size "28px"
                  :font-weight 500
                  :line-height "36px"
                  :line-spacing "-0.02em"}]
            [:h4 {:font-size "21px"
                  :line-height "27px"}]
            [:h5 {:font-size "12px"
                  :font-weight 500
                  :line-height "16px"
                  :line-spacing "0.08em"
                  :text-transform "uppercase"}]
            [:span {:font-size "16px"
                    :line-height "32px"}]
            [:span.block-ref {:font-size     "16px"
                              :line-height   "32px"
                              :border-bottom [["1px" "solid" (:highlight-color COLORS)]]}
             [:&:hover {:background-color (opacify (hex->hsl (:highlight-color COLORS)) (second OPACITIES))
                        :cursor           "alias"}]])])
