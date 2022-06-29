(ns athens.views.query
  (:require
   ["/components/Board/Board" :refer [KanbanBoard]]
   ["/components/KanbanBoard/KanbanBoard" :refer [ExampleKanban ExampleKanban2 #_KanbanBoard]]
   ["/components/Table/Table" :refer [QueryTable]]
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



(defn block-to-flat-map
  [block]
  (let [{:keys [block/uid block/string block/properties]} block
        property-keys (keys properties)
        props-map (reduce (fn [acc prop-key]
                            (assoc acc (keyword prop-key) (get-in properties [prop-key :block/string])))
                          {}
                          property-keys)]
    (merge {:id uid :title string} props-map)))

(defn organize-into-columns
  [tasks]
  (group-by :status tasks))

(defn group-by-swimlane
  [kw columns]
  (into (hash-map)
        (map (fn [[k v]]
               [k (group-by kw v)])
             columns)))


(defn new-card
  [project column]
  (let [parent-of-new-block (:title (dates/get-day)) ;; for now, just create a new block on today's daily notes
        evt (->> (athens.common-events.bfs/internal-representation->atomic-ops
                  @athens.db/dsdb
                  [#:block{:uid    (athens.common.utils/gen-block-uid)
                           :string "Untitled"
                           :properties
                           {"type" #:block{:string "[[athens/task]]"
                                           :uid    (athens.common.utils/gen-block-uid)}
                            "status" #:block{:string column
                                             :uid    (athens.common.utils/gen-block-uid)}
                            "project" #:block{:string project
                                              :uid    (athens.common.utils/gen-block-uid)}}}]
                  {:page/title parent-of-new-block
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


(defn query
  [{:keys [query-data query-layout]}]
  (let []
    (prn query-data)
    [:> Box {:margin-top "40px" :width "100%"}
     (case query-layout
       "board"
       [:> ExampleKanban2 {:boardData (->> query-data
                                           ;; TODO: parameterize group-by's
                                           (group-by :project)
                                           (group-by-swimlane :status))
                           ;; store column order here
                           :columns ["todo" "doing" "done"]
                           :onUpdateStatusClick update-status
                           :onAddNewCardClick new-card
                           :onRenameCard (fn [])
                           :onRenameColumn (fn [])
                           :onClickCard (fn [])
                           :onShiftClickCard (fn [])
                           :onAddNewColumnClick (fn [])
                           :onAddNewProjectClick (fn [])} ]

       [:> QueryTable {:data query-data
                       :columns (keys (first query-data))}])]))


(defn get-query-types
  "Could either be 1-arity (just block/string) or multi-arity (multiple children).

  XXX: to make multi-arity, look at block/children of \"query/types\"
  TODO: what happens if a user enters in multiple refs in a block/string? Should have some sort of schema enforcement, such as 1 ref per block to avoid confusion

  "
  [properties]
  ;; not using :block/string, because i want the node/title without having to do some reg-ex
  ;; (get-in props ["query/types" :block/string])
  (->> (get-in properties ["query/types" :block/refs 0 :db/id])
       (datascript.core/entity @athens.db/dsdb)
       :node/title))

(defn get-query-layout
  [properties]
  (->> (get-in properties ["query/layout" :block/string])))


(defn query-block
  [block-data properties]
  (let [query-types (get-query-types properties)
        ;; TODO: how to handle querying for multiple types?
        query-data (->> (athens.reactive/get-reactive-instances-of-key-value "type" query-types)
                        (map block-to-flat-map))
        query-layout (get-query-layout properties)]

    [:> Box {:width "100%" :border "1px" :borderColor "gray"
             :padding-left 38}
     [query {:query-data query-data
             :query-layout query-layout}]]))
