(ns athens.devcards.spinner
  (:require
    [athens.db]
    [devcards.core :refer-macros [defcard-rg]]
    [stylefy.core :as stylefy :refer [use-style]]))


(defcard-rg Default-Spinner
  [spinner-component (use-style spinner-style)])


(defcard-rg Spinner-with-custom-message
  [spinner-component (use-style spinner-style {:message "Custom Loading Message"})])
