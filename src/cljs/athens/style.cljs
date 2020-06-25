(ns athens.style
  (:require
    [garden.color :refer [opacify hex->hsl]]
    [stylefy.core :as stylefy]))


(def COLORS
  {:link-color         "#0075E1"
   :highlight-color    "#F9A132"
   :warning-color      "#D20000"
   :confirmation-color "#009E23"
   :header-text-color  "#322F38"
   :body-text-color    "#433F38"
   :panel-color        "#EFEDEB"
   :app-bg-color       "#FFFFFF"})


(def HSL-COLORS
  (reduce-kv #(assoc %1 %2 (hex->hsl %3)) {} COLORS))


(def DEPTH-SHADOWS
  {:4                  "0px 1.6px 3.6px rgba(0, 0, 0, 0.13), 0px 0.3px 0.9px rgba(0, 0, 0, 0.1)"
   :8                  "0px 3.2px 7.2px rgba(0, 0, 0, 0.13), 0px 0.6px 1.8px rgba(0, 0, 0, 0.1)"
   :16                 "0px 6.4px 14.4px rgba(0, 0, 0, 0.13), 0px 1.2px 3.6px rgba(0, 0, 0, 0.1)"
   :64                 "0px 24px 60px rgba(0, 0, 0, 0.15), 0px 5px 12px rgba(0, 0, 0, 0.1)"})


(def OPACITIES
  {:opacity-lower  0.10
   :opacity-low    0.25
   :opacity-med    0.50
   :opacity-high   0.75
   :opacity-higher 0.85})


;; Color
;; Provide color keyword
;; (optional) Provide alpha value, either keyword or 0-1

(defn- return-color
  [c]
  (c COLORS))


(defn- return-color-with-alpha
  [c a]
  (if (keyword? a)
    (opacify (c HSL-COLORS) (a OPACITIES))
    (opacify (c HSL-COLORS) a)))


(defn color
  ([c] (return-color c))
  ([c a] (return-color-with-alpha c a)))


;; Base Styles

(stylefy/tag "body" {:background-color (color :app-bg-color)
                     :font-family "IBM Plex Sans, Sans-Serif"
                     :color (color :body-text-color)
                     :font-size "16px"
                     :line-height "1.5"
                     ::stylefy/manual [[:a {:color (color :link-color)}]
                                       [:h1 :h2 :h3 :h4 :h5 :h6 {:margin "0.2em 0"
                                                                 :color (color :header-text-color)}]
                                       [:h1 {:font-size "50px"
                                             :font-weight 600
                                             :line-height "65px"
                                             :letter-spacing "-0.03em"}]
                                       [:h2 {:font-size "38px"
                                             :font-weight 500
                                             :line-height "49px"
                                             :letter-spacing "-0.03em"}]
                                       [:h3 {:font-size "28px"
                                             :font-weight 500
                                             :line-height "36px"
                                             :letter-spacing "-0.02em"}]
                                       [:h4 {:font-size "21px"
                                             :line-height "27px"}]
                                       [:h5 {:font-size "12px"
                                             :font-weight 500
                                             :line-height "16px"
                                             :letter-spacing "0.08em"
                                             :text-transform "uppercase"}]
                                       [:.MuiSvgIcon-root {:font-size "24px"}]
                                       [:input {:font-family "inherit"}]
                                       [:img {:max-width "100%"
                                              :height "auto"}]]})


(stylefy/tag "*" {:box-sizing "border-box"})
