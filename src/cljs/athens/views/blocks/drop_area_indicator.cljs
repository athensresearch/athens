(ns athens.views.blocks.drop-area-indicator
  (:require
    [athens.style :as style]
    [stylefy.core :as stylefy]))


(def drop-area-indicator-style
  {:display "block"
   :height "1px"
   :pointer-events "none"
   :margin-bottom "-1px"
   :opacity (:opacity-high style/OPACITIES)
   :color (style/color :link-color)
   :position "relative"
   :transform-origin "left"
   :z-index 3
   :width "100%"
   ::stylefy/manual [["&:after" {:position "absolute"
                                 :content "''"
                                 :top "-0.5px"
                                 :right "0"
                                 :bottom "-0.5px"
                                 :left "calc(2em - 4px)"
                                 :border-radius "100px"
                                 :background "currentColor"}]
                     ["&.child" {:--indent "1.95em"
                                 :width "calc(100% - var(--indent))"
                                 :margin-left "var(--indent)"}]
                     ["&.child:after" {:border-top-left-radius 0
                                       :border-bottom-left-radius 0}]
                     ["&.child:before" {:position "absolute"
                                        :content "''"
                                        :border-radius "10em"
                                        :border "2px solid "
                                        :--size "4px"
                                        :width "var(--size)"
                                        :height "var(--size)"
                                        :left "var(--indent)"
                                        :top "50%"
                                        :transform "translateY(-50%) translateX(-100%) translateX(-2px)"}]]})


(defn drop-area-indicator
  ([{:keys [style child]}]
   [:div (stylefy/use-style
           (merge drop-area-indicator-style style)
           {:class (when child "child")})]))
