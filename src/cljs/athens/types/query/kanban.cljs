(ns athens.types.query.kanban
  "Views for Athens Tasks"
  (:require
    ["/components/Block/BlockFormInput" :refer [BlockFormInput]]
    ["/components/DnD/DndContext" :refer [DragAndDropContext]]
    ["/components/DnD/Droppable" :refer [Droppable]]
    ["/components/DnD/Sortable" :refer [Sortable]]
    ["/components/Icons/Icons" :refer [ArrowRightOnBoxIcon PlusIcon]]
    ["/components/ModalInput/ModalInput" :refer [ModalInput]]
    ["/components/ModalInput/ModalInputPopover" :refer [ModalInputPopover]]
    ["/components/ModalInput/ModalInputTrigger" :refer [ModalInputTrigger]]
    ["/components/Query/KanbanBoard" :refer [KanbanBoard
                                             KanbanCard
                                             KanbanSwimlane
                                             KanbanColumn]]
    ["@chakra-ui/react" :refer [IconButton
                                HStack
                                Heading
                                ButtonGroup
                                Flex
                                VStack
                                HStack
                                Text]]
    ["@dnd-kit/core" :refer [closestCorners,
                             DragOverlay,]]
    ["@dnd-kit/sortable" :refer [SortableContext,
                                 verticalListSortingStrategy,]]
    [athens.common-db :as common-db]
    [athens.common-events :as common-events]
    [athens.common-events.bfs :as bfs]
    [athens.common-events.graph.composite :as composite]
    [athens.common-events.graph.ops :as graph-ops]
    [athens.common.utils :as utils]
    [athens.dates :as dates]
    [athens.db :as db]
    [athens.parse-renderer :as parse-renderer]
    [athens.reactive :as reactive]
    [athens.self-hosted.presence.views          :as presence]
    [athens.types.query.shared :as shared]
    [clojure.string :refer []]
    [re-frame.core :as rf]
    [reagent.core :as r]))


(defn context-to-block-properties
  [context]
  (apply hash-map
         (->> context
              (map (fn [[k v]]
                     [k #:block{:string v
                                :uid    (utils/gen-block-uid)}]))
              flatten)))


(defn new-card
  "new-card needs to know the context of where it was pressed. For example, pressing it in a given column and swimlane
  would pass along those properties to the new card. Filter conditions would also be passed along. It doesn't matter if
  inherited properties are passed throughu group, subgroup, or filters. It just matters that they are true, and the view should be derived properly.

  context == {:task/status 'todo'
              :task/project '[[Project: ASD]]'"
  [context f-special query-uid]
  (let [context             (js->clj context)
        new-block-props     (context-to-block-properties context)
        parent-of-new-block (if (= f-special "On this page")
                              {:block/uid query-uid}
                              {:page/title (-> (dates/get-day) :title)})
        position            (merge {:relation :last} parent-of-new-block)
        evt                 (->> (bfs/internal-representation->atomic-ops
                                   @athens.db/dsdb
                                   [#:block{:uid        (utils/gen-block-uid)
                                            :string     ""
                                            :properties (merge {":entity/type" #:block{:string "[[athens/task]]"
                                                                                       :uid    (utils/gen-block-uid)}
                                                                ":task/title" #:block{:string "Untitled task"
                                                                                      :uid    (utils/gen-block-uid)}}
                                                               new-block-props)}]
                                   position)
                                 (composite/make-consequence-op {:op/type :new-type})
                                 common-events/build-atomic-event)]
    (re-frame.core/dispatch [:resolve-transact-forward evt])))


;; UPDATE


(defn update-card-container
  [id active-container-context over-container-context]
  (let [{active-swimlane-id :swimlane-id active-column-id :column-id} active-container-context
        {over-swimlane-id :swimlane-id over-column-id :column-id} over-container-context
        diff-column?   (not= active-column-id over-column-id)
        diff-swimlane? (not= active-swimlane-id over-swimlane-id)
        nil-swimlane?  (= over-swimlane-id "None")
        nil-column?    (= over-column-id "None")
        new-column     (str "((" over-column-id "))")]
    (when diff-swimlane?
      (rf/dispatch [:graph/update-in [:block/uid id] [":task/assignee"]
                    (fn [db prop-uid]
                      [(if nil-swimlane?
                         (graph-ops/build-block-remove-op db prop-uid)
                         (graph-ops/build-block-save-op db prop-uid over-swimlane-id))])]))
    (when diff-column?
      (rf/dispatch [:graph/update-in [:block/uid id] [":task/status"]
                    (fn [db prop-uid]
                      [(if nil-column?
                         (graph-ops/build-block-remove-op db prop-uid)
                         (graph-ops/build-block-save-op db prop-uid new-column))])]))))


