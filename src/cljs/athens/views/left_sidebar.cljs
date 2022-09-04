(ns athens.views.left-sidebar
  (:require
   ["/components/Block/Taskbox" :refer [Taskbox]]
   ["/components/Icons/Icons" :refer [InfoIcon FilterCircleIcon FilterCircleFillIcon CalendarEditFillIcon AllPagesIcon ContrastIcon SearchIcon GraphIcon SettingsIcon]]
   ["/components/Layout/MainSidebar" :refer [MainSidebar SidebarSection SidebarSectionHeading]]
   ["/components/SidebarShortcuts/List" :refer [List]]
   ["/components/Widgets/Widget" :refer [Widget WidgetHeader WidgetBody WidgetTitle WidgetToggle]]
   ["@chakra-ui/react" :refer [Tooltip HStack CircularProgress FormLabel Input Heading Button Popover PopoverTrigger PopoverAnchor PopoverContent PopoverBody Portal IconButton Text Divider VStack Flex ButtonGroup Link Flex]]
   [athens.reactive :as reactive]
   [athens.router   :as router]
   [athens.types.query.view :as query]
   [athens.util     :as util]
   [re-frame.core   :as rf]
   [reagent.core    :as r]
   [athens.views.pages.page :as page]))


;; Components

(defn route-button
  []
  (fn [is-active? label icon on-click]
    [:> Button {:isActive is-active?
                :textAlign "start"
                :justifyContent "flex-start"
                :variant "ghost"
                :leftIcon icon
                :onClick on-click}
     label]))


(defn sidebar-task-el
  [task]
  (let [status (get task ":task/status")
        status-block (reactive/get-reactive-block-document [:block/uid status])
        status-string (:block/string status-block "(())")]
    [:> Flex {:display "inline-flex"
              :align "baseline"
              :py 1
              :gap 1}
     [:> Taskbox {:position "relative"
                  :top "3px"
                  :status (when-not (= status-string "(())") status-string)}]
     [:> Text {:fontSize "sm"
               :noOfLines 1} (get task ":task/title")]]))



