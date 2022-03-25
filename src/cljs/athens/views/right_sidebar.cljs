(ns athens.views.right-sidebar
  (:require
   ["@chakra-ui/react" :refer [Flex Box IconButton Accordion AccordionItem AccordionButton AccordionIcon AccordionPanel]]
   ["@material-ui/icons/BubbleChart" :default BubbleChart]
   ["@material-ui/icons/Close" :default Close]
   ["@material-ui/icons/Description" :default Description]
   ["@material-ui/icons/FiberManualRecord" :default FiberManualRecord]
   ["@material-ui/icons/VerticalSplit" :default VerticalSplit]
   ["framer-motion" :refer [AnimatePresence motion]]
   [athens.parse-renderer :as parse-renderer]
   [athens.style :refer [color OPACITIES]]
   [athens.views.pages.block-page :as block-page]
   [athens.views.pages.graph :as graph]
   [athens.views.pages.node-page :as node-page]
   [re-frame.core :refer [dispatch subscribe]]
   [reagent.core :as r]
   [stylefy.core :as stylefy :refer [use-style]]))

(def empty-message-style
  {:align-self "center"
   :display "flex"
   :flex-direction "column"
   :margin "auto auto"
   :align-items "center"
   :text-align "center"
   :color (color :body-text-color :opacity-med)
   :font-size "80%"
   :border-radius "0.5rem"
   :line-height 1.3
   ::stylefy/manual [[:svg {:opacity (:opacity-low OPACITIES)
                            :font-size "1000%"}]
                     [:p {:max-width "13em"}]]})


;; Components


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
                                [:> AnimatePresence {:initial false}
                                 (when open?
                                   [:> (.-div motion)
                                    {:style {:display "flex"
                                             :sx {"-webkit-app-region" "no-drag"}
                                             :flex-direction "column"
                                             :height "100%"
                                             :paddingTop "3.25rem"
                                             :alignItems "stretch"
                                             :justifySelf "stretch"
                                             :transformOrigin "right"
                                             :justifyContent "space-between"
                                             :overflowX "visible"
                                             :position "relative"
                                             :gridArea "secondary-content"}
                                     :initial {:width 0
                                               :opacity 0}
                                     :transition (if (:dragging @state)
                                                   {:type "tween"
                                                    :duration 0}
                                                   nil)
                                     :animate {:width (str (:width @state) "vw")
                                               :opacity 1}
                                     :exit {:width 0
                                            :opacity 0}}
                                    [:> Box {:role "separator"
                                             :aria-orientation "vertical"
                                             :cursor "col-resize"
                                             :position "absolute"
                                             :top 0
                                             :height "100%"
                                             :width "1px"
                                             :zIndex 1
                                             :transitionDuration "0.2s"
                                             :transitionTimingFunction "ease-in-out"
                                             :transitionProperty "common"
                                             :bg "separator.divider"
                                             :_hover {:bg "link"}
                                             :_active {:bg "link"}
                                             :_after {:content "''"
                                                      :position "absolute"
                                                      :inset "-4px"}
                                             :on-mouse-down #(swap! state assoc :dragging true)
                                             :class (when (:dragging @state) "is-dragging")}]
                                    [:> Flex {:flexDirection "column"
                                              :bg "background.upper"
                                              :flex 1;
                                              :maxHeight "calc(100vh - 3.25rem - 1px)"
                                              :width (str (:width @state) "vw")
                                              :overflowY "overlay"}
                                     (if (empty? items)
                                       [empty-message]
                                       [:> Accordion {:allowMultiple true}
                                       (doall
                                        (for [[uid {:keys [open node/title block/string is-graph?]}] items]
                                          ^{:key uid}
                                          [:> AccordionItem {:_first {:borderTop 0}}
                                           [:> Box {:as "h2" :position "relative"}
                                            [:> AccordionButton
                                             [:> AccordionIcon]
                                             (cond
                                               is-graph? [:<> [:> BubbleChart] [parse-renderer/parse-and-render title uid]]
                                               title     [:<> [:> Description] [parse-renderer/parse-and-render title uid]]
                                               :else     [:<> [:> FiberManualRecord] [parse-renderer/parse-and-render string uid]])]
                                            [:> IconButton {:size "sm"
                                                            :position "absolute"
                                                            :right 1
                                                            :top 1
                                                            :background "transparent"
                                                            :onClick #(dispatch [:right-sidebar/close-item uid])}
                                             [:> Close]]]
                                           [:> AccordionPanel
                                            (cond
                                              is-graph? [graph/page uid]
                                              title     [node-page/page [:block/uid uid]]
                                              :else     [block-page/page [:block/uid uid]])]]))])]])])})))


(defn right-sidebar
  []
  (let [open? @(subscribe [:right-sidebar/open])
        items @(subscribe [:right-sidebar/items])
        width @(subscribe [:right-sidebar/width])]
    [right-sidebar-el open? items width]))
