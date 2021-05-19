(ns athens.views.db-switcher.core
  (:require
   ["@material-ui/core/Popover" :as Popover]
   [athens.style :refer [color DEPTH-SHADOWS]]
   [athens.views.buttons :refer [button]]
   [athens.views.db-switcher.db-icon :refer [db-icon]]
   [athens.views.dropdown :refer [menu-style menu-separator-style]]
   [reagent.core :as r]
   [stylefy.core :as stylefy :refer [use-style]]))


;;-------------------------------------------------------------------
;;--- material ui ---

(def m-popover (r/adapt-react-class (.-default Popover)))

(def current-db-path "/Users/coolUser/Documents/athens/index.transit")

(def testdb {:name "Athens Test Remote DB"
    :path "ec2-3-16-89-123.us-east-2.compute.amazonaws.com"
    :token "x"
    :isRemote true})

(def available-dbs
  [{:name "Athens Test Remote DB"
    :path "ec2-3-16-89-123.us-east-2.compute.amazonaws.com"
    :token "x"
    :isRemote true}
   {:name "My DB"
    :path "/Users/coolUser/Dropbox/athens/index.transit"
    :isRemote false}
   {:name "Top Secret"
    :path "/Users/coolUser/Documents/athens2/index.transit"
    :isRemote false}])



;; Style

(def dropdown-style
  {::stylefy/manual [[:.menu {:background (color :background-plus-2)
                              :border-radius "calc(0.25rem + 0.25rem)" ;; Button corner radius + container padding makes "concentric" container radius
                              :padding "0.25rem"
                              :display "inline-flex"
                              :box-shadow [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px rgba(0, 0, 0, 0.05)"]]}]]})


(def db-switcher-button-style
  {:color (color :body-text-color :opacity-high)
   :background "inherit"
   :border "1px solid transparent"
   ::stylefy/manual [["&:hover" {:filter "brightness(110%)"
                                 :background (color :backgorund-plus-2)
                                 :border [["1px solid " (color :border-color)]]}]
                     ["&:active" {:filter "brightness(90%)"
                                  :border [["1px solid " (color :border-color)]]}]
                     [:.active {:filter "brightness(110%)"
                                :box-shadow [["0 1px 3px -2px " (color :border-color)]]
                                :background (color :backgorund-plus-2)
                                :border [["1px solid " (color :border-color)]]}]
                     [:.icon {:width "1.25em"
                              :height "1.25em"}]]})


;; Components


(defn db-switcher
  []
  (r/with-let [ele (r/atom nil)
               active-path current-db-path
               available-dbs available-dbs]
    [:<>
     [button {:class [(when @ele "is-active")]
              :on-click #(reset! ele (.-currentTarget %))
              :style db-switcher-button-style}
      [db-icon {:db testdb
                :status "syncing..."}]]
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
        [:span "Show Local Graph"]]
       [:hr (use-style menu-separator-style)]]]]))