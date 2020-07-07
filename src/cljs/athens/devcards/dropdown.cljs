(ns athens.devcards.dropdown
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.views.dropdown :refer [slash-menu-component block-context-menu-component filter-dropdown-component]]
    [athens.views.textinput :refer [textinput]]
    [devcards.core :refer-macros [defcard-rg]]))


(defcard-rg Slash-Menu
  [slash-menu-component])


(defcard-rg Block-Context-Menu
  [block-context-menu-component])


(defcard-rg Filter-Dropdown
  [filter-dropdown-component])
