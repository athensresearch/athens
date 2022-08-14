(ns athens.views.right-sidebar
  (:require
   ["/components/Icons/Icons" :refer [RightSidebarAddIcon]]
   ["/components/Layout/RightSidebar" :refer [RightSidebar SidebarItem]]
   ["@chakra-ui/react" :refer [Text Box]]
   [athens.parse-renderer :as parse-renderer]
   [athens.views.pages.block-page :as block-page]
   [athens.views.pages.graph :as graph]
   [athens.views.pages.node-page :as node-page]
   [re-frame.core :refer [dispatch subscribe]]))


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
  (let []
    (fn [open? items _]
      [:> RightSidebar
       {:onResizeSidebar #(dispatch [:right-sidebar/set-width %])}
      ;;  [:> Flex
        ;; {:class "right-sidebar-content"
        ;;  :flexDirection "column"
        ;;  :flex 1;
        ;;  :maxHeight "calc(100vh - 3.25rem - 1px)"
        ;;  :width (str (:width @state) "vw")
        ;;  :overflowY "auto"
        ;;  :sx {"@supports (overflow-y: overlay)" {:overflowY "overlay"}}}
       (if (empty? items)
         [empty-message]
         (doall
          (for [[uid {:keys [open node/title block/string is-graph?]}] items]
            [:> SidebarItem {:isOpen open
                             :key uid
                             :type (cond is-graph? "graph" title "node" :else "block")
                             :onRemove #(dispatch [:right-sidebar/close-item uid])
                             :onToggle #(dispatch [:right-sidebar/toggle-item uid])
                                                          ;; nth 1 to get just the title
                             :title (nth [parse-renderer/parse-and-render (or title string) uid] 1)}
             (cond
               is-graph? [graph/page uid]
               title     [node-page/page [:block/uid uid]]
               :else     [block-page/page [:block/uid uid]])])))])))


(defn right-sidebar
  []
  (let [open? @(subscribe [:right-sidebar/open])
        items @(subscribe [:right-sidebar/items])
        width @(subscribe [:right-sidebar/width])]
    [right-sidebar-el open? items width]))
