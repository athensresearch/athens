(ns athens.views
  (:require
    [athens.db :as db]
    [athens.devcards.all-pages :refer [table]]
    [athens.devcards.athena :refer [athena-component]]
    [athens.devcards.block-page :refer [block-page-component]]
    [athens.devcards.buttons :refer [button-primary]]
    [athens.devcards.daily-notes :refer [daily-notes-panel]]
    [athens.devcards.daily-notes :refer [db-scroll-daily-notes]]
    [athens.devcards.devtool :refer [devtool-component]]
    [athens.devcards.left-sidebar :refer [left-sidebar]]
    [athens.devcards.node-page :refer [node-page-component]]
    [athens.devcards.right-sidebar :refer [right-sidebar-component]]
    [athens.devcards.spinner :refer [initial-spinner-component]]
    [athens.subs]
    [posh.reagent :refer [pull]]
    [re-frame.core :refer [subscribe dispatch]]
    [stylefy.core :refer [use-style]]))


;;; Styles


(def app-wrapper-style
  {:display "grid"
   :grid-template-areas
   "'left-sidebar main-content secondary-content'
   'devtool devtool devtool'"
   :grid-template-columns "auto 1fr auto"
   :grid-template-rows "1fr auto"
   :height "100vh"})


(def main-content-style
  {:flex "1 1 100%"
   :overflow-y "auto"})


;;; Components


(defn alert
  "When `:errors` subscription is updated, global alert will be called with its contents and then cleared."
  []
  (let [errors (subscribe [:errors])]
    (when (seq @errors)
      (js/alert (str @errors))
      (dispatch [:clear-errors]))))


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
    [:div
     ;;[:input.input-file {:type      "file"
     ;;                    :name      "file-input"
     ;;                    :on-change (fn [e] (file-cb e))}]
     [button-primary {:label "Load Test Data"
                      :on-click-fn #(dispatch [:get-local-storage-db])}]
     ;;[button {:on-click-fn #(dispatch [:reset-db])}]
     [table db/dsdb]]))


(defn page-panel
  []
  (let [current-route (subscribe [:current-route])
        uid           (-> @current-route :path-params :id)
        node-or-block @(pull db/dsdb '[*] [:block/uid uid])]
    [:div {:style {:margin-left "40px" :margin-right "40px"}}
     (if (:node/title node-or-block)
       [node-page-component (:db/id node-or-block)]
       [block-page-component (:db/id node-or-block)])]))


(defn match-panel
  [name]
  [(case name
     :about about-panel
     :home daily-notes-panel
     :pages pages-panel
     :page page-panel
     daily-notes-panel)])


(defn main-panel
  []
  (let [current-route (subscribe [:current-route])
        loading (subscribe [:loading])]
    (fn []
      [:<>
       [alert]
       [athena-component]
       (if @loading
         [initial-spinner-component]
         [:div (use-style app-wrapper-style)
          [left-sidebar]
          [:div (use-style main-content-style
                           {:on-scroll (when (= (-> @current-route :data :name) :home)
                                         db-scroll-daily-notes)})
           [match-panel (-> @current-route :data :name)]]
          [right-sidebar-component]
          [devtool-component]])])))
