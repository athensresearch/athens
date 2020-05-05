(ns athens.views
  (:require
   [athens.subs]
   [athens.page :as page]
   [re-frame.core :as rf :refer [subscribe dispatch]]
   [reitit.frontend :as rfe]
   [reitit.frontend.easy :as rfee]
   [reagent.core :refer [atom]]

   [datascript.core :as d]
   [athens.db]

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

(defn search-box []
  (let [*matches (atom [])]
    [:div {:style {:position "relative"
                   :display "inline-block"}}
     [:input#find-or-create-input.bp3-input
      {:type "search"
       :placeholder "Find or Create Page",
       :on-change (fn [e]
                    (let [v (.. e -target -value)]
                      ;; FIXME don't use globals, pass db as argument. E.g. via services map
                      (let [db (d/db athens.db/dsdb)
                            matches (take 10
                                          (d/q '[:find [(pull ?node [:db/id
                                                                     :block/string
                                                                     :node/title #_(comment "what else here?")
                                                                     *]) ...]
                                                 :in $ ?query-pattern
                                                 :where
                                                 (or
                                                  [?node :node/title ?txt]
                                                  [?node :block/string ?txt])
                                                 [(re-find ?query-pattern  ?txt)]]
                                               db
                                               ;; Case insensitive search, other options
                                               ;; here https://clojuredocs.org/clojure.core/re-pattern
                                               (re-pattern (str "(?i)" v))))]
                        (reset! *matches matches))
                      ))}]
     [:div {
            :style {:background-color "white"
                    :position "absolute"
                    :z-index 99
                    :top "100%"
                    :left 0
                    :right 0}}
      [(fn []
         [:ul (for [[i item] (map-indexed list @*matches)]
                ;; TODO highlight match
                [:li {:key i} (pr-str item)])])]]]))

(defn main-panel []
  (let [current-route (subscribe [:current-route])]
    (fn []
      [:div
       [alert]
       ;;[:h1 (str "Hello World")]
       [:div
        [:a {:href (rfee/href :pages)} "All /pages"]
        [:span {:style {:margin 0 :margin-left 10}} "Current Route: " [:b (-> @current-route :path)]]]
       [search-box]
       [match-panel (-> @current-route :data :name)]])))
