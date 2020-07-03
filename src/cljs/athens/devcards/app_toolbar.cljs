(ns athens.devcards.app-toolbar
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.devcards.breadcrumbs :refer [breadcrumbs-list breadcrumb]]
    [athens.devcards.buttons :refer [button button-primary]]
    [athens.style :refer [color]]
    [athens.subs]
    [re-frame.core :refer [subscribe dispatch]]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles
 
 
(def app-header-style {:grid-area "app-header"
                       :justify-content "flex-start"
                       :position "absolute"
                       :backdrop-filter "blur(6px)"
                       :border-bottom "1px solid rgba(0,0,0,0.05)"
                       :background-clip "padding-box"
                       :top "0"
                       :left "0"
                       :right "0"
                       :align-items "center"
                       :font-size "16px"
                       :display "grid"
                       :grid-template-columns "auto 1fr auto"
                       :z-index "1000"
                       :background (color :app-bg-color :opacity-higher)
                       :grid-auto-flow "column"
                       :grid-gap "1rem"
                       :padding "0.5rem"
                       ::stylefy/manual [[:svg {:font-size "20px"}]
                                         [:button {:justify-self "flex-start"}]]})

(def app-header-control-section-style
  {:display "grid"
   :grid-auto-flow "column"
   :grid-gap "0.25rem"})

(def app-header-secondary-controls-style
  (merge app-header-control-section-style
         {:color (color :body-text-color :opacity-med)
          :justify-self "flex-end"
          :margin-left "auto"
          ::stylefy/manual [[:button {:color "inherit"}]]}))


;;; Components


(defn app-header
  []
  [:header (use-style app-header-style)
   [:div (use-style app-header-control-section-style)
    [button-primary {:on-click-fn #(dispatch [:toggle-athena])
                     :label [:> mui-icons/Search]}]
    [button {:label [:> mui-icons/Today]}]
    [button {:label [:> mui-icons/Menu]}]]
   [breadcrumbs-list
    [breadcrumb {:key "a"} "thing 1"]
    [breadcrumb {:key "b"} "thing 2"]
    [breadcrumb {:key "c"} "thing 3"]]
   [:div (use-style app-header-secondary-controls-style)
    [button {:label [:> mui-icons/Settings]}]
    [button {:label [:> mui-icons/TextFormat]}]
    [:span {:style {:opacity "0.5"}} " • "]
    [button {:label [:> mui-icons/VerticalSplit]
             :on-click-fn #(dispatch [:right-sidebar/toggle])}]]])


(defn app-header-2
  []
  (let [open? (subscribe [:left-sidebar])]
    (fn []
      (if @open?
        [:header (use-style app-header-style)
         [:div (use-style app-header-control-section-style)
          [button {:label [:> mui-icons/Menu] :on-click-fn #(dispatch [:toggle-left-sidebar])}]
          [button {:label [:> mui-icons/Today]}]]
          [button-primary {:on-click-fn #(dispatch [:toggle-athena])
                           :style {:width "14rem"}
                           :label [:<> [:> mui-icons/Search] [:span "Find or Create a Page"]]}]
        ;;  [breadcrumbs-list
        ;;   [breadcrumb {:key "a"} "thing 1"]
        ;;   [breadcrumb {:key "b"} "thing 2"]
        ;;   [breadcrumb {:key "c"} "thing 3"]]
         [:div (use-style app-header-secondary-controls-style)
          [button {:label [:> mui-icons/Settings]}]
          [:span {:style {:opacity "0.5"}} " • "]
          [button {:label [:> mui-icons/VerticalSplit]
                   :on-click-fn #(dispatch [:right-sidebar/toggle])}]]]
        [:header (use-style app-header-style)
         [:div (use-style app-header-control-section-style)
          [button {:label [:> mui-icons/Menu] :on-click-fn #(dispatch [:toggle-left-sidebar])}]
          [button {:label [:> mui-icons/Today]}]]
          [button-primary {:on-click-fn #(dispatch [:toggle-athena])
                           :style {:width "14rem"}
                           :label [:<> [:> mui-icons/Search] [:span "Find or Create a Page"]]}]
         [breadcrumbs-list
          [breadcrumb {:key "a"} "thing 1"]
          [breadcrumb {:key "b"} "thing 2"]
          [breadcrumb {:key "c"} "thing 3"]]
         [:div (use-style app-header-secondary-controls-style)
          [button {:label [:> mui-icons/Settings]}]
          [:span {:style {:opacity "0.5"}} " • "]
          [button {:label [:> mui-icons/VerticalSplit]
                   :on-click-fn #(dispatch [:right-sidebar/toggle])}]]]))))


;;; Devcards


