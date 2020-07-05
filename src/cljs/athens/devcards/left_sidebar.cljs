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
   :padding "1rem 1.5rem"
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



(def main-navigation-style
  {:margin "3.5rem 0 2rem"
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
   :margin "64px 0 32px"
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
  [route-name]
  (let [open? (subscribe [:left-sidebar/open])
        shortcuts (->> @(q '[:find ?order ?title ?uid
                             :where
                             [?e :page/sidebar ?order]
                             [?e :node/title ?title]
                             [?e :block/uid ?uid]] db/dsdb)
                       seq
                       (sort-by first))]
        (when @open?

          [:div (use-style left-sidebar-style)
          ;;  [:nav (use-style main-navigation-style)

          ;;   [button {:on-click-fn #(navigate :home)
          ;;            :active (when (= route-name :home) true)
          ;;            :label       [:<>
          ;;                          [:> mui-icons/Today]
          ;;                          [:span "Daily Notes"]]}]
          ;;   [button {:on-click-fn #(navigate :pages)
          ;;            :active (when (= route-name :pages) true)
          ;;            :label [:<>
          ;;                    [:> mui-icons/FileCopy]
          ;;                    [:span "Pages"]]}]
          ;;   [button {:disabled true :label [:<>
          ;;                                   [:> mui-icons/BubbleChart]
          ;;                                   [:span "Graph Overview"]]}]]

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
                             :on-click-fn #(dispatch [:get-db/init])}]
            ]])))


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
