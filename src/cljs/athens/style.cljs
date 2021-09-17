(ns athens.style
  (:require
    [athens.config :as config]
    [athens.util :as util]
    [garden.color :refer [opacify hex->hsl]]
    [re-frame.core :refer [reg-sub subscribe]]
    [stylefy.core :as stylefy]
    [stylefy.reagent :as stylefy-reagent]))


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



(reg-sub
  :zoom-level
  (fn [db _]
    (:zoom-level db)))


;; Zoom levels mirror Google Chrome browser zoom levels.
;; Levels determined by zooming Chrome in/out and recording the zoom percent.
(def zoom-levels
  {-5 50
   -4 67
   -3 75
   -2 80
   -1 90
   0 100
   1 110
   2 125
   3 133
   4 140
   5 150
   6 175
   7 200
   8 250
   9 300
   10 400
   11 500})


(def zoom-level-max 11)
(def zoom-level-min -5)


(defn get-zoom-pct
  [n]
  (zoom-levels n))


(defn zoom
  []
  (let [zoom-level (subscribe [:zoom-level])]
    {:style {:font-size (str (get-zoom-pct @zoom-level) "%")}}))


(defn unzoom
  []
  (let [zoom-level (subscribe [:zoom-level])]
    {:style {:font-size (str "calc(1 / " (get-zoom-pct @zoom-level) " * 100 * 100%)")}}))


(defn init
  []
  (stylefy/init {:dom (stylefy-reagent/init)})
  ;; hide re-frame-10x by default
  (when config/debug?
    (util/hide-10x)))
