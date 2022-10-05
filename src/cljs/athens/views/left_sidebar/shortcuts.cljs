(ns athens.views.left-sidebar.shortcuts
  (:require
    ["/components/Empty/Empty" :refer [Empty EmptyTitle EmptyIcon EmptyMessage]]
    ["/components/Icons/Icons" :refer [BookmarkIcon]]
    ["/components/SidebarShortcuts/List" :refer [List]]
    ["/components/Widget/Widget" :refer [Widget WidgetHeader WidgetBody WidgetToggle]]
    [athens.reactive :as reactive]
    [athens.router :as router]
    [athens.views.left-sidebar.subs :as left-sidebar-subs]
    [re-frame.core :as rf]))


(defn global-shortcuts
  []
  (let [shortcuts (reactive/get-reactive-shortcuts)
        current-route (rf/subscribe [:current-route])
        current-route-page-name (get-in @current-route [:path-params :title])
        is-open? (left-sidebar-subs/get-widget-open? "shortcuts")]
    [:> Widget
     {:pr            4
      :defaultIsOpen is-open?}
     [:> WidgetHeader {:title "Shortcuts"
                       :pl    6}
      [:> WidgetToggle {:onClick #(rf/dispatch [:left-sidebar.widgets/toggle-widget "shortcuts"])}]]

     [:> WidgetBody

      (if (seq shortcuts)


        [:> List {:items              shortcuts
                  :currentPageName    current-route-page-name
                  :onOpenItem         (fn [e [_order page]]
                                        (let [shift? (.-shiftKey e)]
                                          (rf/dispatch [:reporting/navigation {:source :left-sidebar
                                                                               :target :page
                                                                               :pane   (if shift?
                                                                                         :right-pane
                                                                                         :main-pane)}])
                                          (router/navigate-page page e)))
                  :onUpdateItemsOrder (fn [oldIndex newIndex]
                                        (cond
                                          (< oldIndex newIndex) (rf/dispatch [:left-sidebar/drop oldIndex newIndex :after])
                                          (> oldIndex newIndex) (rf/dispatch [:left-sidebar/drop oldIndex newIndex :before])))}]

        [:> Empty {:size "sm" :pr 2 :pl 4}
         [:> EmptyIcon {:Icon BookmarkIcon}]
         [:> EmptyTitle "No shortcuts"]
         [:> EmptyMessage "Add shortcuts to mark important pages in your workspace."]])]]))
