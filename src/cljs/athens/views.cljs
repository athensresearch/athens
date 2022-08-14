(ns athens.views
  (:require
    ["/theme/theme" :refer [theme]]
    ["/components/Layout/useLayoutState" :refer [LayoutProvider]]
    ["/components/Layout/RightSidebar2" :refer [RightSidebar]]
    ["/components/Layout/MainContent" :refer [MainContent]]
    ["@chakra-ui/react" :refer [ChakraProvider Flex Grid VStack HStack Spinner Center]]
    [athens.config]
    [athens.electron.db-modal :as db-modal]
    [athens.style :refer [zoom]]
    [athens.subs]
    [athens.views.app-toolbar :as app-toolbar]
    [athens.views.athena :refer [athena-component]]
    [athens.views.help :refer [help-popup]]
    [athens.views.left-sidebar :as left-sidebar]
    [athens.views.pages.core :as pages]
    [athens.views.pages.settings :as settings]
    [athens.views.right-sidebar :as right-sidebar]
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
        settings-open? (rf/subscribe [:settings/open?])]
    (fn []
      [:div (merge {:style {:display "contents"}}
                   (zoom))
       [:> ChakraProvider {:theme theme,
                           :bg "background.basement"}
        [:> LayoutProvider
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
                  [:> VStack {:spacing 0 :height "100vh" :align "stretch" :position "relative"}
                   [app-toolbar/app-toolbar]
                   [:> HStack {:align "stretch" :spacing 0 :flex 1}
                    [left-sidebar/left-sidebar]
                    [:> MainContent
                     [pages/view]]
                    [right-sidebar/right-sidebar]]]])]]])))
