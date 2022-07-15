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
   [athens.router             :as router]
   [clojure.string            :refer [lower-case]]
   [re-frame.core             :as rf]
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

(defn new-card
  [project column]
  (let [parent-of-new-block (:title (dates/get-day)) ;; for now, just create a new block on today's daily notes
        evt (->> (athens.common-events.bfs/internal-representation->atomic-ops
                   @athens.db/dsdb
                   [#:block{:uid    (athens.common.utils/gen-block-uid)
                            :string "Untitled"
                            :properties
                            {":block/type" #:block{:string "[[athens/task]]"
                                                   :uid    (athens.common.utils/gen-block-uid)}
                             ":task/status" #:block{:string column
                                                    :uid    (athens.common.utils/gen-block-uid)}
                             ":task/project" #:block{:string project
                                                     :uid    (athens.common.utils/gen-block-uid)}}}]
                   {:page/title parent-of-new-block
                    :relation  :last})
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


(defn get-prop-node-title
  "Could either be 1-arity (just block/string) or multi-arity (multiple children).

   XXX: to make multi-arity, look at block/children of \"query/types\"
   TODO: what happens if a user enters in multiple refs in a block/string? Should have some sort of schema enforcement, such as 1 ref per block to avoid confusion
   not using :block/string, because i want the node/title without having to do some reg-ex"
  [prop]
  (->> (get-in prop [#_"query/types" :block/refs 0 :db/id])
       (datascript.core/entity @athens.db/dsdb)
       :node/title))


(defn get-query-props
  [properties]
  (->> properties
       (map (fn [[k {:block/keys [children string] nested-properties :block/properties :as v}]]
              [k (cond
                   (= "query/types" k) (get-prop-node-title v)
                   children (->> (sort-by :block/order children)
                                 (mapv :block/string))
                   nested-properties  (zipmap (keys nested-properties) (repeat true))
                   :else string)]))
       (into (hash-map))))

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

;; Views



(defn options-el
  [{:keys [parsed-properties uid query-data]}]
  (let [query-layout           (get parsed-properties "query/layout")
        query-properties-order (get parsed-properties "query/properties-order")
        query-properties-hide  (get parsed-properties "query/properties-hide")]
    [:> Box
     [:> Heading {:size "md"} "Layout"]
     [:> ButtonGroup
      (for [x ["table" "board"]]
        [:> Button {:value    x
                    :onClick  (fn [e]
                                (update-layout uid x))
                    :isActive (or (= query-layout x))}
         (clojure.string/capitalize x)])]
     [:> Box
      [:> Controls {:isCheckedFn      #(get query-properties-hide %)
                     :properties       query-properties-order
                     :hiddenProperties query-properties-hide
                     :menuOptionGroupValue (keys query-properties-hide)
                     :onChange         #(toggle-hidden-property uid %)}]]]))


#_(js/console.log (apply hash-map [:a 1 :b 2 :c 3]))

(defn query-el
  [{:keys [query-data parsed-properties uid]}]
  (let [query-layout (get parsed-properties "query/layout")
        query-group-by (get parsed-properties "query/group-by")
        query-subgroup-by (get parsed-properties "query/subgroup-by")
        query-properties-order (get parsed-properties "query/properties-order")
        query-sort-by (get parsed-properties "query/sort-by")
        query-sort-direction (get parsed-properties "query/sort-direction")
        query-properties-hide (get parsed-properties "query/properties-hide")]

    [:> Box {#_#_:margin-top "40px" :width "100%"}
     (case query-layout
       "board"
       (let [columns              (->> (map #(get % query-group-by) query-data) set)
             rows                 (->> (map #(get % query-subgroup-by) query-data) set)
             boardData            (if (and query-subgroup-by query-group-by)
                                    (group-stuff query-group-by query-subgroup-by query-data)
                                    (group-by query-group-by query-data))]
         [:> QueryKanban {:boardData            boardData
                          ;; store column order here
                          :columns              columns
                          :hasSubGroup          (boolean query-subgroup-by)
                          :rows                 rows
                          :onUpdateStatusClick  update-status
                          :hideProperties query-properties-hide
                          :onAddNewCardClick    new-card
                          :onClickCard          #(rf/dispatch [:right-sidebar/open-item %])
                          :onAddNewColumnClick  (fn [])
                          :onAddNewProjectClick (fn [])}])
       (let [sorted-data (sort-table query-data query-sort-by query-sort-direction)]
         [:> QueryTable {:data sorted-data
                         :columns query-properties-order
                         :onClickSort #(update-sort-by uid query-sort-by query-sort-direction %)
                         :sortBy query-sort-by
                         :sortDirection query-sort-direction
                         :hideProperties query-properties-hide
                         :dateFormatFn #(dates/date-string %)}]))]))


;; XXX: last edit only concerns itself with the last edit of the block itself, not one of the block's property's
;; is this similar to the last edit of a page? does editing a block count as editing a page? or does it have to editing the page/title?


(defn query-block
  [block-data properties]
  (let [block-uid (:block/uid block-data)
        parsed-properties (get-query-props properties)
        query-types (get parsed-properties "query/types")
        query-group-by (get properties "query/group-by")
        query-subgroup-by (get properties "query/subgroup-by")
        query-data (->> (athens.reactive/get-reactive-instances-of-key-value ":block/type" query-types)
                        (map block-to-flat-map))]

    (cond
      (nil? query-group-by) [:> Box {:color "red"} "Please add property query/group-by"]
      (nil? query-subgroup-by) [:> Box {:color "red"} "Please add property query/subgroup-by"]

      :else [:> Box {:width        "100%" #_#_:border "1px" :borderColor "gray"
                     :padding-left 38 :padding-top 15}
             [options-el {:parsed-properties parsed-properties
                          :query-data        query-data
                          :uid               block-uid}]
             [query-el {:query-data        query-data
                        :uid               block-uid
                        :parsed-properties parsed-properties}]])))

