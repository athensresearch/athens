(ns athens.style
  (:require
    [garden.color :refer [opacify hex->hsl]]
    [stylefy.core :as stylefy]))


;; (defn cssv
;;   ;; Helper for accessing CSS Custom Properties defined
;;   ;; in the application's :root
;;   ([variable]
;;    ;; When the variable is alone, reformat it and pass it through
;;   (str "var(--" variable ")"))
  
;;   ([variable alpha]
;;    ;; 1. Create a new color with the requested alpha value
;;    ;; 1a. If this is a new color add it to the :root, with a logical name like "link-color-50" for blue at 50% opacity
;;    ;; 2. Return the custom property name of the new color
;;    (str "var(--" variable "-" alpha ")")))


(def COLORS
  {:link-color         "#2399E7"
   :highlight-color    "#FBBE63"
   :warning-color      "#DE3C21"
   :confirmation-color "#189E36"
   :header-text-color  "#BABABA"
   :body-text-color    "#AAA"
   :border-color       "hsla(32, 81%, 90%, 0.08)"
   :background-minus-1 "#151515"
   :background-minus-2 "#111"
   :background-color   "#1A1A1A"
   :background-plus-1  "#222"
   :background-plus-2  "#333"})


(def THEME-LIGHT
  {:link-color         "#0075E1"
   :highlight-color    "#F9A132"
   :warning-color      "#D20000"
   :confirmation-color "#009E23"
   :header-text-color  "#322F38"
   :body-text-color    "#433F38"
   :border-color       "hsla(32, 81%, 10%, 0.08)"
   :background-plus-2  "#FFFFFF"
   :background-plus-1  "#FFFFFF"
   :background-color   "#FFFFFF"
   :background-minus-1 "#FAF8F6"
   :background-minus-2 "#EFEDEB"})


(def THEME-DARK
  {:link-color         "#2399E7"
   :highlight-color    "#FBBE63"
   :warning-color      "#DE3C21"
   :confirmation-color "#189E36"
   :header-text-color  "#BABABA"
   :body-text-color    "#AAA"
   :border-color       "hsla(32, 81%, 90%, 0.08)"
   :background-minus-1 "#151515"
   :background-minus-2 "#111"
   :background-color   "#1A1A1A"
   :background-plus-1  "#222"
   :background-plus-2  "#333"})


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


;; Based on Bootstrap's excellent Z-index set
(def ZINDICES
  {:zindex-dropdown          1000
   :zindex-sticky            1020
   :zindex-fixed             1030
   :zindex-modal-backdrop    1040
   :zindex-modal             1050
   :zindex-popover           1060
   :zindex-tooltip           1070})


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

(def base-styles
  {:background-color (color :background-color)
   :font-family      "IBM Plex Sans, Sans-Serif"
   :color            (color :body-text-color)
   :font-size        "16px"
   :line-height      "1.5"
   ::stylefy/manual [[:a {:color (color :link-color)}]
                     [:h1 :h2 :h3 :h4 :h5 :h6 {:margin "0.2em 0"
                                               :line-height "1.3"
                                               :color (color :header-text-color)}]
                     [:h1 {:font-size "3.125em"
                           :font-weight 600
                           :letter-spacing "-0.03em"}]
                     [:h2 {:font-size "2.375em"
                           :font-weight 500
                           :letter-spacing "-0.03em"}]
                     [:h3 {:font-size "1.75em"
                           :font-weight 500
                           :letter-spacing "-0.02em"}]
                     [:h4 {:font-size "1.3125em"}]
                     [:h5 {:font-size "0.75em"
                           :font-weight 500
                           :line-height "1rem"
                           :letter-spacing "0.08em"
                           :text-transform "uppercase"}]
                     [:.MuiSvgIcon-root {:font-size "24px"}]
                     [:input {:font-family "inherit"}]
                     [:img {:max-width "100%"
                            :height "auto"}]]})


(def app-styles
  {:overflow "hidden"
   :height   "100vh"
   :width    "100vw"})


(stylefy/tag "html" base-styles)


(stylefy/tag ":root" {:--link-color         (:link-color THEME-LIGHT)
                      :--highlight-color    (:highlight-color THEME-LIGHT)
                      :--warning-color      (:warning-color THEME-LIGHT)
                      :--confirmation-color (:confirmation-color THEME-LIGHT)
                      :--header-text-color  (:header-text-color THEME-LIGHT)
                      :--body-text-color    (:body-text-color THEME-LIGHT)
                      :--border-color       (:border-color THEME-LIGHT)
                      :--background-minus-1 (:background-minus-1 THEME-LIGHT)
                      :--background-minus-2 (:background-minus-2 THEME-LIGHT)
                      :--background-color   (:background-color THEME-LIGHT)
                      :--background-plus-1  (:background-plus-1 THEME-LIGHT)
                      :--background-plus-2  (:background-plus-2 THEME-LIGHT)
                      
                      ::stylefy/media {{:prefers-color-scheme "dark"} {:--link-color         (:link-color THEME-DARK)
                                                                       :--highlight-color    (:highlight-color THEME-DARK)
                                                                       :--warning-color      (:warning-color THEME-DARK)
                                                                       :--confirmation-color (:confirmation-color THEME-DARK)
                                                                       :--header-text-color  (:header-text-color THEME-DARK)
                                                                       :--body-text-color    (:body-text-color THEME-DARK)
                                                                       :--border-color       (:border-color THEME-DARK)
                                                                       :--background-minus-1 (:background-minus-1 THEME-DARK)
                                                                       :--background-minus-2 (:background-minus-2 THEME-DARK)
                                                                       :--background-color   (:background-color THEME-DARK)
                                                                       :--background-plus-1  (:background-plus-1 THEME-DARK)
                                                                       :--background-plus-2  (:background-plus-2 THEME-DARK)}}

                      })


(stylefy/tag "*" {:box-sizing "border-box"})
