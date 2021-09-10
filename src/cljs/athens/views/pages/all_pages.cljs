(ns athens.views.pages.all-pages
  (:require
    ["@material-ui/icons/ArrowDropDown" :default ArrowDropDown]
    ["@material-ui/icons/ArrowDropUp" :default ArrowDropUp]
    [athens.common-db :as common-db]
    [athens.db :as db]
    [athens.router :refer [navigate-uid]]
    [athens.style :as style :refer [color OPACITIES]]
    [athens.util :refer [date-string]]
    [clojure.string :refer [lower-case]]
    [garden.selectors :as selectors]
    [re-frame.core :as rf :refer [dispatch subscribe]]
    [stylefy.core :as stylefy :refer [use-style]]))


;; Styles


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
                     [:th {:opacity (:opacity-med OPACITIES)
                           :user-select "none"}
                      [:&.sortable {:cursor "pointer"}
                       [:.wrap-label {:display "flex"
                                      :align-items "center"}]
                       [:&.date
                        [:.wrap-label {:flex-direction "row-reverse"}]]
                       [:&:hover {:opacity 1}]]]]})


;; Sort state and logic

(defn- get-sorted-by
  [db]
  (get db :all-pages/sorted-by :links-count))


(rf/reg-sub
  :all-pages/sorted-by
  (fn [db _]
    (get-sorted-by db)))


(rf/reg-sub
  :all-pages/sort-order-ascending?
  (fn [db _]
    (get db :all-pages/sort-order-ascending? false)))


(def sort-fn
  {:title       (fn [x] (-> x :node/title lower-case))
   :links-count (fn [x] (count (:block/_refs x)))
   :modified    :edit/time
   :created     :create/time})


(rf/reg-sub
  :all-pages/sorted
  :<- [:all-pages/sorted-by]
  :<- [:all-pages/sort-order-ascending?]
  (fn [[sorted-by growing?] [_ pages]]
    (sort-by (get sort-fn sorted-by)
             (if growing? compare (comp - compare))
             pages)))


(rf/reg-event-db
  :all-pages/sort-by
  (fn [db [_ column-id]]
    (let [sorted-column (get-sorted-by db)]
      (if (= column-id sorted-column)
        (update db :all-pages/sort-order-ascending? not)
        (-> db
            (assoc :all-pages/sorted-by column-id)
            (assoc :all-pages/sort-order-ascending? (= column-id :title)))))))


;; Components

(defn- sortable-header
  ([column-id label]
   (sortable-header column-id label {:date? false}))
  ([column-id label {:keys [date?]}]
   (let [sorted-by @(subscribe [:all-pages/sorted-by])
         growing?  @(subscribe [:all-pages/sort-order-ascending?])]
     [:th {:on-click #(dispatch [:all-pages/sort-by column-id])
           :class ["sortable" (when date? "date")]}
      [:div.wrap-label
       [:h5 label]
       (when (= sorted-by column-id)
         (if growing? [:> ArrowDropUp] [:> ArrowDropDown]))]])))


(defn page
  []
  (let [all-pages (common-db/get-all-pages @db/dsdb)]
    (fn []
      (let [sorted-pages @(subscribe [:all-pages/sorted all-pages])]
        [:div (use-style page-style)
         [:table (use-style table-style)
          [:thead
           [:tr
            [sortable-header :title "Title"]
            [sortable-header :links-count "Links"]
            [sortable-header :modified "Modified" {:date? true}]
            [sortable-header :created "Created" {:date? true}]]]
          [:tbody
           (doall
             (for [{:keys [block/uid node/title block/_refs]
                    modified :edit/time
                    created :create/time} sorted-pages]
               [:tr {:key uid}
                [:td {:class "title" :on-click #(navigate-uid uid %)} title]
                [:td {:class "links"} (count _refs)]
                [:td {:class "date"} (date-string modified)]
                [:td {:class "date"} (date-string created)]]))]]]))))
