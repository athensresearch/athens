(ns athens.views.pages.node-page
  (:require
    ["@material-ui/core/Popover" :as Popover]
    ["@material-ui/icons/Bookmark" :default Bookmark]
    ["@material-ui/icons/BookmarkBorder" :default BookmarkBorder]
    ["@material-ui/icons/BubbleChart" :default BubbleChart]
    ["@material-ui/icons/ChevronRight" :default ChevronRight]
    ["@material-ui/icons/Delete" :default Delete]
    ["@material-ui/icons/KeyboardArrowDown" :default KeyboardArrowDown]
    ["@material-ui/icons/Link" :default Link]
    ["@material-ui/icons/MoreHoriz" :default MoreHoriz]
    [athens.db :as db :refer [get-linked-references get-unlinked-references]]
    [athens.parse-renderer :as parse-renderer :refer [pull-node-from-string parse-and-render]]
    [athens.patterns :as patterns]
    [athens.router :refer [navigate-uid navigate]]
    [athens.style :refer [color DEPTH-SHADOWS]]
    [athens.util :refer [now-ts gen-block-uid escape-str is-daily-note get-caret-position recursively-modify-block-for-embed]]
    [athens.views.alerts :refer [alert-component]]
    [athens.views.blocks.bullet :as bullet]
    [athens.views.blocks.core :as blocks]
    [athens.views.blocks.textarea-keydown :as textarea-keydown]
    [athens.views.breadcrumbs :refer [breadcrumbs-list breadcrumb]]
    [athens.views.buttons :refer [button]]
    [athens.views.dropdown :refer [menu-style menu-separator-style]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as str]
    [datascript.core :as d]
    [garden.selectors :as selectors]
    [komponentit.autosize :as autosize]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]])
  (:import
    (goog.events
      KeyCodes)))

;;-------------------------------------------------------------------
;;--- material ui ---


(def m-popover (r/adapt-react-class (.-default Popover)))

;;; Styles


(def page-style
  {:margin "2rem auto"
   :padding "1rem 2rem 10rem 2rem"
   :flex-basis "100%"
   :max-width "55rem"})


