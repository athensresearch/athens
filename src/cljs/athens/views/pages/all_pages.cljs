(ns athens.views.pages.all-pages
  (:require
    ["/components/AllPagesTable/AllPagesTable" :refer [AllPagesTable]]
    ["@chakra-ui/react" :refer [Table Thead Tbody Tfoot Tr Th Td TableContainer
                                Box
                                Button
                                Stack
                                Checkbox CheckboxGroup]]
    [athens.common-db          :as common-db]
    [athens.dates              :as dates]
    [athens.db                 :as db]
    [athens.router             :as router]
    [clojure.string            :refer [lower-case]]
    [re-frame.core             :as rf]
    [reagent.core :as r]))


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


;; types
    ;; everything
    ;; pages
    ;; blocks
    ;; daily notes
    ;; URLs
    ;; emojis
    ;; tweets
;; properties
    ;; title
    ;; string
    ;; creation time
;; view
    ;;  table
    ;;  gallery
    ;;  outliner
;;  Options


(def entities
  {:pages {:title "Pages"
           :query-fn #()}})

(athens.dates.is-daily-note "01-01-1996")

(defn page
  []
  (let [
        get-pages-fn      (fn []
                            (->>
                             (common-db/get-all-pages @db/dsdb)
                             (filter #(not (athens.dates.is-daily-note (:block/uid %))))))
        get-blocks-fn     #(common-db/get-all-blocks @db/dsdb)
        get-daily-notes-fn     (fn []
                                 (->> (common-db/get-all-pages  @db/dsdb)
                                      (filter #(athens.dates.is-daily-note (:block/uid %)))))
        get-everything-fn #(concat (get-pages-fn) (get-blocks-fn))

        checked-types     (r/atom {:pages false
                                   :blocks false
                                   :daily-notes false})
        page-columns      ["Title" "Links Count" "Modified" "Created"]
        block-columns     ["String" "Links Count" "Modified" "Created"]
        all-columns (set (concat block-columns page-columns))
        properties        (r/atom [])
        ]

    (fn []
      (let [count-checked (-> @checked-types vals frequencies (get true))
            all-checked             (and (pos? count-checked)
                                     (= (count @checked-types) count-checked))
            handle-click-everything (fn []
                                      (if all-checked
                                        (reset! checked-types {:pages false :blocks false :daily-notes false})
                                        (reset! checked-types {:pages true :blocks true :daily-notes true})))
            entities          (cond-> []
                                (:pages @checked-types) (concat (get-pages-fn))
                                (:daily-notes @checked-types) (concat (get-daily-notes-fn))
                                (:blocks @checked-types) (concat (get-blocks-fn)))]

        [:> Box {:margin-top "40px"}
         #_[:h1 (str @entities)]
         [:> Box

          [:> Stack {:spacing 5 :direction "row" :colorScheme "blue"}
           [:> Checkbox {:is-checked all-checked :on-change handle-click-everything} "Everything"]
           [:> Checkbox {:is-checked (:pages @checked-types)
                         :on-change  (fn [e]
                                       (swap! checked-types assoc :pages (.. e -target -checked))
                                       (reset! properties page-columns)
                                       )}
            "Pages"]
           [:> Checkbox {:is-checked (:daily-notes @checked-types)
                         :on-change  (fn [e]
                                       (swap! checked-types assoc :daily-notes (.. e -target -checked))
                                       (reset! properties page-columns))}
            "Daily Notes"]
           [:> Checkbox {:is-checked (:blocks @checked-types)
                         :on-change  (fn [e]
                                       (swap! checked-types assoc :blocks (.. e -target -checked))
                                       (reset! properties block-columns))}
            "Blocks"]]

          #_[:> Stack {:spacing 5 :direction "row" :colorScheme "blue"}
           [:> Checkbox {:is-checked all-checked :on-change handle-click-everything} "Everything"]
           [:> Checkbox {:is-checked (:pages @checked-types)
                         :on-change  (fn [e]
                                      (swap! checked-types assoc :pages (.. e -target -checked))
                                      (reset! properties page-columns)
                                      (reset! entities (get-pages-fn)))}
            "Pages"]
           [:> Checkbox {:is-checked (:blocks @checked-types)
                         :on-change  (fn [e]
                                      (swap! checked-types assoc :blocks (.. e -target -checked))
                                      (reset! properties block-columns)
                                      (reset! entities (get-blocks-fn)))}"Blocks"]]]

         ;; how do we dynamically show what tables
         [:> TableContainer
          [:> Table {:variant "striped"}
           [:> Thead
            [:> Tr
             (for [p @properties]
               [:> Th {:key p} p])]]
           [:> Tbody
            (for [{:keys [:node/title :block/_refs :block/string]
                   create-time :create/time edit-time :edit/time} entities]
              [:> Tr
               [:> Td title]
               ;; [:> Td string]
               [:> Td (count _refs)]
               [:> Td  create-time]
               [:> Td  edit-time]]
              )]]]]))))

[:> TableContainer
 [:> Table
  [:> Thead
   [:> Tr
    [:> Td]]]
  [:> Tbody]
  [:> Tfoot]]]


#_(defn page
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
