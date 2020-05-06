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

(defn file-cb [e]
  (let [fr (js/FileReader.)
        file (.. e -target -files (item 0))]
    (set! (.-onload fr) #(dispatch [:parse-datoms (.. % -target -result)]))
    (.readAsText fr file)))

(defn table
  [nodes]
  [:table {:style {:width "60%" :margin-top 20}}
   [:thead
    [:tr
     [:th {:style {:text-align "left"}} "Page"]
     [:th {:style {:text-align "left"}} "Last Edit"]
     [:th {:style {:text-align "left"}} "Created At"]]]
   [:tbody
    (for [{id :db/id
           bid :block/uid
           title :node/title
           c-time :create/time
           e-time :edit/time} nodes]
      ^{:key id}
      [:tr
       [:td {:style {:height 24}} [:a {:href (rfee/href :page {:id bid})} title]]
       [:td (.toLocaleString  (js/Date. c-time))]
       [:td (.toLocaleString  (js/Date. e-time))]
       ])]])

(defn pages-panel []
  (let [nodes (subscribe [:pull-nodes])]
    (fn []
      [:div
       [:p "Upload your DB " [:a {:href ""} "(tutorial)"]]
       [:input {:type "file"
                :name "file-input"
                :on-change (fn [e] (file-cb e))}]
       [table @nodes]])))

(defn home-panel []
  (fn []
    [:div
     [:h1 "Home Panel"]]))

(defn alert
  "When `:errors` subscription is updated, global alert will be called with its contents and then cleared."
  []
  (let [errors (subscribe [:errors])]
    (when (not (empty? @errors))
                (js/alert (str @errors))
                (dispatch [:clear-errors]))))

(defn match-panel [name]
  [(case name
     :about about-panel
     :pages pages-panel
     :page  page/main
     pages-panel)])

(defn main-panel []
  (let [current-route (subscribe [:current-route])
        loading (subscribe [:loading])]
    (fn []
      [alert]
      (if @loading
        [:h4 "Loading... (at least it'll be faster than Roam)"]
        [:div
         ;;[:h1 (str "Hello World")]
         [:div
          [:a {:href (rfee/href :pages)} "All /pages"]
          [:span {:style {:margin 0 :margin-left 10}} "Current Route: " [:b (-> @current-route :path)]]]
         [match-panel (-> @current-route :data :name)]]))))
