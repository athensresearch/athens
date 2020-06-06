(ns athens.style
  (:require
    [athens.lib.dom.attributes :refer [with-styles]]
    [garden.color :refer [opacify hex->hsl]]
    [garden.core :refer [css]]
    [garden.selectors]))


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


(defn style-guide-css
  []
  [:style (css
            [:body {:margin 0}]
            [:* {:font-family "IBM Plex Sans, Sans-Serif"
                 :box-sizing "border-box"}]
            [:p :span {:color (:body-text-color COLORS)}]
            [:h1 :h2 :h3 :h4 :h5 :h6 {:margin "0.2em 0"
                                      :color (:header-text-color COLORS)}]
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
            [:span {:font-size   "16px"
                    :line-height "32px"}
             [:.block-ref {:border-bottom [["1px" "solid" (:highlight-color COLORS)]]}
              [:&:hover {:background-color (opacify (:highlight-color HSL-COLORS) (first OPACITIES))
                         :cursor           "alias"}]]]
            [:tbody
             [:tr
              [:&:hover {:background-color (opacify (:panel-color HSL-COLORS) (first OPACITIES))}]]]
            [:button :.input-file {:cursor           "pointer"
                                   :padding          "6px 10px"
                                   :border-radius    "4px"
                                   :font-weight      "500"
                                   :border           "none"
                                   :color            "rgba(50, 47, 56, 1)"
                                   :background-color "transparent"}
             [:&:disabled {:color "rgba(0, 0, 0, 0.3)"
                           :background-color "#EFEDEB"
                           :cursor "default"}]
             [:&:hover {:background-color "#EFEDEB"}]
             [:&:active {:color            "rgba(0, 117, 225)"
                         :background-color "rgba(0, 117, 225, 0.1)"}]
             [:&.primary {:color            "rgba(0, 117, 225)"
                          :background-color "rgba(0, 117, 225, 0.1)"}
              [:&:hover {:background-color "rgba(0, 117, 225, 0.25)"}]
              [:&:active {:color "white"
                          :background-color "rgba(0, 117, 225, 1)"}]]]
            [:.athena-result {:display "flex"
                              :padding "12px 32px 12px 32px"
                              :border-top "1px solid rgba(67, 63, 56, 0.2)"}
             [:&:hover {:background-color (:link-color COLORS) :cursor "pointer"}
              [:h4 {:color "rgba(255, 255, 255, 1)"}]
              [:span {:color "rgba(255, 255, 255, .9)"}]]])])
