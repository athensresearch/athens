(ns athens.views.right-sidebar.core
  (:require
    ["/components/Layout/List" :refer [List]]
    ["/components/Icons/Icons" :refer [RightSidebarAddIcon]]
    ["/components/Layout/RightSidebar" :refer [RightSidebarContainer SidebarItem]]
    ["@chakra-ui/react" :refer [Flex Text Box]]
    [athens.parse-renderer :as parse-renderer]
    [athens.views.pages.block-page :as block-page]
    [athens.views.pages.graph :as graph]
    [athens.views.pages.node-page :as node-page]
    [athens.views.right-sidebar.events]
    [athens.views.right-sidebar.subs]
    [athens.reactive :as reactive]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [athens.common-db :as common-db]
    [athens.views.right-sidebar.shared :as shared :refer [NS ns-str]]))


(defn- create-sidebar-list
  "Accepts right-sidebar as a map of uids and entities.
  Entity contains either the block uid or node title, and additionally open/close state and whether the page is a graph view or not."
  [items]
  (doall
    (mapv (fn [entity]
            (let [{:keys [open? name uid type]} entity
                  eid             (shared/get-eid entity)
                  string-or-title (reactive/get-reactive-title-or-string eid)
                  {:keys [node/title block/string]} string-or-title]
              {:isOpen   open?
               :key      uid
               :type     (cond
                           (= type "graph") "graph"
                           (= type "page") "node"
                           :else "block")
               :onRemove #(dispatch [:right-sidebar/close-item uid])
               :onToggle #(dispatch [:right-sidebar/toggle-item uid])
               ;; nth 1 to get just the title
               :title    (nth [parse-renderer/parse-and-render (or title string) name] 1)
               :children (r/as-element (cond
                                         (= type "graph") [graph/page name]
                                         (= type "page") [node-page/page eid]
                                         :else [block-page/page eid]))}))
          items)))


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
   [:> RightSidebarAddIcon {:boxSize "4rem" :color "foreground.tertiary"}]
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
                                 [:> RightSidebarContainer
                                  {:isOpen     open?
                                   :isDragging (:dragging @state)
                                   :width      (:width @state)}
                                  [:> Box
                                   {:role                     "separator"
                                    :aria-orientation         "vertical"
                                    :cursor                   "col-resize"
                                    :position                 "absolute"
                                    :top                      0
                                    :bottom                   0
                                    :width                    "1px"
                                    :zIndex                   1
                                    :transitionDuration       "0.2s"
                                    :transitionTimingFunction "ease-in-out"
                                    :transitionProperty       "common"
                                    :bg                       "separator.divider"
                                    :sx                       {:WebkitAppRegion "no-drag"}
                                    :_hover                   {:bg "link"}
                                    :_active                  {:bg "link"}
                                    :_after                   {:content  "''"
                                                               :position "absolute"
                                                               :sx       {:WebkitAppRegion "no-drag"}
                                                               :inset    "-4px"}
                                    :on-mouse-down            #(swap! state assoc :dragging true)
                                    :class                    (when (:dragging @state) "is-dragging")}]
                                  [:> Flex
                                   {:class         "right-sidebar-content"
                                    :flexDirection "column"
                                    :flex          1 ;
                                    :maxHeight     "calc(100vh - 3.25rem - 1px)"
                                    :width         (str (:width @state) "vw")
                                    :overflowY     "auto"
                                    :sx            {"@supports (overflow-y: overlay)" {:overflowY "overlay"}}}
                                   (if (empty? items)
                                     [empty-message]
                                     [:> List {:items              (create-sidebar-list items)
                                               :onUpdateItemsOrder (fn [source-uid target-uid old-index new-index]
                                                                     (rf/dispatch [:right-sidebar/reorder source-uid target-uid old-index new-index]))}])]])})))




(defn right-sidebar
  []
  (let [open? (shared/get-open?)
        items (shared/get-items)
        width @(subscribe [:right-sidebar/width])]
    [right-sidebar-el open? items width]))