(def dropdown-style
  {::stylefy/manual [[:.menu {:background (color :background-plus-2)
                              :border-radius "calc(0.25rem + 0.25rem)" ;; Button corner radius + container padding makes "concentric" container radius
                              :padding "0.25rem"
                              :display "inline-flex"
                              :box-shadow [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px rgba(0, 0, 0, 0.05)"]]}]]})


(def page-header-style
  {:position "relative"})


(def title-style
  {:position "relative"
   :overflow "visible"
   :flex-grow "1"
   :margin "0.10em 0 0.10em 1rem"
   :letter-spacing "-0.03em"
   :white-space "pre-line"
   :word-break "break-word"
   :line-height "1.40em"
   ::stylefy/manual [[:textarea {:display "none"}]
                     [:&:hover [:textarea {:display "block"
                                           :z-index 1}]]
                     [:textarea {:-webkit-appearance "none"
                                 :cursor "text"
                                 :resize "none"
                                 :transform "translate3d(0,0,0)"
                                 :color "inherit"
                                 :font-weight "inherit"
                                 :padding "0"
                                 :letter-spacing "inherit"
                                 :position "absolute"
                                 :top "0"
                                 :left "0"
                                 :right "0"
                                 :width "100%"
                                 :min-height "100%"
                                 :caret-color (color :link-color)
                                 :background "transparent"
                                 :margin "0"
                                 :font-size "inherit"
                                 :line-height "inherit"
                                 :border-radius "0.25rem"
                                 :transition "opacity 0.15s ease"
                                 :border "0"
                                 :opacity "0"
                                 :font-family "inherit"}]
                     [:textarea:focus
                      :.is-editing {:outline "none"
                                    :z-index 3
                                    :display "block"
                                    :opacity "1"}]
                     [:abbr {:z-index 4}]
                     [(selectors/+ :.is-editing :span) {:opacity 0}]]})


(def references-style {:margin-top "3em"})


(def references-heading-style
  {:font-weight "normal"
   :display "flex"
   :padding "0 0.5rem 0 0"
   :align-items "center"
   ::stylefy/manual [[:svg {:margin-right "0.25em"
                            :font-size "1rem"}]]})


(def references-list-style
  {:font-size "14px"})


(def references-group-title-style
  {:color (color :link-color)
   :margin "0 1.5rem"
   :font-weight "500"
   ::stylefy/manual [[:a:hover {:cursor "pointer"
                                :text-decoration "underline"}]]})


(def references-group-style
  {:background (color :background-minus-2 :opacity-med)
   :padding "1rem 0.5rem"
   :border-radius "0.25rem"
   :margin "0.5em 0"})


(def reference-breadcrumbs-style
  {:font-size "12px"
   :padding "0.25rem calc(2rem - 0.5em)"})


(def references-group-block-style
  {:border-top [["1px solid " (color :border-color)]]
   :width      "100%"
   :padding-block-start "1em"
   :margin-block-start "1em"
   ::stylefy/manual [[:&:first-of-type {:border-top "0"
                                        :margin-block-start "0"}]]})


(def page-menu-toggle-style
  {:position "absolute"
   :left "-1.5rem"
   :border-radius "1000px"
   :padding "0.375rem 0.5rem"
   :color (color :body-text-color :opacity-high)
   :top "0.5rem"})


;;; Helpers


(defn handle-new-first-child-block-click
  [parent-uid]
  (let [new-uid   (gen-block-uid)
        now       (now-ts)]
    (dispatch [:transact [{:block/uid       parent-uid
                           :edit/time       now
                           :block/children  [{:block/order  0
                                              :block/uid    new-uid
                                              :block/open   true
                                              :block/string ""}]}]])
    (dispatch [:editing/uid new-uid])))


(defn handle-enter
  [e uid _state children]
  (.. e preventDefault)
  (let [node-page  (.. e -target (closest ".node-page"))
        block-page (.. e -target (closest ".block-page"))
        {:keys [start value]} (textarea-keydown/destruct-key-down e)]
    (cond
      block-page (dispatch [:split-block-to-children uid value start])
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


(defn map-new-refs
  "Find and replace linked ref with new linked ref, based on title change."
  [linked-refs old-title new-title]
  (map (fn [{:block/keys [uid string]}]
         (let [new-str (str/replace string
                                    (patterns/linked old-title)
                                    (str "$1$3$4" new-title "$2$5"))]
           {:db/id [:block/uid uid]
            :block/string new-str}))
       linked-refs))


(defn get-existing-page
  "?uid used for navigate-uid. Go to existing page following the merge."
  [local-title]
  (d/q '[:find ?uid .
         :in $ ?t
         :where
         [?e :node/title ?t]
         [?e :block/uid ?uid]]
       @db/dsdb local-title))


(defn existing-block-count
  "Count is used to reindex blocks after merge."
  [local-title]
  (count (d/q '[:find [?ch ...]
                :in $ ?t
                :where
                [?e :node/title ?t]
                [?e :block/children ?ch]]
              @db/dsdb local-title)))


(declare init-state)


(defn handle-blur
  "When textarea blurs and its value is different from initial page title:
   - if no other page exists, rewrite page title and linked refs
   - else page with same title does exists: prompt to merge
     - confirm-fn: delete current page, rewrite linked refs, merge blocks, and navigate to existing page
     - cancel-fn: reset state
  The current blocks will be at the end of the existing page."
  [node state linked-refs]
  (let [{dbid :db/id children :block/children} node
        {:keys [title/initial title/local]} @state]
    (when (not= initial local)
      (let [existing-page     (get-existing-page local)
            linked-ref-blocks (mapcat second linked-refs)
            new-linked-refs   (map-new-refs linked-ref-blocks initial local)]
        (if (empty? existing-page)
          (let [new-page   {:db/id dbid :node/title local}
                new-datoms (concat [new-page] new-linked-refs)]
            (swap! state assoc :title/initial local)
            (dispatch [:transact new-datoms]))
          (let [new-parent-uid            existing-page
                existing-page-block-count (existing-block-count local)
                reindex                   (map (fn [{:block/keys [order uid]}]
                                                 {:db/id           [:block/uid uid]
                                                  :block/order     (+ order existing-page-block-count)
                                                  :block/_children [:block/uid new-parent-uid]})
                                               children)
                delete-page               [:db/retractEntity dbid]
                new-datoms                (concat [delete-page]
                                                  new-linked-refs
                                                  reindex)
                cancel-fn                 #(swap! state merge init-state)
                confirm-fn                (fn []
                                            (navigate-uid new-parent-uid)
                                            (dispatch [:transact new-datoms])
                                            (cancel-fn))]
            (swap! state assoc
                   :alert/show true
                   :alert/message (str "\"" local "\"" " already exists, merge pages?")
                   :alert/confirm-fn confirm-fn
                   :alert/cancel-fn cancel-fn)))))))


;;; Components

(defn placeholder-block-el
  [parent-uid]
  [:div {:class "block-container"}
   [:div {:style {:display "flex"}}
    [:span (use-style bullet/bullet-style)]
    [:span {:on-click #(handle-new-first-child-block-click parent-uid)} "Click here to add content..."]]])


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
  (let [{:block/keys [uid] sidebar :page/sidebar title :node/title} node]
    (r/with-let [ele (r/atom nil)]
                [:<>
                 [button {:class    [(when @ele "is-active")]
                          :on-click #(reset! ele (.-currentTarget %))
                          :style    page-menu-toggle-style}
                  [:> MoreHoriz]]
                 [m-popover
                  (merge (use-style dropdown-style)
                         {:style {:font-size "14px"}
                          :open            @ele
                          :anchorEl        @ele
                          :onClose         #(reset! ele nil)
                          :anchorOrigin    #js{:vertical   "bottom"
                                               :horizontal "left"}
                          :marginThreshold 10
                          :transformOrigin #js{:vertical   "top"
                                               :horizontal "left"}
                          :classes {:root "backdrop"
                                    :paper "menu"}})
                  [:div (use-style menu-style)
                   [:<>
                    (if sidebar
                      [button {:on-click #(dispatch [:page/remove-shortcut uid])}
                       [:<>
                        [:> BookmarkBorder]
                        [:span "Remove Shortcut"]]]
                      [button {:on-click #(dispatch [:page/add-shortcut uid])}
                       [:<>
                        [:> Bookmark]
                        [:span "Add Shortcut"]]])
                    [button {:on-click #(dispatch [:right-sidebar/open-item uid true])}
                     [:<>
                      [:> BubbleChart]
                      [:span "Show Local Graph"]]]]
                   [:hr (use-style menu-separator-style)]
                   [button {:on-click #(if daily-note?
                                         (dispatch [:daily-note/delete uid title])
                                         (do
                                           (navigate :pages)
                                           (dispatch [:page/delete uid title])))}
                    [:<> [:> Delete] [:span "Delete Page"]]]]]])))


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
            block (db/get-block-document (:db/id block))]
        [:<>
         [breadcrumbs-list {:style reference-breadcrumbs-style}
          (doall
            (for [{:keys [node/title block/string block/uid]} parents]
              [breadcrumb {:key       (str "breadcrumb-" uid)
                           :on-click #(do (let [new-B (db/get-block-document [:block/uid uid])
                                                new-P (drop-last parents)]
                                            (swap! state assoc :block new-B :parents new-P)))}
               [parse-and-render (or title string) uid]]))]
         [:div.block-embed
          [blocks/block-el
           (recursively-modify-block-for-embed block embed-id)
           linked-ref-data
           {:block-embed? true}]]]))))


(defn linked-ref-el
  [state daily-notes? linked-refs]
  (let [linked? "Linked References"]
    (when (or (and daily-notes? (not-empty linked-refs))
              (not daily-notes?))
      [:section (use-style references-style)
       [:h4 (use-style references-heading-style)
        [button {:on-click (fn [] (swap! state update linked? not))}
         (if (get @state linked?)
           [:> KeyboardArrowDown]
           [:> ChevronRight])]
        [(r/adapt-react-class Link)]
        [:div {:style {:display "flex"
                       :flex "1 1 100%"
                       :justify-content "space-between"}}
         [:span linked?]]]
       (when (get @state linked?)
         [:div (use-style references-list-style)
          (doall
            (for [[group-title group] linked-refs]
              [:div (use-style references-group-style {:key (str "group-" group-title)})
               [:h4 (use-style references-group-title-style)
                [:a {:on-click #(navigate-uid (:block/uid @(pull-node-from-string group-title)) %)} group-title]]
               (doall
                 (for [block group]
                   ^{:key (str "ref-" (:block/uid block))}
                   [:div {:style {:display "flex"
                                  :flex "1 1 100%"
                                  :justify-content "space-between"
                                  :align-items "flex-start"}}
                    [:div (use-style references-group-block-style)
                     [ref-comp block]]]))]))])])))


(defn unlinked-ref-el
  [state daily-notes? unlinked-refs title]
  (let [unlinked? "Unlinked References"]
    (when (not daily-notes?)
      [:section (use-style references-style)
       [:h4 (use-style references-heading-style)
        [button {:on-click (fn []
                             (if (get @state unlinked?)
                               (swap! state assoc unlinked? false)
                               (let [un-refs (get-unlinked-references (escape-str title))]
                                 (swap! state assoc unlinked? true)
                                 (reset! unlinked-refs un-refs))))}
         (if (get @state unlinked?)
           [:> KeyboardArrowDown]
           [:> ChevronRight])]
        [(r/adapt-react-class Link)]
        [:div {:style {:display         "flex"
                       :justify-content "space-between"
                       :width "100%"}}
         [:span unlinked?]
         (when (and unlinked? (not-empty @unlinked-refs))
           [button {:style    {:font-size "14px"}
                    :on-click (fn []
                                (dispatch [:unlinked-references/link-all @unlinked-refs title])
                                (swap! state assoc unlinked? false)
                                (reset! unlinked-refs []))}
            "Link All"])]]
       (when (get @state unlinked?)
         [:div (use-style references-list-style)
          (doall
            (for [[group-title group] @unlinked-refs]
              [:div (use-style references-group-style {:key (str "group-" group-title)})
               [:h4 (use-style references-group-title-style)
                [:a {:on-click #(navigate-uid (:block/uid @(pull-node-from-string group-title)) %)} group-title]]
               (doall
                 (for [block group]
                   ^{:key (str "ref-" (:block/uid block))}
                   [:div {:style {:display         "flex"
                                  :justify-content "space-between"
                                  :align-items     "flex-start"}}
                    [:div (merge
                            (use-style references-group-block-style)
                            {:style {:max-width "90%"}})
                     [ref-comp block]]
                    (when unlinked?
                      [button {:style    {:margin-top "1.5em"}
                               :on-click (fn []
                                           (let [hm                (into (hash-map) @unlinked-refs)
                                                 new-unlinked-refs (->> (update-in hm [group-title] #(filter (fn [{:keys [block/uid]}]
                                                                                                               (= uid (:block/uid block)))
                                                                                                             %))
                                                                        seq)]
                                             ;; ctrl-z doesn't work though, because Unlinked Refs aren't reactive to datascript.
                                             (reset! unlinked-refs new-unlinked-refs)
                                             (dispatch [:unlinked-references/link block title])))}
                       "Link"])]))]))])])))

;; TODO: where to put page-level link filters?
(defn node-page-el
  "title/initial is the title when a page is first loaded.
  title/local is the value of the textarea.
  We have both, because we want to be able to change the local title without transacting to the db until user confirms.
  Similar to atom-string in blocks. Hacky, but state consistency is hard!"
  [_ _ _ _]
  (let [state         (r/atom init-state)
        unlinked-refs (r/atom [])]
    (fn [node editing-uid linked-refs]
      (let [{:block/keys [children uid] title :node/title} node
            {:alert/keys [message confirm-fn cancel-fn] alert-show :alert/show} @state
            daily-note?  (is-daily-note uid)
            on-daily-notes? (= :home @(subscribe [:current-route/name]))]


        (sync-title title state)

        [:div (use-style page-style {:class    ["node-page"]
                                     :data-uid uid})

         (when alert-show
           [:div (use-style {:position "absolute"
                             :top      "50px"
                             :left     "35%"})
            [alert-component message confirm-fn cancel-fn]])


         ;; Header
         [:header (use-style page-header-style)

          ;; Dropdown
          [menu-dropdown node state daily-note?]

          [:h1 (use-style title-style
                          {:data-uid uid
                           :class    "page-header"
                           :on-click (fn [e]
                                       (.. e preventDefault)
                                       (if (or daily-note? (.. e -shiftKey))
                                         (navigate-uid uid e)
                                         (dispatch [:editing/uid uid])))})
          ;; Prevent editable textarea if a node/title is a date
          ;; Don't allow title editing from daily notes, right sidebar, or node-page itself.


           (when-not daily-note?
             [autosize/textarea
              {:value       (:title/local @state)
               :id          (str "editable-uid-" uid)
               :class       (when (= editing-uid uid) "is-editing")
               :on-blur     (fn [_]
                             ;; add title Untitled-n for empty titles
                              (when (empty? (:title/local @state))
                                (swap! state assoc :title/local (auto-inc-untitled)))
                              (handle-blur node state linked-refs))
               :on-key-down (fn [e] (handle-key-down e uid state children))
               :on-change   (fn [e] (handle-change e state))}])
          ;; empty word break to keep span on full height else it will collapse to 0 height (weird ui)
           (if (str/blank? (:title/local @state))
             [:wbr]
             [parse-renderer/parse-and-render (:title/local @state) uid])]]

         ;; Children
         (if (empty? children)
           [placeholder-block-el uid]
           [:div
            (for [{:block/keys [uid] :as child} children]
              ^{:key uid}
              [blocks/block-el child])])

         ;; References
         [linked-ref-el state on-daily-notes? linked-refs]
         [unlinked-ref-el state on-daily-notes? unlinked-refs title]]))))


(defn page
  [ident]
  (let [{:keys [#_block/uid node/title] :as node} (db/get-node-document ident)
        editing-uid   @(subscribe [:editing/uid])
        linked-refs   (get-linked-references title)]
    [node-page-el node editing-uid linked-refs]))
