(ns athens.views
  (:require
    ["/components/Layout/MainContent" :refer [MainContent]]
    ["/components/Layout/RightSidebarResizeControl" :refer [RightSidebarResizeControl]]
    ["/components/Layout/useLayoutState" :refer [LayoutProvider]]
    ["/theme/theme" :refer [theme]]
    ["@chakra-ui/react" :refer [ChakraProvider Flex VStack HStack Spinner Center]]
    [athens.config]
    [athens.electron.db-modal :as db-modal]
    [athens.style :refer [zoom]]
    [athens.subs]
    [athens.views.app-toolbar :as app-toolbar]
    [athens.views.athena :refer [athena-component]]
    [athens.views.help :refer [help-popup]]
    [athens.views.hoc.perf-mon :as perf-mon]
    [athens.views.left-sidebar :as left-sidebar]
    [athens.views.pages.core :as pages]
    [athens.views.pages.quick-capture :as quick-capture]
    [athens.views.pages.settings :as settings]
    [athens.views.right-sidebar.core :as right-sidebar]
    [re-frame.core :as rf]))


;; Components

(defn alert
  []
  (let [alert- (rf/subscribe [:alert])]
    (when-not (nil? @alert-)
      (js/alert (str @alert-))
      (rf/dispatch [:alert/unset]))))


(defn main
  []
  (let [loading        (rf/subscribe [:loading?])
        modal          (rf/subscribe [:modal])
        route-name (rf/subscribe [:current-route/name])
        right-sidebar-open? (rf/subscribe [:right-sidebar/open])
        right-sidebar-width (rf/subscribe [:right-sidebar/width])
        settings-open? (rf/subscribe [:settings/open?])]
    (fn []
      [:div (merge {:style {:display "contents"}}
                   (zoom))
       [:> ChakraProvider {:theme theme,
                           :bg "background.basement"}
        [:> LayoutProvider
         (if
          (= @route-name :quickcapture)
           [perf-mon/hoc-perfmon-no-new-tx {:span-name "quick-capture"}
            [quick-capture/quick-capture]]
           [:<>
            [help-popup]
            [alert]
            [athena-component]
            (cond
              (and @loading @modal) [db-modal/window]

              @loading
              [:> Center {:height "100vh"}
               [:> Flex {:width 28
                         :flexDirection "column"
                         :gap 2
                         :color "foreground.secondary"
                         :borderRadius "lg"
                         :placeItems "center"
                         :placeContent "center"
                         :height 28}
                [:> Spinner {:size "xl"}]]]

              :else [:<>
                     (when @modal
                       [db-modal/window])
                     (when @settings-open?
                       [settings/page])
                     [:> VStack {:overscrollBehavior "contain"
                                 :id "main-layout"
                                 :spacing 0
                                 :overflowY "auto"
                                 :height "100vh"
                                 :bg "background.floor"
                                 :transitionDuration "fast"
                                 :transitionProperty "background"
                                 :transitionTimingFunction "ease-in-out"
                                 :align "stretch"
                                 :position "relative"}
                      [app-toolbar/app-toolbar]
                      [:> HStack {:overscrollBehavior "contain"
                                  :align "stretch"
                                  :spacing 0
                                  :flex 1}
                       [left-sidebar/left-sidebar]
                       [:> MainContent {:rightSidebarWidth @right-sidebar-width
                                        :isRightSidebarOpen @right-sidebar-open?}
                        [pages/view]]
                       [:> RightSidebarResizeControl {:rightSidebarWidth @right-sidebar-width
                                                      :isRightSidebarOpen @right-sidebar-open?
                                                      :onResizeSidebar #(rf/dispatch [:right-sidebar/set-width %])}]
                       [right-sidebar/right-sidebar]]]])])]]])))
