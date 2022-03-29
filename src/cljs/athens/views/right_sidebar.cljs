(ns athens.views.right-sidebar
  (:require
   ["/components/Icons/icons" :refer [RightSidebarAdd]]
   ["@chakra-ui/react" :refer [Flex Text Box AddIcon IconButton Accordion AccordionItem AccordionButton AccordionIcon AccordionPanel]]
   ["@material-ui/icons/Close" :default Close]
   ["framer-motion" :refer [AnimatePresence motion]]
   [athens.parse-renderer :as parse-renderer]
   [athens.views.pages.block-page :as block-page]
   [athens.views.pages.graph :as graph]
   [athens.views.pages.node-page :as node-page]
   [re-frame.core :refer [dispatch subscribe]]
   [reagent.core :as r]))

;; Components


(defn empty-message
  []
  [:> Box {:alignSelf "center"
           :display "flex"
           :flexDirection "column"
           :margin "auto"
           :padding 5
           :gap "1rem"
           :alignItems "center"
           :textAlign "center"
           :color "foreground.secondary"
           :fontSize "80%"
           :borderRadius "0.5rem"
           :lineHeight 1.3}
   [:> RightSidebarAdd {:boxSize "4rem"}]
   [:> Text {:maxWidth "15em"}
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
                                             :WebkitAppRegion "no-drag"
                                             :flex-direction "column"
                                             :height "calc(100% - 3.25rem)"
                                             :marginTop "3.25rem"
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
                                             :bottom 0
                                             :width "1px"
                                             :zIndex 1
                                             :transitionDuration "0.2s"
                                             :transitionTimingFunction "ease-in-out"
                                             :transitionProperty "common"
                                             :bg "separator.divider"
                                             :sx {:WebkitAppRegion "no-drag"}
                                             :_hover {:bg "link"}
                                             :_active {:bg "link"}
                                             :_after {:content "''"
                                                      :position "absolute"
                                                      :sx {:WebkitAppRegion "no-drag"}
                                                      :inset "-4px"}
                                             :on-mouse-down #(swap! state assoc :dragging true)
                                             :class (when (:dragging @state) "is-dragging")}]
                                    [:> Flex {:flexDirection "column"
                                              ;; :bg "background.upper"
                                              :flex 1;
                                              :maxHeight "calc(100vh - 3.25rem - 1px)"
                                              :width (str (:width @state) "vw")
                                              :overflowY "overlay"}
                                     (if (empty? items)
                                       [empty-message]
                                       [:> Accordion {:allowMultiple true}
                                       (doall
                                        (for [[uid {:keys [node/title block/string is-graph?]}] items]
                                          ^{:key uid}
                                          [:> AccordionItem {:_first {:borderTop 0} :borderBottom 0}
                                           [:> Box {:as "h2" :position "relative"}
                                            [:> AccordionButton {:borderBottom "1px solid"
                                                                 :borderBottomColor "separator.divider"}
                                             [:> AccordionIcon {:as AddIcon}]
                                             [parse-renderer/parse-and-render (or title string) uid]]
                                            [:> IconButton {:size "xs"
                                                            :position "absolute"
                                                            :color "foreground.secondary"
                                                            :right 5
                                                            :top 2
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
