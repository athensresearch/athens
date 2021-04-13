(ns athens.devcards.daily-notes
  (:require
    [athens.views.daily-notes :refer [daily-notes-panel]]
    [devcards.core :refer-macros [defcard-rg]]))


(defcard-rg Daily-Notes
  [daily-notes-panel])
