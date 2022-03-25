(ns athens.views
  (:require
   ["/components/Spinner/Spinner" :refer [Spinner]]
   ["/theme/theme" :refer [theme]]
   ["@chakra-ui/react" :refer [ChakraProvider Grid]]
   ["@material-ui/core/Snackbar" :as Snackbar]
   ["@react-aria/overlays" :refer [OverlayProvider]]
   [athens.config]
   [athens.electron.db-modal :as db-modal]
   [athens.electron.utils :as electron.utils]
   [athens.style :refer [zoom]]
   [athens.subs]
   [athens.util :refer [get-os]]
   [athens.views.app-toolbar :as app-toolbar]
   [athens.views.athena :refer [athena-component]]
   [athens.views.devtool :refer [devtool-component]]
   [athens.views.help :refer [help-popup]]
   [athens.views.left-sidebar :as left-sidebar]
   [athens.views.pages.core :as pages]
   [athens.views.right-sidebar :as right-sidebar]
   [re-frame.core :as rf]
   [reagent.core :as r]))


;; Components


(defn alert
  []
  (let [alert- (rf/subscribe [:alert])]
    (when-not (nil? @alert-)
      (js/alert (str @alert-))
      (rf/dispatch [:alert/unset]))))


;; Snackbar

(def m-snackbar (r/adapt-react-class (.-default Snackbar)))


(rf/reg-sub
  :db/snack-msg
  (fn [db]
    (:db/snack-msg db)))


(rf/reg-event-db
  :show-snack-msg
  (fn [db [_ msg-opts]]
    (js/setTimeout #(rf/dispatch [:show-snack-msg {}]) 4000)
    (assoc db :db/snack-msg msg-opts)))


(defn main
  []
  (let [loading    (rf/subscribe [:loading?])
        os         (get-os)
        electron?  electron.utils/electron?
        modal      (rf/subscribe [:modal])]
    (fn []
      [:div (merge {:style {:display "contents"}}
                   (zoom))
       [:> ChakraProvider {:theme theme,
                           :bg "background.basement"}
        [:> OverlayProvider
         [help-popup]
         [alert]
         (let [{:keys [msg type]} @(rf/subscribe [:db/snack-msg])]
           [m-snackbar
            {:message msg
             :open (boolean msg)}
            [:span
             {:style {:background-color (case type
                                          :success "green"
                                          "red")
                      :padding "10px 20px"
                      :color "white"}}
             msg]])
         [athena-component]
         (cond
           (and @loading @modal) [db-modal/window]

           @loading [:> Spinner]

           :else [:<>
                  (when @modal [db-modal/window])
                  [:> Grid
                   {:gridTemplateColumns "auto 1fr auto"
                    :gridTemplateRows "auto 1fr auto"
                    :grid-template-areas
                    "'app-header app-header app-header'
                      'left-sidebar main-content secondary-content'
                    'devtool devtool devtool'"
                    :height "100vh"
                    :overflow "hidden"
                    :sx {"-webkit-app-region" "drag"}
                    :className [(case os
                                  :windows "os-windows"
                                  :mac "os-mac"
                                  :linux "os-linux")
                                (when electron? "is-electron")]}
                   [app-toolbar/app-toolbar]
                   [left-sidebar/left-sidebar]
                   [pages/view]
                   [right-sidebar/right-sidebar]
                   [devtool-component]]])]]])))
