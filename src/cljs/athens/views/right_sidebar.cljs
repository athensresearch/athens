(ns athens.views.right-sidebar
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.style :refer [color OPACITIES]]
    [athens.views.block-page :refer [block-page-component]]
    [athens.views.buttons :refer [button]]
    [athens.views.node-page :refer [node-page-component]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [re-frame.core :refer [dispatch subscribe]]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles


(def sidebar-style
  {:justify-self "stretch"
   :overflow "hidden"
   :width "0"
   :grid-area "secondary-content"
   :display "flex"
   :justify-content "space-between"
   :padding-top "2.75rem"
   :transition-property "width, border, background"
   :transition-duration "0.35s"
   :transition-timing-function "ease-out"
   :background-color (color :background-minus-1)
   :box-shadow [["0 -100px 0 " (color :background-minus-1) ", inset 1px 0 " (color :background-minus-1)]]
   ::stylefy/manual [[:svg {:color (color :body-text-color :opacity-high)}]
                     [:&.is-closed {:width "0"}]
                     [:&.is-open {:width "32vw"
                                  :background-color (color :background-minus-1)}]]})


(def sidebar-content-style
  {:display "flex"
   :flex "0 0 32vw"
   :flex-direction "column"
   :margin-left "0"
   :transition "all 0.35s ease-out"
   :overflow-y "auto"
   ::stylefy/manual [[:&.is-closed {:margin-left "-32vw"
                                    :opacity 0}]
                     [:&.is-open {:opacity 1}]]})


(def sidebar-section-heading-style
  {:font-size "14px"
   :display "flex"
   :flex-direction "row"
   :align-items "center"
   :min-height "2.75rem"
   :padding "0.5rem 1rem 0.25rem 1.5rem"
   ::stylefy/manual [[:h1 {:font-size "inherit"
                           :margin "0 auto 0 0"
                           :line-height "1"
                           :color (color :body-text-color :opacity-med)}]]})


(def sidebar-item-style
  {:display "flex"
   :flex "0 0 auto"
   :flex-direction "column"
   :border-top [["1px solid" (color :border-color)]]})


(def sidebar-item-toggle-style
  {:margin "auto 0.5rem auto 0"
   :flex "0 0 auto"
   :width "1.75rem"
   :height "1.75rem"
   :padding "0"
   :border-radius "1000px"
   :cursor "pointer"
   :place-content "center"
   ::stylefy/manual [[:svg {:transition "all 0.1s ease-out"
                            :margin "0"}]
                     [:&.is-open [:svg {:transform "rotate(90deg)"}]]]})


(def sidebar-item-container-style
  {:padding "0 2rem 1.25rem"
   :line-height "1.5rem"
   :font-size "15px"
   :position "relative"
   :z-index 1
   :width "32vw"})


(def sidebar-item-heading-style
  {:font-size "16px"
   :display "flex"
   :flex "0 0 auto"
   :align-items "center"
   :padding "0.25rem 1rem"
   :position "sticky"
   :z-index 2
   :background (color :background-minus-1) ;; FIXME: Replace with weighted-mix color function
   :top "0"
   :bottom "0"
   ::stylefy/manual [[:h2 {:font-size "inherit"
                           :flex "1 1 100%"
                           :line-height "1"
                           :margin "0"
                           :white-space "nowrap"
                           :text-overflow "ellipsis"
                           :font-weight "normal"
                           :max-width "100%"
                           :overflow "hidden"
                           :align-items "center"
                           :color (color :body-text-color)}
                      [:svg {:opacity (:opacity-med OPACITIES)
                             :display "inline"
                             :vertical-align "-4px"
                             :margin-right "0.2em"}]]
                     [:.controls {:display "flex"
                                  :flex "0 0 auto"
                                  :align-items "stretch"
                                  :flex-direction "row"
                                  :transition "opacity 0.3s ease-out"
                                  :opacity "0.25"}]
                     [:&:hover [:.controls {:opacity "1"}]]
                     [:svg {:font-size "18px"}]
                     [:hr {:width "1px"
                           :background (color :background-minus-1)
                           :border "0"
                           :margin "0.25rem"
                           :flex "0 0 1px"
                           :height "1em"
                           :justify-self "stretch"}]
                     [:&.is-open [:h2 {:font-weight "500"}]]]})


(def empty-message-style
  {:align-self "center"
   :display "flex"
   :flex-direction "column"
   :margin "auto auto"
   :align-items "center"
   :text-align "center"
   :color (color :body-text-color :opacity-med)
   :font-size "14px"
   :border-radius "0.5rem"
   :line-height 1.3
   ::stylefy/manual [[:svg {:opacity (:opacity-low OPACITIES)
                            :font-size "80px"}]
                     [:p {:max-width "13em"}]]})


;;; Components


(defn empty-message
  []
  [:div (use-style empty-message-style)
   [:> mui-icons/VerticalSplit]
   [:p
    "Hold " [:kbd "shift"] " when clicking a page link to view the page in the sidebar."]])


(defn right-sidebar-el
  [open? items]
  [:div (use-style sidebar-style {:class (if open? "is-open" "is-closed")})
   [:div (use-style sidebar-content-style {:class (if open? "is-open" "is-closed")})
    ;; [:header (use-style sidebar-section-heading-style)] ;; Waiting on additional sidebar contents 
    ;;  [:h1 "Pages and Blocks"]]
    ;;  [button [:> mui-icons/FilterList]]

    (if (empty? items)
      [empty-message]
      (doall
        (for [[uid {:keys [open node/title block/string]}] items]
          ^{:key uid}
          [:article (use-style sidebar-item-style)
           [:header (use-style sidebar-item-heading-style {:class (when open "is-open")})
            [button {:style sidebar-item-toggle-style
                     :on-click #(dispatch [:right-sidebar/toggle-item uid])
                     :class (when open "is-open")}
             [:> mui-icons/ChevronRight]]
            [:h2
             (if title
               [:<> [:> mui-icons/Description] title]
               [:<> [:> mui-icons/FiberManualRecord] string])]
            [:div {:class "controls"}
        ;;  [button [:> mui-icons/DragIndicator]]
        ;;  [:hr]
             [button {:on-click #(dispatch [:right-sidebar/close-item uid])}
              [:> mui-icons/Close]]]]
           (when open
             [:div (use-style sidebar-item-container-style)
              (if title
                [node-page-component [:block/uid uid]]
                [block-page-component [:block/uid uid]])])])))]])


(defn right-sidebar-component
  []
  (let [open? @(subscribe [:right-sidebar/open])
        items @(subscribe [:right-sidebar/items])]
    [right-sidebar-el open? items]))
