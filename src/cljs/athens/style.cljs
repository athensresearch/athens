(ns athens.style
  (:require
    [athens.config :as config]
    [athens.util :as util]
    [garden.color :refer [opacify hex->hsl]]
    [stylefy.core :as stylefy]))


(def THEME-DARK
  {:link-color          "#2399E7"
   :highlight-color     "#FBBE63"
   :text-highlight-color "#FBBE63"
   :warning-color       "#DE3C21"
   :confirmation-color  "#189E36"
   :header-text-color   "#BABABA"
   :body-text-color     "#AAA"
   :border-color        "hsla(32, 81%, 90%, 0.08)"
   :background-minus-1  "#151515"
   :background-minus-2  "#111"
   :background-color    "#1A1A1A"
   :background-plus-1   "#222"
   :background-plus-2   "#333"

   :graph-control-bg    "#272727"
   :graph-control-color "white"
   :graph-node-normal   "#909090"
   :graph-node-hlt      "#FBBE63"
   :graph-link-normal   "#323232"

   :error-color         "#fd5243"})


(def THEME-LIGHT
  {:link-color          "#0075E1"
   :highlight-color     "#F9A132"
   :text-highlight-color "#ffdb8a"
   :warning-color       "#D20000"
   :confirmation-color  "#009E23"
   :header-text-color   "#322F38"
   :body-text-color     "#433F38"
   :border-color        "hsla(32, 81%, 10%, 0.08)"
   :background-plus-2   "#fff"
   :background-plus-1   "#fbfbfb"
   :background-color    "#F6F6F6"
   :background-minus-1  "#FAF8F6"
   :background-minus-2  "#EFEDEB"
   :graph-control-bg    "#f9f9f9"
   :graph-control-color "black"
   :graph-node-normal   "#909090"
   :graph-node-hlt      "#0075E1"
   :graph-link-normal   "#cfcfcf"

   :error-color         "#fd5243"})


(def DEPTH-SHADOWS
  {:4  "0 2px 4px rgba(0, 0, 0, 0.2)"
   :8  "0 4px 8px rgba(0, 0, 0, 0.2)"
   :16 "0 4px 16px rgba(0, 0, 0, 0.2)"
   :64 "0 24px 60px rgba(0, 0, 0, 0.2)"})


(def OPACITIES
  {:opacity-lower  0.10
   :opacity-low    0.25
   :opacity-med    0.50
   :opacity-high   0.75
   :opacity-higher 0.85})


;; Based on Bootstrap's excellent Z-index set
(def ZINDICES
  {:zindex-dropdown       1000
   :zindex-sticky         1020
   :zindex-fixed          1030
   :zindex-modal-backdrop 1040
   :zindex-modal          1050
   :zindex-popover        1060
   :zindex-tooltip        1070})


;; Color
(defn color
  "Turns a color and optional opacity into a CSS variable.
  Only accepts keywords."
  ([variable]
   (when (keyword? variable)
     (str "var(--"
          (symbol variable)
          ")")))
  ([variable alpha]
   (when (and (keyword? variable) (keyword? alpha))
     (str "var(--"
          (symbol variable)
          "---"
          (symbol alpha)
          ")"))))


;; Base Styles

(def base-styles
  {:background-color (color :background-color)
   :font-family      "IBM Plex Sans, Sans-Serif"
   :color            (color :body-text-color)
   :font-size        "16px"                                 ;; Sets the Rem unit to 16px
   :line-height      "1.5"
   ::stylefy/manual  [[:a {:color (color :link-color)}]
                      [:h1 :h2 :h3 :h4 :h5 :h6 {:margin      "0.2em 0"
                                                :line-height "1.3"
                                                :color       (color :header-text-color)}]
                      [:h1 {:font-size      "3.125em"
                            :font-weight    600
                            :letter-spacing "-0.03em"}]
                      [:h2 {:font-size      "2.375em"
                            :font-weight    500
                            :letter-spacing "-0.03em"}]
                      [:h3 {:font-size      "1.75em"
                            :font-weight    500
                            :letter-spacing "-0.02em"}]
                      [:h4 {:font-size "1.3125em"}]
                      [:h5 {:font-size      "0.75em"
                            :font-weight    500
                            :line-height    "1em"
                            :letter-spacing "0.08em"
                            :text-transform "uppercase"}]
                      [:.MuiSvgIcon-root {:font-size "1.5rem"}]
                      [:input {:font-family "inherit"}]
                      [:mark {:background-color (color :text-highlight-color)
                              :border-radius "0.25rem"
                              :color "#000"}]
                      [:kbd {:text-transform "uppercase"
                             :font-family    "inherit"
                             :font-size      "0.85em"
                             :letter-spacing "0.05em"
                             :font-weight    600
                             :display        "inline-flex"
                             :background     (color :body-text-color :opacity-lower)
                             :border-radius  "0.25rem"
                             :padding        "0.25em 0.5em"}]
                      [:img {:max-width "100%"
                             :height    "auto"}]
                      [":focus" {:outline-width 0}]
                      [":focus-visible" {:outline-width "1px"}]]})


(def app-styles
  {:overflow "hidden"
   :height   "100vh"
   :width    "100vw"})


(def codemirror-styles
  {:z-index 10
   :height  "min-content"})


(defn permute-color-opacities
  "Permutes all colors and opacities.
  There are 5 opacities and 12 colors. There are 72 keys (includes default opacity, 1.0)"
  [theme]
  (->> theme
       (mapcat (fn [[color-k color-v]]
                 (concat [(keyword (str "--" (symbol color-k)))
                          color-v]
                         (mapcat (fn [[opacity-k opacity-v]]
                                   [(keyword (str "--"
                                                  (symbol color-k)
                                                  "---"
                                                  (symbol opacity-k)))
                                    (opacify (hex->hsl color-v) opacity-v)])
                                 OPACITIES))))
       (apply hash-map)))


(defn init
  []
  (stylefy/init)
  (stylefy/tag "html" base-styles)
  (stylefy/tag "*" {:box-sizing "border-box"})
  (stylefy/class "CodeMirror" codemirror-styles)
  (let [permute-light (permute-color-opacities THEME-LIGHT)
        permute-dark  (permute-color-opacities THEME-DARK)]
    (stylefy/tag ":root" (merge permute-light
                                {::stylefy/media {{:prefers-color-scheme "dark"} permute-dark}})))
  ;; hide re-frame-10x by default
  (when config/debug?
    (util/hide-10x)))
