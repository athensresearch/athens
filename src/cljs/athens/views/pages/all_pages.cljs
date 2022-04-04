(ns athens.views.pages.all-pages
  (:require
    ["@chakra-ui/react" :refer [Table Thead Tr Th Tbody Td Button Box]]
    ["@material-ui/icons/ArrowDropDown" :default ArrowDropDown]
    ["@material-ui/icons/ArrowDropUp" :default ArrowDropUp]
    [athens.common-db          :as common-db]
    [athens.dates              :as dates]
    [athens.db                 :as db]
    [athens.router             :as router]
    [clojure.string            :refer [lower-case]]
    [re-frame.core             :as rf]))


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


(rf/reg-event-fx
  :all-pages/sort-by
  (fn [{:keys [db]} [_ column-id]]
    (let [sorted-column (get-sorted-by db)
          db'           (if (= column-id sorted-column)
                          (update db :all-pages/sort-order-ascending? not)
                          (-> db
                              (assoc :all-pages/sorted-by column-id)
                              (assoc :all-pages/sort-order-ascending? (= column-id :title))))]
      {:db db'
       :dispatch [:posthog/report-feature :all-pages]})))


;; Components

(defn- sortable-header
  ([column-id label width isNumeric]
   (let [sorted-by @(rf/subscribe [:all-pages/sorted-by])
         growing?  @(rf/subscribe [:all-pages/sort-order-ascending?])]
     [:> Th {:width width :isNumeric isNumeric}
      [:> Button {:onClick #(rf/dispatch [:all-pages/sort-by column-id])
                  :size "sm"
                  :variant "link"}
       (when-not isNumeric label)
       (when (= sorted-by column-id)
         (if growing?
           [:> ArrowDropUp]
           [:> ArrowDropDown]))
       (when isNumeric label)]])))


(defn page
  []
  (let [all-pages (common-db/get-all-pages @db/dsdb)]
    (fn []
      (let [sorted-pages @(rf/subscribe [:all-pages/sorted all-pages])]
        [:> Box {:px 4
                 :margin "calc(var(--app-header-height) + 2rem) auto 5rem"}
         [:> Table {:variant "striped"}
          [:> Thead
           [:> Tr
            [sortable-header :title "Title"]
            [sortable-header :links-count "Links" "12rem" true]
            [sortable-header :modified "Modified" "16rem" false {:date? true}]
            [sortable-header :created "Created" "16rem" false {:date? true}]]]
          [:> Tbody
           (doall
             (for [{:keys    [block/uid node/title block/_refs]
                    modified :edit/time
                    created  :create/time} sorted-pages]
               [:> Tr {:key uid}
                [:> Td {:overflow "hidden"}
                 [:> Button {:variant "link"
                             :justifyContent "flex-start"
                             :textAlign "left"
                             :padding "0"
                             :color "link"
                             :display "block"
                             :maxWidth "100%"
                             :whiteSpace "nowrap"
                             :onClick (fn [e]
                                        (let [shift? (.-shiftKey e)]
                                          (rf/dispatch [:reporting/navigation {:source :all-pages
                                                                               :target :page
                                                                               :pane   (if shift?
                                                                                         :right-pane
                                                                                         :main-pane)}])
                                          (router/navigate-page title e)))}
                  title]]
                [:> Td {:width "12rem" :whiteSpace "nowrap" :color "foreground.secondary" :isNumeric true} (count _refs)]
                [:> Td {:width "16rem" :whiteSpace "nowrap" :color "foreground.secondary"} (dates/date-string modified)]
                [:> Td {:width "16rem" :whiteSpace "nowrap" :color "foreground.secondary"} (dates/date-string created)]]))]]]))))
