(ns athens.views
  (:require
    [athens.db :as db]
    [athens.devcards.app-toolbar :refer [app-toolbar]]
    [athens.subs]
    [athens.views.all-pages :refer [table]]
    [athens.views.athena :refer [athena-component]]
    [athens.views.block-page :refer [block-page-component]]
    [athens.views.daily-notes :refer [daily-notes-panel db-scroll-daily-notes]]
    [athens.views.devtool :refer [devtool-component]]
    [athens.views.left-sidebar :refer [left-sidebar]]
    [athens.views.node-page :refer [node-page-component]]
    [athens.views.right-sidebar :refer [right-sidebar-component]]
    [athens.views.spinner :refer [initial-spinner-component]]
    [posh.reagent :refer [pull]]
    [re-frame.core :refer [subscribe dispatch]]
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


(defn file-cb
  [e]
  (let [fr (js/FileReader.)
        file (.. e -target -files (item 0))]
    (set! (.-onload fr) #(dispatch [:parse-datoms (.. % -target -result)]))
    (.readAsText fr file)))


;; Panels


(defn about-panel
  []
  [:div
   [:h1 "About Panel"]])


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
  (let [current-route (subscribe [:current-route])
        uid           (-> @current-route :path-params :id)
        {:keys [node/title block/string db/id]} @(pull db/dsdb '[*] [:block/uid uid])]
    (cond
      title [node-page-component id]
      string [block-page-component id]
      :else [:h3 "404: This page doesn't exist"])))


(defn match-panel
  "When app initializes, `route-name` is `nil`. Side effect of this is that a daily page for today is automatically
  created when app inits. This is expected, but perhaps shouldn't be a side effect here."
  [route-name]
  [(case route-name
     :about about-panel
     :home daily-notes-panel
     :pages pages-panel
     :page page-panel
     daily-notes-panel)])


(defn main-panel
  []
  (let [current-route (subscribe [:current-route])
        loading (subscribe [:loading?])]
    (fn []
      (let [route-name (-> @current-route :data :name)]
        [:<>
         [alert]
         [athena-component]
         (if @loading
           [initial-spinner-component]
           [:div (use-style app-wrapper-style)
            [app-toolbar]
            [left-sidebar]
            [:div (use-style main-content-style
                             {:on-scroll (when (= route-name :home)
                                           db-scroll-daily-notes)})
             [match-panel route-name]]
            [right-sidebar-component]
            [devtool-component]])]))))
