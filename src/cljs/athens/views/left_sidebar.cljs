(ns athens.views.left-sidebar
  (:require
    ["/components/Icons/Icons" :refer [DailyNotesIcon AllPagesIcon ContrastIcon SearchIcon #_ GraphIcon SettingsIcon]]
    ["/components/Layout/MainSidebar" :refer [MainSidebar SidebarSection SidebarSectionHeading]]
    ["/components/SidebarShortcuts/List" :refer [List]]
    ["/components/Widgets/Widgets" :refer [TasksWidget]]
    ["@chakra-ui/react" :refer [Button IconButton Divider VStack Flex ButtonGroup Link Flex]]
    [athens.reactive :as reactive]
    [athens.router   :as router]
    [athens.util     :as util]
    [re-frame.core   :as rf]
    [reagent.core    :as r]))


;; Components

(defn route-button
  []
  (fn [is-active? label icon on-click]
    [:> Button {:isActive is-active?
                :textAlign "start"
                :justifyContent "flex-start"
                :variant "ghost"
                :leftIcon icon
                :onClick on-click}
     label]))


(defn left-sidebar
  []
  (let [current-route-name (rf/subscribe [:current-route/name])
        on-athena              #(rf/dispatch [:athena/toggle])
        on-theme               #(rf/dispatch [:theme/toggle])
        on-settings            (fn [_]
                                 (rf/dispatch [:settings/toggle-open]))
        route-name @current-route-name
        is-open? (rf/subscribe [:left-sidebar/open])
        shortcuts (reactive/get-reactive-shortcuts)]
    [:> MainSidebar {:isMainSidebarOpen @is-open?}

     [:> VStack {:spacing 6 :align "stretch" :height "100%"}

      [:> VStack {:spacing 0.5
                  :role "nav"
                  :alignSelf "stretch"
                  :as ButtonGroup
                  :size "sm"
                  :align "stretch"
                  :px 4}
       [:> Button {:onClick on-athena
                   :variant "outline"
                   :justifyContent "start"
                   :leftIcon (r/as-element [:> SearchIcon])}
        "Find or Create a Page"]
       [route-button (= route-name :home) "Daily Notes" (r/as-element [:> DailyNotesIcon]) (fn [_]
                                                                                             (rf/dispatch [:reporting/navigation {:source :main-sidebar
                                                                                                                                  :target :home
                                                                                                                                  :pane   :main-pane}])
                                                                                             (router/nav-daily-notes))]
       ;; [:> DailyNotesWidget {:isOpen true}]

       [route-button (= route-name :pages) "All Pages" (r/as-element [:> AllPagesIcon]) (fn [_]
                                                                                          (rf/dispatch [:reporting/navigation {:source :main-sidebar
                                                                                                                               :target :all-pages
                                                                                                                               :pane   :main-pane}])
                                                                                          (router/navigate :pages))]

       #_[route-button (= route-name :graph) "Graph" (r/as-element [:> GraphIcon]) (fn [_]
                                                                                     (rf/dispatch [:reporting/navigation {:source :main-sidebar
                                                                                                                          :target :graph
                                                                                                                          :pane   :main-pane}])
                                                                                     (router/navigate :graph))]
       #_[:> DailyNotesWidget {:isOpen true}]]

      [:> TasksWidget {:isOpen true}]

      ;; SHORTCUTS
      [:> SidebarSection
       {:title (r/as-element [:> SidebarSectionHeading {:pl 6} "Shortcuts"])
        :pr 4
        :flex 1
        :isOpen true}
       [:> List {:items shortcuts
                 :onOpenItem (fn [e [_order page]]
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
                                         (> oldIndex newIndex) (rf/dispatch [:left-sidebar/drop oldIndex newIndex :before])))}]]

      ;; LOGO + BOTTOM BUTTONS
      [:> Flex {:as "footer"
                :align "stretch"
                :flexDirection "column"
                :flex "0 0 auto"
                :fontSize "xs"
                :color "foreground.secondary"
                :px 6}
       [:> Divider]
       [:> Flex {:alignItems "center" :py 4}
        [:> Flex {:flex 1 :gap 2}
         [:> Link {:fontWeight "bold"
                   :display "inline-block"
                   :href "https://github.com/athensresearch/athens/issues/new/choose"
                   :target "_blank"}
          "Athens"]
         [:> Link {:color "foreground.secondary"
                   :display "inline-block"
                   :href "https://github.com/athensresearch/athens/blob/master/CHANGELOG.md"
                   :target "_blank"}
          (util/athens-version)]]
        [:> ButtonGroup {:size "sm" :spacing 0 :variant "ghost" :colorScheme "subtle"}

         [:> IconButton {:onClick on-theme
                         :icon (r/as-element [:> ContrastIcon])}]

         [:> IconButton {:onClick on-settings
                         :icon (r/as-element [:> SettingsIcon])}]]]]]]))
