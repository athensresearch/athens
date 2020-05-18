(ns athens.views
  (:require
   #_[reitit.frontend :as rfe]
   [athens.page :as page]
   [athens.style :as style]
   [athens.subs]
   [re-frame.core :as rf :refer [subscribe dispatch]]
   [reitit.frontend.easy :as rfee]))

(defn about-panel []
  [:div [:h1 "About Panel"]])

(defn file-cb [e]
  (let [fr (js/FileReader.)
        file (.. e -target -files (item 0))]
    (set! (.-onload fr) #(dispatch [:parse-datoms (.. % -target -result)]))
    (.readAsText fr file)))

(defn- date-string [x] (if (< x 1)
                         [:span (style/+unknown-date {}) "(unknown date)"]
                         (.toLocaleString  (js/Date. x))))

(defn table
  [nodes]
  [:table (style/+pages-table {})
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
       [:td (date-string c-time)]
       [:td (date-string e-time)]])]])

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

(defn left-sidebar
  []
  (fn []
    (let [favorites (subscribe [:favorites])
          current-route (subscribe [:current-route])]
      [:div
       {:style {:margin "0 10px"
                :max-width 250}}
       [:div
        [:a
         {:href (rfee/href :pages)}
         "All /pages"]]
       [:div
        [:span
         {:style {}}
         "Current Route: "
         [:b
          (-> @current-route :path)]]]
       [:div
        {:style {:border-bottom "1px solid gray"
                 :margin "10px 0"}}]
       [:ul
        (style/+left-sidebar {})
        (for [[_order title bid] @favorites]
          ^{:key bid} [:li
                       [:a
                        {:href (rfee/href :page {:id bid})}
                        title]])]])))

(defn alert
  "When `:errors` subscription is updated, global alert will be called with its contents and then cleared."
  []
  (let [errors (subscribe [:errors])]
    (when (seq @errors)
      (js/alert (str @errors))
      (dispatch [:clear-errors]))))

(defn match-panel [name]
  [(case name
     :about about-panel
     :pages pages-panel
     :page page/main
     pages-panel)])

(defn main-panel []
  (let [current-route (subscribe [:current-route])
        loading (subscribe [:loading])]
    (fn []
      [alert]
      (if @loading
        [:div
         [style/loading-css]
         [:h4#loading-text "Loading database..."]]
        [:div
         {:style {:display "flex"}}
         [style/main-css]
         [left-sidebar]
         [match-panel (-> @current-route :data :name)]]))))
