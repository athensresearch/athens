(ns athens.views.settings-page
  (:require
   ["@material-ui/icons" :as mui-icons]
   [athens.views.buttons :refer [button]]
   [reagent.core :as r]))


(defn opt-out
  [opted-out?]
  (.. js/posthog (capture "opt-out"))
  (.. js/window -posthog opt_out_capturing)
  (js/localStorage.setItem "sentry" "off")
  (reset! opted-out? true))


(defn opt-in
  [opted-out?]
  (.. js/window -posthog opt_in_capturing)
  (.. js/posthog (capture "opt-in"))
  (js/localStorage.setItem "sentry" "on")
  (reset! opted-out? false))


(defn remember-ws
  [remember-window-size?]
  (js/localStorage.setItem "rememberws" "on")
  (reset! remember-window-size? true))


(defn dont-remember-ws
  [remember-window-size?]
  (js/localStorage.setItem "rememberws" "off")
  (reset! remember-window-size? false))


(defn handle-ws
  [remember-window-size?]
  (if @remember-window-size?
    (dont-remember-ws remember-window-size?)
    (remember-ws remember-window-size?)))


(defn handle-click
  [opted-out?]
  (if @opted-out?
    (opt-in opted-out?)
    (opt-out opted-out?)))


(defn settings-page
  []
  (let [opted-out? (r/atom (.. js/window -posthog has_opted_out_capturing))
        remember-window-size? (r/atom false)]
    (fn []
      [:div {:style {:display "flex"
                     :margin "0vh 5vw"
                     :flex-direction "column"}}
       [:h2 "Settings"]
       (if @opted-out?
         [:h5 "Opted Out of Analytics"]
         [:h5 "Opted Into Analytics"])
       [:div {:style {:margin "10px 0"}}
        [button {:primary  (false? @opted-out?)
                 :on-click #(handle-click opted-out?)}
         (if @opted-out?
           [:div {:style {:display "flex"}}
            [:> mui-icons/ToggleOn]
            [:span "\uD83D\uDE41 We understand."]]
           [:div {:style {:display "flex"}}
            [:> mui-icons/ToggleOff]
            [:span "\uD83D\uDE00 Thanks for helping make Athens better!"]])]]
       [:span "Analytics are anonymized and delivered by "
        [:a {:href "https://posthog.com" :target "_blank"} "Posthog"]
        " and " [:a {:href "https://sentry.io" :target "_blank"} "Sentry"]
        ", open-source solutions to measure retention, performance, and crashes.
         This lets the designers and engineers at Athens know if we're really making something people love!"]
       [:br]
       [:h5 "Remember the last Window Size on Athens Startup"]
       [:div {:style {:margin "10px 0"}}
        [button {:primary (true? @remember-window-size?)
                 :on-click #(handle-ws remember-window-size?)}
         (if @remember-window-size?
           [:div {:style {:display "flex"}}
            [:> mui-icons/ToggleOff]
            [:span "\uD83D\uDE00 Yes!"]]
           [:div {:style {:display "flex"}}
            [:> mui-icons/ToggleOn]
            [:span "\uD83D\uDE41 No."]])]]])))


