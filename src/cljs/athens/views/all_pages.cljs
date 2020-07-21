(ns athens.views.all-pages
  (:require
    [athens.db :as db]
    [athens.router :refer [navigate-uid]]
    [athens.style :as style :refer [color OPACITIES]]
    [athens.util :refer [date-string]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as str]
    [garden.selectors :as selectors]
    [posh.reagent :refer [pull-many q]]
    [stylefy.core :as stylefy :refer [use-style use-sub-style]]))


;;; Styles


(def page-style
  {:display "flex"
   :margin "5rem auto"
   :flex-basis "100%"
   :max-width "70rem"})


(def table-style
  {:flex "1 1 100%"
   :margin "0 2rem"
   :text-align "left"
   :border-collapse "collapse"
   ::stylefy/sub-styles {:th-date {:text-align "right"}
                         :td-title {:color (color :link-color)
                                    :width "15vw"
                                    :cursor "pointer"
                                    :min-width "10em"
                                    :word-break "break-word"
                                    :font-weight "500"
                                    :font-size "1.3125em"
                                    :line-height "1.28"}
                         :body-preview {:white-space "wrap"
                                        :word-break "break-word"
                                        :overflow "hidden"
                                        :text-overflow "ellipsis"
                                        :display "-webkit-box"
                                        :-webkit-line-clamp "3"
                                        :-webkit-box-orient "vertical"}
                         :td-date {:text-align "right"
                                   :opacity (:opacity-high OPACITIES)
                                   :font-size "0.75em"
                                   :min-width "9em"}}
   ::stylefy/manual [[:tbody {:vertical-align "top"}
                      [:tr {:transition "background 0.1s ease"}
                       [:td {:border-top (str "1px solid " (color :border-color))
                             :transition "box-shadow 0.1s ease"}
                        [(selectors/& (selectors/first-child)) {:border-radius "0.5rem 0 0 0.5rem"
                                                                :box-shadow "-1rem 0 transparent"}]
                        [(selectors/& (selectors/last-child)) {:border-radius "0 0.5rem 0.5rem 0"
                                                               :box-shadow "1rem 0 transparent"}]]
                       [:&:hover {:background-color (color :background-minus-1 :opacity-med)
                                  :border-radius "0.5rem"}
                        [:td [(selectors/& (selectors/first-child)) {:box-shadow [["-1rem 0 " (color :background-minus-1 :opacity-med)]]}]]
                        [:td [(selectors/& (selectors/last-child)) {:box-shadow [["1rem 0 " (color :background-minus-1 :opacity-med)]]}]]]]]
                     [:td :th {:padding "0.5rem"}]
                     [:th [:h5 {:opacity (:opacity-med OPACITIES)}]]]})


;;; Components


(defn table
  []
  (let [page-eids (q '[:find [?e ...]
                       :where
                       [?e :node/title ?t]]
                     db/dsdb)
        pages (pull-many db/dsdb '["*" {:block/children [:block/string] :limit 5}] @page-eids)]
    [:div (use-style page-style)
     [:table (use-style table-style)
      [:thead
       [:tr
        [:th [:h5 "Title"]]
        [:th [:h5 "Body"]]
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
            [:td (use-sub-style table-style :td-title {:on-click #(navigate-uid uid %)})
             title]
            [:td
             [:div (use-sub-style table-style :body-preview) (str/join " ") (map #(str "• " (:block/string %)) children)]]
            [:td (use-sub-style table-style :td-date) (date-string modified)]
            [:td (use-sub-style table-style :td-date) (date-string created)]]))]]]))
