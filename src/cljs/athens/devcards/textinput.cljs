(ns athens.devcards.textinput
  (:require
    ["@material-ui/icons/Face" :default Face]
    [athens.views.textinput :refer [textinput]]
    [devcards.core :refer-macros [defcard-rg]]))


(defcard-rg Input
  [textinput {:placeholder "pink"}])


(defcard-rg Input-with-icon
  [textinput {:placeholder "pink" :icon [:> Face]}])
