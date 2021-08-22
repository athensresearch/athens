(ns athens.electron.db-menu.core
  (:require
    ["/components/Button/Button" :refer [Button]]
    ["@material-ui/core/Popover" :as Popover]
    ["@material-ui/icons/AddCircleOutline" :default AddCircleOutline]
    [athens.electron.db-menu.db-icon :refer [db-icon]]
    [athens.electron.db-menu.db-list-item :refer [db-list-item]]
    [athens.electron.dialogs :as dialogs]
    [athens.style :refer [color DEPTH-SHADOWS]]
    [athens.views.dropdown :refer [menu-style menu-separator-style]]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


;; -------------------------------------------------------------------
;; --- material ui ---

(def m-popover (r/adapt-react-class (.-default Popover)))


;; Style

(def dropdown-style
  {::stylefy/manual [[:.menu {:background (color :background-plus-2)
                              :color (color :body-text-color)
                              :border-radius "calc(0.25rem + 0.25rem)" ; Button corner radius + container padding makes "concentric" container radius
                              :padding "0.25rem"
                              :display "inline-flex"
                              :box-shadow [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px rgba(0, 0, 0, 0.05)"]]}]]})


(def db-menu-button-style
  {:color (color :body-text-color :opacity-high)
   :background "inherit"
   :padding "0"
   :align-items "stretch"
   :justify-content "stretch"
   :justify-items "stretch"
   :width "1.75em"
   :height "1.75em"
   :border "1px solid transparent"})


(def current-db-area-style
  {:background "rgba(144, 144, 144, 0.05)"
   :margin "-0.25rem -0.25rem 0.125rem"
   :border-bottom [["1px solid " (color :border-color)]]
   :padding "0.25rem"})


(def current-db-tools-style
  {:margin-left "2rem"})


;; Components

(defn current-db-tools
  ([{:keys [db]} all-dbs]
   [:div (use-style current-db-tools-style)
    (if (:is-remote db)
      [:<>
       [:> Button "Import"]
       [:> Button "Copy Link"]
       [:> Button "Remove"]]
      [:<>
       [:> Button {:onClick #(dialogs/move-dialog!)} "Move"]
       ;; [:> Button {:onClick "Rename"]
       [:> Button {:onClick #(if (= 1 (count all-dbs))
                            (js/alert "Can't remove last db from the list")
                            (dialogs/delete-dialog! db))}
        "Delete"]])]))


(defn db-menu
  []
  (r/with-let [ele (r/atom nil)]
              (let [all-dbs          @(subscribe [:db-picker/all-dbs])
                    active-db        @(subscribe [:db-picker/selected-db])
                    inactive-dbs     (dissoc all-dbs (:id active-db))
                    sync-status      (if @(subscribe [:db/synced])
                                       :running
                                       :synchronising)]
                [:<>
                 ;; DB Icon + Dropdown toggle
                 [:> Button {:class [(when @ele "is-active")]
                             :on-click #(reset! ele (.-currentTarget %))
                             :style db-menu-button-style}
                  [db-icon {:db     active-db
                            :status sync-status}]]
                 ;; Dropdown menu
                 [m-popover
                  (merge (use-style dropdown-style)
                         {:style {:font-size "14px"}
                          :open            (boolean @ele)
                          :anchorEl        @ele
                          :onClose         #(reset! ele nil)
                          :anchorOrigin    #js{:vertical   "bottom"
                                               :horizontal "left"}
                          :marginThreshold 10
                          :transformOrigin #js{:vertical   "top"
                                               :horizontal "left"}
                          :classes {:root "backdrop"
                                    :paper "menu"}})
                  [:div (use-style (merge menu-style
                                          {:overflow "visible"}))
                   [:<>
                    ;; Show active DB first
                    [:div (use-style current-db-area-style)
                     [db-list-item {:db active-db
                                    :is-current true
                                    :key (:id active-db)}]
                     [current-db-tools {:db active-db} all-dbs]]
                    ;; Show all inactive DBs and a separator
                    (doall
                      (for [[key db] inactive-dbs]
                        [db-list-item {:db db
                                       :is-current false
                                       :key key}]))
                    [:hr (use-style menu-separator-style)]
                    ;; Add DB control
                    [:> Button {:on-click #(dispatch [:modal/toggle])}
                      [:> AddCircleOutline]
                      [:span "Add Database"]]]]]])))
