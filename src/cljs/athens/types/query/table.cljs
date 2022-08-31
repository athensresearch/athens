(ns athens.types.query.table
  "Views for Athens Tasks"
  (:require
    [datascript.core :as d]
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
    ["/components/Query/Query" :refer [QueryRadioMenu]]
    ["/components/Query/Table" :refer [QueryTable]]
    ["@chakra-ui/react" :refer [Box,
                                IconButton
                                HStack
                                Heading
                                ButtonGroup
                                Flex
                                VStack
                                HStack
                                Button, Table, Thead, Tbody, Th, Td, Tr, Tfoot, textDecoration, Link
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
    [athens.router :as router]
    [athens.self-hosted.presence.views          :as presence]
    [athens.types.core :as types]
    [athens.types.dispatcher :as dispatcher]
    [athens.views.blocks.editor                 :as editor]
    [clojure.string :refer []]
    [re-frame.core :as rf]
    [reagent.core :as r]
    [athens.types.query.shared :as shared]))


(defn render-entity
  [uid children columns]
  (let [entity         (->> (reactive/get-reactive-block-document [:block/uid uid])
                            shared/block-to-flat-map)
        page-title     (common-db/get-page-title @db/dsdb uid)
        task-title     (get entity ":task/title")
        title          (or page-title task-title)
        status         (get entity ":task/status")

        priority       (get entity ":task/priority")
        assignee       (get entity ":task/assignee")
        _page          (get entity ":task/page")
        due-date       (get entity ":task/due-date")

        assignee-value (shared/parse-for-title assignee)
        status-uid     (shared/parse-for-uid status)
        status-value   (common-db/get-block-string @db/dsdb status-uid)
        priority-uid   (shared/parse-for-uid priority)
        priority-value (common-db/get-block-string @db/dsdb priority-uid)
        parent-uid     (:block/uid (common-db/get-parent @db/dsdb [:block/uid uid]))]

    [:> Box {:width "100%"}
     [:> HStack {:justifyContent "space-between" :width "100%"}
      [:> Heading {:size "sm"} title]
      [:> Text {:size "md"} status-value]
      [:> Text {:size "md"} priority-value]
      [:> Text {:size "md"} assignee-value]
      [:> Text {:size "md"} due-date]]
     [:> Box
      (for [[uid children] children]
        ^{:key uid}
        [:> Box {:ml 5}
         [render-entity uid children]])]]))


(defn QueryTableV2
  [{:keys [data columns] :as props}]
  [:> VStack
   [:> HStack {:justifyContent "space-between" :width "100%"}
    (for [column columns]
      ^{:key column}
      [:> Heading {:size "md"} column])]
   [:<>
    (for [[uid children] data]
      ^{:key uid}
      [render-entity uid children columns])]])

