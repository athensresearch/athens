(ns athens.devcards.all-pages
  (:require
    [athens.db :as db]
    [athens.devcards.buttons :refer [button-primary]]
    [athens.devcards.db :refer [load-real-db-button]]
    [athens.lib.dom.attributes :refer [with-attributes]]
    [athens.router :refer [navigate-page]]
    [athens.style :as style :refer [base-styles color OPACITIES]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer [defcard defcard-rg]]
    [garden.core :refer [css]]
    [garden.selectors :as selectors]
    [posh.reagent :refer [transact! pull-many q]]
    [stylefy.core :as stylefy :refer [use-style use-sub-style]]))


;;; Styles


(def table-style
  {:width "100%"
   :text-align "left"
   :border-collapse "collapse"
   ::stylefy/sub-styles {:th-date {:text-align "right"}
                         :td-title {:color (color :link-color)
                                    :width "15vw"
                                    :min-width "10em"
                                    :word-break "break-word"
                                    :font-weight "500"
                                    :font-size "21px"
                                    :line-height "27px"}

                         :body-preview {:white-space "wrap"
                                        :word-break "break-word"
                                        :overflow "hidden"
                                        :text-overflow "ellipsis"
                                        :display "-webkit-box"
                                        :-webkit-line-clamp "3"
                                        :-webkit-box-orient "vertical"}
                         :td-date {:text-align "right"
                                   :opacity (:opacity-high OPACITIES)
                                   :font-size "12px"
                                   :min-width "9em"}}
   ::stylefy/manual [[:tbody {:vertical-align "top"}
                      [:tr
                       [:td {:border-top (str "1px solid " (color :panel-color))}]
                       [:&:hover {:background-color (color :panel-color :opacity-lower)
                                  :border-radius "8px"}
                        [:td [(selectors/& (selectors/first-child)) {:border-radius "8px 0 0 8px"
                                                                     :box-shadow "-16px 0 hsla(30, 11.11%, 93%, 0.1)"}]]
                        [:td [(selectors/& (selectors/last-child)) {:border-radius "0 8px 8px 0"
                                                                    :box-shadow "16px 0 hsla(30, 11.11%, 93%, 0.1)"}]]]]]
                     [:td :th {:padding "8px"}]
                     [:th [:h5 {:opacity (:opacity-med OPACITIES)}]]]})


;;; Components


(defn- date-string
  [x]
  (if (< x 1)
    [:span "(unknown date)"]
    (.toLocaleString  (js/Date. x))))


(defn table
  []
  (let [page-eids (q '[:find [?e ...]
                       :where
                       [?e :node/title ?t]]
                     db/dsdb)
        pages (pull-many db/dsdb '["*" {:block/children [:block/string] :limit 5}] @page-eids)]
    [:table (use-style table-style)
     [:thead
      [:tr
       [:th  [:h5 "Title"]]
       [:th  [:h5 "Body"]]
       [:th (use-sub-style table-style :th-date) [:h5 "Modified"]]
       [:th (use-sub-style table-style :th-date) [:h5 "Created"]]]]
     [:tbody
      (doall
        (for [{uid :block/uid
               title :node/title
               modified :edit/time
               created :create/time
               children :block/children} @pages]
          ^{:key uid}
          [:tr
           [:td (with-attributes
                  (use-sub-style table-style :td-title)
                  {:on-click #(navigate-page uid)})
            title]
           [:td
            [:div (use-sub-style table-style :body-preview) (clojure.string/join " " (map #(str "• " (:block/string %)) children))]]
           [:td (use-sub-style table-style :td-date) (date-string modified)]
           [:td (use-sub-style table-style :td-date) (date-string created)]]))]]))


;;; Devcards


(defcard "# All Pages — [#100](https://github.com/athensresearch/athens/issues/100)")


(defcard-rg Import-Styles
  [:<>
   [base-styles]
   [:style (css [:.com-rigsomelight-devcards-container {:width "90%"}])]])


(defcard-rg Create-Page
  "Page title increments by more than one each time because we create multiple entities (the child blocks)."
  [button-primary {:label "Create Page"
                   :on-click-fn (fn []
                                  (let [n (:max-eid @db/dsdb)]
                                    (transact! db/dsdb [{:node/title     (str "Test Title " n)
                                                         :block/uid      (str "uid" n)
                                                         :block/children [{:block/string "a block string" :block/uid (str "uid-" n "-" (rand))}]
                                                         :create/time    (.getTime (js/Date.))
                                                         :edit/time      (.getTime (js/Date.))}])))}])


(defcard-rg Load-Real-DB
  [load-real-db-button])


(defcard-rg Table
  [table])
