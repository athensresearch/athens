(ns athens.views.db-menu.db-list-item
  (:require
    ["@material-ui/icons/Link" :default Link]
    [athens.style :refer [color]]
    [athens.views.db-menu.db-icon :refer [db-icon]]
    [re-frame.core :refer [dispatch subscribe]]
    [stylefy.core :as stylefy :refer [use-style]]
    [athens.electron.core :as electron]))


(def db-list-item-style
  {:display "flex"
   ::stylefy/manual [[:.icon {:flex "0 0 1.75em"
                              :font-size "inherit"
                              :margin "0 0.5em 0 0"}]
                     [:.body {:display "flex"
                              :text-align "start"
                              :flex "1 1 100%"
                              :padding "0.5rem 0.25rem 0.5rem 0.5rem"
                              :border-radius "0.25rem"
                              :font-weight "normal"
                              :line-height "1.1"}]
                     [:.is-current]
                     [:.label {:display "block"
                               :overflow "hidden"
                               :flex "1 1 100%"}]
                     [:span {:display "block"}]
                     [:.name {:font-weight "600"
                              :color "inherit"}]
                     [:.path {:color (color :body-text-color :opacity-med)
                              :max-width "100%"
                              :overflow "hidden"
                              :text-overflow "ellipsis"
                              :font-size "12px"
                              :white-space "nowrap"}
                      [:svg {:display "inline-block"
                             :font-size "inherit"
                             :position "relative"
                             :top "0.2em"
                             :margin "auto 0.25em auto 0"}]]]})


(defn db-list-item-content
  [{:keys [db]}]
  [:<>
   [db-icon {:db db}]
   [:div.label
    [:span.name (:name db)]
    [:span.path
     {:title (:path db)}
     (when (:is-remote db)
       [:> Link])
     (:path db)]]])


(defn db-list-item
  [{:keys [db is-current] :as props}]
  [:div (use-style db-list-item-style props)
   (if is-current
     [:div.body.is-current
      [db-list-item-content {:db db}]]
     [:button.body.button {:onClick 
			     #(dispatch 
                               [:db-picker/select-new-db 
                                 (:path db) 
				  @(subscribe [:db/synced])])}
      [db-list-item-content {:db db}]])])
