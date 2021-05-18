(ns athens.views.db-switcher
  (:require
   ["@material-ui/core/Popover" :as Popover]
   ["@material-ui/icons/BubbleChart" :default BubbleChart]
   [athens.style :refer [color DEPTH-SHADOWS]]
   [athens.views.buttons :refer [button]]
   [athens.views.dropdown :refer [menu-style menu-separator-style]]
   [reagent.core :as r]
   [stylefy.core :as stylefy :refer [use-style]]))


;;-------------------------------------------------------------------
;;--- material ui ---

(def m-popover (r/adapt-react-class (.-default Popover)))

;; Style

(def dropdown-style
  {::stylefy/manual [[:.menu {:background (color :background-plus-2)
                              :border-radius "calc(0.25rem + 0.25rem)" ;; Button corner radius + container padding makes "concentric" container radius
                              :padding "0.25rem"
                              :display "inline-flex"
                              :box-shadow [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px rgba(0, 0, 0, 0.05)"]]}]]})


(def page-menu-toggle-style
  {:border-radius "1000px"
   :padding "0.375rem 0.5rem"
   :color (color :body-text-color :opacity-high)})


;; Components


(defn db-switcher
  []
  (r/with-let [ele (r/atom nil)]
    [:<>
     [button {:class    [(when @ele "is-active")]
              :on-click #(reset! ele (.-currentTarget %))
              :style    page-menu-toggle-style}
      [:div "picker"]]
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
        [:> BubbleChart]
        [:span "Show Local Graph"]]
       [:hr (use-style menu-separator-style)]]]]))