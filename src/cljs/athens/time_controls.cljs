(ns athens.time-controls
  (:require
    ["/components/Time/TimeSlider" :refer [TimeSlider]]
    [athens.common-db :as common-db]
    [athens.db :as db]
    [clojure.math :as math]
    [goog.functions :refer [throttle]]
    [re-frame.core :refer [reg-sub reg-event-db dispatch subscribe]]))


(defn enabled?
  []
  (:time-controls @(subscribe [:feature-flags])))


(reg-event-db
  :time/set-page-range
  (fn [db [_ title]]
    (assoc db :time/page-range (common-db/time-range @db/dsdb [:node/title title]))))


(reg-sub
  :time/page-range
  (fn [db _]
    (-> db :time/page-range)))


;; Slider

(reg-event-db
  :time/toggle-slider
  (fn [db _]
    (update db :time/slider? not)))


(reg-sub
  :time/slider?
  (fn [db _]
    (-> db :time/slider?)))


(reg-event-db
  :time/set-slider-range
  (fn [db [_ range]]
    (assoc db :time/slider-range range)))


(reg-sub
  :time/slider-range
  (fn [db _]
    (-> db :time/slider-range)))


(defn slider
  []
  (let [[min max] @(subscribe [:time/page-range])]
    (when (and (enabled?)
               @(subscribe [:time/slider?])
               min max)
      [:div {:style {:padding "0 1.5em 0 1.5em"}}
       [:> TimeSlider
        {:min min
         :max max
         :on-change (throttle #(dispatch [:time/set-slider-range (js->clj %)]) 50)}]])))


;; Heatmap

(reg-event-db
  :time/toggle-heatmap
  (fn [db _]
    (update db :time/heatmap? not)))


(reg-sub
  :time/heatmap?
  (fn [db _]
    (-> db :time/heatmap?)))


;; Common

(defn block-styles
  [block]
  (when (enabled?)
    (let [block-time (->> block :block/edits (mapv (comp :time/ts :event/time)) (apply max))]
      (when block-time
        (cond-> {}
          @(subscribe [:time/slider?])
          (merge (let [[start end] @(subscribe [:time/slider-range])]
                   (when (and start end)
                     (merge (when-not (and (>= block-time start)
                                           (<= block-time end))
                              {:opacity 0.2})))))


          @(subscribe [:time/heatmap?])
          (merge (let [[start end] @(subscribe [:time/page-range])
                       percent (* 100 (/ (- block-time start)
                                         (- end start)))
                       hue (math/floor (/ (* (- 100 percent)
                                             120)
                                          100))]
                   {:backgroundColor (str "hsl(" hue ",50%,75%)")})))))))
