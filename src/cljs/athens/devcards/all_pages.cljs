(ns athens.devcards.all-pages
  (:require
   [athens.devcards.db :refer [new-conn posh-conn! load-real-db-button]]
   [athens.lib.dom.attributes :refer [with-styles with-attributes]]
   [athens.router :refer [navigate-page]]
   [athens.style :as style :refer [base-styles +text-align-right +text-align-left +link HSL-COLORS COLORS OPACITIES]]
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer [defcard defcard-rg]]
   [garden.color :refer [opacify]]
   [garden.core :refer [css]]
   [posh.reagent :refer [transact! pull-many q]]
   [stylefy.core :as stylefy :refer [use-style]]))


(defcard-rg Import-Styles
  [base-styles])


(defcard-rg Modify-Devcards
  "Increase width to 90% for table"
  [:style (css [:.com-rigsomelight-devcards-container {:width "90%"}])])


(defcard Instantiate-Dsdb)
(defonce conn (new-conn))
(posh-conn! conn)


(defn handler
  []
  (let [n (:max-eid @conn)]
    (transact! conn [{:node/title     (str "Test Title " n)
                      :block/uid      (str "uid" n)
                      :block/children [{:block/string "a block string" :block/uid (str "uid-" n "-" (rand))}]
                      :create/time    (.getTime (js/Date.))
                      :edit/time      (.getTime (js/Date.))}])))


(defcard-rg Create-Page
  "Page title increments by more than one each time because we create multiple entities (the child blocks)."
  [:button.primary {:on-click handler} "Create Page"])


(defcard-rg Load-Real-DB
  [load-real-db-button conn])


(defn- date-string
  [x]
  (if (< x 1)
    [:span "(unknown date)"]
    (.toLocaleString  (js/Date. x))))


(def tables
  {:width "100%"
   :border-collapse "collapse"
   ::stylefy/manual [[:tbody
                      [:tr
                       [:&:hover {:background-color (opacify (:panel-color HSL-COLORS) (first OPACITIES))
                                  :border-radius "8px"}]
                       [:td {:border-top (str "1px solid " (:panel-color COLORS))}]
                       ]]
                     [:td :th {:padding "8px"}]
                     ]})


(defn table
  [conn]
  (let [page-eids (q '[:find [?e ...]
                       :where
                       [?e :node/title ?t]]
                     conn)
        pages (pull-many conn '["*" {:block/children [:block/string] :limit 5}] @page-eids)]
    [:table (use-style tables)
     [:thead
      [:tr
       [:th [:h5 +text-align-left "Title"]]
       [:th [:h5 +text-align-left "Body"]]
       [:th [:h5 +text-align-right "Modified"]]
       [:th [:h5 +text-align-right "Created"]]]]
     [:tbody
      (for [{uid :block/uid
             title :node/title
             modified :edit/time
             created :create/time
             children :block/children} @pages]
        ^{:key uid}
        [:tr
         [:td
          [:h4 (with-attributes
                 (with-styles +link {:width "200px" :word-break "break-all"})
                 {:on-click #(navigate-page uid)})
           title]]
         [:td (with-styles {:width "500px" :max-height "40px" :white-space "wrap" :overflow "hidden" :text-overflow "ellipsis" :display "block"} +text-align-left)
          (clojure.string/join " " (map #(str "• " (:block/string %)) children))]
         [:td +text-align-right (date-string modified)]
         [:td +text-align-right (date-string created)]])]]))


(defcard-rg Table
  [table conn])

