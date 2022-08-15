(ns athens.views.right-sidebar.core
  (:require
    ["/components/Icons/Icons" :refer [RightSidebarAddIcon]]
    ["/components/Layout/List" :refer [List]]
    ["/components/Layout/RightSidebar" :refer [RightSidebar]]
    ["@chakra-ui/react" :refer [Text Box]]
    [athens.views.right-sidebar.events]
    [athens.views.right-sidebar.shared :as shared]
    [athens.views.right-sidebar.subs]
    [re-frame.core :as rf :refer [dispatch]]))


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
  [open? items rf-width]
  [:> RightSidebar
   {:isOpen open?
    :rightSidebarWidth rf-width}
   (if (empty? items)
     [empty-message]
     [:> List {:items              (shared/create-sidebar-list items)
               :onUpdateItemsOrder (fn [source-uid target-uid old-index new-index]
                                     (rf/dispatch [:right-sidebar/reorder source-uid target-uid old-index new-index]))}])])


(defn right-sidebar
  []
  (let [open? (shared/get-open?)
        items (shared/get-items)
        width @(rf/subscribe [:right-sidebar/width])]
    [right-sidebar-el open? items width]))
