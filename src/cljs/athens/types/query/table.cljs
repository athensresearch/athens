(ns athens.types.query.table
  "Views for Athens Tasks"
  (:require
    ["/components/Block/BlockFormInput" :refer [BlockFormInput]]
    ["/components/Block/Taskbox" :refer [Taskbox]]
    ["/components/Icons/Icons" :refer [PencilIcon GraphChildIcon]]
    ["/components/ModalInput/ModalInput" :refer [ModalInput]]
    ["/components/ModalInput/ModalInputAnchor" :refer [ModalInputAnchor]]
    ["/components/ModalInput/ModalInputPopover" :refer [ModalInputPopover]]
    ["/components/ModalInput/ModalInputTrigger" :refer [ModalInputTrigger]]
    ["@chakra-ui/react" :refer [IconButton
                                HStack
                                Grid
                                Heading
                                Flex
                                HStack
                                Text]]
    ["@dnd-kit/core" :refer []]
    ["@dnd-kit/sortable" :refer []]
    [athens.common-db :as common-db]
    [athens.db :as db]
    [athens.reactive :as reactive]
    [athens.types.query.shared :as shared]
    [clojure.string :refer []]
    [reagent.core :as r]))


(def header-row-cell-style
  {:position "sticky"
   :left 0
   :zIndex 1})


(defn render-entity-row
  [uid children indent grid-template-cols]
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

        curr-indent-width (str (* 1 indent) "rem")

        ;; for when we can collapse items
        _is-collapsed?  false

        assignee-value (shared/parse-for-title assignee)
        status-uid     (shared/parse-for-uid status)
        status-value   (common-db/get-block-string @db/dsdb status-uid)
        priority-uid   (shared/parse-for-uid priority)
        priority-value (common-db/get-block-string @db/dsdb priority-uid)
        _parent-uid     (:block/uid (common-db/get-parent @db/dsdb [:block/uid uid]))
        entity-type (common-db/get-entity-type @db/dsdb [:block/uid uid])
        is-root (= entity-type "page")]



    [:<>
     (case entity-type
       "page"
       [:> Grid {:templateColumns grid-template-cols
                 :templateRows "auto"
                 :pt 8
                 :textAlign "start"}
        [:> Text (merge header-row-cell-style
                        {:color "foreground.secondary"}) title]
        [:> Text {:alignSelf "stretch" :size "md"} status-value]
        [:> Text {:alignSelf "stretch" :size "md"} priority-value]
        [:> Text {:alignSelf "stretch" :size "md"} assignee-value]
        [:> Text {:alignSelf "stretch" :size "md"} due-date]]

       "block"
       [:> Grid {:templateColumns grid-template-cols
                 :textAlign "start"
                 :position "relative"
                 :borderTop "1px solid"
                 :borderColor "separator.border"
                 :_hover {:bg "interaction.surface.hover"}}
        [:> Text (merge header-row-cell-style
                        {:color "foreground.secondary"
                         :pl curr-indent-width
                         :fontSize "xs"})
         (str " • " (get entity ":block/string"))]]

       "[[athens/task]]"
       [:> Grid {:templateColumns grid-template-cols
                 :textAlign "start"
                 :position "relative"
                 :borderTop "1px solid"
                 :borderColor "separator.border"
                 :_hover {:bg "interaction.surface.hover"}}

        [:> Flex (merge
                   header-row-cell-style
                   {:alignSelf "inline-flex"
                    :align "center"
                    :gap 1
                    :pr 1
                    :pl curr-indent-width})

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
            (if status-value
              [:> Taskbox {:mx 1 :my "auto"
                           :status status-value}]
              [:> Taskbox {:mx 1 :my "auto"}])
            [:> BlockFormInput {:variant "unstyled"
                                :flex "1 1 100%"
                                :size "md"}
             [shared/title-editor uid title]]]]]]
        [:> Text {:alignSelf "stretch" :size "md"} status-value]
        [:> Text {:alignSelf "stretch" :size "md"} priority-value]
        [:> Text {:alignSelf "stretch" :size "md"} assignee-value]
        [:> Text {:alignSelf "stretch" :size "md"} due-date]]

       [:> Text {:color "foreground.secondary"
                 :borderTop "1px solid"
                 :borderColor "separator.border"
                 :pl curr-indent-width
                 :fontSize "xs"}
        (str "I am a block with type " entity-type)])

     [:<>
      (for [[uid children] children]
        ^{:key uid}
        [render-entity-row uid children (inc indent) grid-template-cols])]]))


(defn QueryTableV2
  [{:keys [data columns] :as _props}]
  (let [grid-template-cols "minmax(20em, 1fr) 9em 9em 9em 9em"]
    [:> Flex {:flexDirection "column" :align "stretch" :py 4 :width "100%" :overflowX "auto"}
     [:> Grid {:templateColumns grid-template-cols :textAlign "start"}
      (for [column columns]
        ^{:key column}
        [:> Heading (merge
                      {:size "sm" :fontWeight "normal" :color "foreground.secondary"}
                      (when (= column "Title")
                        header-row-cell-style))
         column])]
     [:<>
      (for [[uid children] data]
        ^{:key uid}
        [render-entity-row uid children 0 grid-template-cols])]]))

