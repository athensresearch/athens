(ns athens.views.breadcrumbs
  (:require
    [athens.db]
    [athens.style :refer [cssv color OPACITIES]]
    [cljsjs.react]
    [cljsjs.react.dom]
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
   :flex-wrap "nowrap"
   :color (color :body-text-color :opacity-high)
   ::stylefy/manual [[:svg {:font-size "inherit"
                            :color "inherit"
                            :margin "auto 0"}]]})


(def breadcrumb-style
  {:flex "0 1 auto"
   :overflow "hidden"
   :max-width "100%"
   :min-width "2.5em"
   :white-space "nowrap"
   :text-overflow "ellipsis"
   :transition "all 0.3s ease"
   ::stylefy/manual [[:a {:text-decoration "none"
                          :cursor "pointer"
                          :color "inherit"}]
                     [:&:last-child {:color (cssv "body-text-color")}]
                     [:&:hover {:flex-shrink "0"
                                :color (cssv "link-color")}]
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
