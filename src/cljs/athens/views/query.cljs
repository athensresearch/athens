(ns athens.views.query
  (:require
   ["/components/Query/KanbanBoard" :refer [QueryKanban]]
   [com.rpl.specter :as s]
   ["/components/Query/Table" :refer [QueryTable]]
   ["/components/Query/Query" :refer [Controls QueryRadioMenu]]
   ["/components/References/References" :refer [ReferenceBlock ReferenceGroup]]
   ["@chakra-ui/react" :refer [Box
                               Button
                               HStack
                               VStack
                               Toggle
                               Breadcrumb
                               BreadcrumbItem
                               BreadcrumbLink
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
   [athens.common-events.graph.composite :as composite]
   [athens.dates              :as dates]
   [athens.reactive :as reactive]
   [athens.router             :as router]
   [clojure.string            :refer [lower-case]]
   [re-frame.core             :as rf]
   [athens.common-events.bfs :as bfs]
   [athens.parse-renderer :as parse-renderer]
   ;;[athens.views.pages.node-page :as node-page]
   [athens.util :as util]
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
                                 (composite/make-consequence-op {:op/type :new-type})
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

(defn update-entity-type
  [uid new-type]
  (rf/dispatch [:properties/update-in [:block/uid uid] ["query/type"]
                (fn [db prop-uid]
                  [(graph-ops/build-block-save-op db prop-uid new-type)])]))

(defn update-filter-author
  [uid author]
  (rf/dispatch [:properties/update-in [:block/uid uid] ["query/filter-author"]
                (fn [db prop-uid]
                  [(graph-ops/build-block-save-op db prop-uid author)])]))

(defn update-special-filters
  [uid special-filter]
  (rf/dispatch [:properties/update-in [:block/uid uid] ["query/special-filters"]
                (fn [db prop-uid]
                  [(graph-ops/build-block-save-op db prop-uid special-filter)])]))

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
  [id new-sort-by]
  (rf/dispatch [:properties/update-in [:block/uid id] ["query/sort-direction"]
                (fn [db prop-uid] [(graph-ops/build-block-save-op db prop-uid "desc")])])
  (rf/dispatch [:properties/update-in [:block/uid id] ["query/sort-by"]
                (fn [db prop-uid] [(graph-ops/build-block-save-op db prop-uid new-sort-by)])]))

(defn update-sort-direction
  [id new-sort-dir]
  (rf/dispatch [:properties/update-in [:block/uid id] ["query/sort-direction"]
                (fn [db prop-uid] [(graph-ops/build-block-save-op db prop-uid new-sort-dir)])]))


(defn order-children
  [children]
  (->> (sort-by :block/order children)
       (mapv :block/string)))

(def DEFAULT-PROPS
  {"query/layout" "table"
   ":entity/type" "[[athens/query]]"
   "query/type" "[[athens/comment-thread]]"
   "query/filter-author" "None"
   "query/special-filters" "None"
   "query/sort-by" ":create/time"
   "query/sort-direction" "desc"})


(defn get-query-props
  [properties]
  (->> properties
       (reduce-kv
        (fn [acc k {:block/keys [children string] nested-properties :block/properties :as v}]
          (assoc acc k (cond
                        (and (seq children) (not (clojure.string/blank? string))) {:key string :values (order-children children)}
                        (seq children) (order-children children)
                        nested-properties  (reduce-kv (fn [acc k v]
                                                        (assoc acc k (:block/string v)))
                                                      {}
                                                      nested-properties)
                        :else string)))
        {})
       (merge DEFAULT-PROPS)))
    

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

(defn parse-for-title
  "should be able to pass in a plain string, a wikilink, or both?"
  [s]
  (when (seq s)
    (let [re #"\[\[(.*)\]\]"]
      (cond
        (re-find re s) (second (re-find re s))
        (clojure.string/blank? s) (throw "parse-for-title got an empty string")
        :else s))))


(defn str-to-title
  [s]
  (str "[[" s "]]"))


(def base-schema
  [":block/uid" ":create/auth" ":create/time" ":last-edit/auth" ":last-edit/time"])


(def SCHEMA
  {"[[athens/task]]" (concat base-schema ["title" "status" "assignee" "project" "due date"])
   "[[athens/comment-thread]]" (concat base-schema [])})

(defn get-schema
  [k]
  (or (get SCHEMA k) base-schema))

(def entity-types
  (keys SCHEMA))

(def AUTHORS
  ["None" "Sid" "Jeff" "Stuart" "Filipe" "Alex"])

(def LAYOUTS
  ["table" "board" "list"])

;; Views

(defn get-merged-breadcrumbs
  [uids]
  (->> uids
       (map #(datascript.core/entity @db/dsdb [:block/uid %]))
       (mapv :db/id)
       (db/eids->groups)
       vec))


(defn ref-comp
  [block]
  (let [
        state           (r/atom {:block    (db/get-comment-threads-for-query @db/dsdb (:block/uid block))
                                 :embed-id (random-uuid)
                                 :parents  (rest (:block/parents block))})
        linked-ref-data {:linked-ref     true
                         :initial-open   true
                         :linked-ref-uid (:block/uid block)
                         :parent-uids    (set (map :block/uid (:block/parents block)))}]
    (fn [_]
      (let [{:keys [block parents embed-id]} @state]
        [:> VStack {:spacing 0
                    :align "stretch"}
         [:> Breadcrumb {:fontSize "sm"
                         :variant "strict"
                         :color "foreground.secondary"}
          (doall
            (for [{:keys [block/uid]} parents]
              [:> BreadcrumbItem {:key (str "breadcrumb-" uid)}
               [:> BreadcrumbLink
                {:onClick #(let [new-B (db/get-block [:block/uid uid])
                                 new-P (drop-last parents)]
                             (swap! state assoc :block new-B :parents new-P))}
                [parse-renderer/parse-and-render (common-db/breadcrumb-string @db/dsdb uid) uid]]]))]
         [:> Box {:class "block-embed"}
          [athens.views.blocks.core/block-el
           (util/recursively-modify-block-for-embed block embed-id)
           linked-ref-data
           {:block-embed? true}]]]))))

(defn breadcrumbs-el
  [linked-refs]
  (when (seq linked-refs)
    (doall
      [:div
       (for [[group-title group] linked-refs]
         [:> ReferenceGroup {:key          (str "group-" group-title)
                             :title        group-title
                             :onClickTitle (fn [e]
                                             (let [shift?       (.-shiftKey e)
                                                   parsed-title (athens.parse-renderer/parse-title group-title)]
                                               (rf/dispatch [:reporting/navigation {:source :block-page-linked-refs
                                                                                    :target :page
                                                                                    :pane   (if shift?
                                                                                              :right-pane
                                                                                              :main-pane)}])
                                               (router/navigate-page parsed-title)))}
          (doall
            (for [block group]
              [:> ReferenceBlock {:key (str "ref-" (:block/uid block))}
               [ref-comp block]]))])])))

(defn options-el
  [{:keys [properties parsed-properties uid schema]}]
  (let [query-layout           (get parsed-properties "query/layout")
        query-type             (get parsed-properties "query/type")
        query-properties-order (get parsed-properties "query/properties-order")
        query-properties-hide  (get parsed-properties "query/properties-hide")
        query-filter-author    (get parsed-properties "query/filter-author")
        query-special-filters  (get parsed-properties "query/special-filters")
        query-sort-by          (get parsed-properties "query/sort-by")
        query-sort-by          (parse-for-title query-sort-by)
        query-sort-direction   (get parsed-properties "query/sort-direction")
        query-properties-hide (if (clojure.string/blank? query-properties-hide)
                                {}
                                query-properties-hide)
        menuOptionGroupValue (keys query-properties-hide)
        menuOptionGroupValue (if (nil? menuOptionGroupValue) [] menuOptionGroupValue)]
    [:> Stack {:direction "row" :spacing 5}

     [:> QueryRadioMenu {:heading "Entity Type"
                         :options entity-types
                         :onChange #(update-entity-type uid %)
                         :value    query-type}]

     [:> QueryRadioMenu {:heading "Layout"
                         :options LAYOUTS
                         :onChange #(update-layout uid %)
                         :value query-layout}]

     [:> QueryRadioMenu {:heading "Filter By Author"
                         :options AUTHORS
                         :onChange #(update-filter-author uid %)
                         :value query-filter-author}]

     [:> QueryRadioMenu {:heading "Special Filters"
                          :options ["None" "On this page" #_"Created in the past week"]
                          :onChange #(update-special-filters uid %)
                          :value query-special-filters}]

     [:> QueryRadioMenu {:heading  "Sort By"
                         :options  schema
                         :onChange #(update-sort-by uid %)
                         :value    query-sort-by}]

     [:> QueryRadioMenu {:heading "Sort Direction"
                         :options ["asc" "desc"]
                         :onChange #(update-sort-direction uid %)
                         :value query-sort-direction}]

     #_[:> Controls {:isCheckedFn          #(get query-properties-hide %)
                     :properties           schema
                     :hiddenProperties     query-properties-hide
                     :menuOptionGroupValue menuOptionGroupValue
                     :onChange             #(toggle-hidden-property uid %)}]
     #_[:> Button {:onClick #(prn parsed-properties) :disabled true}
        [:> Heading {:size "sm"} "Save View"]]]))

(defn query-el
  [{:keys [query-data parsed-properties uid schema]}]
  (let [query-layout           (get parsed-properties "query/layout")
        query-properties-order (get parsed-properties "query/properties-order")
        query-sort-by          (get parsed-properties "query/sort-by")
        query-sort-by          (parse-for-title query-sort-by)
        query-sort-direction   (get parsed-properties "query/sort-direction")
        query-filter-author    (get parsed-properties "query/filter-author")
        query-special-filters  (get parsed-properties "query/special-filters")

        filter-author-fn       (fn [x]
                                 (let [entity-author (get x ":create/auth")]
                                   (or (= query-filter-author "None")
                                       (= query-filter-author
                                           entity-author))))



        special-filter-fn      (fn [x]
                                 (cond
                                   (= query-special-filters "On this page") (let [comment-uid (get x ":block/uid")
                                                                                  comments-parent-page (-> (db/get-root-parent-page comment-uid)
                                                                                                           :node/title)
                                                                                  current-page-of-query (-> (db/get-root-parent-page uid)
                                                                                                            :node/title)]
                                                                              (= comments-parent-page current-page-of-query))
                                   :else true))

        query-data             (filterv special-filter-fn query-data)
        query-data             (filterv filter-author-fn query-data)

        query-data             (sort-table query-data query-sort-by query-sort-direction)


        ;; TODO
        query-properties-hide  (or (get parsed-properties "query/properties-hide") [])]
    [:> Box {#_#_:margin-top "40px" :width "100%"}
     (case query-layout
       "board"
       (let [query-group-by    (get parsed-properties "query/group-by")
             query-group-by    (parse-for-title query-group-by)
             query-subgroup-by (get parsed-properties "query/subgroup-by")
             query-subgroup-by (parse-for-title query-subgroup-by)
             columns           (or (get-reactive-property [:node/title query-group-by] ":property/values") [])
             boardData         (if (and query-subgroup-by query-group-by)
                                 (group-stuff query-group-by query-subgroup-by query-data)
                                 (group-by query-group-by query-data))]
         [:> QueryKanban {:boardData            boardData
                          ;; store column order here
                            :columns              columns
                            :hasSubGroup          (boolean query-subgroup-by)
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

       ;; what about groupBy page or something

       "list"
       (let [uids    (map #(get % ":block/uid") query-data)
             threads (map #(db/get-comment-threads-for-query @db/dsdb %) uids)
             merged-parents (get-merged-breadcrumbs uids)]
             ;; only works for comments
            [:> Box {:borderWidth "1px" :borderRadius "lg"}
             [breadcrumbs-el merged-parents]
             #_(for [thread threads]
                 [athens.views.blocks.core/block-el thread])])


       [:> QueryTable {:data           query-data
                       :columns        schema
                       ;;:onClickSort    #(update-sort-by uid (str-to-title query-sort-by) query-sort-direction (str-to-title %))
                       :sortBy         query-sort-by
                       :sortDirection  query-sort-direction
                       :rowCount       (count query-data)
                       :hideProperties query-properties-hide
                       :dateFormatFn   #(dates/date-string %)}])]))



(defn invalid-query?
  [parsed-props]
  (let [layout (get parsed-props "query/layout")
        groupBy (get parsed-props "query/group-by")]
    (and (= layout "board")
         (nil? groupBy))))

;; TODO: fix proeprties
;; clicking on them can add an SVG somehow
;; and then if there are block/children, it is no bueno

(defn query-block
  [block-data properties]
  (let [block-uid         (:block/uid block-data)
        parsed-properties (get-query-props properties)
        query-type        (get parsed-properties "query/type")
        schema            (get-schema query-type)
        query-data        (->> (reactive/get-reactive-instances-of-key-value ":entity/type" query-type)
                               (map block-to-flat-map))]

    (if (invalid-query? parsed-properties)
      [:> Box {:color "red"} "invalid query"]
      [:> Box {:width        "100%" :borderColor "gray"
               :padding-left 38 :padding-top 15}
        [options-el {:parsed-properties parsed-properties
                     :properties        properties
                     :schema            schema
                     :query-data        query-data
                     :uid               block-uid}]
        [query-el {:query-data        query-data
                   :uid               block-uid
                   :schema            schema
                   :parsed-properties parsed-properties}]])))


