(ns athens.views.breadcrumbs
  (:require
    [athens.db]
    [athens.style :refer [color OPACITIES]]
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
                          :position "relative"
                          :color "inherit"}]
                     [:* {:display "inline"
                          :margin 0
                          :padding 0
                          :font-size "inherit"}]
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
  (into [:ol (use-style (merge breadcrumbs-list-style style))] children))


(defn breadcrumb
  ([children] [breadcrumb {} children])
  ([{:keys [style] :as props} children]
   [:li (use-style (merge breadcrumb-style style))
    [:a (merge props)
     children]]))
