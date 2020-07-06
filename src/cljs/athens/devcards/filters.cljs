(ns athens.devcards.filters
  (:require
    [devcards.core :refer [defcard-rg]]
    #_[re-frame.core :as re-frame :refer [dispatch]]
    [stylefy.core :as stylefy :refer [use-style #_use-sub-style]]))


(def devcard-wrapper {:width "300px"})


(defcard-rg Filters
  [:div (use-style devcard-wrapper)
   [filters-el "((some-uid))" items]])
