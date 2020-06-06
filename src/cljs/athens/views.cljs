(ns athens.views
  (:require
    [athens.db :as db]
    [athens.devcards.all-pages :refer [table]]
    [athens.devcards.athena :refer [athena]]
    [athens.devcards.left-sidebar :refer [left-sidebar]]
    [athens.lib.dom.attributes :refer [with-styles]]
    [athens.page :as page]
    [athens.style :as style]
    [athens.subs]
    [re-frame.core :as rf :refer [subscribe dispatch]]))


(defn about-panel
  []
  [:div
   [:h1 "About Panel"]])


(defn file-cb
  [e]
  (let [fr (js/FileReader.)
        file (.. e -target -files (item 0))]
    (set! (.-onload fr) #(dispatch [:parse-datoms (.. % -target -result)]))
    (.readAsText fr file)))


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


(defn alert
  "When `:errors` subscription is updated, global alert will be called with its contents and then cleared."
  []
  (let [errors (subscribe [:errors])]
    (when (seq @errors)
      (js/alert (str @errors))
      (dispatch [:clear-errors]))))


(defn match-panel
  [name]
  [:div (with-styles {:margin "5rem auto" :min-width "500px" :max-width "900px"})
   [(case name
      :about about-panel
      :pages pages-panel
      :page page/main
      pages-panel)]])


(defn main-panel
  []
  (let [current-route (subscribe [:current-route])
        loading (subscribe [:loading])]
    (fn []
      [:<>
       [style/style-guide-css]
       [alert]
       [athena db/dsdb]
       (if @loading
         [:h1 (with-styles {:margin-top "50vh" :text-align "center" :opacity "0.9"}) "Loading Athens 😈"]
         [:div
          (with-styles {:display "flex" :height "100vh"})
          [style/style-guide-css]
          [left-sidebar db/dsdb]
          [:div
            (with-styles {:flex "1 1 100%" :overflow-y "auto"})
            [match-panel (-> @current-route :data :name)]]])])))
