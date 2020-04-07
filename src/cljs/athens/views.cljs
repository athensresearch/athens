(ns athens.views
  (:require
   [athens.subs]
   [athens.page :as page]
   [re-frame.core :as rf :refer [subscribe dispatch]]
   [reitit.frontend :as rfe]
   [reitit.frontend.easy :as rfee]
   ))

(defn about-panel []
  [:div [:h1 "About Panel"]])

(defn pages-panel []
  (let [nodes (subscribe [:nodes])]
    (fn []
      [:div
       [:table {:style {:width "100%"}}
        [:thead
         [:tr
          [:th {:style {:text-align "left"}} "Page"]
          [:th {:style {:text-align "left"}} "Last Edit"]
          [:th {:style {:text-align "left"}} "Created At"]]]
        [:tbody
         (for [[_ title id create-t edit-t] @nodes]
           ^{:key id}
           [:tr
            [:td {:style {:height 24}} [:a {:href (rfee/href :page {:id id})} title]]
            [:td (.toLocaleString  (js/Date. create-t))]
            [:td (.toLocaleString  (js/Date. edit-t))]])]]])))

(defn home-panel []
  [:h1 "Home Panel"])

(defn match-panel [name]
  [(case name
      :about about-panel
      :pages pages-panel
      :page  page/main 
      home-panel)])

(defn main-panel []
  (let [current-route (subscribe [:current-route])]
    (fn []
      [:div
       ;[:h1 "Hello World"]
       [:p "Current Route: " [:b (-> @current-route :path)]]
       [match-panel (-> @current-route :data :name)]
       ])))
