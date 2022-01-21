(ns athens.electron.db-menu.db-list-item
  (:require
    ["@material-ui/icons/Clear" :default Clear]
    ["@material-ui/icons/Link" :default Link]
    [athens.electron.db-menu.db-icon :refer [db-icon]]
    [athens.electron.dialogs :as dialogs]
    [athens.style :refer [color]]
    [re-frame.core :refer [dispatch]]
    [stylefy.core :as stylefy :refer [use-style]]))


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
                              :background "inherit"
                              :color "inherit"
                              :appearance "none"
                              :border "none"
                              :line-height "1.1"}
                      [:.MuiSvgIcon-root {:opacity "50%"}]
                      ["&:hover" {:filter "brightness(110%)"}]]
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
     {:title (:id db)}
     (when (:is-remote db)
       [:> Link])
     (:id db)]]])


(defn db-list-item
  [{:keys [db is-current]}]
  (let [remove-db-click-handler (fn [e]
                                  (dialogs/delete-dialog! db)
                                  (.. e stopPropagation))]
    [:div (use-style db-list-item-style)
     (if is-current
       [:div.body.is-current
        [db-list-item-content {:db db}]]
       [:button.body.button {:onClick #(dispatch [:db-picker/select-db db])}
        [db-list-item-content {:db db}]
        [:> Clear {:on-click remove-db-click-handler}]])]))

