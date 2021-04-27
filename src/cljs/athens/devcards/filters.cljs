(ns athens.devcards.filters
  (:require
    [athens.views.filters :refer [filters-el]]
    [devcards.core :refer [defcard-rg]]
    #_[re-frame.core :as re-frame :refer [dispatch]]
    [stylefy.core :as stylefy :refer [use-style #_use-sub-style]]))


(def items
  {"Amet"   {:count 6 :state :added}
   "At"     {:count 130 :state :excluded}
   "Diam"   {:count 6}
   "Donec"  {:count 6}
   "Elit"   {:count 30}
   "Elitudomin mesucen defibocutruon"  {:count 1}
   "Erat"   {:count 11}
   "Est"    {:count 2}
   "Eu"     {:count 2}
   "Ipsum"  {:count 2 :state :excluded}
   "Magnis" {:count 10 :state :added}
   "Metus"  {:count 29}
   "Mi"     {:count 7 :state :added}
   "Quam"   {:count 1}
   "Turpis" {:count 97}
   "Vitae"  {:count 1}})


(def devcard-wrapper {:width "18rem"})


(defcard-rg Filters
  [:div (use-style devcard-wrapper)
   [filters-el "((some-uid))" items]])
