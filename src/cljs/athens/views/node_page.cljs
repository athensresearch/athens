(ns athens.views.node-page
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db :refer [get-linked-references get-unlinked-references]]
    [athens.keybindings :refer [destruct-key-down arrow-key-direction block-start? block-end?]]
    [athens.parse-renderer :as parse-renderer :refer [pull-node-from-string]]
    [athens.patterns :as patterns]
    [athens.router :refer [navigate-uid navigate]]
    [athens.style :refer [color]]
    [athens.util :refer [now-ts gen-block-uid escape-str is-timeline-page get-caret-position]]
    [athens.views.alerts :refer [alert-component]]
    [athens.views.blocks :refer [block-el bullet-style]]
    [athens.views.breadcrumbs :refer [breadcrumbs-list breadcrumb]]
    [athens.views.buttons :refer [button]]
    [athens.views.dropdown :refer [dropdown-style menu-style menu-separator-style]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as str]
    [datascript.core :as d]
    [garden.selectors :as selectors]
    [goog.events :refer [listen unlisten]]
    [komponentit.autosize :as autosize]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]])
  (:import
    (goog.events
      KeyCodes)))

;;; Styles


(def page-style
  {:margin "2rem auto"
   :padding "1rem 2rem 10rem 2rem"
   :flex-basis "100%"
   :max-width "55rem"})


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
                     [(selectors/+ :.is-editing :span) {:opacity 0}]]})


(def references-style {:margin-block "3em"})


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
   :padding-block-start "1em"
   :margin-block-start "1em"
   ::stylefy/manual [[:&:first-of-type {:border-top "0"
                                        :margin-block-start "0"}]]})


(def page-menu-toggle-style
  {:position "absolute"
   :left "-0.5rem"
   :border-radius "1000px"
   :padding "0.375rem 0.5rem"
   :color (color :body-text-color :opacity-high)
   :top "50%"
   :transform "translate(-100%, -50%)"})


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
        {:keys [start value]} (destruct-key-down e)]
    (cond
      block-page (dispatch [:split-block-to-children uid value start])
      node-page (if (empty? children)
                  (handle-new-first-child-block-click uid)
                  (dispatch [:down])))))


(defn handle-page-arrow-key
  [e uid state]
  (let [{:keys [key-code target]} (destruct-key-down e)
        start?          (block-start? e)
        end?            (block-end? e)
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
                                  (dispatch [:up uid]))
      (or (and down? bottom-row?)
          (and right? end?)) (do (.. e preventDefault)
                                 (dispatch [:down uid])))))


(defn handle-key-down
  [e uid state children]
  (let [{:keys [key-code shift]} (destruct-key-down e)
        caret-position (get-caret-position (.. e -target))]
    (swap! state assoc :caret-position caret-position)
    (cond
      (arrow-key-direction e) (handle-page-arrow-key e uid state)
      (and (not shift) (= key-code KeyCodes.ENTER)) (handle-enter e uid state children))))


(defn handle-change
  [e state]
  (let [value (.. e -target -value)]
    (swap! state assoc :title/local value)))


