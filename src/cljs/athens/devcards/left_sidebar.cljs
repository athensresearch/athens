(ns athens.devcards.left-sidebar
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.devcards.athena :refer [athena-prompt]]
    [athens.devcards.buttons :refer [button button-primary]]
    [athens.devcards.db :refer [new-conn posh-conn!]]
    [athens.lib.dom.attributes :refer [with-styles with-attributes]]
    [athens.router :refer [navigate navigate-page]]
    [athens.style :refer [base-styles +link +flex-column +flex-space-between +width-100 COLORS]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer [defcard-rg]]
    [posh.reagent :refer [q transact!]]
    [re-frame.core :as re-frame :refer [dispatch]]
    [reagent.core :as r]))


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


(def +flex-column-align-start
  (with-styles +flex-column {:align-items "flex-start"}))


(def +left-sidebar
  (with-styles +flex-column-align-start
    {:flex               "0 0 288px"
     :padding            "32px 32px 16px 32px"
      :box-shadow         (str "1px 0 " (:panel-color COLORS))}))


(def +left-sidebar-collapsed
  (with-styles +left-sidebar
    {:flex "0 0 40px"
     :align-items "flex-start"
     :justify-content "flex-start"
     :padding "32px 4px"
     :display "grid"
     :grid-gap "4px"
     :overflow-x "hidden"}))


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
          [:div +left-sidebar-collapsed
           [button {:on-click-fn #(swap! open? not)
                    :label [:> mui-icons/ChevronRight (with-styles {:font-size "18px"})]}]
           [button-primary {:on-click-fn #(dispatch [:toggle-athena])
                            :label [:> mui-icons/Search (with-styles {:font-size "18px"})]}]
           [:div (with-styles {:margin-top "auto"} +flex-column)
            [button {:disabled true
                     :label [:> mui-icons/TextFormat (with-styles {:font-size "18px"})]
                     :style {:margin-bottom "8px"}}]
            [button {:disabled true
                     :label [:> mui-icons/Settings (with-styles {:font-size "18px"})]}]]]

          ;; IF EXPANDED
          [:div +left-sidebar
           [:div (with-styles {:margin-bottom "40px" :width "100%"} +flex-space-between)
            [athena-prompt]
            [button {:on-click-fn #(swap! open? not)
                     :label [:> mui-icons/ChevronLeft]}]]
           [:div (with-styles +flex-column-align-start {:margin-bottom "40px"})
            [button {:disabled true :label [:<>
                                            [:> mui-icons/Today (with-styles {:font-size "16px"})]
                                            [:span "Daily Notes"]]}]
            [button {:on-click-fn #(navigate :home) :label [:<>
                                                            [:> mui-icons/FileCopy (with-styles {:font-size "16px"})]
                                                            [:span "All Pages"]]}]
            [button {:disabled true :label [:<>
                                            [:> mui-icons/BubbleChart (with-styles {:font-size "16px"})]
                                            [:span "Graph Overview"]]}]]

           ;; SHORTCUTS
           [:div (with-styles +flex-column-align-start +width-100 {:height "60vh"})
            [:span.small (with-styles {:opacity 0.5}) "Shortcuts"]
            [:div (with-styles +width-100 {:overflow-y "auto"})
             (for [[_order title uid] sorted-shortcuts]
               ^{:key uid}
               [:div (with-styles {:margin "12px 0"})
                [:span (with-attributes +link {:on-click #(navigate-page uid)}) title]])]]

           ;; LOGO + BOTTOM BUTTONS
           [:div (with-styles +flex-space-between {:flex-direction "row" :margin-top "auto" :width "100%"})
            [:div
             [:a {:href "https://github.com/athensresearch/athens" :target "_blank"}
              [:h3 (with-styles {:font-family "'IBM Plex Serif', Sans-Serif"}) "Athens"]]]
            [:div (with-styles {:display "flex"})
             [button {:disabled true
                      :label [:> mui-icons/TextFormat (with-styles {:font-size "16px"})]
                      :style {:margin-right "8px"}}]
             [button {:disabled true
                      :label [:> mui-icons/Settings (with-styles {:font-size "16px"})]}]]]])))))


(defcard-rg Comments
  "`position: fixed` for left-sidebar doesn't work with DevCards.

  But `position: sticky` doesn't work well when in app.

  Has to do with absolute vs relative positioning I believe.")


(defcard-rg Left-Sidebar
  [:div
   [left-sidebar conn]]
  {}
  {:padding false})


