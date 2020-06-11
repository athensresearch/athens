(ns athens.style
  (:require
    [athens.lib.dom.attributes :refer [with-styles]]
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


;; Functions that add styles to an element. Prefer to directly add styles when possible, otherwise
;; use classes, and style above.

;; Color Functions

(def +link-bg
  (with-styles {:background-color (:link-color COLORS)}))


(def +link
  (with-styles {:color (:link-color COLORS) :cursor "pointer"}))

;; Shadow Functions

(def +text-shadow
  (with-styles {:text-shadow "0px 8px 20px rgba(0, 0, 0, 0.1)"}))


(def +box-shadow
  (with-styles {:box-shadow "0px 8px 20px rgba(0, 0, 0, 0.1)"}))


(def +depth-64
  (with-styles {:box-shadow "0px 24px 60px rgba(0, 0, 0, 0.15), 0px 5px 12px rgba(0, 0, 0, 0.1)"}))


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


;; Text Align

(def +text-align-left
  (with-styles {:text-align "left"}))


(def +text-align-right
  (with-styles {:text-align "right"}))


;; Width and Height


(def +width-100
  (with-styles {:width "100%"}))


;; Class Functions

;; Style Guide

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
