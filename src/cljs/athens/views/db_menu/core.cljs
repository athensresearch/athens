(ns athens.views.db-menu.core
  (:require
    ["@material-ui/core/Popover" :as Popover]
    ["@material-ui/icons/AddCircleOutline" :default AddCircleOutline]
    [athens.electron :as electron]
    [athens.db :as dba]
    [athens.style :refer [color DEPTH-SHADOWS]]
    [athens.views.buttons :refer [button]]
    [athens.views.db-menu.db-icon :refer [db-icon]]
    [athens.views.db-menu.db-list-item :refer [db-list-item]]
    [athens.views.dropdown :refer [menu-style menu-separator-style]]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


;; -------------------------------------------------------------------
;; --- material ui ---

(def m-popover (r/adapt-react-class (.-default Popover)))


;; temporary local defs

;; Save last visited db in local storage and get current-db path from local storage on startup
(def current-db-path "ec2-3-16-89-123.us-east-2.compute.amazonaws.com")


;; make this a subscription handler, and subscribe to this in db-menu
(def dummy-all-dbs
  [{:name "Athens Test Remote DB"
    :path "ec2-3-16-89-123.us-east-2.compute.amazonaws.com"
    :token "x"
    :is-remote true}
   {:name "My DB"
    :path "/Users/coolUser/Documents/athens/index.transit"
    :is-remote false}
   {:name "Top Secret"
    :path "/Users/coolUser/Documents/athens2/index.transit"
    :is-remote false}])


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
  ([{:keys [db]}]
   [:div (use-style current-db-tools-style)
    (if (:is-remote db)
      [:<>
       [button "Import"]
       [button "Copy Link"]
       [button "Remove"]]
      [:<>
       [button "Move"]
       [button "Rename"]
       [button "Import"]
       [button "Delete"]])]))


(defn db-menu
  []
  (r/with-let [ele (r/atom nil)
               current-db-path @(subscribe [:db/filepath])
               all-dbs @(subscribe [:db-picker/all-dbs]) ; is this correct ?
               ;; active-db (filter #(= (:path %) current-db-path) all-dbs)
               active-db (nth all-dbs 0) ; TODO for the time being let it be like this
               inactive-dbs (filter #(not= (:path %) current-db-path) all-dbs)]
              (println [" all-dbs is -->" all-dbs])
              (println ["items in all-dbs" (count all-dbs)])
              (println ["active-dbs is -->" active-db])
              (println ["inactive-dbs is -->" inactive-dbs])
              (println ["current db path is " current-db-path])

              [:<>
               ;; DB Icon + Dropdown toggle
               [button {:class [(when @ele "is-active")]
                        :on-click #(reset! ele (.-currentTarget %))
                        :style db-menu-button-style}
                [db-icon {:db active-db
                          :status :running}]]
               ;; Dropdown menu
               [m-popover
                (merge (use-style dropdown-style)
                       {:style {:font-size "14px"}
                        :open            @ele
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
                                  :key (:path active-db)}]
                   [current-db-tools {:db active-db}]]
                  ;; Show all inactive DBs and a separator
                  (doall
                    (for [db inactive-dbs]
                      [db-list-item {:db db
                                     :is-current false
                                     :key (:path db)}]))
                  [:hr (use-style menu-separator-style)]
                  ;; Add DB control
                  [button {:on-click #(dispatch [:modal/toggle])}
                   [:<>
                    [:> AddCircleOutline]
                    [:span "Add Database"]]]]]]]))
