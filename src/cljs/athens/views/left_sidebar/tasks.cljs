(ns athens.views.left-sidebar.tasks
  (:require
    ["/components/Block/Taskbox"    :refer [Taskbox]]
    ["/components/Empty/Empty"      :refer [Empty
                                            EmptyIcon
                                            EmptyTitle
                                            EmptyMessage]]
    ["/components/Icons/Icons"      :refer [FilterCircleIcon FilterCircleFillIcon CheckboxIcon]]
    ["/components/Widget/Widget"    :refer [Widget WidgetHeader WidgetBody WidgetTitle WidgetToggle]]
    ["@chakra-ui/react"             :refer [FormControl Select FormLabel Heading Popover PopoverTrigger PopoverAnchor PopoverContent PopoverBody Portal IconButton Link Text VStack Flex Link Flex]]
    ["framer-motion"                :refer [motion AnimatePresence]]
    [athens.parse-renderer          :as parse-renderer]
    [athens.reactive                :as reactive]
    [athens.router                  :as router]
    [athens.types.query.shared      :as query]
    [athens.types.tasks.handlers    :as task-handlers]
    [athens.types.tasks.shared      :as shared]
    [athens.views.left-sidebar.subs :as left-sidebar-subs]
    [re-frame.core                  :as rf]
    [reagent.core                   :as r]))


(defn sidebar-task-el
  [task]
  (let [task-uid      (get task ":block/uid")
        task-title    (get task ":task/title")
        status-uid    (get task ":task/status")
        status-options (->> (shared/find-allowed-statuses)
                            (map (fn [{:block/keys [string]}]
                                   string)))
        status-block  (reactive/get-reactive-block-document [:block/uid status-uid])
        status-string (:block/string status-block)]
    [:> Flex {:display "inline-flex"
              :as (.-div motion)
              :initial {:opacity 0
                        :height 0}
              :animate {:opacity 1
                        :height "auto"}
              :exit {:opacity 0
                     :height 0}
              :align   "baseline"
              :gap     1}
     [:> Taskbox {:position "relative"
                  :top      "3px"
                  :options status-options
                  :onChange #(task-handlers/update-task-status task-uid %)
                  :status   status-string}]
     [:> Link {:fontSize  "sm"
               :py 1
               :noOfLines 1
               ;; TODO: clicking on refs might take you to ref instead of task
               :onClick   #(router/navigate-uid task-uid %)}
      [parse-renderer/parse-and-render task-title task-uid]]]))


(defn sort-tasks-list
  [tasks]
  (sort-by (juxt #(get % ":task/due") #(get % ":task/priority") #(get % ":task/title")) tasks))


(defn my-tasks
  []
  (let [all-tasks            (->> (reactive/get-reactive-instances-of-key-value ":entity/type" "[[athens/task]]")
                                  (map query/block-to-flat-map)
                                  (map query/get-root-page))
        me                   @(rf/subscribe [:presence/current-username])
        max-tasks-shown      (left-sidebar-subs/get-max-tasks)
        get-is-done          (fn [task]
                               (let [status        (get task ":task/status")
                                     status-block  (reactive/get-reactive-block-document [:block/uid status])
                                     status-string (:block/string status-block "(())")]
                                 (or (= status-string "Done")
                                     (= status-string "Cancelled"))))
        is-filtered?         true
        fn-assigned-to-me    (fn [task]
                               (= (str "@" me)
                                  (get task ":task/assignee")))
        tasks-assigned-to-me (filterv fn-assigned-to-me all-tasks)
        grouped-tasks        (group-by #(get % ":task/page") tasks-assigned-to-me)
        set-num-shown        (fn [num]
                               (rf/dispatch [:left-sidebar.tasks/set-max-tasks num]))
        ;; sort by due date, then priority, then title
        widget-open?         (left-sidebar-subs/get-widget-open? "tasks")]


    [:> Widget {:defaultIsOpen widget-open?}

     ;; Widget header, including settings popover
     [:> Popover {:placement "right-start" :size "sm"}

      ;; Widget header
      [:> PopoverAnchor
       [:> WidgetHeader {:title "Assigned to Me"
                         :pl 6
                         :pb 2
                         :pr 4}
        [:> PopoverTrigger
         [:> IconButton {:icon
                         (r/as-element (if is-filtered?
                                         [:> FilterCircleFillIcon]
                                         [:> FilterCircleIcon]))
                         :size "xs"
                         :colorScheme "subtle"
                         :variant "ghost"}]]

        ;; Count of shown tasks
        [:> Text {:fontSize "xs"
                  :color "foreground.secondary"} (count tasks-assigned-to-me)]

        ;; standard widget toggle
        [:> WidgetToggle {:onClick    #(rf/dispatch [:left-sidebar.widgets/toggle-widget "tasks"])}]]]

      ;; Widget settings popover
      [:> Portal
       [:> PopoverContent {:width "16em"}
        [:> PopoverBody
         [:> Heading {:size "xs"}
          "Display Settings"]
         [:> VStack {:py 2}
          [:> FormControl {:display "flex" :flexDirection "row"}
           [:> FormLabel {:flex "1 1 100%" :py 1} "Tasks per page"]
           [:> Select {:value max-tasks-shown
                       :size "xs"
                       :onChange #(set-num-shown (-> % .-target .-value js/parseInt))}
            [:option {:value 3} "3"]
            [:option {:value 5} "5"]
            [:option {:value 10} "7"]
            [:option {:value 20} "20"]]]]]]]]

     ;; Body of the main widget
     [:> WidgetBody {:as VStack
                     :pl 6
                     :pr 4
                     :spacing 2
                     :align "stretch"}

      (if (seq tasks-assigned-to-me)
        (doall
          (for [[page tasks] grouped-tasks]

            ;; TODO: filter out pages with no tasks assigned to me
            ;; before getting to this point
            (when (seq (filterv #(not (get-is-done %)) tasks))

              ;; Per page of tasks...
              (let [incomplete-tasks (filterv #(not (get-is-done %)) tasks)
                    section-open? (left-sidebar-subs/get-task-section-open? page)]

                ^{:key page}
                [:> Widget {:defaultIsOpen section-open?
                            :borderTop "1px solid"
                            :borderColor "separator.divider"
                            :pt 2}
                 [:> WidgetHeader {:spacing 0}
                  [:> WidgetTitle [:> Link {:onClick #(router/navigate-page page %)} page]]
                  [:> Text {:className "shown-on-hover" :fontSize "xs" :color "foreground.secondary"} (count tasks)]
                  [:> WidgetToggle {:className "shown-on-hover" :onClick #(rf/dispatch [:left-sidebar.tasks.section/toggle page])}]]
                 [:> WidgetBody
                  [:> AnimatePresence {:initial false}
                   (doall
                     ;; show sorted list of limited number of incomplete tasks
                     (for [task (take max-tasks-shown (sort-tasks-list incomplete-tasks))]
                       ^{:key (get task ":block/uid")}
                       [:f> sidebar-task-el task]))]]]))))

        [:> Empty {:size "sm" :pl 0}
         [:> EmptyIcon {:Icon CheckboxIcon}]
         [:> EmptyTitle "All done"]
         [:> EmptyMessage "Tasks assigned to you will appear here."]])]]))
