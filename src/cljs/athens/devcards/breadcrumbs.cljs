(ns athens.devcards.breadcrumbs
  (:require
    [athens.db]
    [athens.style :refer [color OPACITIES]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-rg]]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles


(def breadcrumbs-list-style
  {:list-style "none"
   :display "flex"
   :flex "1 1 auto"
   :margin "0"
   :padding "0"
   :flex-direction "row"
   :height "inherit"
   :align-items "stretch"
   :flex-wrap "nowrap"})


(def breadcrumb-style
  {:flex "0 1 auto"
   :overflow "hidden"
   :max-width "100%"
   :white-space "nowrap"
   :text-overflow "ellipsis"
   :transition "all 0.3s ease"
   ::stylefy/manual [[:a {:text-decoration "none"
                          :opacity (:opacity-high OPACITIES)
                          :color "inherit"}]
                     [:a:hover {:color (color :link-color)
                                :opacity "1"}]
                     [:&:last-child [:a {:opacity "1"}]]
                     [:&:hover {:flex-shrink "0"}]
                     [:&:before {:display "inline-block"
                                 :padding "0 0.15em"
                                 :content "'>'"
                                 :opacity (:opacity-low OPACITIES)
                                 :transform "scaleX(0.5)"}]
                     [:&:first-child:before {:content "none"}]]})


;;; Components


(defn breadcrumbs-list
  [& children]
  (into [:ol (use-style breadcrumbs-list-style) children]))


(defn breadcrumb
  [{:keys [key]} & label]
  [:li (use-style breadcrumb-style {:key key})
   [:a {:href "#"} label]])


;;; Devcards


(defcard-rg Normal-Breadcrumb
  [breadcrumbs-list
   [breadcrumb {:key 0} "Athens"]
   [breadcrumb {:key 1} "Components"]
   [breadcrumb {:key 2} "Breadcrumbs"]])


(defcard-rg Breadcrumb-with-many-items
  [breadcrumbs-list
   [breadcrumb {:key 0} "lorem"]
   [breadcrumb {:key 1} "Ipsum"]
   [breadcrumb {:key 2} "Laudantium"]
   [breadcrumb {:key 3} "Accusamus"]
   [breadcrumb {:key 4} "Reprehenderit"]
   [breadcrumb {:key 5} "Aliquam"]
   [breadcrumb {:key 6} "Corrupti"]
   [breadcrumb {:key 7} "Omnis"]
   [breadcrumb {:key 8} "Quis"]
   [breadcrumb {:key 9} "Necessitatibus"]])


(defcard-rg Breadcrumb-with-long-items
  [breadcrumbs-list
   [breadcrumb {:key 0} "Exercitationem qui dicta officia aut alias eum asperiores voluptates exercitationem"]
   [breadcrumb {:key 2} "Sapiente ad quia sunt libero"]
   [breadcrumb {:key 1} "Accusantium veritatis placeat quaerat unde odio officia"]])
