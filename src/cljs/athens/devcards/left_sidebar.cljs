(ns athens.devcards.left-sidebar
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.devcards.athena :refer [athena-prompt]]
    [athens.devcards.db :refer [new-conn posh-conn!]]
    [athens.lib.dom.attributes :refer [with-styles with-attributes]]
    [athens.router :refer [navigate navigate-page]]
    [athens.style :refer [style-guide-css +link +flex-column +flex-space-between +width-100]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer [defcard-rg]]
    [posh.reagent :refer [q transact!]]
    [re-frame.core :as re-frame :refer [dispatch]]
    [reagent.core :as r]))


(defcard-rg Import-Styles
  [style-guide-css])


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
  [:button.primary {:on-click handler} "Create Shortcut"])


(def +flex-column-align-start
  (with-styles +flex-column {:align-items "flex-start"}))


(def +left-sidebar
  (with-styles +flex-column-align-start
    {:flex               "0 0 288px"
     :padding            "32px 32px 16px 32px"
     :box-shadow         "1px 0 #433f3880"}))


(def +left-sidebar-collapsed
  (with-styles +left-sidebar
    {:flex "0 0 32px"
     :padding "32px 0px"
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
           [:button {:on-click #(swap! open? not)} [:> mui-icons/ChevronRight]]
           [:button.primary {:on-click #(dispatch [:toggle-athena])} [:> mui-icons/Search]]
           [:div (with-styles {:margin-top "auto"} +flex-column)
            [:button (with-attributes (with-styles {:margin-bottom "5px"})
                       {:disabled true}) [:> mui-icons/TextFormat]]
            [:button {:disabled true} [:> mui-icons/Settings]]]]

          ;; IF EXPANDED
          [:div +left-sidebar
           [:div (with-styles {:margin-bottom "40px" :width "100%"} +flex-space-between)
            [athena-prompt]
            [:button {:on-click #(swap! open? not)} [:> mui-icons/ChevronLeft]]]
           [:div (with-styles +flex-column-align-start {:margin-bottom "40px"})
            [:button {:disabled true} [:> mui-icons/Today] [:span "Daily Notes"]]
            [:button {:on-click #(navigate :home)} [:> mui-icons/FileCopy] [:span "All Pages"]]
            [:button {:disabled true} [:> mui-icons/BubbleChart] [:span "Graph Overview"]]]

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
             [:button (with-attributes (with-styles {:margin-right "16px"})
                        {:disabled true}) [:> mui-icons/TextFormat]]
             [:button {:disabled true} [:> mui-icons/Settings]]]]])))))


(defcard-rg Comments
  "`position: fixed` for left-sidebar doesn't work with DevCards.

  But `position: sticky` doesn't work well when in app.

  Has to do with absolute vs relative positioning I believe.")


(defcard-rg Left-Sidebar
  [:div
   [left-sidebar conn]]
  {}
  {:padding false})


