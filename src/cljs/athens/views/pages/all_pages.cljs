(ns athens.views.pages.all-pages
  (:require
    [athens.db :as db]
    [athens.router :refer [navigate-uid]]
    [athens.style :as style :refer [color OPACITIES]]
    [athens.util :refer [date-string]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :refer [lower-case]]
    [datascript.core :as d]
    [garden.selectors :as selectors]
    [posh.reagent :as p]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


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
   ::stylefy/manual [[:tbody {:vertical-align "top"}
                      [:tr {:transition "background 0.1s ease"}
                       [:td {:border-top (str "1px solid " (color :border-color))
                             :transition "box-shadow 0.1s ease"}
                        [:&.title {:color (color :link-color)
                                   :width "15vw"
                                   :cursor "pointer"
                                   :min-width "10em"
                                   :word-break "break-word"
                                   :font-weight "500"
                                   :font-size "1.3125em"
                                   :line-height "1.28"}]
                        [:&.links {:font-size "1em"
                                   :text-align "center"}]
                        [:&.body-preview {:word-break "break-word"
                                          :overflow "hidden"
                                          :text-overflow "ellipsis"
                                          :display "-webkit-box"
                                          :-webkit-mask "linear-gradient(to bottom, #fff calc(100% - 1em), transparent)"
                                          :-webkit-line-clamp "3"
                                          :-webkit-box-orient "vertical"}
                         [:span:empty {:display "none"}]
                         [(selectors/+ :span :span)
                          [:&:before {:content "'•'"
                                      :margin-inline "0.5em"
                                      :opacity (:opacity-low OPACITIES)}]]]
                        [:&.date {:text-align "right"
                                  :opacity (:opacity-high OPACITIES)
                                  :font-size "0.75em"
                                  :min-width "9em"}]
                        [:&:first-child {:border-radius "0.5rem 0 0 0.5rem"
                                         :box-shadow "-1rem 0 transparent"}]
                        [:&:last-child {:border-radius "0 0.5rem 0.5rem 0"
                                        :box-shadow "1rem 0 transparent"}]]
                       [:&:hover {:background-color (color :background-minus-1 :opacity-med)
                                  :border-radius "0.5rem"}
                        [:td [:&:first-child {:box-shadow [["-1rem 0 " (color :background-minus-1 :opacity-med)]]}]]
                        [:td [:&:last-child {:box-shadow [["1rem 0 " (color :background-minus-1 :opacity-med)]]}]]]]]
                     [:td :th {:padding "0.5rem"}]
                     [:th [:h5 {:opacity (:opacity-med OPACITIES)
                                :user-select "none"}]
                      [:&.sortable
                       [:h5 {:cursor "pointer"}
                        [:&:hover {:opacity 1}]]]
                      [:&.date {:text-align "end"}]]]})


;;; Components

(defn- preview-body [block-children]
  (map (fn [{:keys [db/id block/string]}]
         ^{:key id}
         [:span string])
       block-children))

(def sort-fn
  {:title       (fn [x] (-> x :node/title lower-case))
   :links-count (fn [x] (count (:block/_refs x)))
   :modified    :edit/time
   :created     :create/time})

(defn page
  []
  (let [sorted-by   (r/atom :links-count)
        growing?    (r/atom false)
        flip-order! #(swap! growing? not)
        sort!       (fn [column]
                      (if (= @sorted-by column)
                        (flip-order!)
                        (do (reset! sorted-by column)
                            (reset! growing? false))))
        pages       (->> (d/q '[:find [?e ...]
                                :where
                                [?e :node/title ?t]]
                              @db/dsdb)
                         (p/pull-many db/dsdb '["*" :block/_refs {:block/children [:block/string] :limit 5}])
                         deref)]
    (fn []
      (let [sorted-pages (sort-by (get sort-fn @sorted-by)
                                  (if @growing? compare (comp - compare))
                                  pages)]
        [:div (use-style page-style)
         [:table (use-style table-style)
          [:thead
           [:tr
            [:th {:class "sortable"
                  :on-click #(sort! :title)} [:h5 "Title"]]
            [:th {:class "sortable"
                  :on-click #(sort! :links-count)} [:h5 "Links"]]
            [:th [:h5 "Body"]]
            [:th {:class "sortable date"
                  :on-click #(sort! :modified)} [:h5 "Modified"]]
            [:th {:class "sortable date"
                  :on-click #(sort! :created)} [:h5 "Created"]]]]
          [:tbody
           (doall
            (for [{:keys [block/uid node/title block/children block/_refs]
                   modified :edit/time
                   created :create/time} sorted-pages]
              [:tr {:key uid}
               [:td {:class "title" :on-click #(navigate-uid uid %)} title]
               [:td {:class "links"} (count _refs)]
               [:td {:class "body-preview"} (preview-body children)]
               [:td {:class "date"} (date-string modified)]
               [:td {:class "date"} (date-string created)]]))]]]))))
