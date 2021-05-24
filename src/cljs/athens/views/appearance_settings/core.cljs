(ns athens.views.appearance-settings.core
  (:require
   ["@material-ui/core/Popover" :as Popover]
   ["@material-ui/icons/Brightness3" :default Brightness3]
   ["@material-ui/icons/Brightness7" :default Brightness7]
   ["@material-ui/icons/Style" :default Style]
   [athens.style :as style :refer [color DEPTH-SHADOWS]]
   [athens.views.buttons :refer [button]]
   [athens.views.dropdown :refer [menu-style]]
   [re-frame.core :refer [dispatch subscribe]]
   [reagent.core :as r]
   [stylefy.core :as stylefy :refer [use-style]]))


;;-------------------------------------------------------------------
;;--- material ui ---

(def m-popover (r/adapt-react-class (.-default Popover)))

;; Icons 

(def normal-width-icon
  [:svg {:viewBox "0 0 24 16"}
   [:path {:d "M5,3  h15"}]
   [:path {:d "M5,8  h15"}]
   [:path {:d "M5,13 h11"}]])

(def large-width-icon
  [:svg {:viewBox "0 0 24 24"}
   [:path {:d "M2 7H22"}]
   [:path {:d "M2 12H22"}]
   [:path {:d "M2 17H12"}]])

(def unlimited-width-icon
  [:svg {:viewBox "0 0 24 24"}
   [:path
    {:d "M17.5
         8.5L20.5
         11.5M20.5
         11.5L17.5
         14.5M20.5
         11.5L3.5
         11.5M3.5
         11.5L6.5
         8.5M3.5
         11.5L6.5
         14.5"}]])

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
                              :border-radius "calc(0.25rem + 0.25rem)" ;; Button corner radius + container padding makes "concentric" container radius
                              :padding "0.25rem 0"
                              :display "inline-flex"
                              :box-shadow [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px rgba(0, 0, 0, 0.05)"]]}]]})


(def preferences-help-style
  {:text-align "center"
   :color "var(--body-text-color---opacity-med)"
   :margin "0.25rem 0 0.5rem"
   :font-size "80%"})


(def preferences-set-style
  {:display "grid"
   :grid-auto-flow "column"
   :grid-auto-columns "1fr"
   :border-radius "0.25rem"
   :margin "0.125rem 0.5rem"
   :box-shadow "inset 0 0 0 1px transparent, 0 0 0 1px transparent"
   :transition "box-shadow 0.2s ease, filter 0.2s ease"
   :background "inherit"
   ::stylefy/manual [[:&:hover {:filter "brightness(120%)"
                                :box-shadow "inset 0 0 0 1px var(--border-color),
      0 0 0 1px var(--background-color)"}]
                     [:>button {:text-align "center"
                               :display "flex"
                               :align-items "center"
                               :justify-content "center"
                               :background "inherit"
                               :min-height "2.5rem"
                               :padding "0.5rem"
                               :font-weight "500"}
                      [:svg {:vector-effect "non-scaling-stroke"
                             :stroke "currentColor"
                             :stroke-weight "0.06rem"
                             :width "24px"
                             :height "24px"}]]]})

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
  [{:content [:> Brightness7]
    :id "theme-dark"
    :fn #(dispatch [:theme/toggle])}
   {:content [:> Brightness3]
    :id "theme-light"
    :fn #(dispatch [:theme/toggle])}])

(def font-settings
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
  [{:content normal-width-icon
    :id "width-normal"
    :fn #(dispatch [:appearance/set-width "width-normal"])}
   {:content large-width-icon
    :id "width-large"
    :fn #(dispatch [:appearance/set-width "width-large"])}
   {:content unlimited-width-icon
    :id "width-unlimited"
    :fn #(dispatch [:appearance/set-width "width-unlimited"])}])


#_ (def density-settings
  {:fn :appearance/set-density
   :content [{:content tight-density-icon  :id "density-tight"}
             {:content normal-density-icon :id "density-normal"}
             {:content loose-density-icon  :id "density-loose"}]})


(defn appearance-settings
  []
  (r/with-let [ele (r/atom nil)
               help-text (r/atom "Appearance settings")]
    [:<>
     ;; Dropdown toggle
     [button {:class [(when @ele "is-active")]
              :title "Change appearance preferences"
              :on-click #(reset! ele (.-currentTarget %))}
      [:> Style]]
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
       ;; Help area
       [:p (use-style preferences-help-style) @help-text]
       ;; Options
       [preferences-set {:prefs theme-settings
                         :current (if @(subscribe [:theme/dark]) "theme-light" "theme-dark")}]
       [preferences-set {:prefs font-settings
                         :current @(subscribe [:appearance/font])}]
       [preferences-set {:prefs width-settings
                         :current @(subscribe [:appearance/width])}]
       #_ [preferences-set {:prefs density-settings
                         :current @(subscribe [:appearance/density])}]]]]))
