(ns athens.views.db-switcher.core
  (:require
   ["@material-ui/core/Popover" :as Popover]
   ["@material-ui/icons/AddCircle" :default AddCircle]
   [athens.style :refer [color DEPTH-SHADOWS]]
   [athens.views.buttons :refer [button]]
   [athens.views.db-switcher.db-icon :refer [db-icon]]
   [athens.views.db-switcher.db-list-item :refer [db-list-item]]
   [athens.views.dropdown :refer [menu-style menu-separator-style]]
   [reagent.core :as r]
   [stylefy.core :as stylefy :refer [use-style]]))


;;-------------------------------------------------------------------
;;--- material ui ---

(def m-popover (r/adapt-react-class (.-default Popover)))

;; temporary local defs

(def current-db-path "/Users/coolUser/Documents/athens/index.transit")

(def all-dbs
  [{:name "Athens Test Remote DB"
    :path "ec2-3-16-89-123.us-east-2.compute.amazonaws.com"
    :token "x"
    :isRemote true}
   {:name "My DB"
    :path "/Users/coolUser/Documents/athens/index.transit"
    :isRemote false}
   {:name "Top Secret"
    :path "/Users/coolUser/Documents/athens2/index.transit"
    :isRemote false}])

;; Style

(def dropdown-style
  {::stylefy/manual [[:.menu {:background (color :background-plus-2)
                              :color (color :body-text-color)
                              :border-radius "calc(0.25rem + 0.25rem)" ;; Button corner radius + container padding makes "concentric" container radius
                              :padding "0.25rem"
                              :display "inline-flex"
                              :box-shadow [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px rgba(0, 0, 0, 0.05)"]]}]]})


(def db-switcher-button-style
  {:color (color :body-text-color :opacity-high)
   :background "inherit"
   :padding "0.25rem"
   :border "1px solid transparent"
   ::stylefy/manual [["&:hover" {:filter "brightness(110%)"
                                 :background (color :backgorund-plus-2)
                                 :border [["1px solid " (color :border-color)]]}]
                     ["&:active" {:filter "brightness(90%)"
                                  :border [["1px solid " (color :border-color)]]}]
                     [:&.is-active {:color (color :body-text-color);
                                    :background (color :body-text-color :opacity-lower)}]
                     [:.icon {:width "1.2em"
                              :height "1.2em"}]]})


(def current-db-tools-style
  {:margin-left "0rem"})


;; Components

(defn current-db-tools
  ([{:keys [db]}]
   [:div (use-style current-db-tools-style)
    [button "Move"]
    [button "Rename"]
    [button "Delete"]]))


(defn db-switcher
  []
  (r/with-let [ele (r/atom nil)
              ;;  active-db (filter #(= (:path %) current-db-path) all-dbs)
               active-db (nth all-dbs 1)
               inactive-dbs (filter #(not= (:path %) current-db-path) all-dbs)]
    [:<>
     ;; DB Icon + Dropdown toggle
     [button {:class [(when @ele "is-active")]
              :on-click #(reset! ele (.-currentTarget %))
              :style db-switcher-button-style}
      [db-icon {:db active-db
                :status "syncing..."}]]
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
      [:div (use-style menu-style)
       [:<>
       ;; Show active DB first
        [db-list-item {:db active-db
                       :is-current true
                       :key (:path active-db)}]
        [current-db-tools {:db active-db}]
        ;; Conditional separator
        (when (seq inactive-dbs)
          [:hr (use-style menu-separator-style)])
        ;; Show all inactive DBs and a separator
        (doall
         (for [db inactive-dbs]
           [db-list-item {:db db
                          :is-current false
                          :key (:path db)}]))
        [:hr (use-style menu-separator-style)]
        ;; Add DB control
        [button [:<>
                 [:> AddCircle]
                 [:span "Add Database"]]]]]]]))