(ns athens.devcards.table
  (:require
    [athens.db :as db]
    [athens.devcards.db :refer [-main]]
    [athens.lib.dom.attributes :refer [with-styles with-attributes]]
    [athens.router :refer [navigate-page]]
    [athens.style :as style :refer [style-guide-css COLORS]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer [defcard-rg]]
    [garden.core :refer [css]]
    [posh.reagent :refer [pull-many q]]))


(-main)


(defn- date-string
  [x]
  (if (< x 1)
    [:span (style/+unknown-date {}) "(unknown date)"]
    (.toLocaleString  (js/Date. x))))


(defn main-css
  []
  [:style (css [:.com-rigsomelight-devcards-container {:width "90%"}])])


(def +text-align-left
  (with-styles {:text-align "left"}))


(def +text-align-right
  (with-styles {:text-align "right"}))


(def +width-100
  (with-styles {:width "100%"}))


(def +link
  (with-styles {:color (:link-color COLORS) :cursor "pointer"}))


(defcard-rg Import-Styles
  [style-guide-css])


(defcard-rg Modify-Devcards
  "Increase width to 90% for table"
  [main-css])


(defn table
  []
  (let [page-eids (q '[:find [?e ...]
                       :where
                       [?e :node/title ?t]]
                     db/dsdb)
        pages (pull-many db/dsdb '["*" {:block/children [:block/string] :limit 5}] @page-eids)
        get-pages (take 10 @pages)]
    [:table +width-100
     [:thead
      [:tr
       [:th [:h5 +text-align-left "Page"]]
       [:th [:h5 +text-align-left "Body"]]
       [:th [:h5 +text-align-right "Modified"]]
       [:th [:h5 +text-align-right "Created"]]]]
     [:tbody
      (for [{uid :block/uid
             title :node/title
             modified :edit/time
             created :create/time
             children :block/children} get-pages]
        ^{:key uid}
        [:tr
         [:td
          {:style {:height 24}}
          [:h4 (with-attributes +link {:on-click #(navigate-page uid)}) title]]
         [:td +text-align-left (clojure.string/join " " (map #(str "• " (:block/string %)) children))]
         [:td +text-align-right (date-string modified)]
         [:td +text-align-right (date-string created)]])]]))


(defcard-rg Table
  [table])

