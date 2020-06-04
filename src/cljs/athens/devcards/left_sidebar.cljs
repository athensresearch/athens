(ns athens.devcards.left-sidebar
  (:require
    [athens.devcards.athena :refer [athena-prompt]]
    [athens.devcards.db :refer [new-conn posh-conn!]]
    [athens.lib.dom.attributes :refer [with-styles with-attributes]]
    [athens.router :refer [navigate-page]]
    [athens.style :refer [style-guide-css +link +flex-column +flex-space-between]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer [defcard-rg]]
    [posh.reagent :refer [q transact!]]
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
    {:width              "288px"
     :height             "700px"
     :padding            "32px 32px 16px 32px"
     :border-right-width "1px"
     :border-right-style "solid"
     :border-right-color "#433f3880"
     :overflow-y "auto"}))


(def +left-sidebar-collapsed
  (with-styles +left-sidebar
    {:width "32px"
     :padding "32px 0px"
     :overflow-x "hidden"}))


(def q-shortcuts
  '[:find ?order ?title ?uid
    :where
    [?e :page/sidebar ?order]
    [?e :node/title ?title]
    [?e :block/uid ?uid]])


(defn left-sidebar
  []
  (let [open? (r/atom true)
        shortcuts (q q-shortcuts conn)]
    (fn []
      (let [sorted-shortcuts (->> @shortcuts
                                  (into [])
                                  (sort-by first))]
        (if (not @open?)

          [:div +left-sidebar-collapsed
           [:button {:on-click #(swap! open? not)} ">"]
           [:button.primary "🔍"]
           [:div (with-styles {:margin-top "auto"} +flex-column)
            [:button (with-styles {:margin-bottom "5px"}) "🅰"]
            [:button "❃"]]]

          [:div +left-sidebar
           [:div (with-styles {:margin-bottom "40px" :width "100%"} +flex-space-between)
            [athena-prompt]
            [:button {:on-click #(swap! open? not)} "<"]]
           [:div (with-styles +flex-column-align-start {:margin-bottom "40px"})
            [:button.primary "Daily Notes"]
            [:button "All Pages"]
            [:button "Graph Overview"]]
           [:div (with-styles +flex-column-align-start)
            [:span.small (with-styles {:opacity 0.5}) "Shortcuts"]
            (for [[_order title uid] sorted-shortcuts]
              ^{:key uid}
              [:div (with-styles {:margin "8px 0"})
               [:span (with-attributes +link {:on-click #(navigate-page uid)}) title]])]
           [:div (with-styles +flex-space-between {:flex-direction "row" :margin-top "auto" :width "100%"})
            [:div
             [:a {:href "https://github.com/athensresearch/athens" :target "_blank"}
              [:h3 (with-styles {:font-family "'IBM Plex Serif', Sans-Serif"}) "Athens"]]]
            [:div (with-styles {:display "flex"})
             [:button (with-styles {:margin-right "16px"}) "🅰"]
             [:button "❃"]]]])))))


(defcard-rg Left-Sidebar
  [left-sidebar]
  {}
  {:padding false})


