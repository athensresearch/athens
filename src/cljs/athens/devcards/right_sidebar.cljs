(ns athens.devcards.right-sidebar
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.devcards.block-page :refer [block-page-component]]
    [athens.devcards.buttons :refer [button button-primary]]
    [athens.devcards.node-page :refer [node-page-component]]
    [athens.style :refer [color OPACITIES]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer [defcard-rg]]
    [garden.color :refer [lighten]]
    [re-frame.core :refer [dispatch subscribe]]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles


(def sidebar-width "32vw")


(stylefy/keyframes "content-appears"
                   [:from
                    {:opacity "0"
                     :width "0"
                     :transform "translateX(10%)"}]
                   [:to
                    {:opacity "1"
                     :width sidebar-width
                     :transform "translateX(0)"}])


(stylefy/keyframes "content-disappears"
                   [:from
                    {:opacity "1"
                     :width sidebar-width
                     :transform "translateX(0)"}]
                   [:to
                    {:opacity "0"
                     :width "0"
                     :transform "translateX(10%)"}])


(def sidebar-style
  {:justify-self "stretch"
   :overflow "auto"
   :flex "0 0 auto"
   :display "flex"
   :justify-content "space-between"
   :transition "opacity 0.5s ease"
   ::stylefy/manual [[:svg {:color (color :body-text-color :opacity-high)}]
                     [:&.is-open {:border-left [["1px solid " (color :panel-color :opacity-low)]]
                                  :background-color (color :panel-color :opacity-low)}
                      [:> [:div {:animation "content-appears 0.15s"
                                 :animation-fill-mode "both"}]]]
                     [:&.is-closed [:> [:div {:animation "content-disappears 0.1s"
                                              :animation-fill-mode "both"}]]]]})


(def sidebar-toggle-style
  {:border-radius "0"
   :flex-shrink "0"
   :align-items "flex-start"
   :padding "80px 4px 0"
   :position "relative"
   :z-index "10"
   :box-shadow [["inset 1px 0 0 " (color :panel-color)]]
   ::stylefy/manual [[:& {:transition-duration "0.15s"}]
                     [:&:hover {:background (lighten (color :panel-color) 5)}]
                     [:&:focus :active {:outline "none"
                                        :color "inherit"}]]})


(def sidebar-content-style
  {:display "flex"
   :width sidebar-width
   :opacity "0"
   :animation-fill-mode "both"
   :animation-timing-function "ease-out"
   :flex-direction "column"
   :overflow-y "auto"})


(def sidebar-section-heading-style
  {:font-size "14px"
   :display "flex"
   :flex-direction "row"
   :align-items "center"
   :min-height "40px"
   :padding "8px 16px 8px 24px"
   ::stylefy/manual [[:h1 {:font-size "inherit"
                           :margin "0 auto 0 0"
                           :line-height "1"
                           :color (color :body-text-color :opacity-med)}]]})


(def sidebar-item-style
  {:display "flex"
   :flex-direction "column"
   :border-top [["1px solid" (color :panel-color)]]})


(def sidebar-item-toggle-style
  {:margin "auto 8px auto 0"
   :flex "0 0 auto"
   :width "28px"
   :height "28px"
   :padding "0"
   :border-radius "1000px"
   :cursor "pointer"
   :place-content "center"
   ::stylefy/manual [[:svg {:transition "all 0.1s ease"
                            :margin "0"}]
                     [:&.is-open [:svg {:transform "rotate(90deg)"}]]]})


(def sidebar-item-container-style
  {:padding "0 32px 20px"
   :line-height "24px"
   :font-size "15px"
   :position "relative"
   :z-index "1"
   :width sidebar-width})


(def sidebar-item-heading-style
  {:font-size "16px"
   :display "flex"
   :align-items "center"
   :padding "4px 16px"
   :position "sticky"
   :backdrop-filter "blur(12px)"
   :z-index "10"
   :background "#FBFAFA" ;; FIXME: Replace with weighted-mix color function
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
                                  :transition "opacity 0.3s ease"
                                  :opacity "0.25"}]
                     [:&:hover [:.controls {:opacity "1"}]]
                     [:svg {:font-size "18px"}]
                     [:hr {:width "1px"
                           :background (color :panel-color)
                           :border "0"
                           :margin "4px"
                           :flex "0 0 1px"
                           :height "1em"
                           :justify-self "stretch"}]
                     [:&.is-open [:h2 {:font-weight "500"}]]]})


(def empty-message-style {:align-self "center"
                          :display "flex"
                          :margin "auto auto"
                          :align-items "center"
                          :color (color :body-text-color :opacity-med)
                          :font-size "14px"
                          :border-radius "8px"
                          :line-height 1.3
                          ::stylefy/manual [[:p {:max-width "13em"}]]})


;;; Components


(defn empty-message []
  [:div (use-style empty-message-style)
   [:p "Hold shift when clicking a page link to view the page in the sidebar."]])


(defn right-sidebar-el
  [open? items]
  [:div (use-style sidebar-style {:class (if open? "is-open" "is-closed")})
   [:div (use-style sidebar-content-style)
    [:header (use-style sidebar-section-heading-style)
     [:h1 "Pages and Blocks"]
    ;;  [button {:label [:> mui-icons/FilterList]}]
     ]
    (if (empty? items)
      [empty-message]
      (doall
       (for [[uid {:keys [open node/title block/string]}] items
             :let [node-page? (boolean title)]]
         ^{:key uid}
         [:article (use-style sidebar-item-style)
          [:header (use-style sidebar-item-heading-style {:class (when open "is-open")})
           [button {:style sidebar-item-toggle-style
                    :on-click-fn #(dispatch [:right-sidebar/toggle-item uid])
                    :class (when open "is-open")
                    :label [:> mui-icons/ChevronRight]}]
           [:h2
            (if title
              [:<> [:> mui-icons/Description] title]
              [:<> [:> mui-icons/FiberManualRecord] string])]
           [:div {:class "controls"}
        ;;  [button {:label [:> mui-icons/DragIndicator]}]
        ;;  [:hr]
            [button {:on-click-fn #(dispatch [:right-sidebar/close-item uid])
                     :label [:> mui-icons/Close]}]]]
          (when open
            [:div (use-style sidebar-item-container-style)
             (if node-page?
               [node-page-component [:block/uid uid]]
               [block-page-component [:block/uid uid]])])])))]
   [button {:style sidebar-toggle-style
            :class (if open? "is-open" "is-closed")
            :on-click-fn #(dispatch [:right-sidebar/toggle])
            :label (if open? [:> mui-icons/Close] [:> mui-icons/Add])}]])


(defn right-sidebar-component
  []
  (let [open? @(subscribe [:right-sidebar/open])
        items @(subscribe [:right-sidebar/items])]
    [right-sidebar-el open? items]))


;;; Devcards


;;(defcard-rg Init
;;  [button-primary {:label "Toggle" :on-click-fn #(dispatch [:open-in-rightbar "data"])}])



(defcard-rg Toggle
  [button-primary {:label "Toggle" :on-click-fn #(dispatch [:right-sidebar/toggle])}])


(defcard-rg Right-Sidebar
  [:div {:style {:display "flex" :height "60vh" :justify-content "flex-end"}}
   [right-sidebar-component]]
  {:padding false})
