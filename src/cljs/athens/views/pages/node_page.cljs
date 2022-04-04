(ns athens.views.pages.node-page
  (:require
    ["/components/Block/components/Anchor" :refer [Anchor]]
    ["/components/Confirmation/Confirmation" :refer [Confirmation]]
    ["/components/Page/Page" :refer [PageHeader PageBody PageFooter EditableTitleContainer]]
    ["@chakra-ui/react" :refer [Text Box Button Portal IconButton AccordionIcon AccordionItem AccordionPanel MenuDivider MenuButton Menu MenuList MenuItem Accordion AccordionButton Breadcrumb BreadcrumbItem BreadcrumbLink VStack]]
    ["@material-ui/icons/Bookmark" :default Bookmark]
    ["@material-ui/icons/BookmarkBorder" :default BookmarkBorder]
    ["@material-ui/icons/BubbleChart" :default BubbleChart]
    ["@material-ui/icons/Delete" :default Delete]
    ["@material-ui/icons/MoreHoriz" :default MoreHoriz]
    [athens.common-db :as common-db]
    [athens.common.sentry :refer-macros [wrap-span-no-new-tx]]
    [athens.common.utils :as utils]
    [athens.dates :as dates]
    [athens.db :as db :refer [get-unlinked-references]]
    [athens.parse-renderer :as parse-renderer :refer [parse-and-render]]
    [athens.reactive :as reactive]
    [athens.router :as router]
    [athens.util :refer [escape-str get-caret-position recursively-modify-block-for-embed]]
    [athens.views.blocks.core :as blocks]
    [athens.views.blocks.textarea-keydown :as textarea-keydown]
    [athens.views.hoc.perf-mon :as perf-mon]
    [athens.views.references :refer [reference-group reference-block]]
    [clojure.string :as str]
    [datascript.core :as d]
    [komponentit.autosize :as autosize]
    [re-frame.core :as rf :refer [dispatch subscribe]]
    [reagent.core :as r])
  (:import
    (goog.events
      KeyCodes)))


;; Helpers


(defn handle-new-first-child-block-click
  [parent-uid]
  (let [new-uid               (utils/gen-block-uid)
        [parent-uid embed-id] (db/uid-and-embed-id parent-uid)
        parent-block          (db/get-block [:block/uid parent-uid])]
    (dispatch [:enter/add-child {:block     parent-block
                                 :new-uid   new-uid
                                 :embed-id  embed-id}])
    (dispatch [:editing/uid new-uid])))


(defn handle-enter
  [e uid _state children]
  (.. e preventDefault)
  (let [node-page             (.. e -target (closest ".node-page"))
        block-page            (.. e -target (closest ".block-page"))
        [uid embed-id]        (common-db/uid-and-embed-id uid)
        new-uid               (utils/gen-block-uid)
        {:keys [start value]} (textarea-keydown/destruct-key-down e)]
    (cond
      block-page (dispatch [:enter/split-block {:uid        uid
                                                :value      value
                                                :index      start
                                                :new-uid    new-uid
                                                :embed-id   embed-id
                                                :relation   :first}])
      node-page (if (empty? children)
                  (handle-new-first-child-block-click uid)
                  (dispatch [:down uid])))))


(defn handle-page-arrow-key
  [e uid state]
  (let [{:keys [key-code target]} (textarea-keydown/destruct-key-down e)
        start?          (textarea-keydown/block-start? e)
        end?            (textarea-keydown/block-end? e)
        {caret-position :caret-position} @state
        textarea-height (.. target -offsetHeight)
        {:keys [top height]} caret-position
        rows            (js/Math.round (/ textarea-height height))
        row             (js/Math.ceil (/ top height))
        top-row?        (= row 1)
        bottom-row?     (= row rows)
        up?             (= key-code KeyCodes.UP)
        down?           (= key-code KeyCodes.DOWN)
        left?           (= key-code KeyCodes.LEFT)
        right?          (= key-code KeyCodes.RIGHT)]

    (cond
      (or (and up? top-row?)
          (and left? start?)) (do (.. e preventDefault)
                                  (dispatch [:up uid e]))
      (or (and down? bottom-row?)
          (and right? end?)) (do (.. e preventDefault)
                                 (dispatch [:down uid e])))))


