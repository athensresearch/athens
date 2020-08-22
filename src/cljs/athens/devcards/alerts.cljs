(ns athens.devcards.alerts
  (:require
    [athens.views.alerts :refer [alert]]
    [devcards.core :refer-macros [defcard-rg]]))


(defcard-rg Alert
  [alert "All is right in the universe."])
;;
;;(defcard-rg Alert
;;  [alert "bad"])
