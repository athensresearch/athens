(ns athens.views.blocks.drop-area-indicator
  (:require
    ["@chakra-ui/react" :refer [Box]]))


(defn drop-area-indicator
  ([{:keys [placement child?]}]
   [:> Box {:display "block"
            :height "1px"
            :pointerEvents "none"
            :background "link"
            :gridArea (if (= placement "above") "above" "below")
            :marginLeft (if child? "4rem" "2rem")
            :marginBottom "-1px"
            :position "relative"
            :transformOrigin "left"
            :zIndex 3}]))
