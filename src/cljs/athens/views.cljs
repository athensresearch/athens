(ns athens.views
  (:require
    ["/components/App/ContextMenuContext" :refer [ContextMenuProvider]]
    ["/components/Layout/MainContent" :refer [MainContent]]
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
    [athens.views.left-sidebar.core :as left-sidebar]
    [athens.views.pages.core :as pages]
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
        right-sidebar-open? (rf/subscribe [:right-sidebar/open])
        right-sidebar-width (rf/subscribe [:right-sidebar/width])
        settings-open? (rf/subscribe [:settings/open?])]
    (fn []
      [:div (merge {:style {:display "contents"}}
                   (zoom))
       [:> ChakraProvider {:theme theme,
                           :bg "background.basement"}
        [:> ContextMenuProvider
         [:> LayoutProvider {:rightSidebarWidth @right-sidebar-width}
          [help-popup]
          [alert]
          [athena-component]
          (cond
            (and @loading @modal) [db-modal/window]

            @loading
            [:> Center {:height "var(--app-height)"}
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
                               :height "var(--app-height)"
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
                     [right-sidebar/right-sidebar]]]])]]]])))
