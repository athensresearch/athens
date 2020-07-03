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
   :overflow "hidden"
   :height "inherit"
   :align-items "stretch"
   :flex-wrap "nowrap"})


(def breadcrumb-style
  {:flex "0 1 auto"
   :overflow "hidden"
   :max-width "100%"
   :min-width "2.5em"
   :white-space "nowrap"
   :text-overflow "ellipsis"
   :transition "all 0.3s ease"
   :color (color :body-text-color :opacity-high)
   ::stylefy/manual [[:a {:text-decoration "none"
                          :cursor "pointer"
                          :color "inherit"}]
                     [:&:last-child {:color (color :body-text-color)}]
                     [:&:hover {:flex-shrink "0"
                                :color (color :link-color)}]
                     [:&:before {:display "inline-block"
                                 :padding "0 0.15em"
                                 :content "'>'"
                                 :opacity (:opacity-low OPACITIES)
                                 :transform "scaleX(0.5)"}]
                     [:&:first-child:before {:content "none"}]]})


;;; Components


(defn breadcrumbs-list
  [{:keys [style]} & children]
  (into [:ol (use-style (merge breadcrumbs-list-style style)) children]))


(defn breadcrumb
  [{:keys [style on-click]} & label]
  [:li (use-style (merge breadcrumb-style style) {:title label})
   [:a {:on-click on-click} label]])


;;; Devcards


(defcard-rg Normal-Breadcrumb
  [breadcrumbs-list {}
   [breadcrumb {:key 0} "Athens"]
   [breadcrumb {:key 1} "Components"]
   [breadcrumb {:key 2} "Breadcrumbs"]])


(defcard-rg One-Item
  [breadcrumbs-list {}
   [breadcrumb {:key 2} "Athens"]])


(defcard-rg Breadcrumb-with-many-items
  [breadcrumbs-list {}
   [breadcrumb {:key "a"} "Lorem"]
   [breadcrumb {:key "b"} "Ipsum"]
   [breadcrumb {:key "c"} "Laudantium"]
   [breadcrumb {:key "d"} "Accusamus"]
   [breadcrumb {:key "e"} "Reprehenderit"]
   [breadcrumb {:key "f"} "Aliquam"]
   [breadcrumb {:key "g"} "Corrupti"]
   [breadcrumb {:key "h"} "Omnis"]
   [breadcrumb {:key "i"} "Quis"]
   [breadcrumb {:key "a"} "Lorem"]
   [breadcrumb {:key "b"} "Ipsum"]
   [breadcrumb {:key "c"} "Laudantium"]
   [breadcrumb {:key "d"} "Accusamus"]
   [breadcrumb {:key "e"} "Reprehenderit"]
   [breadcrumb {:key "f"} "Aliquam"]
   [breadcrumb {:key "g"} "Corrupti"]
   [breadcrumb {:key "h"} "Omnis"]
   [breadcrumb {:key "i"} "Quis"]
   [breadcrumb {:key "a"} "Lorem"]
   [breadcrumb {:key "b"} "Ipsum"]
   [breadcrumb {:key "c"} "Laudantium"]
   [breadcrumb {:key "d"} "Accusamus"]
   [breadcrumb {:key "e"} "Reprehenderit"]
   [breadcrumb {:key "f"} "Aliquam"]
   [breadcrumb {:key "g"} "Corrupti"]
   [breadcrumb {:key "h"} "Omnis"]
   [breadcrumb {:key "i"} "Quis"]
   [breadcrumb {:key "a"} "Lorem"]
   [breadcrumb {:key "b"} "Ipsum"]
   [breadcrumb {:key "c"} "Laudantium"]
   [breadcrumb {:key "d"} "Accusamus"]
   [breadcrumb {:key "e"} "Reprehenderit"]
   [breadcrumb {:key "f"} "Aliquam"]
   [breadcrumb {:key "g"} "Corrupti"]
   [breadcrumb {:key "h"} "Omnis"]
   [breadcrumb {:key "i"} "Quis"]
   [breadcrumb {:key "j"} "Necessitatibus"]])


(defcard-rg Breadcrumb-with-long-items
  [breadcrumbs-list {}
   [breadcrumb {:key 0} "Exercitationem qui dicta officia aut alias eum asperiores voluptates exercitationem"]
   [breadcrumb {:key 1} "Sapiente ad quia sunt libero"]
   [breadcrumb {:key 2} "Accusantium veritatis placeat quaerat unde odio officia"]])


(defcard-rg Breadcrumb-with-one-long-item-at-start
  [breadcrumbs-list {}
   [breadcrumb {:key 0} "Voluptates exercitationem dicta officia aut alias eum asperiores voluptates exercitationem"]
   [breadcrumb {:key 2} "Libero"]
   [breadcrumb {:key 3} "Unde"]])


(defcard-rg Breadcrumb-with-one-long-item-at-end
  [breadcrumbs-list {}
   [breadcrumb {:key 0} "Unde"]
   [breadcrumb {:key 1} "Libero"]
   [breadcrumb {:key 2} "Exercitationem qui dicta officia aut alias eum asperiores voluptates exercitationem dicta officia aut alias eum asperiores voluptates exercitationem"]])
