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
                               Heading
                               Checkbox
                               CheckboxGroup]]
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

#_(defn toggle-reaction
    "Toggle reaction on block uid. Cleans up when toggling the last one off.
  Stores emojis in the [:reactions/emojis reaction user-id] property path."
    [id reaction user-id]
    (rf/dispatch [:properties/update-in id [":reactions" reaction user-id]
                  (fn [db user-reaction-uid]
                    (let [user-reacted?       (common-db/block-exists? db [:block/uid user-reaction-uid])
                          reaction            (when user-reacted?
                                                (->> [:block/uid user-reaction-uid]
                                                     (common-db/get-parent-eid db)
                                                     (common-db/get-block db)))
                          reactions           (when reaction
                                                (->> (:db/id reaction)
                                                     (common-db/get-parent-eid db)
                                                     (common-db/get-block db)))
                          last-user-reaction? (= 1 (count (-> reaction :block/properties)))
                          last-reaction?      (= 1 (count (-> reactions :block/properties)))]
                      [(cond
                         ;; This reaction doesn't exist yet, so we add it.
                         (not user-reacted?)
                         (graph-ops/build-block-save-op db user-reaction-uid "")

                         ;; This was the last of all reactions, remove the reactions property
                         ;; on the parent.
                         (and last-user-reaction? last-reaction?)
                         (graph-ops/build-block-remove-op @db/dsdb (:block/uid reactions))

                         ;; This was the last user reaction of this type, but not the last
                         ;; of all reactions. Remove reaction block.
                         last-user-reaction?
                         (graph-ops/build-block-remove-op @db/dsdb (:block/uid reaction))

                         ;; Just remove this particular user reaction.
                         :else
                         (graph-ops/build-block-remove-op @db/dsdb user-reaction-uid))]))]))

(defn update-hidden-properties
  "If property is already hidden, remove from block/children. Otherwise, add property to block/children."
  [id property]
  ;;(prn "UPDATE" id property)
  #_(rf/dispatch [:properties/update-in [:block/uid id] ["query/properties-hide" hidden-property-id]
                  (fn [db hidden-prop-uid]
                    [(graph-ops/build-block-save-op db prop-uid new-layout)])]))


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
  (let [query-layout          (get parsed-properties "query/layout")
        query-properties-order          (get parsed-properties "query/properties-order")
        query-properties-hide (get parsed-properties "query/properties-hide")]
    (prn query-properties-hide query-data)
    [:> Box
     ;; [:> Heading {:size "sm"} "Layout"]
     [:> ButtonGroup
      (for [x ["table" #_"list" "board"]]
        [:> Button {:value x
                    :onClick (fn [e]
                               (update-layout uid x))
                    :isActive (or (= query-layout x))}
         (clojure.string/capitalize x)])]
     [:> Stack {:direction "row"}
      [:> CheckboxGroup
       (for [property query-properties-order]
         [:> Checkbox {:isChecked (get query-properties-hide property)
                       :onChange (fn [] (update-hidden-properties uid property))}
          property])]]]))

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
       (let [query-group-by-kw    (symbol query-group-by)
             query-subgroup-by-kw (symbol query-subgroup-by)
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
                          :hideProperties query-properties-hide
                          :onAddNewCardClick    new-card
                          :onRenameCard         (fn [])
                          :onRenameColumn       (fn [])
                          :onClickCard          (fn [])
                          :onShiftClickCard     (fn [])
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

    (prn properties)


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

