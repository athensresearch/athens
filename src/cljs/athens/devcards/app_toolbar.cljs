(ns athens.devcards.app-toolbar
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.router :refer [navigate]]
    [athens.style :refer [color]]
    [athens.subs]
    [athens.views.buttons :refer [button]]
    [re-frame.core :refer [subscribe dispatch]]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles


(def app-header-style
  {:grid-area "app-header"
   :justify-content "flex-start"
   :background-clip "padding-box"
   :align-items "center"
   :font-size "16px"
   :display "grid"
   :position "absolute"
   :top 0
   :right 0
   :left 0
   :grid-template-columns "auto 1fr auto"
   :z-index "1000"
   :grid-auto-flow "column"
   :padding "0.25rem 0.75rem 0.25rem 0.25rem"
                      ;;  :padding "0.25rem 0.75rem 0.25rem 66px" ;; Electron styling
   ::stylefy/manual [[:svg {:font-size "20px"}]
                     [:button {:justify-self "flex-start"}]]})


(def app-header-control-section-style
  {:display "grid"
   :grid-auto-flow "column"
   :backdrop-filter "blur(6px) contrast(50%) brightness(170%)"
   :padding "0.25rem"
   :border-radius "6px"
   :grid-gap "0.25rem"})


(def app-header-secondary-controls-style
  (merge app-header-control-section-style
         {:color (color :body-text-color :opacity-med)
          :justify-self "flex-end"
          :backdrop-filter "blur(6px)"
          :margin-left "auto"
          ::stylefy/manual [[:button {:color "inherit"}]]}))


(def separator-style
  {:border "0"
   :background (color :panel-color :opacity-high)
   :margin-inline "20%"
   :margin-block "0"
   :inline-size "1px"
   :block-size "auto"})


;;; Components


(defn separator
  []
  [:hr (use-style separator-style)])


(defn app-toolbar
  []
  (let [left-open? (subscribe [:left-sidebar/open])
        right-open? (subscribe [:right-sidebar/open])
        current-route (subscribe [:current-route])
        route-name (-> @current-route :data :name)]

    [:header (use-style app-header-style)
     [:div (use-style app-header-control-section-style)
      [button {:active (when @left-open? true)
               :label [:> mui-icons/Menu] :on-click-fn #(dispatch [:left-sidebar/toggle])}]
      [separator]
      [button {:on-click-fn #(navigate :home)
               :label [:> mui-icons/ChevronLeft]}]
      [button {:on-click-fn #(navigate :home)
               :label [:> mui-icons/ChevronRight]}]
      [separator]
      [button {:on-click-fn #(navigate :home)
               :active (when (= route-name :home) true)
               :label [:> mui-icons/Today]}]
      [button {:on-click-fn #(navigate :pages)
               :active (when (= route-name :pages) true)
               :label [:> mui-icons/FileCopy]}]
      [button {:on-click-fn #(dispatch [:athena/toggle])
               :style {:width "14rem" :margin-left "1rem" :background (color :panel-color :opacity-med)}
               :active (when @(subscribe [:athena/open]) true)
               :label [:<> [:> mui-icons/Search] [:span "Find or Create a Page"]]}]]

     [:div (use-style app-header-secondary-controls-style)
      [button {:label [:> mui-icons/Settings]}]
      [separator]
      [button {:label [:> mui-icons/VerticalSplit {:style {:transform "scaleX(-1)"}}]
               :active (when @right-open? true)
               :on-click-fn #(dispatch [:right-sidebar/toggle])}]]]))


;;; Devcards


