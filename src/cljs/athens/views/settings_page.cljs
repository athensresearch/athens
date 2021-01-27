(ns athens.views.settings-page
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.subs]
    [athens.views.buttons :refer [button]]
    [stylefy.core :as stylefy :refer [use-style]]
    [reagent.core :as r]))


(defn settings-panel
  []
  (let [opted-out (r/atom (.. js/window -posthog has_opted_out_capturing))]
    (fn []
      [:div {:style {:display "flex"
                     :margin "0vh 5vw"
                     :flex-direction "column"}}
       [:h1 "Settings"]
       [:div
        (if @opted-out
          [:h5 "Opted Out of Analytics"]
          [:h5 "Opted Into Analytics"])
        [:div {:style {:margin "10px 0"}}
         [button {:primary  (false? @opted-out)
                  :on-click (fn []
                              (if @opted-out
                                (.. js/window -posthog opt_in_capturing)
                                (.. js/window -posthog opt_out_capturing))
                              (swap! opted-out not))}
          (if @opted-out
            [:div {:style {:display "flex"}}
             [:> mui-icons/ToggleOn]
             [:span "\uD83D\uDE41 We understand."]]
            [:div {:style {:display "flex"}}
             [:> mui-icons/ToggleOff]
             [:span "\uD83D\uDE00 Thanks for helping make Athens better!"]])]]
        [:span "Analytics are anonymized and delivered by "
         [:a {:href "https://posthog.com" :target "_blank"} "Posthog"]
         ", an open-source provider of product analytics. This lets the designers and engineers at Athens know if we're really making something people love!"]]

       [:div
        [:span "Beta"]]])))