(defn my-tasks
  []
  (let [all-tasks            (->> (reactive/get-reactive-instances-of-key-value ":entity/type" "[[athens/task]]")
                                  (map query/block-to-flat-map)
                                  (map query/get-root-page))
        me                   @(rf/subscribe [:presence/current-username])
        get-is-done          (fn [task]
                               (let [status (get task ":task/status")
                                     status-block (reactive/get-reactive-block-document [:block/uid status])
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
        max-tasks-shown      (r/atom 3)
        set-num-shown        (fn [num]
                               (reset! max-tasks-shown num))
        ;; sort by due date, then priority, then title
        sort-tasks-list      (fn [tasks]
                                 (sort-by (juxt #(get % ":task/due") #(get % ":task/priority") #(get % ":task/title")) tasks))
        get-num-done         (fn [tasks]
                               (count (filterv #(get-is-done %) tasks)))]

    [:> Widget {:defaultIsOpen true}

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
        [:> WidgetToggle]]]

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
          [:> Button {:isActive (= 3 @max-tasks-shown)
                      :onClick #(set-num-shown 3)}
           "3"]
          [:> Button {:isActive (= 7 @max-tasks-shown)
                      :onClick #(set-num-shown 7)}
           "7"]
          [:> Button {:isActive (= 20 @max-tasks-shown)
                      :onClick #(set-num-shown 20)}
           "Max"]]]]]]

     ;; Body of the main widget
     [:> WidgetBody {:as VStack
                     :py 2
                     :pl 6
                     :pr 4
                     :spacing 2
                     :align "stretch"}

      (doall
       (for [[page tasks] grouped-tasks]

         ;; Per page of tasks...
         ^{:key page}
         (let [pct-done (get-pct-done tasks)]
           [:> Widget {:defaultIsOpen true
                       :borderTop "1px solid"
                       :borderColor "separator.divider"
                       :pt 1}
            [:> Tooltip {:placement "right-start"
                         :label (r/as-element
                                 [:> VStack {:align "stretch"}
                                  [:> Text (str (get-num-done tasks) " / " (count tasks) " completed")]])}
             [:> WidgetHeader {:spacing 0}
              [:> CircularProgress {:thickness "16"
                                    :capIsRound true
                                    :size "1em"
                                    :trackColor "background.attic"
                                    :value pct-done}]
              [:> WidgetTitle page]
              [:> InfoIcon {:boxSize 3 :color "foreground.secondary"}]
              [:> WidgetToggle]]]
            [:> WidgetBody
             (doall
              (for [task (take @max-tasks-shown (sort-tasks-list tasks))]
                ^{:key (get task ":block/uid")}
                [:f> sidebar-task-el task]))]])))]]))


(defn left-sidebar
  []
  (let [current-route-name (rf/subscribe [:current-route/name])
        on-athena              #(rf/dispatch [:athena/toggle])
        on-theme               #(rf/dispatch [:theme/toggle])
        on-settings            (fn [_]
                                 (rf/dispatch [:settings/toggle-open]))
        route-name @current-route-name
        is-open? (rf/subscribe [:left-sidebar/open])
        shortcuts (reactive/get-reactive-shortcuts)]
    [:> MainSidebar {:isMainSidebarOpen @is-open?}

     [:> Flex {:flexDirection "column" :gap 6 :alignItems "stretch" :height "100%"}

      [:> VStack {:spacing 0.5
                  :role "nav"
                  :alignSelf "stretch"
                  :as ButtonGroup
                  :size "sm"
                  :align "stretch"
                  :px 4}
       [:> Button {:onClick on-athena
                   :variant "outline"
                   :justifyContent "start"
                   :leftIcon (r/as-element [:> SearchIcon])}
        "Find or Create a Page"]
       [route-button (= route-name :home) "Daily Notes" (r/as-element [:> CalendarEditFillIcon]) (fn [_]
                                                                                                   (rf/dispatch [:reporting/navigation {:source :main-sidebar
                                                                                                                                        :target :home
                                                                                                                                        :pane   :main-pane}])
                                                                                                   (router/nav-daily-notes))]
       [route-button (= route-name :pages) "All Pages" (r/as-element [:> AllPagesIcon]) (fn [_]
                                                                                          (rf/dispatch [:reporting/navigation {:source :main-sidebar
                                                                                                                               :target :all-pages
                                                                                                                               :pane   :main-pane}])
                                                                                          (router/navigate :pages))]

       [route-button (= route-name :graph) "Graph" (r/as-element [:> GraphIcon]) (fn [_]
                                                                                   (rf/dispatch [:reporting/navigation {:source :main-sidebar
                                                                                                                        :target :graph
                                                                                                                        :pane   :main-pane}])
                                                                                   (router/navigate :graph))]]

      [:f> my-tasks]

      ;; SHORTCUTS
      [:> Widget
       {:pr 4
        :defaultIsOpen true}
       [:> WidgetHeader {:title "Shortcuts"
                         :pl 6}
        [:> WidgetToggle]]
       [:> WidgetBody
        [:> List {:items shortcuts
                  :onOpenItem (fn [e [_order page]]
                                (let [shift? (.-shiftKey e)]
                                  (rf/dispatch [:reporting/navigation {:source :left-sidebar
                                                                       :target :page
                                                                       :pane   (if shift?
                                                                                 :right-pane
                                                                                 :main-pane)}])
                                  (router/navigate-page page e)))
                  :onUpdateItemsOrder (fn [oldIndex newIndex]
                                        (cond
                                          (< oldIndex newIndex) (rf/dispatch [:left-sidebar/drop oldIndex newIndex :after])
                                          (> oldIndex newIndex) (rf/dispatch [:left-sidebar/drop oldIndex newIndex :before])))}]]]

      ;; LOGO + BOTTOM BUTTONS
      [:> Flex {:as "footer"
                :align "stretch"
                :flexDirection "column"
                :flex "0 0 auto"
                :fontSize "xs"
                :mt "auto"
                :color "foreground.secondary"
                :px 6}
       [:> Divider]
       [:> Flex {:alignItems "center" :py 4}
        [:> Flex {:flex 1 :gap 2}
         [:> Link {:fontWeight "bold"
                   :display "inline-block"
                   :href "https://github.com/athensresearch/athens/issues/new/choose"
                   :target "_blank"}
          "Athens"]
         [:> Link {:color "foreground.secondary"
                   :display "inline-block"
                   :href "https://github.com/athensresearch/athens/blob/master/CHANGELOG.md"
                   :target "_blank"}
          (util/athens-version)]]
        [:> ButtonGroup {:size "sm" :spacing 0 :variant "ghost" :colorScheme "subtle"}

         [:> IconButton {:onClick on-theme
                         :icon (r/as-element [:> ContrastIcon])}]

         [:> IconButton {:onClick on-settings
                         :icon (r/as-element [:> SettingsIcon])}]]]]]]))
