(ns athens.devcards.left-sidebar
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.devcards.athena :refer [athena-prompt]]
    [athens.devcards.buttons :refer [button button-primary]]
    [athens.devcards.db :refer [new-conn posh-conn!]]
    [athens.router :refer [navigate navigate-page]]
    [athens.style :refer [base-styles COLORS]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer [defcard-rg]]
    [posh.reagent :refer [q transact!]]
    [re-frame.core :as re-frame :refer [dispatch]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style use-sub-style]]))


(defcard-rg Import-Styles
  [base-styles])


(defcard-rg Instantiate-Dsdb)
(defonce conn (new-conn))
(posh-conn! conn)


(defn handler
  []
  (let [n (:max-eid @conn)]
    (transact! conn [{:page/sidebar n
                      :node/title   (str "Page " n)
                      :block/uid    (str "uid" n)}])))


(defcard-rg Create-Shortcut
  [button-primary {:on-click-fn handler :label "Create Shortcut"}])



(def left-sidebar-style {:flex "0 0 288px"
                         :width "288px"
                         :min-height "60vh"
                         :display "flex"
                         :flex-direction "column"
                         :padding "32px 32px 16px 32px"
                         :box-shadow (str "1px 0 " (:panel-color COLORS))
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


(def left-sidebar-collapsed-style (merge left-sidebar-style {:flex "0 0 44px"
                                                             :width "44px"
                                                             :min-height "60vh"
                                                             :align-items "flex-start"
                                                             :justify-content "flex-start"
                                                             :grid-template-rows "min-content"
                                                             :padding "32px 4px 16px"
                                                             :display "grid"
                                                             :grid-auto-flow "row"
                                                             :grid-gap "4px"
                                                             :overflow-x "hidden"
                                                             ::stylefy/sub-styles {:footer {:margin-top "40px"
                                                                                            :align-self "flex-end"
                                                                                            :display "grid"
                                                                                            :grid-gap "4px"
                                                                                            :grid-auto-flow "row"}}}))


(def main-navigation {:margin "0 0 32px"
                      :display "grid"
                      :grid-auto-flow "row"
                      :grid-gap "4px"
                      :justify-content "flex-start"
                      ::stylefy/manual [[:svg {:font-size "16px"}]
                                        [:button {:justify-self "flex-start"}]]})


(def shortcuts-list-style {:flex "1 1 100%"
                           :display "flex"
                           :list-style "none"
                           :flex-direction "column"
                           :padding "0"
                           :margin "0 0 32px"
                           :overflow-y "auto"
                           ::stylefy/sub-styles {:heading {:flex "0 0 auto"
                                                           :opacity "0.5"
                                                           :line-height "1"
                                                           :margin "0 0 4px"
                                                           :font-size "inherit"}}})


(def shortcut-style {:color (:link-color COLORS)
                     :cursor "pointer"
                     :display "flex"
                     :flex "0 0 auto"
                     :padding "4px 0"
                     :transition "all 0.05s ease"
                     ::stylefy/mode [[:hover {:opacity "0.8"}]]})


(def notional-logotype {:font-family "IBM Plex Serif"
                        :font-size "18px"
                        :opacity "0.5"
                        :letter-spacing "-0.05em"
                        :font-weight "bold"
                        :text-decoration "none"
                        :justify-self "flex-start"
                        :align-self "center"
                        :color "#000"
                        :transition "all 0.05s ease"
                        ::stylefy/mode [[:hover {:opacity "0.8"}]]})



(def q-shortcuts
  '[:find ?order ?title ?uid
    :where
    [?e :page/sidebar ?order]
    [?e :node/title ?title]
    [?e :block/uid ?uid]])


(defn left-sidebar
  [conn]
  (let [open? (r/atom true)
        shortcuts (q q-shortcuts conn)]
    (fn []
      (let [sorted-shortcuts (->> @shortcuts
                                  (into [])
                                  (sort-by first))]
        (if (not @open?)

          ;; IF COLLAPSED
          [:div (use-style left-sidebar-collapsed-style)
           [button {:on-click-fn #(swap! open? not)
                    :label [:> mui-icons/ChevronRight]}]
           [button-primary {:on-click-fn #(dispatch [:toggle-athena])
                            :label [:> mui-icons/Search ]}]
           [:footer (use-sub-style left-sidebar-collapsed-style :footer)
            [button {:disabled true
                     :label [:> mui-icons/TextFormat]}]
            [button {:disabled true
                     :label [:> mui-icons/Settings]}]]]

          ;; IF EXPANDED
          [:div (use-style left-sidebar-style)
           [:div (use-sub-style left-sidebar-style :top-line)
            [athena-prompt]
            [button {:on-click-fn #(swap! open? not)
                     :label [:> mui-icons/ChevronLeft]}]]
           [:nav (use-style main-navigation)
            [button {:disabled true :label [:<>
                                            [:> mui-icons/Today]
                                            [:span "Daily Notes"]]}]
            [button {:on-click-fn #(navigate :home) :label [:<>
                                                            [:> mui-icons/FileCopy]
                                                            [:span "All Pages"]]}]
            [button {:disabled true :label [:<>
                                            [:> mui-icons/BubbleChart]
                                            [:span "Graph Overview"]]}]]

           ;; SHORTCUTS
           [:ol (use-style shortcuts-list-style)
            [:h2 (use-sub-style shortcuts-list-style :heading) "Shortcuts"]
             (for [[_order title uid] sorted-shortcuts]
               ^{:key uid}
               [:li>a (use-style shortcut-style {:on-click #(navigate-page uid)}) title])]

           ;; LOGO + BOTTOM BUTTONS
            [:footer (use-sub-style left-sidebar-style :footer)
             [:a (use-style notional-logotype {:href "https://github.com/athensresearch/athens" :target "_blank"}) "Athens"]
             [button {:disabled true
                      :label [:> mui-icons/TextFormat]}]
             [button {:disabled true
                      :label [:> mui-icons/Settings]}]]])))))


(defcard-rg Left-Sidebar
   [left-sidebar conn]
  {}
  {:padding false})


