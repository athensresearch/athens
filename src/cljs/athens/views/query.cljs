(ns athens.views.query
  (:require
   ["/components/Board/Board" :refer [KanbanBoard]]
   ["/components/KanbanBoard/KanbanBoard" :refer [ExampleKanban ExampleKanban2 #_KanbanBoard]]
   ["@chakra-ui/react" :refer [Box
                               Button
                               Stack
                               Text
                               Heading]]
   [athens.common-db          :as common-db]
   [athens.common-events.graph.ops            :as graph-ops]
   [athens.dates              :as dates]
   [athens.db                 :as db]
   [athens.router             :as router]
   [clojure.string            :refer [lower-case]]
   [re-frame.core             :as rf]
   [reagent.core :as r]))



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


;; (def tmp-data
;;   (let [entity-type "[[athens/task]]"
;;         columns :project
;;         swimlanes :status]
;;     (->> (common-db/get-all-blocks-of-type @athens.db/dsdb entity-type)
;;          blocks-to-tasks
;;          (group-by :project)
;;          (group-by-swimlane :status))))

;; (athens.reactive/get-reactive-instances-of-key-value "type" "athens/task")

(defn query
  [query-data]
  (let [organized-data (->> query-data
                            blocks-to-tasks
                            (group-by :project)
                            (group-by-swimlane :status))]
    [:> Box {:margin-top "40px" :width "100%"}
     [:> ExampleKanban2 {:boardData organized-data
                         ;; store column order here
                         :columns ["todo" "doing" "done"]
                         :onUpdateStatusClick update-status
                         :onAddNewCardClick new-card
                         :onRenameCard (fn [])
                         :onRenameColumn (fn [])
                         :onClickCard (fn [])
                         :onShiftClickCard (fn [])
                         :onAddNewColumnClick (fn [])
                         :onAddNewProjectClick (fn [])} ]]))
