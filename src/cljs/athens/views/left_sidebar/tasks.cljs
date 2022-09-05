(ns athens.views.left-sidebar.tasks
  (:require
    ["/components/Block/Taskbox" :refer [Taskbox]]
    ["/components/Icons/Icons" :refer [InfoIcon FilterCircleIcon FilterCircleFillIcon]]
    ["/components/Widget/Widget" :refer [Widget WidgetHeader WidgetBody WidgetTitle WidgetToggle]]
    ["@chakra-ui/react" :refer [Tooltip CircularProgress FormLabel Input Heading Button Popover PopoverTrigger PopoverAnchor PopoverContent PopoverBody Portal IconButton Link Text VStack Flex ButtonGroup Link Flex]]
    [athens.parse-renderer :as parse-renderer]
    [athens.reactive :as reactive]
    [athens.router   :as router]
    [athens.types.query.view :as query]
    [athens.types.tasks.view :as tasks]
    [athens.views.left-sidebar.subs :as left-sidebar-subs]
    [re-frame.core   :as rf]
    [reagent.core    :as r]))


(defn sidebar-task-el
  [task]
  (let [task-uid      (get task ":block/uid")
        task-title    (get task ":task/title")
        status-uid    (get task ":task/status")
        status-block  (reactive/get-reactive-block-document [:block/uid status-uid])
        status-string (:block/string status-block)]
    [:> Flex {:display "inline-flex"
              :align   "baseline"
              :py      1
              :gap     1}
     [:> Taskbox {:position "relative"
                  :top      "3px"
                  :isEditable true
                  :onChange #(tasks/on-update-status task-uid %)
                  :status   status-string}]
     [:> Link {:fontSize  "sm"
               :noOfLines 1
               ;; TODO: clicking on refs might take you to ref instead of task
               :onClick   #(router/navigate-uid task-uid %)}
      [parse-renderer/parse-and-render task-title task-uid]]]))


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
                                 (= status-string "Done")))
        is-filtered?         true
        fn-assigned-to-me    (fn [task]
                               (= (str "@" me)
                                  (get task ":task/assignee")))
        tasks-assigned-to-me (filterv fn-assigned-to-me all-tasks)
        grouped-tasks        (group-by #(get % ":task/page") tasks-assigned-to-me)
        get-pct-done         (fn [tasks]
                               (let [total-tasks (count tasks)
                                     done-tasks  (count (filterv #(get-is-done %) tasks))]
                                 (if (zero? total-tasks)
                                   0
                                   (int (/ (* 100 done-tasks) total-tasks)))))
        set-num-shown        (fn [num]
                               (rf/dispatch [:left-sidebar.tasks/set-max-tasks num]))
        ;; sort by due date, then priority, then title
        sort-tasks-list      (fn [tasks]
                               (sort-by (juxt #(get % ":task/due") #(get % ":task/priority") #(get % ":task/title")) tasks))
        get-num-done         (fn [tasks]
                               (count (filterv #(get-is-done %) tasks)))
        widget-open?         (left-sidebar-subs/get-widget-open? "tasks")]


    [:> Widget {:defaultIsOpen widget-open?}

     ;; Widget header, including settings popover
     [:> Popover {:placement "right-start"}

      ;; Widget header
      [:> PopoverAnchor
       [:> WidgetHeader {:title "Tasks"
                         :pl 6
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
        [:> WidgetToggle {:isDisabled (not (seq tasks-assigned-to-me))
                          :onClick    #(rf/dispatch [:left-sidebar.widgets/toggle-close "tasks"])}]]]

      ;; Widget settings popover
      [:> Portal
       [:> PopoverContent
        [:> PopoverBody
         [:> Heading {:size "xs"}
          "Tasks"]
         [:> FormLabel "Filter"]
         [:> Input {:size "xs"
                    :placeholder "Search tasks"}]
         [:> ButtonGroup {:size "xs"}
          [:> Button {:isActive (= 3 max-tasks-shown)
                      :onClick #(set-num-shown 3)}
           "3"]
          [:> Button {:isActive (= 7 max-tasks-shown)
                      :onClick #(set-num-shown 7)}
           "7"]
          [:> Button {:isActive (= 20 max-tasks-shown)
                      :onClick #(set-num-shown 20)}
           "Max"]]]]]]

     ;; Body of the main widget
     [:> WidgetBody {:as VStack
                     :pl 6
                     :pr 4
                     :spacing 2
                     :align "stretch"}

      (doall

        (for [[page tasks] grouped-tasks]

          ;; Per page of tasks...
          (let [pct-done (get-pct-done tasks)
                section-open? (left-sidebar-subs/get-task-section-open? page)]

            ^{:key page}
            [:> Widget {:defaultIsOpen section-open?
                        :borderTop "1px solid"
                        :borderColor "separator.divider"
                        :pt 1}
             [:> Tooltip {:placement "right-start"
                          :label (r/as-element
                                   [:> VStack {:align "stretch"}
                                    [:> Text (str (get-num-done tasks)
                                                  " / "
                                                  (count tasks) "
                                                   completed")]])}
              [:> WidgetHeader {:spacing 0}
               [:> CircularProgress {:thickness "16"
                                     :capIsRound true
                                     :size "1em"
                                     :trackColor "background.attic"
                                     :value pct-done}]
               [:> WidgetTitle [:> Link {:onClick #(router/navigate-page page %)} page]]
               [:> InfoIcon {:boxSize 3 :color "foreground.secondary"}]
               [:> WidgetToggle {:onClick #(rf/dispatch [:left-sidebar.tasks.section/toggle-close page])}]]]
             [:> WidgetBody
              (doall
                (for [task (take max-tasks-shown (sort-tasks-list tasks))]
                  ^{:key (get task ":block/uid")}
                  [:f> sidebar-task-el task]))]])))]]))
