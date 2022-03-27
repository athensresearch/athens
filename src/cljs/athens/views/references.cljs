(ns athens.views.references
  (:require
  ["@chakra-ui/react" :refer [Box Button Heading]]))


(defn references-container
  ([_ children]
     [:> Box {:as "section"
              :key "Inline Linked References"
              :ml 6
              :borderRadius "md"
              :background "background.basement"}
      children]))


(defn reference-header
  ([props]
   (let [{:keys [on-click title]} props]
     [:> Heading {:as "h4"
                  :py 2
                  :color "foreground.secondary"
                  :textTransform "uppercase"
                  :fontWeight "bold"
                  :fontSize "0.75rem"
                  :size "md"}
      [:> Button {:onClick on-click
                  :color "inherit"
                  :textTransform "inherit"
                  :_hover {:textDecoration "none"
                           :opacity 0.5}
                  :fontWeight "inherit"
                  :fontSize "inherit"
                  :variant "link"}
       title]])))


(defn reference-group
  ([props children]
   (let [{:keys [on-click-title title]} props]
     [:> Box
      [reference-header {:on-click on-click-title
                         :title title}]
      children])))


(defn reference-block
  ([props children]
   (let [{:keys [actions]} props]
     [:> Box
      children
      actions])))

