(ns athens.devcards.alerts
  (:require
    [athens.views.alerts :refer [alert-component]]
    [devcards.core :refer-macros [defcard-rg]]))


(defcard-rg Alert
  [alert-component "Page \"Athens\" already exists, merge pages?" #(prn "confirm") #(prn "cancel")])