(defn get-linked-refs
  [ref-groups]
  (->> ref-groups
       first
       second
       (mapcat second)))


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
  [node state ref-groups]
  (let [{dbid :db/id children :block/children} node
        {:keys [title/initial title/local]} @state]
    (when (not= initial local)
      (let [existing-page   (get-existing-page local)
            linked-refs     (get-linked-refs ref-groups)
            new-linked-refs (map-new-refs linked-refs initial local)]
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
    [:span (use-style bullet-style)]
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
   :menu/x               nil
   :menu/y               nil
   :title/initial        nil
   :title/local          nil
   :alert/show           nil
   :alert/message        nil
   :alert/confirm-fn     nil
   :alert/cancel-fn      nil
   "Linked References"   true
   "Unlinked References" false})


(defn menu-dropdown
  [_node state]
  (let [ref                  (atom nil)
        handle-click-outside (fn [e]
                               (when (and (:menu/show @state)
                                          (not (.. @ref (contains (.. e -target)))))
                                 (swap! state assoc :menu/show false)))]
    (r/create-class
      {:display-name "node-page-menu"
       :component-did-mount (fn [_this] (listen js/document "mousedown" handle-click-outside))
       :component-will-unmount (fn [_this] (unlisten js/document "mousedown" handle-click-outside))
       :reagent-render   (fn [node state]
                           (let [{:block/keys [uid] sidebar :page/sidebar title :node/title} node
                                 {:menu/keys [show x y]} @state
                                 timeline-page? (is-timeline-page uid)]
                             (when show
                               [:div (merge (use-style dropdown-style
                                                       {:ref #(reset! ref %)})
                                            {:style {:font-size "14px"
                                                     :position "fixed"
                                                     :left (str x "px")
                                                     :top (str y "px")}})
                                [:div (use-style menu-style)
                                 (if sidebar
                                   [button {:on-click #(dispatch [:page/remove-shortcut uid])}
                                    [:<>
                                     [:> mui-icons/BookmarkBorder]
                                     [:span "Remove Shortcut"]]]
                                   [button {:on-click #(dispatch [:page/add-shortcut uid])}
                                    [:<>
                                     [:> mui-icons/Bookmark]
                                     [:span "Add Shortcut"]]])
                                 (when-not timeline-page?
                                   [:hr (use-style menu-separator-style)])
                                 (when-not timeline-page?
                                   [button {:on-click #(do
                                                         (navigate :pages)
                                                         (dispatch [:page/delete uid title]))}
                                    [:<> [:> mui-icons/Delete] [:span "Delete Page"]]])]])))})))


(defn ref-comp
  [block]
  (let [state           (r/atom {:block   block
                                 :parents (rest (:block/parents block))})
        linked-ref-data {:linked-ref     true
                         :initial-open   true
                         :linked-ref-uid (:block/uid block)
                         :parent-uids    (set (map :block/uid (:block/parents block)))}]
    (fn [_]
      (let [{:keys [block parents]} @state
            block (db/get-block-document (:db/id block))]
        [:<>
         [breadcrumbs-list {:style reference-breadcrumbs-style}
          (doall
            (for [{:keys [node/title block/string block/uid]} parents]
              [breadcrumb {:key      (str "breadcrumb-" uid)
                           :on-click #(do (let [new-B (db/get-block-document [:block/uid uid])
                                                new-P (drop-last parents)]
                                            (swap! state assoc :block new-B :parents new-P)))}
               (or title string)]))]
         [block-el block linked-ref-data]]))))


;; TODO: where to put page-level link filters?
(defn node-page-el
  "title/initial is the title when a page is first loaded.
  title/local is the value of the textarea.
  We have both, because we want to be able to change the local title without transacting to the db until user confirms.
  Similar to atom-string in blocks. Hacky, but state consistency is hard!"
  [_ _ _]
  (let [state (r/atom init-state)]
    (fn [node editing-uid ref-groups]
      (let [{:block/keys [children uid] title :node/title} node
            {:menu/keys [show] :alert/keys [message confirm-fn cancel-fn] alert-show :alert/show} @state
            timeline-page? (is-timeline-page uid)]

        (sync-title title state)

        [:div (use-style page-style {:class ["node-page"]
                                     :data-uid uid})

         (when alert-show
           [:div (use-style {:position "absolute"
                             :top "50px"
                             :left "35%"})
            [alert-component message confirm-fn cancel-fn]])

         ;; Header
         [:h1 (use-style title-style
                         {:data-uid uid
                          :class    "page-header"
                          :on-click (fn [e] (navigate-uid uid e))})
          ;; Prevent editable textarea if a node/title is a date
          ;; Don't allow title editing from daily notes, right sidebar, or node-page itself.
          (when-not timeline-page?
            [autosize/textarea
             {:value         (:title/local @state)
              :id            (str "editable-uid-" uid)
              :class         (when (= editing-uid uid) "is-editing")
              :on-blur       (fn [_] (handle-blur node state ref-groups))
              :on-key-down   (fn [e] (handle-key-down e uid state children))
              :on-change     (fn [e] (handle-change e state))}])
          [button {:class    [(when show "active")]
                   :on-click (fn [e]
                               (.. e stopPropagation)
                               (if show
                                 (swap! state assoc :menu/show false)
                                 (let [rect (.. e -target getBoundingClientRect)]
                                   (swap! state merge {:menu/show true
                                                       :menu/x    (.. rect -left)
                                                       :menu/y    (.. rect -bottom)}))))
                   :style    page-menu-toggle-style}
           [:> mui-icons/MoreHoriz]]
          (:title/local @state)]
          ;;(parse-renderer/parse-and-render title uid)]

         ;; Dropdown
         [menu-dropdown node state]

         ;; Children
         (if (empty? children)
           [placeholder-block-el uid]
           [:div
            (for [{:block/keys [uid] :as child} children]
              ^{:key uid}
              [block-el child])])


         ;; References
         (doall
           (for [[linked-or-unlinked refs] ref-groups]
             (when (not-empty refs)
               [:section (use-style references-style {:key linked-or-unlinked})
                [:h4 (use-style references-heading-style)
                 [button {:on-click (fn [] (swap! state update linked-or-unlinked not))}
                  (if (get @state linked-or-unlinked)
                    [:> mui-icons/KeyboardArrowDown]
                    [:> mui-icons/ChevronRight])]
                 [(r/adapt-react-class mui-icons/Link)]
                 [:div {:style {:display "flex"
                                :flex "1 1 100%"
                                :justify-content "space-between"}}
                  [:span linked-or-unlinked]
                  (when (= linked-or-unlinked "Unlinked References")
                    [button {:style {:font-size "14px"}
                             :on-click #(dispatch [:unlinked-references/link-all refs title])}
                     "Link All"])]]
                 ;; Hide button until feature is implemented
                 ;;[button {:disabled true} [(r/adapt-react-class mui-icons/FilterList)]]]
                (when (get @state linked-or-unlinked)
                  [:div (use-style references-list-style)
                   (doall
                     (for [[group-title group] refs]
                       [:div (use-style references-group-style {:key (str "group-" group-title)})
                        [:h4 (use-style references-group-title-style)
                         [:a {:on-click #(navigate-uid (:block/uid @(pull-node-from-string group-title)))} group-title]]
                        (doall
                          (for [block group]
                            ^{:key (str "ref-" (:block/uid block))}
                            [:div {:style {:display "flex"
                                           :flex "1 1 100%"
                                           :justify-content "space-between"
                                           :align-items "flex-start"}}
                             [:div (use-style references-group-block-style)
                              [ref-comp block]]
                             (when (= linked-or-unlinked "Unlinked References")
                               [button {:style {:margin-top "1.5em"}
                                        :on-click #(dispatch [:unlinked-references/link block title])}
                                "Link"])]))]))])])))]))))


(defn node-page-component
  [ident]
  (let [{:keys [#_block/uid node/title] :as node} (db/get-node-document ident)
        editing-uid @(subscribe [:editing/uid])]
    (when-not (str/blank? title)
      (let [ref-groups [["Linked References" (get-linked-references (escape-str title))]
                        ["Unlinked References" (get-unlinked-references (escape-str title))]]]
        [node-page-el node editing-uid ref-groups]))))
