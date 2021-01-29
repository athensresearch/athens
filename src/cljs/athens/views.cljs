(ns athens.views
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.subs]
    [athens.views.all-pages :refer [table]]
    [athens.views.app-toolbar :refer [app-toolbar]]
    [athens.views.athena :refer [athena-component]]
    [athens.views.block-page :refer [block-page-component]]
    [athens.views.buttons :refer [button]]
    [athens.views.daily-notes :refer [daily-notes-panel db-scroll-daily-notes]]
    [athens.views.devtool :refer [devtool-component]]
    [athens.views.filesystem :as filesystem]
    [athens.views.graph-page :as graph-page]
    [athens.views.left-sidebar :refer [left-sidebar]]
    [athens.views.node-page :refer [node-page-component]]
    [athens.views.right-sidebar :refer [right-sidebar-component]]
    [athens.views.spinner :refer [initial-spinner-component]]
    [posh.reagent :refer [pull]]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


;;; Styles


(def app-wrapper-style
  {:display "grid"
   :grid-template-areas
   "'app-header app-header app-header'
    'left-sidebar main-content secondary-content'
   'devtool devtool devtool'"
   :grid-template-columns "auto 1fr auto"
   :grid-template-rows "auto 1fr auto"
   :height "100vh"})


(def main-content-style
  {:flex "1 1 100%"
   :grid-area "main-content"
   :align-items "flex-start"
   :justify-content "stretch"
   :padding-top "2.5rem"
   :display "flex"
   :overflow-y "auto"})


;;; Components


(defn alert
  []
  (let [alert- (subscribe [:alert])]
    (when-not (nil? @alert-)
      (js/alert (str @alert-))
      (dispatch [:alert/unset]))))


;; Panels


(defn settings-panel
  []
  (let [opted-out (r/atom (.. js/window -posthog has_opted_out_capturing))]
    (fn []
      [:div {:style {:display "flex"
                     :margin "0vh 5vw"
                     :flex-direction "column"}}
       [:h1 "Settings"]
       (if @opted-out
         [:h5 "Opted Out of Analytics"]
         [:h5 "Opted Into Analytics"])
       [:div {:style {:margin "10px 0"}}
        [button {:primary (false? @opted-out)
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
        ", an open-source provider of product analytics. This lets the designers and engineers at Athens know if we're really making something people love!"]])))


;;(prn (.. js/window -posthog opt_out_capturing))
;;(prn (.. js/window -posthog opt_in_capturing))



(defn pages-panel
  []
  (fn []
    ;;[:div
    ;; [:input.input-file {:type      "file"
    ;;                     :name      "file-input"
    ;;                     :on-change (fn [e] (file-cb e))}]]
    [table db/dsdb]))


(defn page-panel
  []
  (let [uid (subscribe [:current-route/uid])
        {:keys [node/title block/string db/id]} @(pull db/dsdb '[*] [:block/uid @uid])]
    (cond
      title [node-page-component id]
      string [block-page-component id]
      :else [:h3 "404: This page doesn't exist"])))


(defn match-panel
  "When app initializes, `route-name` is `nil`. Side effect of this is that a daily page for today is automatically
  created when app inits. This is expected, but perhaps shouldn't be a side effect here."
  [route-name]
  [(case route-name
     :settings settings-panel
     :home daily-notes-panel
     :pages pages-panel
     :page page-panel
     :graph graph-page/graph-page
     daily-notes-panel)])


(defn main-panel
  []
  (let [route-name (subscribe [:current-route/name])
        loading    (subscribe [:loading?])
        modal      (subscribe [:modal])]
    (fn []
      [:<>
       [alert]
       [athena-component]
       (cond
         (and @loading @modal) [athens.views.filesystem/window]

         @loading [initial-spinner-component]

         :else [:<>
                (when @modal [filesystem/window])
                [:div (use-style app-wrapper-style)
                 [app-toolbar]
                 [left-sidebar]
                 [:div (use-style main-content-style
                                  {:on-scroll (when (= @route-name :home)
                                                #(db-scroll-daily-notes %))})
                  [match-panel @route-name]]
                 [right-sidebar-component]
                 [devtool-component]]])])))
