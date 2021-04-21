(ns athens.views
  (:require
    ["@material-ui/core/Snackbar" :as Snackbar]
    ;;[athens.views.right-sidebar :refer [right-sidebar-component]]
    ;;[athens.views.settings-page :as settings-page]
    [athens.config]
    [athens.db :as db]
    [athens.style :refer [color]]
    [athens.subs]
    [athens.views.app-toolbar :refer [app-toolbar]]
    [athens.views.athena :refer [athena-component]]
    [athens.views.devtool :refer [devtool-component]]
    [athens.views.filesystem :as filesystem]
    [athens.views.left-sidebar :refer [left-sidebar]]
    [athens.views.pages.all-pages :as all-pages]
    [athens.views.pages.block-page :as block-page]
    [athens.views.pages.page :as page]
    [athens.views.pages.daily-notes :as daily-notes]
    [athens.views.pages.graph :as graph]
    [athens.views.pages.node-page :as node-page]
    [athens.views.pages.settings :as settings]
    [athens.views.spinner :refer [initial-spinner-component]]
    [posh.reagent :refer [pull]]
    [re-frame.core :refer [subscribe dispatch] :as rf]
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
   :overflow-y "auto"
   ::stylefy/mode {"::-webkit-scrollbar" {:background (color :background-minus-1)
                                          :width "0.5rem"
                                          :height "0.5rem"}
                   "::-webkit-scrollbar-corner" {:background (color :background-minus-1)}
                   "::-webkit-scrollbar-thumb" {:background (color :background-minus-2)
                                                :border-radius "0.5rem"}}})


;;; Components


(defn alert
  []
  (let [alert- (subscribe [:alert])]
    (when-not (nil? @alert-)
      (js/alert (str @alert-))
      (dispatch [:alert/unset]))))


;; Panels


(defn match-panel
  "When app initializes, `route-name` is `nil`. Side effect of this is that a daily page for today is automatically
  created when app inits. This is expected, but perhaps shouldn't be a side effect here."
  [route-name]
  [(case route-name
     :settings settings/page
     :pages all-pages/page
     :page page/page
     :graph graph/page
     :home daily-notes/page
     daily-notes/page)])


(def m-snackbar (r/adapt-react-class (.-default Snackbar)))


(rf/reg-sub
  :db/snack-msg
  (fn [db]
    (:db/snack-msg db)))


(rf/reg-event-db
  :show-snack-msg
  (fn [db [_ msg-opts]]
    (js/setTimeout #(dispatch [:show-snack-msg {}]) 4000)
    (assoc db :db/snack-msg msg-opts)))


(defn main-panel
  []
  (let [route-name (subscribe [:current-route/name])
        loading    (subscribe [:loading?])
        modal      (subscribe [:modal])]
    (fn []
      [:<>
       [alert]
       (let [{:keys [msg type]} @(subscribe [:db/snack-msg])]
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
         (and @loading @modal) [filesystem/window]

         @loading [initial-spinner-component]

         :else [:<>
                (when @modal [filesystem/window])
                [:div (use-style app-wrapper-style)
                 [app-toolbar]
                 [left-sidebar]
                 [:div (use-style main-content-style
                                  {:on-scroll (when (= @route-name :home)
                                                #(daily-notes/db-scroll-daily-notes %))})
                  [match-panel @route-name]]
                 [right-sidebar-component]
                 [devtool-component]]])])))
