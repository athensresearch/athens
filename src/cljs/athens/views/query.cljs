(ns athens.views.query
  (:require
   ["/components/Query/KanbanBoard" :refer [QueryKanban]]
   ["/components/Query/Table" :refer [QueryTable]]
   ["/components/Query/Query" :refer [Controls]]
   ["@chakra-ui/react" :refer [Box
                               Button
                               ButtonGroup
                               ListItem
                               UnorderedList
                               Stack
                               Text
                               Heading
                               Checkbox
                               CheckboxGroup
                               Menu]]
   [athens.db          :as db]
   [athens.common-db          :as common-db]
   [athens.common-events.graph.ops            :as graph-ops]
   [athens.dates              :as dates]
   [athens.reactive :as reactive]
   [athens.router             :as router]
   [clojure.string            :refer [lower-case]]
   [re-frame.core             :as rf]
   [athens.common-events.bfs :as bfs]
   [athens.common.utils :as utils]
   [reagent.core :as r]))



;; Helpers

(defn get-create-auth-and-time
  [create-event]
  {":create/auth" (get-in create-event [:event/auth :presence/id])
   ":create/time" (get-in create-event [:event/time :time/ts])})

(defn get-last-edit-auth-and-time
  [edit-events]
  (let [last-edit (last edit-events)]
    {":last-edit/auth" (get-in last-edit [:event/auth :presence/id])
     ":last-edit/time" (get-in last-edit [:event/time :time/ts])}))

