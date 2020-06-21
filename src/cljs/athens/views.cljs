(ns athens.views
  (:require
    [athens.db :as db]
    [athens.devcards.all-pages :refer [table]]
    [athens.devcards.athena :refer [athena-component]]
    [athens.devcards.block-page :refer [block-page-component]]
    [athens.devcards.left-sidebar :refer [left-sidebar]]
    [athens.devcards.node-page :refer [node-page-component]]
    [athens.devcards.spinner :refer [spinner]]
    [athens.subs]
    [posh.reagent :refer [pull]]
    [re-frame.core :refer [subscribe dispatch]]
    [stylefy.core :refer [use-style]]))


;;; Styles


(def spinner-container
  {:margin-top "50vh"
   :transform "translateY(-50%)"})


(def app-wrapper-style
  {:display "flex"
   :height "100vh"})


(def match-panel-style
  {:margin "5rem auto"
   :min-width "500px"
   :max-width "900px"})


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
     [:p
      "Upload your DB " [:a {:href ""} "(tutorial)"]]
     [:input.input-file {:type      "file"
                         :name      "file-input"
                         :on-change (fn [e] (file-cb e))}]
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
  [:div (use-style match-panel-style)
   [(case name
      :about about-panel
      :pages pages-panel
      :page page-panel
      pages-panel)]])


(defn main-panel
  []
  (let [current-route (subscribe [:current-route])
        loading (subscribe [:loading])]
    (fn []
      [:<>
       [alert]
       [athena-component]
       (if @loading
         [:div (use-style spinner-container)
          [spinner]]
         [:div (use-style app-wrapper-style)
          [left-sidebar db/dsdb]
          [:div (use-style main-content-style)
           [match-panel (-> @current-route :data :name)]]])])))
