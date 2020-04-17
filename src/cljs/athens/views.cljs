(ns athens.views
  (:require
   [athens.subs]
   [athens.page :as page]
   [re-frame.core :as rf :refer [subscribe dispatch]]
   [reitit.frontend :as rfe]
   [reitit.frontend.easy :as rfee]
   ))

(defn about-panel []
  [:div [:h1 "About Panel"]])

;; https://reactjs.org/docs/uncontrolled-components.html#the-file-input-tag
;; https://codepen.io/tangsauce/pen/pojyOyV?editors=0010
;; https://github.com/reagent-project/reagent/blob/master/doc/FAQ/UsingRefs.md
;; https://developer.mozilla.org/en-US/docs/Web/API/File/Using_files_from_web_applications
(defn file-cb [e]
  (let [fr (js/FileReader.)
        file (.. e -target -files (item 0))]
    (set! (.-onload fr) #(dispatch [:upload-dsdb (.. % -target -result)]))
    (.readAsText fr file)))

(defn pages-panel []
  (let [nodes (subscribe [:nodes])]
    (fn []
      [:div
       [:p "Upload your DB " [:a {:href ""} "(tutorial)"]]
       [:input {:type "file"
                :name "file-input"
                :on-change (fn [e] (file-cb e))}]
       [:table {:style {:width "80%" :margin-top 20}}
        [:thead
         [:tr
          [:th {:style {:text-align "left"}} "Page"]
          [:th {:style {:text-align "left"}} "Last Edit"]
          [:th {:style {:text-align "left"}} "Created At"]]]
        [:tbody
         (for [[_ title id create-t edit-t] @nodes]
           ^{:key id}
           [:tr
            [:td {:style {:height 24}} [:a {:href (rfee/href :page {:id id})} title]]
            [:td (.toLocaleString  (js/Date. create-t))]
            [:td (.toLocaleString  (js/Date. edit-t))]])]]])))


(defn home-panel []
  (fn []
    [:div
     [:h1 "Home Panel"]]))

(defn match-panel [name]
  [(case name
     :about about-panel
     :pages pages-panel
     :page  page/main 
     pages-panel)])

(defn main-panel []
  (let [current-route (subscribe [:current-route])]
    (fn []
      [:div
       ;; [:h1 "Hello World"]
       [:div
        [:a {:href (rfee/href :pages)} "All /pages"]
        [:span {:style {:margin 0 :margin-left 10}} "Current Route: " [:b (-> @current-route :path)]]]
       [match-panel (-> @current-route :data :name)]
       ])))