(defn block-to-flat-map
  [block]
  (let [{:block/keys [uid #_string properties create edits]} block
        create-auth-and-time    (get-create-auth-and-time create)
        last-edit-auth-and-time (get-last-edit-auth-and-time edits)
        property-keys           (keys properties)
        props-map               (reduce (fn [acc prop-key]
                                          (assoc acc prop-key (get-in properties [prop-key :block/string])))
                                        {}
                                        property-keys)
        merged-map              (merge {":block/uid" uid}
                                       props-map
                                       create-auth-and-time
                                       last-edit-auth-and-time)]
    merged-map))


(defn nested-group-by
  "You have to pass the first group"
  [kw columns]
  (into (hash-map)
        (map (fn [[k v]]
               [k (group-by #(get % kw) v)])
             columns)))

(defn group-stuff
  [g sg items]
  (->> items
       (group-by #(get % sg))
       (nested-group-by g)))

(defn context-to-block-properties
  [context]
  (apply hash-map
         (->> context
              (map (fn [[k v]]
                     [k #:block{:string v
                                :uid    (utils/gen-block-uid)}]))

              flatten)))

(defn new-kanban-column
  "This creates a new block/child at the property/values key, but the kanban board doesn't trigger a re-render because it isn't aware of property/values yet."
  [group-by-id]
  (rf/dispatch [:properties/update-in [:node/title group-by-id] [":property/values"]
                 (fn [db prop-uid]
                   [(graph-ops/build-block-new-op db (utils/gen-block-uid) {:block/uid prop-uid :relation :last})])]))


(defn new-card
  "new-card needs to know the context of where it was pressed. For example, pressing it in a given column and swimlane
  would pass along those properties to the new card. Filter conditions would also be passed along. It doesn't matter if
  inherited properties are passed throughu group, subgroup, or filters. It just matters that they are true, and the view should be derived properly.

  context == {:task/status 'todo'
              :task/project '[[Project: ASD]]'"
  [context]
  (let [context             (js->clj context)
        new-block-props     (context-to-block-properties context)
        parent-of-new-block (:title (dates/get-day))        ;; for now, just create a new block on today's daily notes
        evt                 (->> (athens.common-events.bfs/internal-representation->atomic-ops
                                   @athens.db/dsdb
                                   [#:block{:uid        (utils/gen-block-uid)
                                            :string     ""
                                            :properties (merge {":block/type" #:block{:string "[[athens/task]]"
                                                                                      :uid    (utils/gen-block-uid)}
                                                                ":task/title" #:block{:string "Untitled task"
                                                                                      :uid    (utils/gen-block-uid)}}
                                                               new-block-props)}]
                                   {:page/title parent-of-new-block
                                    :relation   :last})
                                 (athens.common-events.graph.composite/make-consequence-op {:op/type :new-type})
                                 athens.common-events/build-atomic-event)]
    (re-frame.core/dispatch [:resolve-transact-forward evt])))

(defn update-status
  ""
  [id new-status]
  (rf/dispatch [:properties/update-in [:block/uid id] [":task/status"]
                (fn [db prop-uid]
                  [(graph-ops/build-block-save-op db prop-uid new-status)])]))

(defn update-layout
  [id new-layout]
  (rf/dispatch [:properties/update-in [:block/uid id] ["query/layout"]
                (fn [db prop-uid]
                  [(graph-ops/build-block-save-op db prop-uid new-layout)])]))

(defn update-properties
  [db key value new-value]
  (->> (common-db/get-instances-of-key-value db key value)
       (map #(get-in % [key :block/uid]))
       (map (fn [uid]
              (graph-ops/build-block-save-op db uid new-value)))))


(defn update-kanban-column
  "Update the property page that is the source of values for a property.
  Also update all the blocks that are using that property."
  [property-key property-value new-value]
  (rf/dispatch [:properties/update-in [:node/title property-key] [":property/values"]
                (fn [db prop-uid]
                  (let [{:block/keys [children]} (common-db/get-block-document db [:block/uid prop-uid])
                        update-uid (->> children
                                        (map (fn [{:block/keys [string uid]}] [string uid]))
                                        (filter #(= (first %) property-value))
                                        (first)
                                        second)
                        ;; update all blocks that match key:value to key:new-value
                        update-ops (update-properties db property-key property-value new-value)]

                    (vec (concat [(graph-ops/build-block-save-op db update-uid new-value)]
                                 update-ops))))]))

(defn update-task-title
  [id new-title]
  (rf/dispatch [:properties/update-in [:block/uid id] [":task/title"]
                (fn [db prop-uid]
                  [(graph-ops/build-block-save-op db prop-uid new-title)])]))


(defn toggle-hidden-property
  "If property is hidden, remove key. Otherwise, add property key."
  [id hidden-property-id]
  (rf/dispatch [:properties/update-in [:block/uid id] ["query/properties-hide" hidden-property-id]
                (fn [db hidden-prop-uid]
                  (let [property-hidden? (common-db/block-exists? db [:block/uid hidden-prop-uid])]
                    [(if property-hidden?
                       (graph-ops/build-block-remove-op @db/dsdb hidden-prop-uid)
                       (graph-ops/build-block-save-op db hidden-prop-uid ""))]))]))



(defn flip-sort-dir
  [sort-dir]
  (if (= sort-dir "asc")
    "desc"
    "asc"))

(defn update-sort-by
  [id curr-sort-by curr-sort-dir new-sort-by]
  (if (= curr-sort-by new-sort-by)
    (rf/dispatch [:properties/update-in [:block/uid id] ["query/sort-direction"]
                  (fn [db prop-uid]
                    [(graph-ops/build-block-save-op db prop-uid (flip-sort-dir curr-sort-dir))])])
    (do
      (rf/dispatch [:properties/update-in [:block/uid id] ["query/sort-direction"]
                     (fn [db prop-uid] [(graph-ops/build-block-save-op db prop-uid "desc")])])
      (rf/dispatch [:properties/update-in [:block/uid id] ["query/sort-by"]
                    (fn [db prop-uid]
                      [(graph-ops/build-block-save-op db prop-uid new-sort-by)])]))))


#_(defn get-prop-node-title
   "Could either be 1-arity (just block/string) or multi-arity (multiple children).

   XXX: to make multi-arity, look at block/children of \"query/types\"
   TODO: what happens if a user enters in multiple refs in a block/string? Should have some sort of schema enforcement, such as 1 ref per block to avoid confusion
   not using :block/string, because i want the node/title without having to do some reg-ex"
   [prop]
   (->> (get-in prop [#_"query/types" :block/refs 0 :db/id])
        (datascript.core/entity @athens.db/dsdb)
        :node/title))

(defn order-children
  [children]
  (->> (sort-by :block/order children)
       (mapv :block/string)))

(defn get-query-props
  [properties]
  (->> properties
       (map (fn [[k {:block/keys [children string] nested-properties :block/properties :as v}]]
              [k (cond
                   (and (seq children) (not (clojure.string/blank? string))) {:key string :values (order-children children)}
                   (seq children) (order-children children)
                   nested-properties  (zipmap (keys nested-properties) (repeat true))
                   :else string)]))
       (into (hash-map))))

(defn get-reactive-property
  [eid property-key]
  (let [property-page (reactive/get-reactive-block-document eid)
        property (get-in property-page [:block/properties property-key])
        {:block/keys [children properties sting]} property]
    (cond
      (seq children) (order-children children)
      (seq properties) (keys properties))))

(defn sort-dir-fn
  [query-sort-direction]
  (if (= query-sort-direction "asc")
    compare
    (comp - compare)))

(defn sort-table
  [query-data query-sort-by query-sort-direction]
  (->> query-data
       (sort-by #(get % query-sort-by)
                (sort-dir-fn query-sort-direction))))

;; (def data (atom nil))

#_(defn save-view
   [properties]
   (reset! data properties)
   (prn properties))


#_(let [data @data]
   (for [[k v] data]
     [k (:block/string v) (:block/children v)]))


(defn save-query
  [db block-uid position title description priority creator assignee due-date status project-relation]
  (->> (bfs/internal-representation->atomic-ops
        db
        [#:block{:uid        (utils/gen-block-uid)
                 :string     ""
                 :properties {":block/type"
                              #:block{:string "athens/task"
                                      :uid    (utils/gen-block-uid)}
                              ":task/title"
                              #:block{:string title
                                      :uid    (utils/gen-block-uid)}
                              ":task/description"
                              #:block{:string description
                                      :uid    (utils/gen-block-uid)}
                              ":task/priority"
                              #:block{:string priority
                                      :uid    (utils/gen-block-uid)}
                              ":task/creator"
                              #:block{:string creator
                                      :uid    (utils/gen-block-uid)}
                              ":task/assignee"
                              #:block{:string assignee
                                      :uid    (utils/gen-block-uid)}
                              ":task/due-date"
                              #:block{:string due-date
                                      :uid    (utils/gen-block-uid)}
                              ":task/status"
                              #:block{:string status
                                      :uid    (utils/gen-block-uid)}
                              ":comment/project-relation"
                              #:block{:string project-relation
                                      :uid    (utils/gen-block-uid)}}}]
        {:block/uid block-uid
         :relation  position})
       (athens.common-events.graph.composite/make-consequence-op {:op/type :new-type})))


