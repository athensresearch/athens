(ns athens.devcards.all-pages
  (:require
    [athens.devcards.buttons :refer [button-primary]]
    [athens.devcards.db :refer [new-conn posh-conn! load-real-db-button]]
    [athens.lib.dom.attributes :refer [with-styles with-attributes]]
    [athens.router :refer [navigate-page]]
    [athens.style :as style :refer [base-styles +link HSL-COLORS COLORS OPACITIES]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer [defcard defcard-rg]]
    [garden.color :refer [opacify]]
    [garden.core :refer [css]]
    [garden.selectors :as selectors]
    [posh.reagent :refer [transact! pull-many q]]
    [stylefy.core :as stylefy :refer [use-style use-sub-style]]))


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
  [button-primary {:on-click-fn handler :label "Create Page"}])


(defcard-rg Load-Real-DB
  [load-real-db-button conn])


(defn- date-string
  [x]
  (if (< x 1)
    [:span "(unknown date)"]
    (.toLocaleString  (js/Date. x))))


(def table-cell-background-color-hover
  (opacify (:panel-color HSL-COLORS) (first OPACITIES)))


(def tables
  {:width "100%"
   :text-align "left"
   :border-collapse "collapse"
   ::stylefy/sub-styles {:thead {}
                         :tr-item {}
                         :th-title {}
                         :th-body {}
                         :th-date {:text-align "right"}
                         :td-title {:width "15vw"
                                    :min-width "10em"
                                    :word-break "break-word"
                                    :font-weight "500"
                                    :font-size "21px"
                                    :line-height "27px"}
                         :td-body {}
                         :body-preview {:white-space "wrap"
                                        :word-break "break-word"
                                        :overflow "hidden"
                                        :text-overflow "ellipsis"
                                        :display "-webkit-box"
                                        :-webkit-line-clamp "3"
                                        :-webkit-box-orient "vertical"}
                         :td-date {:text-align "right"
                                   :opacity "0.75"
                                   :font-size "12px"
                                   :min-width "9em"}}
   ::stylefy/manual [[:tbody {:vertical-align "top"}
                      [:tr
                       [:td {:border-top (str "1px solid " (:panel-color COLORS))}]
                       [:&:hover {:background-color table-cell-background-color-hover
                                  :border-radius "8px"}
                        ;; [:td {:border-top-color "transparent"}]
                        [:td [(selectors/& (selectors/first-child)) {:border-radius "8px 0 0 8px"
                                                                     :box-shadow "-16px 0 hsla(30, 11.11%, 93%, 0.1)"}]]
                        [:td [(selectors/& (selectors/last-child)) {:border-radius "0 8px 8px 0"
                                                                    :box-shadow "16px 0 hsla(30, 11.11%, 93%, 0.1)"}]]]]]
                     [:td :th {:padding "8px"}]
                     [:th [:h5 {:opacity "0.5"}]]]})


(defn table
  [conn]
  (let [page-eids (q '[:find [?e ...]
                       :where
                       [?e :node/title ?t]]
                     conn)
        pages (pull-many conn '["*" {:block/children [:block/string] :limit 5}] @page-eids)]
    [:table (use-style tables)
     [:thead (use-sub-style tables :thead)
      [:tr
       [:th (use-sub-style tables :th-title) [:h5 "Title"]]
       [:th (use-sub-style tables :th-body) [:h5 "Body"]]
       [:th (use-sub-style tables :th-date) [:h5 "Modified"]]
       [:th (use-sub-style tables :th-date) [:h5 "Created"]]]]
     [:tbody
      (doall
        (for [{uid :block/uid
               title :node/title
               modified :edit/time
               created :create/time
               children :block/children} @pages]
          ^{:key uid}
          [:tr (use-sub-style tables :tr-item)
           [:td (with-attributes
                  (use-sub-style tables :td-title)
                  {:on-click #(navigate-page uid)})
            title]
           [:td (use-sub-style tables :td-body)
            [:div (use-sub-style tables :body-preview) (clojure.string/join " " (map #(str "• " (:block/string %)) children))]]
           [:td (use-sub-style tables :td-date) (date-string modified)]
           [:td (use-sub-style tables :td-date) (date-string created)]]))]]))


(defcard-rg Table
  [table conn])

