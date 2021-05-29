(ns athens.views.blocks.drop-area-indicator
  (:require
    [athens.style :as style :refer [OPACITIES]]
    [stylefy.core :as stylefy]))


(stylefy/keyframes "drop-area-appear"
                   [:from
                    {:opacity "0"}]
                   [:to
                    {:opacity "1"}])


(stylefy/keyframes "drop-area-color-pulse"
                   [:from
                    {:opacity (:opacity-lower style/OPACITIES)}]
                   [:to
                    {:opacity (:opacity-med style/OPACITIES)}])


(def drop-area-indicator-style
  {:display "block"
   :height "1px"
   :pointer-events "none"
   :margin-bottom "-1px"
   :opacity (:opacity-high OPACITIES)
   :color (style/color :link-color)
   :animation "drop-area-appear 0.2s both"
   :position "relative"
   :transform-origin "left"
   :z-index 3
   :width "100%"
   ::stylefy/manual [["&:after" {:position "absolute"
                                 :content "''"
                                 :top "-0.5px"
                                 :right "0"
                                 :bottom "-0.5px"
                                 :left "2em"
                                 :border-radius "100px"
                                 :background "currentColor"}]
                     ["&.child" {:--indent "2rem"
                                 :width "calc(100% - var(--indent))"
                                 :margin-left "var(--indent)"}]
                     ["&.child:after" {:border-top-left-radius 0
                                       :border-bottom-left-radius 0}]
                     ["&.child:before" {:position "absolute"
                                        :content "''"
                                        :width "0.3rem"
                                        :height "0.3rem"
                                        :border-radius "10em"
                                        :left "var(--indent)"
                                        :top "50%"
                                        :transform "translateY(-50%) translateX(-0.3rem))"
                                        :border "2px solid "}]]})


(defn drop-area-indicator
  ([{:keys [style child]}]
   [:div (stylefy/use-style
           (merge drop-area-indicator-style style)
           {:class (when child "child")})]))
