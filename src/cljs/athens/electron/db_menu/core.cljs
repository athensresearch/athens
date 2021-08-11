(ns athens.electron.db-menu.core
  (:require
    ["@material-ui/core/Popover" :as Popover]
    ["@material-ui/icons/AddCircleOutline" :default AddCircleOutline]
    [athens.db :as db]
    [athens.electron.db-menu.db-icon :refer [db-icon]]
    [athens.electron.db-menu.db-list-item :refer [db-list-item]]
    [athens.electron.dialogs :as dialogs]
    [athens.style :refer [color DEPTH-SHADOWS]]
    [athens.views.buttons :refer [button]]
    [athens.views.dropdown :refer [menu-style menu-separator-style]]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


;; -------------------------------------------------------------------
;; --- material ui ---

(def m-popover (r/adapt-react-class (.-default Popover)))


(def dummy-all-dbs
  {"/home/jeff/Documents/alex/index.transit" {:last-open     '#inst"2021-06-30T18:35:38.277-00:00"
                                              :last-modified '#inst"2021-06-30T18:35:38.277-00:00"
                                              :path          "/home/jeff/Documents/alex/index.transit"
                                              :name          "alex"
                                              :remote?       false
                                              :synced?       true}
   "http://192.168.0.0"                      {:last-open     '#inst"2021-09-13T18:35:38.277-00:00"
                                              :last-modified '#inst"2021-09-13T18:35:38.277-00:00"
                                              :path          "http://192.168.0.0"
                                              :name          "192.168.0.0"
                                              :password      "x"
                                              :remote?       true
                                              :synced        false}
   "athensresarch.org/company-x"             {:last-open     '#inst"2021-09-13T18:35:38.277-00:00"
                                              :last-modified '#inst"2021-09-13T18:35:38.277-00:00"
                                              :path          "athensresarch.org/company-x"
                                              :name          "athensresarch.org/company-x"
                                              :password      "x"
                                              :remote?       true
                                              :synced        false}})


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
   :border "1px solid transparent"
   ::stylefy/manual [["&:hover" {:filter "brightness(110%)"}]
                     ["&:active" {:filter "brightness(90%)"}]
                     [:&.is-active {:color (color :body-text-color);
                                    :filter "brightness(90%)"}]
                     [:.icon {:width "1.75em"
                              :height "1.75em"}]]})


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
       [button "Import"]
       [button "Copy Link"]
       [button "Remove"]]
      [:<>
       [button {:onClick #(dialogs/move-dialog!)} "Move"]
       ;; [button {:onClick "Rename"]
       [button {:onClick #(if (= 1 (count all-dbs))
                            (js/alert "Can't remove last db from the list")
                            (dialogs/delete-dialog! db))}
        "Delete"]])]))


(defn db-menu
  []
  (r/with-let [ele (r/atom nil)]
              (let [all-dbs          @(subscribe [:db-picker/all-dbs])
                    active-db        @(subscribe [:db-picker/selected-db])
                    inactive-dbs     (dissoc all-dbs (:base-dir active-db))
                    sync-status      (if @(subscribe [:db/synced])
                                       :running
                                       :synchronising)]
                [:<>
                 ;; DB Icon + Dropdown toggle
                 [button {:class [(when @ele "is-active")]
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
                                    :key (:base-dir active-db)}]
                     [current-db-tools {:db active-db} all-dbs]]
                    ;; Show all inactive DBs and a separator
                    (doall
                      (for [[key db] inactive-dbs]
                        [db-list-item {:db db
                                       :is-current false
                                       :key key}]))
                    [:hr (use-style menu-separator-style)]
                    ;; Add DB control
                    [button {:on-click #(dispatch [:modal/toggle])}
                     [:<>
                      [:> AddCircleOutline]
                      [:span "Add Database"]]]]]]])))
