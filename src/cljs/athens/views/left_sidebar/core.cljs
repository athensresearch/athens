(ns athens.views.left-sidebar.core
  (:require
    ["/components/Icons/Icons" :refer [CalendarEditFillIcon AllPagesIcon ContrastIcon SearchIcon GraphIcon SettingsIcon]]
    ["/components/Layout/MainSidebar" :refer [MainSidebar]]
    ["@chakra-ui/react" :refer [Button Flex VStack ButtonGroup Divider Link IconButton]]
    [athens.router   :as router]
    [athens.util     :as util]
    [athens.views.left-sidebar.events]
    [athens.views.left-sidebar.shortcuts :as shortcuts]
    [athens.views.left-sidebar.subs :as left-sidebar-subs]
    [athens.views.left-sidebar.tasks :as left-sidebar-tasks]
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
        on-athena          #(rf/dispatch [:athena/toggle])
        on-theme           #(rf/dispatch [:theme/toggle])
        tasks-enabled?     (rf/subscribe [:feature-flags/enabled? :tasks])
        on-settings        (fn [_]
                             (rf/dispatch [:settings/toggle-open]))
        route-name         @current-route-name
        is-open?          (left-sidebar-subs/get-sidebar-open?)]
    [:> MainSidebar {:isMainSidebarOpen is-open?}

     [:> Flex {:flexDirection "column" :gap 6 :alignItems "stretch" :height "100%"}

      [:> VStack {:spacing 0.5
                  :role "nav"
                  :alignSelf "stretch"
                  :as ButtonGroup
                  :size "sm"
                  :align "stretch"
                  :px 2.5}
       [:> Button {:onClick on-athena
                   :variant "outline"
                   :justifyContent "start"
                   :leftIcon (r/as-element [:> SearchIcon])}
        "Find or Create a Page"]
       [route-button (= route-name :home) "Daily Notes" (r/as-element [:> CalendarEditFillIcon]) (fn [_]
                                                                                                   (rf/dispatch [:reporting/navigation {:source :main-sidebar
                                                                                                                                        :target :home
                                                                                                                                        :pane   :main-pane}])
                                                                                                   (router/nav-daily-notes))]
       [route-button (= route-name :pages) "All Pages" (r/as-element [:> AllPagesIcon]) (fn [_]
                                                                                          (rf/dispatch [:reporting/navigation {:source :main-sidebar
                                                                                                                               :target :all-pages
                                                                                                                               :pane   :main-pane}])
                                                                                          (router/navigate :pages))]

       [route-button (= route-name :graph) "Graph" (r/as-element [:> GraphIcon]) (fn [_]
                                                                                   (rf/dispatch [:reporting/navigation {:source :main-sidebar
                                                                                                                        :target :graph
                                                                                                                        :pane   :main-pane}])
                                                                                   (router/navigate :graph))]]

      (when @tasks-enabled? [:f> left-sidebar-tasks/my-tasks])

      [:f> shortcuts/global-shortcuts]


      ;; LOGO + BOTTOM BUTTONS
      [:> Flex {:as "footer"
                :align "stretch"
                :position "sticky"
                :bottom 0
                :bg "background.vibrancy"
                :backdropFilter "blur(20px)"
                :flexDirection "column"
                :flex "0 0 auto"
                :fontSize "xs"
                :mt "auto"
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
        [:> ButtonGroup {:size "xs"
                         :variant "ghost"
                         :colorScheme "subtle"}
         [:> IconButton {:onClick on-theme
                         :icon (r/as-element [:> ContrastIcon])}]

         [:> IconButton {:onClick on-settings
                         :icon (r/as-element [:> SettingsIcon])}]]]]]]))
