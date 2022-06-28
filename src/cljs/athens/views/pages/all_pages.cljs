(ns athens.views.pages.all-pages
  (:require
    ["/components/AllPagesTable/AllPagesTable" :refer [AllPagesTable]]
    ["/components/Board/Board" :refer [KanbanBoard]]
    ["/components/KanbanBoard/KanbanBoard" :refer [ExampleKanban ExampleKanban2 #_KanbanBoard]]
    ["@chakra-ui/react" :refer [Table Thead Tbody Tfoot Tr Th Td TableContainer
                                Box
                                Button
                                Stack
                                Text
                                Heading
                                Radio RadioGroup
                                SimpleGrid
                                Checkbox CheckboxGroup]]
    ["react-twitter-embed" :refer [TwitterTweetEmbed]]
    [athens.common-db          :as common-db]
    [athens.common-events.graph.ops            :as graph-ops]
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


(defn reshape-block-into-task
  [block]
  (let [{:keys [block/uid block/string block/properties]} block
        {:strs [status assignee project]} properties
        assignee-str (:block/string assignee)
        status-str (:block/string status)
        project-str (:block/string project)]
    {:id uid :title string :status status-str :assignee assignee-str :project project-str}))

(defn organize-into-columns
  [tasks]
  (group-by :status tasks))

(defn blocks-to-columns
  [blocks]
  (->> (map reshape-block-into-task blocks)
       organize-into-columns))

(defn blocks-to-tasks
  [blocks]
  (map reshape-block-into-task blocks))


(defn group-by-swimlane
  [kw columns]
  (into (hash-map)
        (map (fn [[k v]]
               [k (group-by kw v)])
             columns)))


(defn new-card
  [project column]
  (let [evt (->> (athens.common-events.bfs/internal-representation->atomic-ops
                  @athens.db/dsdb
                  [#:block{:uid    (athens.common.utils/gen-block-uid)
                           :string "Untitled"
                           :properties
                           {"type" #:block{:string "[[athens/task]]"
                                           :uid    (athens.common.utils/gen-block-uid)}
                            "status" #:block{:string column
                                             :uid    (athens.common.utils/gen-block-uid)}
                            "project" #:block{:string project
                                              :uid    (athens.common.utils/gen-block-uid)} }
                           }]
                  {#_#_:block/uid "49bdef200"
                   :page/title "June 25, 2022"
                   :relation  :last})
                 (athens.common-events.graph.composite/make-consequence-op {:op/type :new-type})
                 athens.common-events/build-atomic-event)]
    (re-frame.core/dispatch [:resolve-transact-forward evt])))


(defn update-status
  ""
  [id new-status]
  (rf/dispatch [:properties/update-in [:block/uid id] ["status"]
                (fn [db prop-uid]
                 [(graph-ops/build-block-save-op db prop-uid new-status)])]))

;; {:tasks {:title "Tasks"
;;          :query-fn #(->> (common-db/get-all-blocks-of-type @db/dsdb "[[athens/task]]"))}}

;; needs to react when query updates
;; needs to update when children update

(def tmp-data
  (let [entity-type "[[athens/task]]"
        columns :project
        swimlanes :status]
    (->> (common-db/get-all-blocks-of-type @athens.db/dsdb entity-type)
         blocks-to-tasks
         (group-by :project)
         (group-by-swimlane :status))))

(common-db/get-all-blocks-of-type @db/dsdb "[[athens-task]]")


(defn page
  []
  (let []
    (fn []
      (let []
        [:> Box {:margin-top "40px" :width "100%"}
         [:> ExampleKanban2 {:boardData tmp-data
                             ;; store column order here
                             :columns [ "done" "todo" "doing"]
                             :onUpdateStatusClick update-status
                             :onAddNewCardClick new-card
                             :onAddNewColumnClick (fn [])
                             :onAddNewProjectClick (fn [])} ]]))))


;; (defn page
;;   []
;;   (let [all-pages (common-db/get-all-pages @db/dsdb)]
;;     (fn []
;;       (let [sorted-pages @(rf/subscribe [:all-pages/sorted all-pages])]
;;         [:> AllPagesTable {:sortedPages (clj->js sorted-pages :keyword-fn str)
;;                            :sortedBy @(rf/subscribe [:all-pages/sorted-by])
;;                            :dateFormatFn #(dates/date-string %)
;;                            :sortDirection (if @(rf/subscribe [:all-pages/sort-order-ascending?]) "asc" "desc")
;;                            :onClickSort #(rf/dispatch [:all-pages/sort-by (cond
;;                                                                             (= % "title") :title
;;                                                                             (= % "links-count") :links-count
;;                                                                             (= % "modified") :modified
;;                                                                             (= % "created") :created)])
;;                            :onClickItem (fn [e title]
;;                                           (let [shift? (.-shiftKey e)]
;;                                             (rf/dispatch [:reporting/navigation {:source :all-pages
;;                                                                                  :target :page
;;                                                                                  :pane   (if shift?
;;                                                                                            :right-pane
;;                                                                                            :main-pane)}])
;;                                             (router/navigate-page title e)))}]))))
