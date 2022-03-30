(ns athens.views.references
  (:require
  ["@chakra-ui/react" :refer [Box Button Heading VStack]]))


(defn reference-header
  ([props]
   (let [{:keys [on-click title]} props]
     [:> Heading {:as "h4"
                  :color "foreground.secondary"
                  :textTransform "uppercase"
                  :pt 4
                  :borderTop "1px solid"
                  :borderTopColor "separator.divider"
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
     [:> VStack {:spacing 2 :align "stretch"}
      [reference-header {:on-click on-click-title
                         :title title}]
      children])))


(defn reference-block
  ([props children]
   (let [{:keys [actions]} props]
     [:> Box
      children
      actions])))

