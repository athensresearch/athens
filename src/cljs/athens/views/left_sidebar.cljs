(ns athens.views.left-sidebar
  (:require
    [athens.db :as db]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [cssv OPACITIES]]
    [athens.views.buttons :refer [button-primary]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [posh.reagent :refer [q]]
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
   :padding "120px 32px 16px 32px"
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
  {:color (cssv "link-color")
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
   :color (cssv "header-text-color")
   :transition "all 0.05s ease"
   ::stylefy/mode [[:hover {:opacity (:opacity-high OPACITIES)}]]})


;;; Components


(defn left-sidebar
  []
  (let [open? (subscribe [:left-sidebar/open])
        shortcuts (->> @(q '[:find ?order ?title ?uid
                             :where
                             [?e :page/sidebar ?order]
                             [?e :node/title ?title]
                             [?e :block/uid ?uid]] db/dsdb)
                       seq
                       (sort-by first))]
    (when @open?

      ;; IF EXPANDED
      [:div (use-style left-sidebar-style)

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
                         :on-click-fn #(dispatch [:get-db/init])}]]])))