;; one difference between Notion and Athens queries is that we could have different inputs to the query
;; but i don't think we should have different views over different types for one query. that could get confusing
;; views should only deal with the middle and output, not inputs. because we want different views over the same DATA
;; and different types lead to different queries

;; i could either use the block/properties and re-add new block/uids (but would i have to strip the other meta-datas like edit/time)
;; or map the parsed-properties back to the block/strings and such

;; TODO: now save all these properties on another property
;; is there an analog for `merge` such as we have with `:properties/update-in`?

;; TODO: on clicking a different saved view, update all the current values

#_(let [d {"query/properties-order" [":task/title" ":task/status" ":task/assignee" ":task/due-date" ":block/uid" ":task/project" ":task/priority" ":create/auth" ":create/time" ":last-edit/auth" ":last-edit/time"], "query/types" "[[athens/task]]", "query/layout" "board", "query/subgroup-by" ":task/project", "query/group-by" ":task/status", ":block/type" "[[athens/query]]", "query/group-by-columns" ["todo" "doing" "done"], "query/sort-by" ":create/time", "query/properties-hide" {":create/auth" true, ":last-edit/auth" true, ":last-edit/time" true, ":task/priority" true, ":block/uid" true, ":task/due-date" true, ":task/assignee" true}, "query/sort-direction" "asc"}]
   (->> (map (fn [[k v]]
               [k
                (merge
                 {:block/uid (utils/gen-block-uid)}
                 (cond
                   (vector? v) {:block/children (mapv (fn [x] {:block/uid (utils/gen-block-uid)
                                                               :block/string x}) v)}
                   (map? v) {:block/properties (zipmap (map (fn [[k v]] k) v)
                                                       (repeatedly (fn [] {:block/uid (utils/gen-block-uid)
                                                                           :block/string ""})))}
                   :else
                   {:block/string v}))])
             d)
        flatten
        (apply hash-map)))


;; Views

