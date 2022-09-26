(ns athens.views.right-sidebar.core
  (:require
    ["/components/Empty/Empty" :refer [Empty EmptyIcon EmptyMessage]]
    ["/components/Icons/Icons" :refer [RightSidebarAddIcon]]
    ["/components/Layout/List" :refer [List]]
    ["/components/Layout/RightSidebar" :refer [RightSidebar]]
    [athens.views.right-sidebar.events]
    [athens.views.right-sidebar.shared :as shared]
    [athens.views.right-sidebar.subs]
    [re-frame.core :as rf]))


;; Components

(defn right-sidebar-el
  "Resizable: use local atom for width, but dispatch value to re-frame on mouse up. Instantiate local value with re-frame width too."
  [open? items on-resize rf-width]
  [:> RightSidebar
   {:isOpen open?
    :onResize on-resize
    :rightSidebarWidth rf-width}
   (if (empty? items)
     [:> Empty {:size "lg" :mt 4}
      [:> EmptyIcon {:Icon RightSidebarAddIcon}]
      [:> EmptyMessage "Hold " [:kbd "shift"] " when clicking a page link to view the page in the sidebar."]]
     [:> List {:items              (shared/create-sidebar-list items)
               :onUpdateItemsOrder (fn [source-uid target-uid old-index new-index]
                                     (rf/dispatch [:right-sidebar/reorder source-uid target-uid old-index new-index]))}])])


(defn right-sidebar
  []
  (let [open? (shared/get-open?)
        items (shared/get-items)
        on-resize #(rf/dispatch [:right-sidebar/set-width %])
        width @(rf/subscribe [:right-sidebar/width])]
    [right-sidebar-el open? items on-resize width]))
