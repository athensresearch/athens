(ns athens.style
  (:require
    [athens.config :as config]
    [athens.util :as util]
    [re-frame.core :refer [reg-sub subscribe]]))


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
    {:font-size (str "calc(1 / " (get-zoom-pct @zoom-level) " * 100 * 100%)")}))


(defn init
  []
  ;; hide re-frame-10x by default
  (when config/debug?
    (util/hide-10x)))
