(ns athens.views.pages.all-pages
  (:require
    ["/components/AllPagesTable/AllPagesTable" :refer [AllPagesTable]]
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
   :modified    :time/modified
   :created     :time/created})


(defn add-modified
  [{:block/keys [create edits] :as page}]
  (assoc page
         :time/modified (->> edits
                             (map (comp :time/ts :event/time))
                             last)
         :time/created (-> create :event/time :time/ts)))


(rf/reg-sub
  :all-pages/sorted
  :<- [:all-pages/sorted-by]
  :<- [:all-pages/sort-order-ascending?]
  (fn [[sorted-by growing?] [_ pages]]
    (->> pages
         (map add-modified)
         (sort-by (get sort-fn sorted-by)
                  (if growing? compare (comp - compare))))))


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


(defn page
  []
  (let [all-pages (common-db/get-all-pages @db/dsdb)]
    (fn []
      (let [sorted-pages @(rf/subscribe [:all-pages/sorted all-pages])]
        [:> AllPagesTable {:sortedPages (clj->js sorted-pages :keyword-fn str)
                           :sortedBy @(rf/subscribe [:all-pages/sorted-by])
                           :dateFormatFn #(dates/date-string %)
                           :sortDirection (if @(rf/subscribe [:all-pages/sort-order-ascending?]) "asc" "desc")
                           :onClickSort #(rf/dispatch [:all-pages/sort-by (cond
                                                                            (= % "title") :title
                                                                            (= % "links-count") :links-count
                                                                            (= % "modified") :modified
                                                                            (= % "created") :created)])
                           :onClickItem (fn [e title]
                                          (let [shift? (.-shiftKey e)]
                                            (rf/dispatch [:reporting/navigation {:source :all-pages
                                                                                 :target :page
                                                                                 :pane   (if shift?
                                                                                           :right-pane
                                                                                           :main-pane)}])
                                            (router/navigate-page title e)))}]))))
