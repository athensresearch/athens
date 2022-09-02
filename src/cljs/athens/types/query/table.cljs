(ns athens.types.query.table
  "Views for Athens Tasks"
  (:require
    ["/components/Block/BlockFormInput" :refer [BlockFormInput]]
    ["/components/Block/Taskbox" :refer [Taskbox]]
    ["/components/Block/Toggle" :refer [Toggle]]
    ["/components/DnD/DndContext" :refer [DragAndDropContext]]
    ["/components/DnD/Droppable" :refer [Droppable]]
    ["/components/DnD/Sortable" :refer [Sortable]]
    ["/components/Icons/Icons" :refer [ChevronDownVariableIcon PencilIcon GraphChildIcon ArrowRightOnBoxIcon PlusIcon]]
    ["/components/ModalInput/ModalInput" :refer [ModalInput]]
    ["/components/ModalInput/ModalInputAnchor" :refer [ModalInputAnchor]]
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
                                Grid
                                Heading
                                Input
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
    [athens.types.query.shared :as shared]
    [athens.views.blocks.editor                 :as editor]
    [clojure.string :refer []]
    [datascript.core :as d]
    [re-frame.core :as rf]
    [reagent.core :as r]))


(defn render-entity
  [uid children indent]
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

        ;; forwhen we can collapse items
        is-collapsed?  false

        assignee-value (shared/parse-for-title assignee)
        status-uid     (shared/parse-for-uid status)
        status-value   (common-db/get-block-string @db/dsdb status-uid)
        priority-uid   (shared/parse-for-uid priority)
        priority-value (common-db/get-block-string @db/dsdb priority-uid)
        parent-uid     (:block/uid (common-db/get-parent @db/dsdb [:block/uid uid]))
        entity-type (common-db/get-entity-type @db/dsdb [:block/uid uid])
        is-root (= entity-type "page")]



    [:<>
     (case entity-type
       "page"
       [:> Grid {:templateColumns "3fr 1fr 1fr 1fr 1fr"
                 :mt 4
                 :textAlign "start"}
        [:> Text {:pl 1
                  :color "foreground.secondary"} title]
        [:> Text {:alignSelf "stretch" :size "md"} status-value]
        [:> Text {:alignSelf "stretch" :size "md"} priority-value]
        [:> Text {:alignSelf "stretch" :size "md"} assignee-value]
        [:> Text {:alignSelf "stretch" :size "md"} due-date]]

       "block"
       [:> Grid {:templateColumns "3fr 1fr 1fr 1fr 1fr"}
        [:span (str (get entity ":block/string") " > ")]]

       "[[athens/task]]"
       [:> Grid {:templateColumns "3fr 1fr 1fr 1fr 1fr"
                 :textAlign "start"
                 :borderTop "1px solid"
                 :borderColor "separator.border"
                 :_hover {:bg "interaction.surface.hover"}}

        [:> Flex {:position "sticky"
                  :left 0
                  :alignSelf "inline-flex"
                  :align "center"
                  :gap 1
                  :ml 1
                  :pr 1
                  :pl (str (* 1 indent) "em")}

         ;; Comment out until we figure out how to persist open/close state on tables
         #_[:> IconButton {:size "xs"
                           :variant "ghost"
                           :colorScheme "subtle"
                           :onClick #(js/alert "TODO: implement toggle")}

            [:> ChevronDownVariableIcon {:sx {:path {:strokeWidth "1.5px"}}
                                         :boxSize 3
                                         :transform (if is-collapsed? "rotate(-90deg)" "")}]]
         [:> ModalInput {:placement "right-start"
                         :autoFocus true}
          [:> ModalInputAnchor
           [:> Flex {:alignSelf "inline-flex"
                     :flex "1 1 100%"
                     :align "center"
                     :gap 0.5}
            (if status-value
              [:> Taskbox {:status status-value}]
              [:> Taskbox {}])
            [:> Text {:pl 1
                      :as "span"
                      :color (if is-root "foreground.secondary" "foreground.primary")} title]
            (when (seq children)
              [:> HStack {:spacing 0
                          :color "foreground.tertiary"}
               [:> GraphChildIcon]
               [:> Text {:as "span"} (count children)]])]]
          [:> ModalInputTrigger
           [:> IconButton {:size "sm"
                           :variant "ghost"
                           :colorScheme "subtle"
                           :icon (r/as-element [:> PencilIcon])}]]
          [:> ModalInputPopover {:popoverContentProps {:mx "-5px" :my "-1px"}}
           [:> Flex {:align "center"
                     :gap 0.5}
            (when status-value
              [:> Taskbox {:status status-value
                           :mx 1 :my "auto"}])
            [:> BlockFormInput {:variant "unstyled"
                                :flex "1 1 100%"
                                :size "md"}
             [shared/title-editor uid title]]]]]]
        [:> Text {:alignSelf "stretch" :size "md"} status-value]
        [:> Text {:alignSelf "stretch" :size "md"} priority-value]
        [:> Text {:alignSelf "stretch" :size "md"} assignee-value]
        [:> Text {:alignSelf "stretch" :size "md"} due-date]]

       [:div (str "I am a block with type " entity-type)])
     [:<>
      (for [[uid children] children]
        ^{:key uid}
        [render-entity uid children (inc indent)])]]))


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
      [render-entity uid children 0])]])