(defn options-el
  [{:keys [properties parsed-properties uid]}]
  (let [query-layout           (get parsed-properties "query/layout")
        query-properties-order (get parsed-properties "query/properties-order")
        query-properties-hide  (get parsed-properties "query/properties-hide")]
    [:> Stack {:direction "row" :spacing 5}
     [:> ButtonGroup
      (for [x ["table" "board"]]
        [:> Button {:value    x
                    :onClick  (fn [e]
                                (update-layout uid x))
                    :isActive (or (= query-layout x))}
         (clojure.string/capitalize x)])]

     [:> Controls {:isCheckedFn          #(get query-properties-hide %)
                   :properties           query-properties-order
                   :hiddenProperties     query-properties-hide
                   :menuOptionGroupValue (keys query-properties-hide)
                   :onChange             #(toggle-hidden-property uid %)}]
     [:> Button {:onClick #(prn parsed-properties)}
      [:> Heading {:size "sm"} "Save View"]]]))

(defn query-el
  [{:keys [query-data parsed-properties uid]}]
  (let [query-layout           (get parsed-properties "query/layout")
        query-properties-order (get parsed-properties "query/properties-order")
        query-sort-by          (get parsed-properties "query/sort-by")
        query-sort-direction   (get parsed-properties "query/sort-direction")
        query-properties-hide  (get parsed-properties "query/properties-hide")]

    [:> Box {#_#_:margin-top "40px" :width "100%"}
     (case query-layout
       "board"
       (let [query-group-by    (get parsed-properties "query/group-by")
             query-subgroup-by (get parsed-properties "query/subgroup-by")
             columns           (get-reactive-property [:node/title ":task/status"] ":property/values")
             rows              (->> (map #(get % query-subgroup-by) query-data) set)
             boardData         (if (and query-subgroup-by query-group-by)
                                 (group-stuff query-group-by query-subgroup-by query-data)
                                 (group-by query-group-by query-data))]
         [:> QueryKanban {:boardData            boardData
                          ;; store column order here
                            :columns              columns
                            :hasSubGroup          (boolean query-subgroup-by)
                            :rows                 rows
                            :onUpdateStatusClick  update-status
                            :hideProperties       query-properties-hide
                            :onAddNewCardClick    new-card
                            :groupBy              query-group-by
                            :subgroupBy           query-subgroup-by
                            :filter               nil
                            :onClickCard          #(rf/dispatch [:right-sidebar/open-item %])
                            :onUpdateTaskTitle    update-task-title
                            :onUpdateKanbanColumn update-kanban-column
                            :onAddNewColumn  #(new-kanban-column query-group-by)
                            :onAddNewProjectClick (fn [])}])
       (let [sorted-data (sort-table query-data query-sort-by query-sort-direction)]
         [:> QueryTable {:data           sorted-data
                         :columns        query-properties-order
                         :onClickSort    #(update-sort-by uid query-sort-by query-sort-direction %)
                         :sortBy         query-sort-by
                         :sortDirection  query-sort-direction
                         :hideProperties query-properties-hide
                         :dateFormatFn   #(dates/date-string %)}]))]))

(defn parse-for-title
  "should be able to pass in a plain string, a wikilink, or both?"
  [s]
  (let [re #"\[\[(.*)\]\]"]
    (cond
      (re-find re s) (second (re-find re s))
      (clojure.string/blank? s) (throw "parse-for-title got an empty string")
      :else s)))

(defn get-schema
  [query-types]
  (let [base-schema [":block/uid" ":create/auth" ":create/time" ":last-edit/auth" ":last-edit/time"]]
    (if (string? query-types)
      (get-schema [query-types])
      (->> query-types
           (map (fn [title]
                  (get-reactive-property [:node/title (parse-for-title title)] ":entity.type/schema")))
           flatten
           (concat base-schema)))))

#_(concat [1 2] [3 4])

(get-schema ["athens/task"])
(get-schema ["[[athens/task]]"])

;; (re-find #"\[\[(.+)\]\]" "a")

(parse-for-title "[[athens/task]]")

(defn update-query-types-hook
  [prev curr state]
  (prn prev curr)
  (when (not= prev curr)
    (prn "UPDATE")
    (reset! state curr)))


(defn query-block
  [block-data properties]
  (let [last-query-types (r/atom nil)]
    (fn [block-data properties]
      (let [block-uid (:block/uid block-data)
            parsed-properties (get-query-props properties)
            query-types (get parsed-properties "query/types")
            schema (get-schema query-types)
            query-group-by (get properties "query/group-by")
            query-subgroup-by (get properties "query/subgroup-by")
            query-data (->> (reactive/get-reactive-instances-of-key-value ":block/type" query-types)
                            (map block-to-flat-map))]
        
        (update-query-types-hook @last-query-types query-types last-query-types)
        
        (cond
          (nil? query-group-by) [:> Box {:color "red"} "Please add property query/group-by"]
          (nil? query-subgroup-by) [:> Box {:color "red"} "Please add property query/subgroup-by"]

          :else [:> Box {:width        "100%" #_#_:border "1px" :borderColor "gray"
                         :padding-left 38 :padding-top 15}
                 [options-el {:parsed-properties parsed-properties
                              :properties properties
                              :query-data        query-data
                              :uid               block-uid}]
                 [query-el {:query-data        query-data
                            :uid               block-uid
                            :parsed-properties parsed-properties}]])))))

