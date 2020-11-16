(ns athens.views.app-toolbar
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.router :as router]
    [athens.style :refer [color]]
    [athens.subs]
    #_[athens.util :as util]
    [athens.views.buttons :refer [button]]
    [re-frame.core :refer [subscribe dispatch]]
    #_[reagent.core :as r]
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
   :z-index "1000"
   :grid-auto-flow "column"
   :padding "0.25rem 0.75rem"
   ::stylefy/manual [[:svg {:font-size "20px"}]
                     [:button {:justify-self "flex-start"}]]})


(def app-header-control-section-style
  {:display "grid"
   :grid-auto-flow "column"
   :background (:color :background-color :opacity-med)
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
        theme-dark  (subscribe [:theme/dark])]
    (fn []
      [:<>
       [:header (use-style app-header-style)
        [:div (use-style app-header-control-section-style)
         [button {:active   @left-open?
                  :on-click #(dispatch [:left-sidebar/toggle])}
          [:> mui-icons/Menu]]
         [separator]
         ;; TODO: refactor to effects
         [button {:on-click #(.back js/window.history)} [:> mui-icons/ChevronLeft]]
         [button {:on-click #(.forward js/window.history)} [:> mui-icons/ChevronRight]]
         [separator]
         [button {:on-click router/nav-daily-notes
                  :active   (= @route-name :home)} [:> mui-icons/Today]]
         [button {:on-click #(router/navigate :pages)
                  :active   (= @route-name :pages)} [:> mui-icons/FileCopy]]
         [button {:on-click #(dispatch [:athena/toggle])
                  :style    {:width "14rem" :margin-left "1rem" :background (color :background-minus-1)}
                  :active   @(subscribe [:athena/open])}
          [:<> [:> mui-icons/Search] [:span "Find or Create a Page"]]]]

        [:div (use-style app-header-secondary-controls-style)
         ;; Click to Open
         #_[button {:on-click #(prn "TODO")}
            [(r/adapt-react-class mui-icons/FolderOpen)
             {:style {:align-self "center"}}]]
         ;; sync UI
         [(reagent.core/adapt-react-class mui-icons/FiberManualRecord)
          {:style {:color      (color (if @(subscribe [:db/synced])
                                        :confirmation-color
                                        :highlight-color))
                   :align-self "center"}}]
         #_[separator]
         [button {:on-click #(dispatch [:modal/toggle])
                  #_(swap! state assoc :modal :folder)}
          [:> mui-icons/FolderOpen]]
         ;;[:> mui-icons/Publish]]
         [separator]
         [button {:on-click #(dispatch [:theme/toggle])}
          (if @theme-dark
            [:> mui-icons/ToggleOff]
            [:> mui-icons/ToggleOn])]
         [separator]
         [button {:active   @right-open?
                  :on-click #(dispatch [:right-sidebar/toggle])}
          [:> mui-icons/VerticalSplit {:style {:transform "scaleX(-1)"}}]]]]])))

