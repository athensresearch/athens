(ns athens.style
  (:require
    [garden.color :refer [opacify hex->hsl]]
    [garden.core :refer [css]]))


(def COLORS
  {:link-color         "#0075E1"
   :highlight-color    "#F9A132"
   :warning-color      "#D20000"
   :confirmation-color "#009E23"
   :header-text-color  "#322F38"
   :body-text-color    "#433F38"
   :panel-color        "#EFEDEB"
   :app-bg-color       "#FFFFFF"})


(def DEPTH-SHADOWS
  {:4                  "0px 1.6px 3.6px rgba(0, 0, 0, 0.13), 0px 0.3px 0.9px rgba(0, 0, 0, 0.1)"
   :8                  "0px 3.2px 7.2px rgba(0, 0, 0, 0.13), 0px 0.6px 1.8px rgba(0, 0, 0, 0.1)"
   :16                 "0px 6.4px 14.4px rgba(0, 0, 0, 0.13), 0px 1.2px 3.6px rgba(0, 0, 0, 0.1)"
   :64                 "0px 24px 60px rgba(0, 0, 0, 0.15), 0px 5px 12px rgba(0, 0, 0, 0.1)"})


(def HSL-COLORS
  (reduce-kv #(assoc %1 %2 (hex->hsl %3)) {} COLORS))


(def OPACITIES [0.1 0.25 0.5 0.75 1])


;; Base Styles

(defn base-styles
  []
  [:style (css
            [:body {:margin 0
                    :font-family "IBM Plex Sans, Sans-Serif"
                    :color (:body-text-color COLORS)
                    :font-size "16px"}]
            [:* {:box-sizing "border-box"}]
            [:h1 :h2 :h3 :h4 :h5 :h6 {:margin "0.2em 0"
                                      :color (:header-text-color COLORS)}]
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
            [:span
             [:.block-ref {:border-bottom [["1px" "solid" (:highlight-color COLORS)]]}
              [:&:hover {:background-color (opacify (:highlight-color HSL-COLORS) (first OPACITIES))
                         :cursor           "alias"}]]]
            [:.athena-result {:display "flex"
                              :padding "12px 32px 12px 32px"
                              :border-top "1px solid rgba(67, 63, 56, 0.2)"}
             [:&:hover {:background-color (:link-color COLORS) :cursor "pointer"}
              [:h4 {:color "rgba(255, 255, 255, 1)"}]
              [:span {:color "rgba(255, 255, 255, .9)"}]]])])