(defn- find-container-id
  "Accepts event.active or event.over"
  [e active-or-over]
  (try
    (case active-or-over
      :active (.. e -active -data -current -sortable -containerId)
      :over (.. e -over -data -current -sortable -containerId))
    (catch js/Object _
      (case active-or-over
        :active (.. e -active -id)
        :over (.. e -over -id)))))


(defn- get-container-context
  [container-id]
  (let [swimlane-id (-> (re-find #"swimlane-(@?\w+)" container-id) second)
        column-id   (-> (re-find #"column-(\w+)" container-id) second)]
    {:swimlane-id swimlane-id
     :column-id column-id}))


(defn render-card
  [uid over?]
  (let [card           (-> (reactive/get-reactive-block-document [:block/uid uid])
                           shared/block-to-flat-map
                           shared/get-root-page)
        title          (get card ":task/title")
        status         (get card ":task/status")
        priority       (get card ":task/priority")
        assignee       (get card ":task/assignee")
        _page          (get card ":task/page")
        _due-date      (get card ":task/due-date")
        assignee-value (shared/parse-for-title assignee)
        status-uid     (shared/parse-for-uid status)
        _status-value  (common-db/get-block-string @db/dsdb status-uid)
        priority-uid   (shared/parse-for-uid priority)
        priority-value (common-db/get-block-string @db/dsdb priority-uid)
        parent-uid     (:block/uid (common-db/get-parent @db/dsdb [:block/uid uid]))
        ;; TODO: figure out how to give unique id when one card can show up multiple times on a query, e.g. a card that belongs to multiple projects
        ;; could use swimlane and column data for uniqueness
        id             (str uid)]
    [:> Sortable {:id id :key id}
     [:> KanbanCard {:isOver over?}
      [:> VStack {:spacing 0
                  :align   "stretch"}
       [:> ModalInput {:autoFocus true}
        [:> ModalInputTrigger
         ;; TODO show something if empty title
         [:> Text {:fontWeight    "medium"
                   :onPointerDown #(.stopPropagation %)
                   :lineHeight    "short"} [parse-renderer/parse-and-render title uid]]]
        [:> ModalInputPopover {:preventScroll false}
         [:> BlockFormInput {:size "md"
                             :isMultiline true
                             :onPointerDown #(.stopPropagation %)}
          [shared/title-editor uid title]
          [presence/inline-presence-el uid]]]]
       [:> HStack {:justifyContent "space-between"
                   :fontSize       "sm"
                   :color          "foreground.secondary"}
        [:> HStack
         [:> Text assignee-value]
         [:> Text priority-value]]
        [:> ButtonGroup {:justifyContent "space-between"
                         :size           "xs"
                         :variant        "ghost"
                         :colorScheme    "subtle"
                         :onPointerDown  #(.stopPropagation %)}
         [:> IconButton {:zIndex  1
                         :onClick #(rf/dispatch [:right-sidebar/open-item [:block/uid parent-uid]])}
          [:> ArrowRightOnBoxIcon]]]]]]]))


(defn DragAndDropKanbanBoard
  [_props]
  (let [active-id (r/atom nil)
        over-id   (r/atom nil)]
    (fn [props]
      (let [{:keys [query-uid f-special boardData all-possible-group-by-columns groupBy subgroupBy]} props]
        [:> DragAndDropContext {:collisionDetection closestCorners
                                :onDragStart (fn [e]
                                               (reset! active-id (.. e -active -id)))
                                :onDragOver  (fn [e]
                                               (reset! over-id (.. e -over -id)))
                                :onDragEnd   (fn [e]
                                               ;; TODO: should context metadata be stored at the card level or the container level?
                                               (let [over-container           (find-container-id e :over)
                                                     active-container         (find-container-id e :active)
                                                     over-container-context   (get-container-context over-container)
                                                     active-container-context (get-container-context active-container)]
                                                 (update-card-container @active-id active-container-context over-container-context)
                                                 (reset! active-id nil)
                                                 (reset! over-id nil)))}

         [:> KanbanBoard
          [:> Heading {:size "md"} "TODO: Create title handler for queries"]
          (doall
            (for [swimlanes boardData]
              (let [[swimlane-id swimlane-columns] swimlanes
                    nil-swimlane-id? (nil? swimlane-id)
                    ;; TODO: doesn't handle empty assignee well, or values that are not expected
                    swimlane-id      (if swimlane-id swimlane-id "None")
                    swimlane-key     (if nil-swimlane-id? "None" swimlane-id)]

                [:> KanbanSwimlane {:name swimlane-key
                                    :key swimlane-key
                                    :bg "background.basement"}
                 (doall
                   (for [possible-group-by-column all-possible-group-by-columns]
                     (let [{:block/keys [string uid]} possible-group-by-column
                           cards-from-a-column (if (= string "None")
                                                 (get swimlane-columns nil)
                                                 (get swimlane-columns uid))
                           ;; context-object assumes group-by is always status, because of the uid stuff
                           context-object      (cond-> {}
                                                 (and (= groupBy ":task/status")
                                                      (not (nil? uid))) (assoc groupBy (str "((" uid "))"))
                                                 (not nil-swimlane-id?) (assoc subgroupBy (str "[[" swimlane-id "]]")))
                           column-id           (if uid uid "None")
                           column-id           (str "swimlane-" swimlane-id "-column-" column-id)]

                       [:> Droppable {:key column-id :id column-id}
                        (fn [over?]
                          (r/as-element
                            [:> SortableContext {:id column-id
                                                 :items (or cards-from-a-column [])
                                                 :strategy verticalListSortingStrategy}
                             [:> KanbanColumn {:key column-id
                                               :isOver over?}
                              [:> Flex {:color "foreground.secondary"
                                        :gap 2
                                        :px 4
                                        :py 1
                                        :alignItems "center"}
                               [:> Heading {:fontWeight "medium"
                                            :mr "auto"
                                            :size "sm"}
                                string]
                               [:> Text {:fontWeight "medium"
                                         :fontSize "sm"}
                                (str (count cards-from-a-column))]
                               [:> ButtonGroup {:size "sm"
                                                :variant "ghost"}
                                [:> IconButton {:onClick #(new-card context-object f-special query-uid)
                                                :icon    (r/as-element [:> PlusIcon])}]]]
                              (doall
                                (for [card cards-from-a-column]
                                  (let [card-uid (get card ":block/uid")
                                        over?    (= @over-id card-uid)]
                                    ^{:key card-uid} [render-card card-uid over?])))]]))])))])))

          [:> DragOverlay
           (when @active-id
             [:<>
              ;; [:h1 @over-id]
              [render-card @active-id]])]]]))))


#_(defn update-status
    [id new-status]
    (rf/dispatch [:graph/update-in [:block/uid id] [":task/status"]
                  (fn [db prop-uid]
                    [(graph-ops/build-block-save-op db prop-uid new-status)])]))


;; All commented out for when we modify kanban columns
#_(defn new-kanban-column
    "This creates a new block/child at the property/values key, but the kanban board doesn't trigger a re-render because it isn't aware of property/values yet."
    [group-by-id]
    (rf/dispatch [:graph/update-in [:node/title group-by-id] [":property/values"]
                  (fn [db prop-uid]
                    [(graph-ops/build-block-new-op db (utils/gen-block-uid) {:block/uid prop-uid :relation :last})])]))


#_(defn update-many-properties
    [db key value new-value]
    (->> (common-db/get-instances-of-key-value db key value)
         (map #(get-in % [key :block/uid]))
         (map (fn [uid]
                (graph-ops/build-block-save-op db uid new-value)))))


#_(defn update-kanban-column
    "Update the property page that is the source of values for a property.
  Also update all the blocks that are using that property."
    [property-key property-value new-value]
    (rf/dispatch [:graph/update-in [:node/title property-key] [":property/values"]
                  (fn [db prop-uid]
                    (let [{:block/keys [children]} (common-db/get-block-document db [:block/uid prop-uid])
                          update-uid (->> children
                                          (map (fn [{:block/keys [string uid]}] [string uid]))
                                          (filter #(= (first %) property-value))
                                          (first)
                                          second)
                          ;; update all blocks that match key:value to key:new-value
                          update-ops (update-many-properties db property-key property-value new-value)]

                      (vec (concat [(graph-ops/build-block-save-op db update-uid new-value)]
                                   update-ops))))]))

