(ns athens.devcards.app-toolbar
  (:require
   ["@material-ui/icons" :as mui-icons]
   [athens.views.buttons :refer [button]]
   [athens.router :refer [navigate]]
   [athens.style :refer [color]]
   [athens.subs]
   [re-frame.core :refer [subscribe dispatch]]
   [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles
 
 
(def app-header-style {:grid-area "app-header"
                       :justify-content "flex-start"
                       :backdrop-filter "blur(6px)"
                       :border-bottom "1px solid rgba(0,0,0,0.05)"
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


(def separator-style {:border "0"
                      :background (color :panel-color :opacity-high)
                      :margin-inline "20%"
                      :margin-block "0"
                      :inline-size "1px"
                      :block-size "auto"})


;;; Components


(defn separator[]
  [:hr (use-style separator-style)])


(defn app-header-2
  [route-name]
  (let [open? (subscribe [:left-sidebar/open])]
      [:header (use-style app-header-style)
       [:div (use-style app-header-control-section-style)
        [button {:active (when @open? true)
                 :label [:> mui-icons/Menu] :on-click-fn #(dispatch [:left-sidebar/toggle])}]
        [separator]
        [button {:on-click-fn #(navigate :home)
                 :active (when (= route-name :home) true)
                 :label       [:<>
                               [:> mui-icons/Today]
                               [:span "Timeline"]]}]
        [button {:on-click-fn #(navigate :pages)
                 :active (when (= route-name :pages) true)
                 :label [:<>
                         [:> mui-icons/FileCopy]
                         [:span "Pages"]]}]]
       [button {:on-click-fn #(dispatch [:athena/toggle])
                :style {:width "14rem" :background (color :panel-color :opacity-med)}
                :active (when @(subscribe [:athena/open]) true)
                :label [:<> [:> mui-icons/Search] [:span "Find or Create a Page"]]}]
       [:div (use-style app-header-secondary-controls-style)
        [button {:label [:> mui-icons/Settings]}]
        [separator]
        [button {:label [:> mui-icons/VerticalSplit]
                 :on-click-fn #(dispatch [:right-sidebar/toggle])}]]]))


;;; Devcards


