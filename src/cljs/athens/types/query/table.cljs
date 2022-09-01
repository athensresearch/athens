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
    ["/components/Block/Taskbox" :refer [Taskbox]]
    ["@chakra-ui/react" :refer [Box,
                                IconButton
                                HStack
                                Grid
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
  [uid children indent is-root]
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

    [:<>
     (if is-root
       [:> Grid {:templateColumns "3fr 1fr 1fr 1fr 1fr"
                 :mt 2
                 :textAlign "start"}
        [:> Text {:pl 1
                  :color "foreground.secondary"} title]
        [:> Text {:alignSelf "stretch" :size "md"} status-value]
        [:> Text {:alignSelf "stretch" :size "md"} priority-value]
        [:> Text {:alignSelf "stretch" :size "md"} assignee-value]
        [:> Text {:alignSelf "stretch" :size "md"} due-date]]
       [:> Grid {:templateColumns "3fr 1fr 1fr 1fr 1fr"
                 :textAlign "start"
                 :borderTop "1px solid"
                 :borderColor "separator.border"
                 :_hover {:bg "interaction.surface.hover"
                          :borderRadius "sm"}}
        [:> Flex {:alignSelf "inline-flex" :align "center" :gap 0.5 :size "sm" :ml 1 :pl (str (* 1 indent) "em")}
         (when status-value [:> Taskbox {:status status-value}])
         [:> Text {:pl 1
                   :color (if is-root "foreground.secondary" "foreground.primary")} title]]
        [:> Text {:alignSelf "stretch" :size "md"} status-value]
        [:> Text {:alignSelf "stretch" :size "md"} priority-value]
        [:> Text {:alignSelf "stretch" :size "md"} assignee-value]
        [:> Text {:alignSelf "stretch" :size "md"} due-date]])
     [:<>
      (for [[uid children] children]
        ^{:key uid}
        [render-entity uid children (if is-root 0 (inc indent)) false])]]))


(defn QueryTableV2
  [{:keys [data columns] :as props}]
  [:> Flex {:flexDirection "column" :align "stretch" :py 4}
   [:> Grid {:templateColumns "3fr 1fr 1fr 1fr 1fr" :textAlign "start"}
    (for [column columns]
      ^{:key column}
      [:> Heading {:size "sm" :fontWeight "normal" :color "foreground.secondary"} column])]
   [:<>
    (for [[uid children] data]
      ^{:key uid}
      [render-entity uid children 0 true])]])

