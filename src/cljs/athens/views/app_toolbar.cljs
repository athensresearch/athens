(ns athens.views.app-toolbar
  (:require
    ["@material-ui/icons/BubbleChart" :default BubbleChart]
    ["@material-ui/icons/ChevronLeft" :default ChevronLeft]
    ["@material-ui/icons/ChevronRight" :default ChevronRight]
    ["@material-ui/icons/FiberManualRecord" :default FiberManualRecord]
    ["@material-ui/icons/FileCopy" :default FileCopy]
    ["@material-ui/icons/FolderOpen" :default FolderOpen]
    ["@material-ui/icons/Menu" :default Menu]
    ["@material-ui/icons/MergeType" :default MergeType]
    ["@material-ui/icons/Search" :default Search]
    ["@material-ui/icons/Settings" :default Settings]
    ["@material-ui/icons/Today" :default Today]
    ["@material-ui/icons/ToggleOff" :default ToggleOff]
    ["@material-ui/icons/ToggleOn" :default ToggleOn]
    ["@material-ui/icons/VerticalSplit" :default VerticalSplit]
    [athens.router :as router]
    [athens.style :refer [color]]
    [athens.subs]
    [athens.util :as util]
    [athens.views.buttons :refer [button]]
    [athens.views.filesystem :as filesystem]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles


(def app-header-style
  {:grid-area "app-header"
   :justify-content "flex-start"
   :background-clip "padding-box"
   :align-items "center"
   :display "grid"
   :position "absolute"
   :top "-0.25rem"
   :right 0
   :left 0
   :grid-template-columns "auto 1fr auto"
   :z-index "1070"
   :grid-auto-flow "column"
   :padding "0.25rem 0.75rem"
   ::stylefy/manual [[:svg {:font-size "20px"}]
                     [:button {:justify-self "flex-start"}]]})


(def app-header-control-section-style
  {:display "grid"
   :grid-auto-flow "column"
   :background (color :background-color :opacity-high)
   :backdrop-filter "blur(0.375rem)"
   :padding "0.25rem"
   :border-radius "calc(0.25rem + 0.25rem)" ;; Button corner radius + container padding makes "concentric" container radius
   :grid-gap "0.25rem"})


(def app-header-secondary-controls-style
  (merge app-header-control-section-style
         {:color (color :body-text-color :opacity-med)
          :justify-self "flex-end"
          :margin-left "auto"
          ::stylefy/manual [[:button {:color "inherit"}]]}))


(def separator-style
  {:border "0"
   :background (color :background-minus-1 :opacity-high)
   :margin-inline "20%"
   :margin-block "0"
   :inline-size "1px"
   :block-size "auto"})


(stylefy/keyframes "fade-in"
                   [:from
                    {:opacity "0"}]
                   [:to
                    {:opacity "1"}])


;;; Components


(defn separator
  []
  [:hr (use-style separator-style)])


(defn app-toolbar
  []
  (let [left-open?  (subscribe [:left-sidebar/open])
        right-open? (subscribe [:right-sidebar/open])
        route-name  (subscribe [:current-route/name])
        electron? (util/electron?)
        theme-dark  (subscribe [:theme/dark])
        merge-open? (reagent.core/atom false)]
    (fn []
      [:<>


       (when @merge-open?
         [filesystem/merge-modal merge-open?])

       [:header (use-style app-header-style)
        [:div (use-style app-header-control-section-style)
         [button {:active   @left-open?
                  :on-click #(dispatch [:left-sidebar/toggle])}
          [:> Menu]]
         [separator]
         ;; TODO: refactor to effects
         (when electron?
           [:<>
            [button {:on-click #(.back js/window.history)} [:> ChevronLeft]]
            [button {:on-click #(.forward js/window.history)} [:> ChevronRight]]
            [separator]])
         [button {:on-click router/nav-daily-notes
                  :active   (= @route-name :home)} [:> Today]]
         [button {:on-click #(router/navigate :pages)
                  :active   (= @route-name :pages)} [:> FileCopy]]
         [button {:on-click #(router/navigate :graph)
                  :active   (= @route-name :graph)} [:> BubbleChart]]
         ;; below is used for testing error tracking
         #_[button {:on-click #(throw (js/Error "error"))
                    :style {:border "1px solid red"}} [:> Warning]]
         [button {:on-click #(dispatch [:athena/toggle])
                  :style    {:width "14rem" :margin-left "1rem" :background (color :background-minus-1)}
                  :active   @(subscribe [:athena/open])}
          [:<> [:> Search] [:span "Find or Create a Page"]]]]

        [:div (use-style app-header-secondary-controls-style)
         (if electron?
           [:<>
            [(reagent.core/adapt-react-class FiberManualRecord)
             {:style {:color      (color (if @(subscribe [:db/synced])
                                           :confirmation-color
                                           :highlight-color))
                      :align-self "center"}}]
            [button {:on-click #(swap! merge-open? not)}
             [:> MergeType]]
            [button {:on-click #(router/navigate :settings)
                     :active   (= @route-name :settings)}
             [:> Settings]]
            [button {:on-click #(dispatch [:modal/toggle])}
             [:> FolderOpen]]
            [separator]]
           [button {:on-click #(dispatch [:get-db/init]) :primary true} "Load Test DB"])
         [button {:on-click #(dispatch [:theme/toggle])}
          (if @theme-dark
            [:> ToggleOff]
            [:> ToggleOn])]
         [separator]
         [button {:active   @right-open?
                  :on-click #(dispatch [:right-sidebar/toggle])}
          [:> VerticalSplit {:style {:transform "scaleX(-1)"}}]]]]])))

