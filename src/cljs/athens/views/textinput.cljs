(ns athens.views.textinput
  (:require
    [athens.db]
    [athens.style :refer [color OPACITIES DEPTH-SHADOWS]]))


;; Styles


(def textinput-style
  {:min-height "2rem"
   :color (color :body-text-color)
   :caret-color (color :link-color)
   :border-radius "0.25rem"
   :background (color :background-minus-1)
   :padding "0.125rem 0.5rem"
   :border [["1px solid " (color :border-color)]]
   :transition-property "box-shadow, border, background"
   :transition-duration "0.1s"
   :transition-timing-function "ease"
   })
