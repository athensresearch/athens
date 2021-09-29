(ns athens.views.blocks.tooltip
  (:require
    ["/components/Block/components/DetailPopover" :refer [DetailPopover]]
    [athens.style :as style]
    [athens.util :as util]
    [stylefy.core :as stylefy]))


;; Styles

(stylefy/keyframes "tooltip-appear"
                   [:from
                    {:opacity "0"
                     :transform "scale(0)"}]
                   [:to
                    {:opacity "1"
                     :transform "scale(1)"}])


;; View
