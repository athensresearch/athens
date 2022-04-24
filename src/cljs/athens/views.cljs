(ns athens.views
  (:require
    ["/components/InteractionManager/InteractionManager" :refer [InteractionManager]]
    ["/theme/theme" :refer [theme]]
    ["@chakra-ui/react" :refer [ChakraProvider Flex Grid Spinner Center Text]]
    [athens.common-db :as common-db]
    [athens.config]
    [athens.db :as db]
    [athens.electron.db-modal :as db-modal]
    [athens.reactive               :as reactive]
    [athens.router :refer [navigate-page navigate-uid]]
    [athens.style :refer [zoom]]
    [athens.subs]
    [athens.views.app-toolbar :as app-toolbar]
    [athens.views.athena :refer [athena-component]]
    [athens.views.devtool :refer [devtool-component]]
    [athens.views.help :refer [help-popup]]
    [athens.views.left-sidebar :as left-sidebar]
    [athens.views.pages.block-preview :as block-preview]
    [athens.views.pages.core :as pages]
    [athens.views.pages.node-preview :as node-preview]
    [athens.views.right-sidebar :as right-sidebar]
    [re-frame.core :as rf]
    [reagent.core :as r]))


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
        preview (r/atom {:value nil :type nil})
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
                  [:> InteractionManager
                   {:shouldShowPreviews true
                    :shouldShowActions false
                    :shouldSetBlockIsHovered true
                    :actions (clj->js [{:children "log uid"
                                        :onClick (fn [e block] (js/console.log block))}])
                    :setPreview (fn [value type] (reset! preview {:value value :type type}))
                    :previewEl (r/as-element (let [val (:value @preview)
                                                   type (:type @preview)]
                                               (cond
                                                 (= type "page") (let [page-eid (common-db/e-by-av @db/dsdb :node/title val)]
                                                                   [node-preview/page page-eid])
                                                 (= type "block") (let [block-eid (reactive/get-reactive-block-or-page-by-uid val)]
                                                                    [block-preview/page block-eid])
                                                 (= type "url") [:> Text val])))
                    :onNavigateUid (fn [e uid] (navigate-uid uid e))
                    :onNavigatePage (fn [e title] (navigate-page title e))}
                   [left-sidebar/left-sidebar]
                   [pages/view]
                   [right-sidebar/right-sidebar]]

                  [devtool-component]]])]])))