(defn handle-key-down
  [e uid state children]
  (let [{:keys [key-code shift]} (textarea-keydown/destruct-key-down e)
        caret-position (get-caret-position (.. e -target))]
    (swap! state assoc :caret-position caret-position)
    (cond
      (textarea-keydown/arrow-key-direction e) (handle-page-arrow-key e uid state)
      (and (not shift) (= key-code KeyCodes.ENTER)) (handle-enter e uid state children))))


(defn auto-inc-untitled
  ([] (auto-inc-untitled nil))
  ([n]
   (if (empty? (d/q '[:find [?e ...]
                      :in $ ?t
                      :where
                      [?e :node/title ?t]]
                    @db/dsdb (str "Untitled" (when n (str "-" n)))))
     (str "Untitled" (when n (str "-" n)))
     (auto-inc-untitled (+ n 1)))))


(defn handle-change
  [e state]
  (let [value (.. e -target -value)]
    (swap! state assoc :title/local value)))


(declare init-state)


(defn handle-blur
  "When textarea blurs and its value is different from initial page title:
   - if no other page exists, rewrite page title and linked refs
   - else page with same title does exists: prompt to merge
     - confirm-fn: delete current page, rewrite linked refs, merge blocks, and navigate to existing page
     - cancel-fn: reset state
  The current blocks will be at the end of the existing page."
  [node state]
  (let [{page-uid :block/uid} node
        {:title/keys [initial
                      local]} @state
        do-nothing?           (= initial local)]
    (js/console.debug "handle-blur: do-nothing?" do-nothing?
                      ", local:" (pr-str local)
                      ", page-uid:" page-uid)
    (when-not do-nothing?
      (let [existing-page-uid (common-db/get-page-uid @db/dsdb local)
            merge?            (not (nil?  existing-page-uid))]
        (js/console.debug "new-page-name:" (pr-str local)
                          ", existing-page-uid:" (pr-str existing-page-uid))
        (if-not merge?
          (dispatch [:page/rename {:page-uid page-uid
                                   :old-name initial
                                   :new-name local
                                   :callback #(swap! state assoc :title/initial local)}])

          (let [cancel-fn  #(swap! state merge init-state)
                confirm-fn (fn []
                             (rf/dispatch [:reporting/navigation {:source :page-title-merge
                                                                  :target :page
                                                                  :pane   :main-pane}])
                             (router/navigate-page local)
                             (rf/dispatch [:page/merge {:from-name initial
                                                        :to-name   local
                                                        :callback  cancel-fn}]))]
            ;; display alert
            ;; NOTE: alert should be global reusable component, not local to node_page
            (swap! state assoc
                   :alert/show true
                   :alert/message (str "\"" local "\"" " already exists, merge pages?")
                   :alert/confirm-fn confirm-fn
                   :alert/cancel-fn cancel-fn)))))))


;; Components

(defn placeholder-block-el
  [parent-uid]
  [:> Box {:class "block-container"
           :pl "1em"
           :display "flex"}
   [:> Anchor]
   [:> Button {:variant "link"
               :flex "1 1 100%"
               :pl 1
               :textAlign "left"
               :justifyContent "flex-start"
               :onClick #(handle-new-first-child-block-click parent-uid)}
    "Click here to add content..."]])


(defn sync-title
  "Ensures :title/initial is synced to node/title.
  Cases:
  - User opens a page for the first time.
  - User navigates from a page to another page.
  - User merges current page with existing page, navigating to existing page."
  [title state]
  (when (not= title (:title/initial @state))
    (swap! state assoc :title/initial title :title/local title)))


(def init-state
  {:menu/show            false
   :title/initial        nil
   :title/local          nil
   :alert/show           nil
   :alert/message        nil
   :alert/confirm-fn     nil
   :alert/cancel-fn      nil
   "Linked References"   true
   "Unlinked References" false})


(defn menu-dropdown
  [node daily-note?]
  (let [{:block/keys [uid] sidebar
         :page/sidebar title
         :node/title} node]
    [:> Menu
     [:> MenuButton {:as IconButton
                     :gridArea "menu"
                     :justifySelf "flex-end"
                     :alignSelf "center"
                     :bg "transparent"
                     :height "2.25em"
                     :width "2.25em"
                     :mr "0.5em"
                     :borderRadius "full"
                     :sx {"span" {:display "contents"}
                          "button svg:first-of-type" {:marginRight "0.25rem"}}}
      [:> MoreHoriz]]
     [:> Portal
      [:> MenuList {:sx {"button svg:first-of-type" {:marginRight "0.25rem"}}}
       [:<>
        (if sidebar
          [:> MenuItem {:onClick #(dispatch [:left-sidebar/remove-shortcut title])}
           [:> BookmarkBorder]
           "Remove Shortcut"]
          [:> MenuItem {:onClick #(dispatch [:left-sidebar/add-shortcut title])}
           [:> Bookmark]
           [:span "Add Shortcut"]])
        [:> MenuItem {:onClick #(dispatch [:right-sidebar/open-item uid true])}
         [:> BubbleChart]
         "Show Local Graph"]]
       [:> MenuDivider]
       [:> MenuItem {:onClick (fn []
                                ;; if page being deleted is in right sidebar, remove from right sidebar
                                (when (contains? @(subscribe [:right-sidebar/items]) uid)
                                  (dispatch [:right-sidebar/close-item uid]))
                                ;; if page being deleted is open, navigate to all pages
                                (when (or (= @(subscribe [:current-route/page-title]) title)
                                          (= @(subscribe [:current-route/uid]) uid))
                                  (rf/dispatch [:reporting/navigation {:source :page-title-delete
                                                                       :target :all-pages
                                                                       :pane   :main-pane}])
                                  (router/navigate :pages))
                                ;; if daily note, delete page and remove from daily notes, otherwise just delete page
                                (if daily-note?
                                  (dispatch [:daily-note/delete uid title])
                                  (dispatch [:page/delete title])))}
        [:> Delete] "Delete Page"]]]]))


(defn ref-comp
  [block]
  (let [state           (r/atom {:block     block
                                 :embed-id  (random-uuid)
                                 :parents   (rest (:block/parents block))})
        linked-ref-data {:linked-ref     true
                         :initial-open   true
                         :linked-ref-uid (:block/uid block)
                         :parent-uids    (set (map :block/uid (:block/parents block)))}]
    (fn [_]
      (let [{:keys [block parents embed-id]} @state
            block (reactive/get-reactive-block-document (:db/id block))]
        [:<>
         [:> Breadcrumb {:fontSize "0.7em" :pl 6 :opacity 0.75}
          (doall
            (for [{:keys [node/title block/string block/uid]} parents]
              [:> BreadcrumbItem {:key (str "breadcrumb-" uid)}
               [:> BreadcrumbLink
                {:onClick #(let [new-B (db/get-block [:block/uid uid])
                                 new-P (drop-last parents)]
                             (swap! state assoc :block new-B :parents new-P))}
                [parse-and-render (or title string) uid]]]))]
         [:> Box {:class "block-embed"}
          [blocks/block-el
           (recursively-modify-block-for-embed block embed-id)
           linked-ref-data
           {:block-embed? true}]]]))))


(defn linked-ref-el
  [state daily-notes? title]
  (let [linked?     "Linked References"
        linked-refs (wrap-span-no-new-tx "get-reactive-linked-references"
                                         (reactive/get-reactive-linked-references [:node/title title]))]
    (when (or (and daily-notes? (not-empty linked-refs))
              (not daily-notes?))
      [:> Accordion {:index (if (get @state linked?) 0 nil)}
       [:> AccordionItem
        [:h2
         [:> AccordionButton {:onClick (fn [] (swap! state update linked? not))}
          [:> AccordionIcon]
          linked?
          [:> Text {:ml "auto"
                    :fontWeight "medium"
                    :color "foreground.secondary"
                    :borderRadius "full"}
           (count linked-refs)]]]
        [:> AccordionPanel {:p 0}
         [:> VStack {:spacing 6
                     :pl 9
                     :align "stretch"}
          (doall
            (for [[group-title group] linked-refs]
              [reference-group {:key (str "group-" group-title)
                                :title group-title
                                :on-click-title (fn [e]
                                                  (let [shift?       (.-shiftKey e)
                                                        parsed-title (parse-renderer/parse-title group-title)]
                                                    (rf/dispatch [:reporting/navigation {:source :main-page-linked-refs ; NOTE: this might be also used in right-pane situation
                                                                                         :target :page
                                                                                         :pane   (if shift?
                                                                                                   :right-pane
                                                                                                   :main-pane)}])
                                                    (router/navigate-page parsed-title e)))}
               (doall
                 (for [block group]
                   [reference-block {:key (str "ref-" (:block/uid block))}
                    [ref-comp block]]))]))]]]])))


(defn unlinked-ref-el
  [state daily-notes? unlinked-refs title]
  (let [unlinked? "Unlinked References"]
    (when (not daily-notes?)
      [:> Accordion {:index (if (get @state unlinked?) 0 nil)}
       [:> AccordionItem {:isDisabled (empty @unlinked-refs)}
        [:> Box {:as "h2" :position "relative"}
         [:> AccordionButton {:onClick (fn []
                                         (if (get @state unlinked?)
                                           (swap! state assoc unlinked? false)
                                           (let [un-refs (get-unlinked-references (escape-str title))]
                                             (swap! state assoc unlinked? true)
                                             (reset! unlinked-refs un-refs))))}
          [:> AccordionIcon]
          unlinked?
          [:> Text {:ml "auto"
                    :fontWeight "medium"
                    :color "foreground.secondary"
                    :borderRadius "full"}
           (count @unlinked-refs)]]
         (when (and unlinked? (not-empty @unlinked-refs))
           [:> Button {:position "absolute"
                       :size "xs"
                       :top 1
                       :right 1
                       :onClick (fn []
                                  (let [unlinked-str-ids (->> @unlinked-refs
                                                              (mapcat second)
                                                              (map #(select-keys % [:block/string :block/uid])))] ; to remove the unnecessary data before dispatching the event
                                    (dispatch [:unlinked-references/link-all unlinked-str-ids title]))

                                  (swap! state assoc unlinked? false)

                                  (reset! unlinked-refs []))}
            "Link All"])]
        [:> AccordionPanel {:p 0}
         [:> VStack {:spacing 6
                     :pl 1
                     :align "stretch"}
          (doall
            (for [[[group-title] group] @unlinked-refs]
              [reference-group
               {:title group-title
                :on-click-title (fn [e]
                                  (let [shift?       (.-shiftKey e)
                                        parsed-title (parse-renderer/parse-title group-title)]
                                    (rf/dispatch [:reporting/navigation {:source :main-unlinked-refs ; NOTE: this isn't always `:main-unlinked-refs` it can also be `:right-pane-unlinked-refs`
                                                                         :target :page
                                                                         :pane   (if shift?
                                                                                   :right-pane
                                                                                   :main-pane)}])
                                    (router/navigate-page parsed-title e)))}
               (doall
                 (for [block group]
                   [reference-block
                    {:key (str "ref-" (:block/uid block))
                     :actions (when unlinked?
                                [:> Button {:marginTop "1.5em"
                                            :size "xs"
                                            :flex "0 0"
                                            :float "right"
                                            :variant "link"
                                            :onClick (fn []
                                                       (let [hm                (into (hash-map) @unlinked-refs)
                                                             new-unlinked-refs (->> (update-in hm [group-title] #(filter (fn [{:keys [block/uid]}]
                                                                                                                           (= uid (:block/uid block)))
                                                                                                                         %))
                                                                                    seq)]
                                                         ;; ctrl-z doesn't work though, because Unlinked Refs aren't reactive to datascript.
                                                         (reset! unlinked-refs new-unlinked-refs)
                                                         (dispatch [:unlinked-references/link block title])))}
                                 "Link"])}
                    [ref-comp block]]))]))]]]])))


;; TODO: where to put page-level link filters?
(defn node-page-el
  "title/initial is the title when a page is first loaded.
  title/local is the value of the textarea.
  We have both, because we want to be able to change the local title without transacting to the db until user confirms.
  Similar to atom-string in blocks. Hacky, but state consistency is hard!"
  [_]
  (let [state         (r/atom init-state)
        unlinked-refs (r/atom [])
        block-uid     (r/atom nil)]
    (fn [node]
      (when (not= @block-uid (:block/uid node))
        (reset! state init-state)
        (reset! unlinked-refs [])
        (reset! block-uid (:block/uid node)))
      (let [{:block/keys [children uid] title :node/title}                      node
            {:alert/keys [message confirm-fn cancel-fn] alert-show :alert/show} @state
            daily-note?                                                         (dates/is-daily-note uid)
            on-daily-notes?                                                     (= :home @(subscribe [:current-route/name]))]

        (sync-title title state)

        [:<>

         [:> Confirmation {:isOpen    alert-show
                           :title     message
                           :onConfirm confirm-fn
                           :onClose   cancel-fn}]

         ;; Header
         [:> PageHeader

          ;; Dropdown
          [menu-dropdown node daily-note?]

          [:> EditableTitleContainer
           {:isEditing @(subscribe [:editing/is-editing uid])}
           ;; Prevent editable textarea if a node/title is a date
           ;; Don't allow title editing from daily notes, right sidebar, or node-page itself.

           (when-not daily-note?
             [autosize/textarea
              {:value       (:title/local @state)
               :id          (str "editable-uid-" uid)
               :class       (when @(subscribe [:editing/is-editing uid]) "is-editing")
               :on-blur     (fn [_]
                              ;; add title Untitled-n for empty titles
                              (when (empty? (:title/local @state))
                                (swap! state assoc :title/local (auto-inc-untitled)))
                              (handle-blur node state))
               :on-key-down (fn [e] (handle-key-down e uid state children))
               :on-change   (fn [e] (handle-change e state))}])
           ;; empty word break to keep span on full height else it will collapse to 0 height (weird ui)
           (if (str/blank? (:title/local @state))
             [:wbr]
             [perf-mon/hoc-perfmon {:span-name "parse-and-render"}
              [parse-renderer/parse-and-render (:title/local @state) uid]])]]

         [:> PageBody

          ;; Children
          (if (empty? children)
            [placeholder-block-el uid]
            [:div
             (for [{:block/keys [uid] :as child} children]
               ^{:key uid}
               [perf-mon/hoc-perfmon {:span-name "block-el"}
                [blocks/block-el child]])])]

         ;; References
         [:> PageFooter
          [perf-mon/hoc-perfmon-no-new-tx {:span-name "linked-ref-el"}
           [linked-ref-el state on-daily-notes? title]]
          [perf-mon/hoc-perfmon-no-new-tx {:span-name "unlinked-ref-el"}
           [unlinked-ref-el state on-daily-notes? unlinked-refs title]]]]))))


(defn page
  [ident]
  (let [node (wrap-span-no-new-tx "db/get-reactive-node-document"
                                  (reactive/get-reactive-node-document ident))]
    [node-page-el node]))
