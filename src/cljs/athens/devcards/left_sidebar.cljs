(ns athens.devcards.left-sidebar
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.devcards.athena :refer [athena-prompt-el]]
    [athens.devcards.buttons :refer [button button-primary]]
    [athens.router :refer [navigate navigate-uid]]
    [athens.style :refer [color OPACITIES]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer [defcard-rg]]
    [posh.reagent :refer [q transact!]]
    [re-frame.core :as re-frame :refer [dispatch subscribe]]
    [stylefy.core :as stylefy :refer [use-style use-sub-style]]))


;;; Styles


(def left-sidebar-style
  {:flex "0 0 288px"
   :grid-area "left-sidebar"
   :width "288px"
   :height "100%"
   :display "flex"
   :flex-direction "column"
   :padding "32px 32px 16px 32px"
   :box-shadow (str "1px 0 " (color :panel-color))
   ::stylefy/manual [[]]
   ::stylefy/sub-styles {:top-line {:margin-bottom "40px"
                                    :display "flex"
                                    :flex "0 0 auto"
                                    :justify-content "space-between"}
                         :footer {:margin-top "auto"
                                  :flex "0 0 auto"
                                  :align-self "stretch"
                                  :display "grid"
                                  :grid-auto-flow "column"
                                  :grid-template-columns "1fr auto auto"
                                  :grid-gap "4px"}
                         :small-icon {:font-size "16px"}
                         :large-icon {:font-size "22px"}}})


(def left-sidebar-collapsed-style
  (merge left-sidebar-style {:flex "0 0 44px"
                             :display "grid"
                             :padding "32px 4px 16px"
                             :grid-gap "4px"
                             :width "44px"
                             :box-shadow "1px 0 #EFEDEB"
                             :overflow-x "hidden"
                             :grid-template-rows "auto auto 1fr"
                             :align-self "stretch"
                             ::stylefy/sub-styles {:footer {:padding-top "40px"
                                                            :align-self "flex-end"
                                                            :margin-top "auto"
                                                            :display "grid"
                                                            :grid-gap "4px"
                                                            :grid-auto-flow "row"}}}))


(def main-navigation-style
  {:margin "0 0 32px"
   :display "grid"
   :grid-auto-flow "row"
   :grid-gap "4px"
   :justify-content "flex-start"
   ::stylefy/manual [[:svg {:font-size "16px"}]
                     [:button {:justify-self "flex-start"}]]})


(def shortcuts-list-style
  {:flex "1 1 100%"
   :display "flex"
   :list-style "none"
   :flex-direction "column"
   :padding "0"
   :margin "0 0 32px"
   :overflow-y "auto"
   ::stylefy/sub-styles {:heading {:flex "0 0 auto"
                                   :opacity (:opacity-med OPACITIES)
                                   :line-height "1"
                                   :margin "0 0 4px"
                                   :font-size "inherit"}}})


(def shortcut-style
  {:color (color :link-color)
   :cursor "pointer"
   :display "flex"
   :flex "0 0 auto"
   :padding "4px 0"
   :transition "all 0.05s ease"
   ::stylefy/mode [[:hover {:opacity (:opacity-high OPACITIES)}]]})


(def notional-logotype-style
  {:font-family "IBM Plex Serif"
   :font-size "18px"
   :opacity (:opacity-med OPACITIES)
   :letter-spacing "-0.05em"
   :font-weight "bold"
   :text-decoration "none"
   :justify-self "flex-start"
   :align-self "center"
   :color (color :header-text-color)
   :transition "all 0.05s ease"
   ::stylefy/mode [[:hover {:opacity (:opacity-high OPACITIES)}]]})


;;; Components


(defn left-sidebar
  []
  (let [open? (subscribe [:left-sidebar/open])
        ;; current-route (subscribe [:current-route]) ;; TODO: disabled primary button if current route == navigation button
        shortcuts (->> @(q '[:find ?order ?title ?uid
                             :where
                             [?e :page/sidebar ?order]
                             [?e :node/title ?title]
                             [?e :block/uid ?uid]] db/dsdb)
                       seq
                       (sort-by first))]
    (if (not @open?)

      ;; IF COLLAPSED
      [:div (use-style left-sidebar-collapsed-style)
       [button {:on-click-fn #(dispatch [:toggle-left-sidebar])
                :label       [:> mui-icons/ChevronRight]}]
       [button-primary {:on-click-fn #(dispatch [:toggle-athena])
                        :label       [:> mui-icons/Search]}]
       [:footer (use-sub-style left-sidebar-collapsed-style :footer)
        [button {:disabled true
                 :label    [:> mui-icons/TextFormat]}]
        [button {:disabled true
                 :label    [:> mui-icons/Settings]}]]]

      ;; IF EXPANDED
      [:div (use-style left-sidebar-style)
       [:div (use-sub-style left-sidebar-style :top-line)
        [athena-prompt-el]
        [button {:on-click-fn #(dispatch [:toggle-left-sidebar])
                 :label       [:> mui-icons/ChevronLeft]}]]
       [:nav (use-style main-navigation-style)

        [button {:on-click-fn #(navigate :home)
                 :label       [:<>
                               [:> mui-icons/Today]
                               [:span "Daily Notes"]]}]
        [button {:on-click-fn #(navigate :pages)
                 :label       [:<>
                               [:> mui-icons/FileCopy]
                               [:span "All Pages"]]}]
        [button {:disabled true
                 :label    [:<>
                            [:> mui-icons/BubbleChart]
                            [:span "Graph Overview"]]}]]

       ;; SHORTCUTS
       [:ol (use-style shortcuts-list-style)
        [:h2 (use-sub-style shortcuts-list-style :heading) "Shortcuts"]
        (doall
          (for [[_order title uid] shortcuts]
            ^{:key uid}
            [:li>a (use-style shortcut-style {:on-click #(navigate-uid uid)}) title]))]

       ;; LOGO + BOTTOM BUTTONS
       [:footer (use-sub-style left-sidebar-style :footer)
        [:a (use-style notional-logotype-style {:href "https://github.com/athensresearch/athens" :target "_blank"}) "Athens"]
        [button-primary {:label "Load Test Data"
                         :on-click-fn #(dispatch [:get-local-storage-db])}]]])))
        ;;[button {:disabled true
        ;;         :label    [:> mui-icons/TextFormat]}]
        ;;[button {:disabled true
        ;;         :label    [:> mui-icons/Settings]}]]])))


;;; Devcards


(defcard-rg Create-Shortcut
  [button-primary {:on-click-fn (fn []
                                  (let [n (:max-eid @db/dsdb)]
                                    (transact! db/dsdb [{:page/sidebar n
                                                         :node/title   (str "Page " n)
                                                         :block/uid    (str "uid" n)}]))) :label "Create Shortcut"}])


(defcard-rg Left-Sidebar
  [:div {:style {:display "flex" :height "60vh"}}
   [left-sidebar]]
  {:padding false})
