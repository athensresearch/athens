(ns athens.views
  (:require
    ["/theme/theme" :refer [theme]]
    ["@chakra-ui/react" :refer [ChakraProvider Flex Grid Spinner Center]]
    [athens.config]
    [athens.electron.db-modal :as db-modal]
    [athens.style :refer [zoom]]
    [athens.subs]
    [athens.views.app-toolbar :as app-toolbar]
    [athens.views.athena :refer [athena-component]]
    [athens.views.devtool :refer [devtool-component]]
    [athens.views.help :refer [help-popup]]
    [athens.views.left-sidebar :as left-sidebar]
    [athens.views.pages.core :as pages]
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
  (let [loading    (rf/subscribe [:loading?])
        modal      (rf/subscribe [:modal])]
    (fn []
      [:div (merge {:style {:display "contents"}}
                   (zoom))
       [:> ChakraProvider {:theme theme,
                           :bg "background.basement"}
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
                 (when @modal [db-modal/window])
                 [:> Grid
                  {:gridTemplateColumns "auto 1fr auto"
                   :gridTemplateRows "auto 1fr auto"
                   :grid-template-areas
                   "'app-header app-header app-header'
                      'left-sidebar main-content secondary-content'
                    'devtool devtool devtool'"
                   :height "100vh"
                   :overflow "hidden"
                   :sx {"WebkitAppRegion" "drag"
                        "--app-toolbar-height" "3.25rem"
                        ".os-mac &" {"--app-header-height" "52px"}
                        ".os-windows &" {"--toolbar-height" "44px"}
                        ".os-linux &" {"--toolbar-height" "44px"}}}
                  [app-toolbar/app-toolbar]
                  [left-sidebar/left-sidebar]
                  [pages/view]
                  [right-sidebar/right-sidebar]
                  [devtool-component]]])]])))
