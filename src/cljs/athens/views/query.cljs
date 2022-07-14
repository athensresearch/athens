(ns athens.views.query
  (:require
   ["/components/KanbanBoard/KanbanBoard" :refer [QueryKanban]]
   ["/components/Table/Table" :refer [QueryTable]]
   ["@chakra-ui/react" :refer [Box
                               Button
                               ButtonGroup
                               ListItem
                               UnorderedList
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
        props-map     (reduce (fn [acc prop-key]
                                (assoc acc (symbol prop-key) (get-in properties [prop-key :block/string])))
                              {}
                              property-keys)
        merged-map     (merge {":block/uid" uid} props-map)]
    merged-map))


(defn organize-into-columns
  [tasks]
  (group-by :status tasks))

(defn nested-group-by
  "You have to pass the first group"
  [kw columns]
  (into (hash-map)
        (map (fn [[k v]]
               [k (group-by kw v)])
             columns)))


(defn group-stuff
  [g sg items]
  (->> items
       (group-by sg)
       (nested-group-by g)))


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

(defn update-layout
  [id new-layout]
  (rf/dispatch [:properties/update-in [:block/uid id] ["query/layout"]
                (fn [db prop-uid]
                  [(graph-ops/build-block-save-op db prop-uid new-layout)])]))

;; (let [query-group-by-kw :status
;;       query-subgroup-by-kw :project
;;       query-data (->> (athens.reactive/get-reactive-instances-of-key-value "type" "athens/task")
;;                       (map block-to-flat-map))]
;;   (if (and query-subgroup-by-kw query-group-by-kw)
;;     (group-stuff query-group-by-kw query-subgroup-by-kw query-data)
;;     (group-stuff query-group-by-kw query-data)))


(defn query
  [{:keys [query-data query-layout property-keys query-group-by query-subgroup-by]}]
  (let []
    [:> Box {#_#_:margin-top "40px" :width "100%"}
     (case query-layout
       #_#_"list"
       [:> UnorderedList
        (for [x query-data]
          [:> ListItem {:display "flex" :justify-content "space-between"}
           [:> Text (:title x)]
           [:> Text (:status x)]])]
       "board"
       (let [query-group-by-kw    (keyword query-group-by)
             query-subgroup-by-kw (keyword query-subgroup-by)
             ;; column headers are not ordered correctly with record fields
             columns              (->> (map query-group-by-kw query-data) set)
             rows                 (->> (map query-subgroup-by-kw query-data) set)

             boardData            (if (and query-subgroup-by-kw query-group-by-kw)
                                    (group-stuff query-group-by-kw query-subgroup-by-kw query-data)
                                    (group-by query-group-by-kw query-data))]
         [:> QueryKanban {:boardData            boardData
                          ;; store column order here
                          :columns              columns
                          :hasSubGroup          (boolean query-subgroup-by-kw)
                          :rows                 rows
                          :onUpdateStatusClick  update-status
                          :onAddNewCardClick    new-card
                          :onRenameCard         (fn [])
                          :onRenameColumn       (fn [])
                          :onClickCard          (fn [])
                          :onShiftClickCard     (fn [])
                          :onAddNewColumnClick  (fn [])
                          :onAddNewProjectClick (fn [])}])

       [:> QueryTable {:data query-data
                       :columns [":block/uid",
                                 ":task/title",
                                 ":task/assignee",
                                 ":task/due-date",
                                 ":task/status",
                                 ":task/priority",
                                 ":task/project"]}])]))


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

(defn options
  [{:keys [query-layout uid]}]
  (let []
    [:> Box
     ;; [:> Heading {:size "sm"} "Layout"]
     [:> ButtonGroup
      (for [x ["table" #_"list" "board"]]
        [:> Button {:value x
                    :onClick (fn [e]
                               (update-layout uid x))
                    :isActive (or (= query-layout x))}
         (clojure.string/capitalize x)])]]))

(defn get-query-group-by
  [properties]
  (->> (get-in properties ["query/group-by" :block/string])))

(defn get-query-subgroup-by
  [properties]
  (->> (get-in properties ["query/subgroup-by" :block/string])))

(defn query-block
  [block-data properties]
  (let [query-types (get-query-types properties)
        query-data (->> (athens.reactive/get-reactive-instances-of-key-value ":block/type" query-types)
                        (map block-to-flat-map))
        query-layout (get-query-layout properties)
        property-keys (keys (first query-data))
        query-group-by (get-query-group-by properties)
        query-subgroup-by (get-query-subgroup-by properties)]

    (cond
      (nil? query-group-by) [:> Box {:color "red"} "Please add property query/group-by"]
      (nil? query-subgroup-by) [:> Box {:color "red"} "Please add property query/subgroup-by"]

      :else [:> Box {:width        "100%" #_#_:border "1px" :borderColor "gray"
                     :padding-left 38 :padding-top 15}
             [options {:query-data    query-data
                       :query-layout  query-layout
                       :property-keys property-keys
                       :uid           (:block/uid block-data)}]
             [query {:query-data        query-data
                     :query-layout      query-layout
                     :property-keys     property-keys
                     :query-group-by    query-group-by
                     :query-subgroup-by query-subgroup-by}]])))
