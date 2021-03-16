(ns athens.views.right-sidebar
  (:require
    ["@material-ui/icons/BubbleChart" :default BubbleChart]
    ["@material-ui/icons/ChevronRight" :default ChevronRight]
    ["@material-ui/icons/Close" :default Close]
    ["@material-ui/icons/Description" :default Description]
    ["@material-ui/icons/FiberManualRecord" :default FiberManualRecord]
    ["@material-ui/icons/VerticalSplit" :default VerticalSplit]
    [athens.parse-renderer :as parse-renderer]
    [athens.style :refer [color OPACITIES ZINDICES]]
    [athens.views.block-page :refer [block-page-component]]
    [athens.views.buttons :refer [button]]
    [athens.views.graph-page :refer [graph-page]]
    [athens.views.node-page :refer [node-page-component]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
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
                                  :background-color (color :background-minus-1)}]
                     ["::-webkit-scrollbar" {:background (color :background-minus-1)
                                             :width "0.5rem"
                                             :height "0.5rem"}]
                     ["::-webkit-scrollbar-thumb" {:background (color :background-minus-2)
                                                   :border-radius "0.5rem"}]]})


(def sidebar-content-style
  {:display "flex"
   :flex "1 1 32vw"
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
   ::stylefy/manual [[:h1 {:font-size "1.5em"
                           :display "-webkit-box"
                           :-webkit-box-orient "vertical"
                           :-webkit-line-clamp 1
                           :line-clamp 1
                           :overflow "hidden"
                           :text-overflow "ellipsis"}]
                     [:.node-page :.block-page {:margin-top 0}]]})


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
   [:> VerticalSplit]
   [:p
    "Hold " [:kbd "shift"] " when clicking a page link to view the page in the sidebar."]])


(defn right-sidebar-el
  "Resizable: use local atom for width, but dispatch value to re-frame on mouse up. Instantiate local value with re-frame width too."
  [_ _ rf-width]
  (let [state (r/atom {:dragging false
                       :width rf-width})
        move-handler     (fn [e]
                           (when (:dragging @state)
                             (.. e preventDefault)
                             (let [x       (.-clientX e)
                                   inner-w js/window.innerWidth
                                   width   (-> (- inner-w x)
                                               (/ inner-w)
                                               (* 100))]
                               (swap! state assoc :width width))))
        mouse-up-handler (fn []
                           (when (:dragging @state)
                             (swap! state assoc :dragging false)
                             (dispatch [:right-sidebar/set-width (:width @state)])))]
    (r/create-class
      {:display-name           "right-sidebar"
       :component-did-mount    (fn []
                                 (js/document.addEventListener "mousemove" move-handler)
                                 (js/document.addEventListener "mouseup" mouse-up-handler))
       :component-will-unmount (fn []
                                 (js/document.removeEventListener "mousemove" move-handler)
                                 (js/document.removeEventListener "mouseup" mouse-up-handler))
       :reagent-render         (fn [open? items _]
                                 [:div (merge (use-style sidebar-style
                                                         {:class ["right-sidebar" (if open? "is-open" "is-closed")]})
                                              {:style (cond-> {}
                                                        (:dragging @state) (assoc :transition-duration "0s")
                                                        open? (assoc :width (str (:width @state) "vw")))})
                                  [:div (use-style {:cursor           "col-resize"
                                                    :height           "100%"
                                                    :position         "absolute"
                                                    :top              0
                                                    :width            "3px"
                                                    :z-index          (:zindex-fixed ZINDICES)
                                                    :background-color (color :border-color)}
                                                   {:on-mouse-down #(swap! state assoc :dragging true)})]
                                  [:div (use-style sidebar-content-style {:class (if open? "is-open" "is-closed")})
                                   ;; [:header (use-style sidebar-section-heading-style)] ;; Waiting on additional sidebar contents
                                   ;;  [:h1 "Pages and Blocks"]]
                                   ;;  [button [:> FilterList]]
                                   (if (empty? items)
                                     [empty-message]
                                     (doall
                                       (for [[uid {:keys [open node/title block/string is-graph?]}] items]
                                         ^{:key uid}
                                         [:article (use-style sidebar-item-style)
                                          [:header (use-style sidebar-item-heading-style {:class (when open "is-open")})
                                           [button {:style    sidebar-item-toggle-style
                                                    :on-click #(dispatch [:right-sidebar/toggle-item uid])
                                                    :class    (when open "is-open")}
                                            [:> ChevronRight]]
                                           [:h2
                                            (cond
                                              is-graph? [:<> [:> BubbleChart] [parse-renderer/parse-and-render title uid]]
                                              title     [:<> [:> Description] [parse-renderer/parse-and-render title uid]]
                                              :else     [:<> [:> FiberManualRecord] [parse-renderer/parse-and-render string uid]])]
                                           [:div {:class "controls"}
                                            ;;  [button [:> DragIndicator]]
                                            ;;  [:hr]
                                            [button {:on-click #(dispatch [:right-sidebar/close-item uid])}
                                             [:> Close]]]]
                                          (when open
                                            [:div (use-style sidebar-item-container-style)
                                             (cond
                                               is-graph? [graph-page uid]
                                               title     [node-page-component [:block/uid uid]]
                                               :else     [block-page-component [:block/uid uid]])])])))]])})))


(defn right-sidebar-component
  []
  (let [open? @(subscribe [:right-sidebar/open])
        items @(subscribe [:right-sidebar/items])
        width @(subscribe [:right-sidebar/width])]
    [right-sidebar-el open? items width]))
