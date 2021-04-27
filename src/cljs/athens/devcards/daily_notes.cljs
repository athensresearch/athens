(ns athens.devcards.daily-notes
  (:require
    [athens.views.pages.daily-notes :as daily-notes]
    [devcards.core :refer-macros [defcard-rg]]))


(defcard-rg Daily-Notes
  [daily-notes/page])
