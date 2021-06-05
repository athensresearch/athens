(ns athens.views.appearance-settings.core
  (:require
    ["@material-ui/core/Popover" :as Popover]
    ["@material-ui/icons/Brightness3" :default Brightness3]
    ["@material-ui/icons/Brightness7" :default Brightness7]
    ["@material-ui/icons/TextFormat" :default TextFormat]
    [athens.style :as style :refer [color DEPTH-SHADOWS]]
    [athens.views.buttons :refer [button]]
    [athens.views.dropdown :refer [menu-style]]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


;; -------------------------------------------------------------------
;; --- material ui ---

(def m-popover (r/adapt-react-class (.-default Popover)))


;; Icons 

(def width-background [:rect {:x "-2" :y "0" :width "28" :height "24" :rx "3" :opacity "0.25"}])


(def normal-width-icon
  [:svg {:viewBox "0 0 24 24"}
   width-background
   [:path {:d "M7,04 H18"}]
   [:path {:d "M7,08 H18"}]
   [:path {:d "M7,12 H18"}]
   [:path {:d "M7,16 H18"}]
   [:path {:d "M7,20 H12"}]])


1


(def wide-width-icon
  [:svg {:viewBox "0 0 24 24"}
   width-background
   [:path {:d "M3,04 H20"}]
   [:path {:d "M3,08 H20"}]
   [:path {:d "M3,12 H16"}]])


(def unlimited-width-icon
  [:svg {:viewBox "0 0 24 24"}
   width-background
   [:path {:d "M2,04 H22"}]
   [:path {:d "M2,08 H22"}]
   [:path {:d "M2,12 H8"}]])


#_ (def tight-density-icon
  [:svg {:viewBox "0 0 16 16"}
   [:path {:d "M1,5  H15"}]
   [:path {:d "M1,8  H15"}]
   [:path {:d "M1,11 H15"}]])


#_ (def normal-density-icon
  [:svg {:viewBox "0 0 16 16"}
   [:path {:d "M1,4  H15"}]
   [:path {:d "M1,8  H15"}]
   [:path {:d "M1,12 H15"}]])


#_ (def loose-density-icon
  [:svg {:viewBox "0 0 16 16"}
   [:path {:d "M1,3  H15"}]
   [:path {:d "M1,8  H15"}]
   [:path {:d "M1,13 H15"}]])


;; Style

