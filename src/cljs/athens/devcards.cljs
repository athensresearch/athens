(ns athens.devcards
  (:require
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :as devcards :include-macros true :refer [defcard]]
    [reagent.core :as r :include-macros true]))


(def bmi-data (r/atom {:height 180 :weight 80}))


(defn calc-bmi
  [bmi-data]
  (let [{:keys [height weight bmi] :as data} bmi-data
        h (/ height 100)]
    (if (nil? bmi)
      (assoc data :bmi (/ weight (* h h)))
      (assoc data :weight (* bmi h h)))))


(defn slider
  [bmi-data param value min max]
  [:input {:type "range" :value value :min min :max max
           :style {:width "100%"}
           :on-change (fn [e]
                        (swap! bmi-data assoc param (.. e -target -value))
                        (when (not= param :bmi)
                          (swap! bmi-data assoc :bmi nil)))}])


(defn bmi-component
  [bmi-data]
  (let [{:keys [weight height bmi]} (calc-bmi @bmi-data)
        [color diagnose] (cond
                           (< bmi 18.5) ["orange" "underweight"]
                           (< bmi 25) ["inherit" "normal"]
                           (< bmi 30) ["orange" "overweight"]
                           :else ["red" "obese"])]
    [:div
     [:h3 "BM calculator"]
     [:div
      "Height: " (int height) "cm"
      [slider bmi-data :height height 100 220]]
     [:div
      "Weight: " (int weight) "kg"
      [slider bmi-data :weight weight 30 150]]
     [:div
      "BMI: " (int bmi) " "
      [:span {:style {:color color}} diagnose]
      [slider bmi-data :bmi bmi 10 50]]]))


(defcard bmi-calculator
  "*Code taken from the Reagent readme.*"
  (devcards/reagent bmi-component)
  bmi-data
  {:inspect-data true
   :frame true
   :history true})


(defn ^:export main
  []
  (devcards.core/start-devcard-ui!))
