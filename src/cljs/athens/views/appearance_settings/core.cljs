(ns athens.views.appearance-settings.core
  (:require
   ["@material-ui/core/Popover" :as Popover]
   ["@material-ui/icons/Brightness3" :default Brightness3]
   ["@material-ui/icons/Brightness7" :default Brightness7]
   ["@material-ui/icons/Style" :default Style]
   [athens.style :refer [color DEPTH-SHADOWS]]
   [athens.views.buttons :refer [button]]
   [athens.views.dropdown :refer [menu-style]]
   [re-frame.core :refer [dispatch subscribe]]
   [reagent.core :as r]
   [stylefy.core :as stylefy :refer [use-style]]))


;;-------------------------------------------------------------------
;;--- material ui ---

(def m-popover (r/adapt-react-class (.-default Popover)))

;;


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
                     [:button {:text-align "center"
                               :display "flex"
                               :align-items "center"
                               :justify-content "center"
                               :background "inherit"
                               :padding-left 0
                               :padding-right 0
                               :font-weight "500"}]]})

;; Components

(defn preferences-set
  [{:keys [prefs current]}]
  [:div (use-style preferences-set-style)
   (doall (for [option (:content prefs)]
            [button {:class (:id option)
                     :key (:id option)
                     :active (= (:id option) current)
                     :on-click #(dispatch [(:fn prefs) (:id option)])}
             (:content option)]))])


(def theme-settings
  {:fn :appearance/set-theme
   :content [{:content [:> Brightness7] :id "theme-dark"}
             {:content "Auto"           :id "theme-auto"}
             {:content [:> Brightness3] :id "theme-light"}]})

(def font-settings
  {:fn :appearance/set-font
   :content [{:content "Serif" :id "font-serif"}
             {:content "Sans"  :id "font-sans"}
             {:content "Mono"  :id "font-mono"}]})


(def width-settings
  {:fn :appearance/set-width
   :content [{:content "normal"    :id "width-normal"}
             {:content "large"     :id "width-large"}
             {:content "unlimited" :id "width-unlimited"}]})


(def density-settings
  {:fn :appearance/set-density
   :content [{:content "tight"  :id "density-tight"}
             {:content "normal" :id "density-normal"}
             {:content "loose"  :id "density-loose"}]})


(defn appearance-settings
  []
  (r/with-let [ele (r/atom nil)
               help-text (r/atom "Appearance settings")]
    [:<>
     ;; Dropdown toggle
     [button {:class [(when @ele "is-active")]
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
                         :current @(subscribe [:appearance/theme])}]
       [preferences-set {:prefs font-settings
                         :current @(subscribe [:appearance/font])}]
       [preferences-set {:prefs width-settings
                         :current @(subscribe [:appearance/width])}]
       [preferences-set {:prefs density-settings
                         :current @(subscribe [:appearance/density])}]]]]))
