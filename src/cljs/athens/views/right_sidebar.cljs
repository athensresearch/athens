(ns athens.views.right-sidebar
  (:require
    ["/components/Icons/Icons" :refer [RightSidebarAddIcon XmarkIcon]]
    ["/components/Layout/Layout" :refer [RightSidebarContainer]]
    ["@chakra-ui/react" :refer [Flex Text Box IconButton Accordion AccordionItem AccordionButton AccordionIcon AccordionPanel]]
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
   [:> RightSidebarAddIcon {:boxSize "4rem"}]
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
                                 [:> RightSidebarContainer {:isOpen open?
                                                            :isDragging (:dragging @state)
                                                            :width (:width @state)}
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
                                          [:> AccordionItem {:borderWidth 0
                                                             :borderColor "separator.divider"
                                                             :borderBottomWidth "1px"}
                                           [:> Box {:as "h2" :position "relative"}
                                            [:> AccordionButton {:borderRadius "0"
                                                                 :py 4
                                                                 :pl 3
                                                                 :pr 4
                                                                 :height "auto"
                                                                 :textAlign "left"
                                                                 :overflow "hidden"
                                                                 :whiteSpace "nowrap"
                                                                 :border 0}
                                             [:> AccordionIcon]
                                             [:> Box {:flex "1 1 100%"
                                                      :mx 3
                                                      :tabIndex -1
                                                      :pointerEvents "none"
                                                      :position "relative"
                                                      :bottom "1px"
                                                      :overflow "hidden"
                                                      :sx {:maskImage "linear-gradient(to right, black, black calc(100% - 4rem), transparent calc(100% - 2rem))"}} [parse-renderer/parse-and-render (or title string) uid]]]
                                            [:> IconButton {:size "sm"
                                                            :position "absolute"
                                                            :color "foreground.secondary"
                                                            :right 5
                                                            :top 3
                                                            :background "transparent"
                                                            :onClick #(dispatch [:right-sidebar/XmarkIcon-item uid])}
                                             [:> XmarkIcon]]]
                                           [:> AccordionPanel {:p 4}
                                            (cond
                                              is-graph? [graph/page uid]
                                              title     [node-page/page [:block/uid uid]]
                                              :else     [block-page/page [:block/uid uid]])]]))])]])})))


(defn right-sidebar
  []
  (let [open? @(subscribe [:right-sidebar/open])
        items @(subscribe [:right-sidebar/items])
        width @(subscribe [:right-sidebar/width])]
    [right-sidebar-el open? items width]))
