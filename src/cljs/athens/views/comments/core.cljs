(ns athens.views.comments.core
  (:require [athens.views.comments.inline :as inline]
            [athens.views.comments.right-side :as right-side]))


;; :author and :time in the future
(def mock-data
  [{:string "Agree with the jumpiness"}
   {:string "Also experiencing this. Someone closed the parent of the block I was on (I was not zoomed in) and I got kicked out of the block"}])