(def dropdown-style
  {::stylefy/manual [[:.menu {:background (color :background-plus-2)
                              :color (color :body-text-color)
                              :border-radius "calc(0.25rem + 0.25rem)" ; Button corner radius + container padding makes "concentric" container radius
                              :padding "0.25rem 0"
                              :display "inline-flex"
                              :box-shadow [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px rgba(0, 0, 0, 0.05)"]]}]]})


(def preferences-set-style
  {:display "grid"
   :grid-auto-flow "column"
   :grid-gap "1px"
   :grid-auto-columns "1fr"
   :border-radius "calc(0.25rem + 1px)"
   :margin "0.125rem 0.5rem"
   :padding "1px"
   :box-shadow "inset 0 0 0 1px transparent, 0 0 0 1px transparent"
   :transition "box-shadow 0.1s ease, filter 0.1s ease"
   :background "inherit"
   ::stylefy/manual [[:&:hover {:filter "brightness(120%)"
                                :box-shadow [["0 0 0 1px " (color :border-color)]]}]
                     [:>button {:text-align "center"
                                :display "flex"
                                :gap "0.25rem"
                                :text-transform "uppercase"
                                :flex-direction "column"
                                :align-items "center"
                                :justify-content "center"
                                :background "inherit"
                                :min-height "2.5rem"
                                :padding "0.5rem"
                                :font-weight "500"}
                      [:span {:font-size "85%"
                              :color (style/color :body-text-color :opacity-med)}]
                      [:svg {:vector-effect "non-scaling-stroke"
                             :overflow "visible"
                             :width "24px"
                             :height "24px"}]
                      ["svg:not(.MuiSvgIcon-root)" {:fill "none"
                                                    :stroke-linecap "round"
                                                    :stroke "currentColor"
                                                    :stroke-width "0.12rem"}]]]})


;; Components

(defn preferences-set
  [{:keys [prefs current]}]
  [:div (use-style preferences-set-style)
   (doall (for [option prefs]
            [button {:class (:id option)
                     :key (:id option)
                     :active (= (:id option) current)
                     :on-click (:fn option)}
             (:content option)]))])


(def theme-settings
  [{:content [:<> [:> Brightness7] [:span "Light"]]
    :id "theme-light"
    :fn #(dispatch [:theme/set-light])}
   #_ {:content "Auto"
    :id "theme-dark"
    :fn #(dispatch [:theme/set-dark])}
   {:content [:<> [:> Brightness3] [:span "Dark"]]
    :id "theme-dark"
    :fn #(dispatch [:theme/set-dark])}])


#_ (def font-settings
  [{:content [:span
              {:style
               {:display "contents"
                :font-size "18px"
                :font-family (:serif style/font-family)}}
              "Se"]
    :fn #(dispatch [:appearance/set-font :serif])
    :id "font-serif"}
   {:content [:span
              {:style
               {:display "contents"
                :font-size "18px"
                :font-family (:sans style/font-family)}}
              "Sa"]
    :fn #(dispatch [:appearance/set-font :sans])
    :id "font-sans"}
   {:content [:span
              {:style
               {:display "contents"
                :font-size "18px"
                :font-family (:mono style/font-family)}}
              "Mo"]
    :fn #(dispatch [:appearance/set-font :mono])
    :id "font-mono"}])


(def width-settings
  [{:content [:<> normal-width-icon [:span "normal"]]
    :id "width-normal"
    :fn #(dispatch [:appearance/set-width "width-normal"])}
   {:content [:<> wide-width-icon [:span "large"]]
    :id "width-wide"
    :fn #(dispatch [:appearance/set-width "width-wide"])}
   {:content [:<> unlimited-width-icon [:span "full"]]
    :id "width-unlimited"
    :fn #(dispatch [:appearance/set-width "width-unlimited"])}])


#_ (def density-settings
  {:fn :appearance/set-density
   :content [{:content tight-density-icon  :id "density-tight"}
             {:content normal-density-icon :id "density-normal"}
             {:content loose-density-icon  :id "density-loose"}]})


(defn appearance-settings
  []
  (r/with-let [ele (r/atom nil)]
              [:<>
               ;; Dropdown toggle
               [button {:class [(when @ele "is-active")]
                        :title "Change appearance preferences"
                        :on-click #(reset! ele (.-currentTarget %))}
                [:> TextFormat]]
               ;; Dropdown menu
               [m-popover
                (merge (use-style dropdown-style)
                       {:style {:font-size "14px"}
                        :open            @ele
                        :anchorEl        @ele
                        :onClose         #(reset! ele nil)
                        :anchorOrigin    #js{:vertical   "bottom"
                                             :horizontal "right"}
                        :marginThreshold 10
                        :transformOrigin #js{:vertical   "top"
                                             :horizontal "right"}
                        :classes {:root "backdrop"
                                  :paper "menu"}})
                [:div (use-style (merge menu-style))
                 ;; Options
                 [preferences-set {:prefs theme-settings
                                   :current (if @(subscribe [:theme/dark]) "theme-dark" "theme-light")}]
                 #_ [preferences-set {:prefs font-settings
                         :current @(subscribe [:appearance/font])}]
                 [preferences-set {:prefs width-settings
                                   :current @(subscribe [:appearance/width])}]
                 ;; Density disabled until block styles can support the varying line-heights
                 #_[preferences-set {:prefs density-settings
                           :current @(subscribe [:appearance/density])}]]]]))
