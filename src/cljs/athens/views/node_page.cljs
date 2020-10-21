(ns athens.views.node-page
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db :refer [get-linked-references get-unlinked-references]]
    [athens.keybindings :refer [destruct-key-down]]
    [athens.parse-renderer :as parse-renderer :refer [pull-node-from-string]]
    [athens.patterns :as patterns]
    [athens.router :refer [navigate-uid navigate]]
    [athens.style :refer [color]]
    [athens.util :refer [now-ts gen-block-uid escape-str is-timeline-page]]
    [athens.views.alerts :refer [alert-component]]
    [athens.views.blocks :refer [block-el bullet-style]]
    [athens.views.breadcrumbs :refer [breadcrumbs-list breadcrumb]]
    [athens.views.buttons :refer [button]]
    [athens.views.dropdown :refer [dropdown-style menu-style menu-separator-style]]
    [athens.views.filters :refer [filters-el]]
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
   :margin "0.2em 0 0.2em 1rem"
   :letter-spacing "-0.03em"
   :white-space "pre-line"
   :word-break "break-word"
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
   ;;:padding "0 2rem"
   :align-items "center"
   ::stylefy/manual [[:svg {:margin-right "0.25em"
                            :font-size "1rem"}]
                     [:span {:flex "1 1 100%"}]]})


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


(defn handle-key-down
  "When user presses shift-enter, normal behavior: create a linebreak.
  When user presses enter, blur."
  [e _state]
  (let [{:keys [key-code shift]} (destruct-key-down e)]
    (when
      (and (not shift) (= key-code KeyCodes.ENTER)) (.. e -target blur))))


(defn handle-change
  [e state]
  (let [value (.. e -target -value)]
    (swap! state assoc :title/local value)))


(defn get-linked-refs
  [ref-groups]
  (->> ref-groups
       first
       second
       first
       second))


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
  {:menu/show        false
   :menu/x           nil
   :menu/y           nil
   :title/initial    nil
   :title/local      nil
   :alert/show       nil
   :alert/message    nil
   :alert/confirm-fn nil
   :alert/cancel-fn  nil})


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
                           (let [{:block/keys [uid] sidebar :page/sidebar} node
                                 {:menu/keys [show x y]} @state]
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
                                 [:hr (use-style menu-separator-style)]
                                 [button {:on-click #(do
                                                       (navigate :pages)
                                                       (dispatch [:page/delete uid]))}
                                  [:<> [:> mui-icons/Delete] [:span "Delete Page"]]]]])))})))


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
              :class         (when (= editing-uid uid) "is-editing")
              :on-blur       (fn [_] (handle-blur node state ref-groups))
              :on-key-down   (fn [e] (handle-key-down e state))
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
           (for [[linked-or-unlinked ref-filters] ref-groups]
             ;;(prn references)
             (when (not-empty @ref-filters)
               [:section (use-style references-style {:key linked-or-unlinked})
                [:h4 (use-style references-heading-style)
                 [(r/adapt-react-class mui-icons/Link)]
                 [:span linked-or-unlinked]]
                ;; Hide button until feature is implemented
                ;;[button {:disabled true} [(r/adapt-react-class mui-icons/FilterList)]]]
                [filters-el uid ref-filters]
                ;;(prn "REF" @ref-filters)
                [:div (use-style references-list-style)
                 (doall
                   (let [any-includes? (some (fn [[k v]] (:state v)) @ref-filters)]
                     (for [[group-title {:keys [refs state]}] @ref-filters]
                       (cond

                         any-includes?
                         (when (= state :included)
                           [:div (use-style references-group-style {:key (str "group-" group-title)})
                            [:h4 (use-style references-group-title-style)
                             [:a {:on-click #(navigate-uid (:block/uid @(pull-node-from-string group-title)))} group-title]]
                            (doall
                              (for [block refs]
                                [:div (use-style references-group-block-style {:key (str "ref-" (:block/uid block))})
                                 [ref-comp block]]))])

                         :else
                         [:div (use-style references-group-style {:key (str "group-" group-title)})
                          [:h4 (use-style references-group-title-style)
                           [:a {:on-click #(navigate-uid (:block/uid @(pull-node-from-string group-title)))} group-title]]
                          (doall
                            (for [block refs]
                              [:div (use-style references-group-block-style {:key (str "ref-" (:block/uid block))})
                               [ref-comp block]]))]))))]])))]))))


(->> {"September 28, 2020" {:refs [{:db/id 253, :block/uid "d71452238", :block/string "ok. just had [[Baptiste]] [[Beta Test]]. here's what I learned:", :block/open true, :block/order 5, :block/children [{:db/id 299, :block/uid "0beecc322", :block/string "in person was the move. i shouldn't have texted Haitham, Austin, Ian, Shanberg, etc.", :block/open true, :block/order 0} {:db/id 300, :block/uid "56baf27aa", :block/string "there's still so many bugs that it doesn't really matter if the person uses roam or not. usability is everything, and that is pretty independent: DOES IT WORK?", :block/open true, :block/order 1}], :block/parents [{:db/id 220, :node/title "September 28, 2020", :block/uid "09-28-2020"} {:db/id 281, :block/uid "013ab4ecc", :block/string "[[v1.0.0-beta.7]]"}]} {:db/id 262, :block/uid "ba9b3400e", :block/string "[[Baptiste]]", :block/open false, :block/order 2, :block/children [{:db/id 267, :block/uid "49280b413", :block/string "{{[[DONE]]}} if you press enter while debouncing hasn't finished, leads to freeze", :block/open true, :block/order 0, :block/children [{:db/id 263, :block/uid "3bba42182", :block/string "create a new page", :block/open true, :block/order 0}]} {:db/id 266, :block/uid "2b9a90f50", :block/string "gray refresh", :block/open true, :block/order 1} {:db/id 268, :block/uid "d54e5259a", :block/string "test2 doesn't exist?", :block/open true, :block/order 2} {:db/id 269, :block/uid "56169929c", :block/string "{{[[TODO]]}} nested links sometimes aren't generated, and link doesn't always work", :block/open true, :block/order 3} {:db/id 292, :block/uid "ef4dbef6d", :block/string "go to the block if you click on the link", :block/open true, :block/order 4} {:db/id 270, :block/uid "d6cb78837", :block/string "fix (())", :block/open true, :block/order 5} {:db/id 311, :block/uid "e3b572f2b", :block/string "shortcuts", :block/open false, :block/order 6, :block/children [{:db/id 271, :block/uid "7029cdc5e", :block/string "cmd vs ctrl enter for todo", :block/open true, :block/order 0, :block/_refs [{:db/id 315}]} {:db/id 272, :block/uid "b404b488a", :block/string "{{[[TODO]]}} ctrl-z is weird", :block/open true, :block/order 1} {:db/id 273, :block/uid "a833ed85d", :block/string "{{[[TODO]]}} ctrl-b should place caret", :block/open true, :block/order 2}]} {:db/id 274, :block/uid "2a51a3c0a", :block/string "{{[[TODO]]}} welcome datoms", :block/open false, :block/order 7, :block/children [{:db/id 275, :block/uid "611cb06ea", :block/string "links should work", :block/open true, :block/order 0} {:db/id 276, :block/uid "66b556a4b", :block/string "favorite?", :block/open true, :block/order 1} {:db/id 277, :block/uid "d17b2fa37", :block/string "outside nested links", :block/open true, :block/order 2} {:db/id 291, :block/uid "0aaab0f25", :block/string "the welcome datoms use links that don't exist in the db", :block/open true, :block/order 3} {:db/id 290, :block/uid "060178b32", :block/string "links with double brackets order is off", :block/open true, :block/order 4} {:db/id 287, :block/uid "ef0e0d490", :block/string "onboarding — where does this get saved? how often?", :block/open true, :block/order 5} {:db/id 315, :block/uid "7a633afa0", :block/string "((7029cdc5e))", :block/open true, :block/order 6}]} {:db/id 278, :block/uid "0fd0e0b5b", :block/string "nav", :block/open true, :block/order 8, :block/children [{:db/id 310, :block/uid "2acf372d6", :block/string "clicking on the same page should not add to navigation stack", :block/open true, :block/order 0}]} {:db/id 309, :block/uid "55571cee0", :block/string "blocks", :block/open true, :block/order 9, :block/children [{:db/id 279, :block/uid "58ec56cf8", :block/string "{{[[TODO]]}} enter on an indented empty block freezes", :block/open true, :block/order 0} {:db/id 280, :block/uid "c0bbe4a43", :block/string "{{[[TODO]]}} enter on a closed block should create new block", :block/open true, :block/order 1} {:db/id 285, :block/uid "918ebc941", :block/string "{{[[TODO]]}} clicking into side of page exit edit mode?", :block/open true, :block/order 2} {:db/id 286, :block/uid "c313f0c2a", :block/string "{{[[TODO]]}} show block references when zoomed in", :block/open true, :block/order 3}]} {:db/id 303, :block/uid "3693870dc", :block/string "help UI or shortcut", :block/open true, :block/order 10, :block/children [{:db/id 283, :block/uid "c7bd56ff4", :block/string "{{[[TODO]]}} list of shortcuts or even a keybinding", :block/open true, :block/order 0}]} {:db/id 289, :block/uid "5a93e0ae5", :block/string "sync/save?", :block/open true, :block/order 11, :block/children [{:db/id 312, :block/uid "32b9ae264", :block/string "what does yellow circle do?", :block/open true, :block/order 0} {:db/id 313, :block/uid "27d185785", :block/string "auto-save", :block/open true, :block/order 1}]} {:db/id 307, :block/uid "b4a169052", :block/string "left sidebar", :block/open true, :block/order 12, :block/children [{:db/id 284, :block/uid "09477f472", :block/string "{{[[TODO]]}} any tooltip info for left sidebar?", :block/open true, :block/order 0}]} {:db/id 306, :block/uid "98c0bce49", :block/string "right sidebar", :block/open true, :block/order 13, :block/children [{:db/id 288, :block/uid "6d0df8642", :block/string "{{[[DONE]]}} closing out of last right sidebar item should close sidebar", :block/open true, :block/order 0}]} {:db/id 308, :block/uid "80a0fbc80", :block/string "biz model", :block/open true, :block/order 14, :block/children [{:db/id 293, :block/uid "34cd44c05", :block/string "e2e? for cloud", :block/open true, :block/order 0}]}], :block/parents [{:db/id 220, :node/title "September 28, 2020", :block/uid "09-28-2020"} {:db/id 281, :block/uid "013ab4ecc", :block/string "[[v1.0.0-beta.7]]"} {:db/id 226, :block/uid "e405bd192", :block/string "[[Beta Testers]]"}]}], :count 1}, "September 29, 2020" {:refs [{:db/id 317, :block/uid "c6aec6632", :block/string "[[Baptiste]] it's actually ridiculous that I even drafted an email with 50 people. in the heat of the moment of a real startup, I only understand the cliche almost after it's too late. in this case, I just gotta [[Do Things That Don't Scale]]", :block/open true, :block/order 0, :block/parents [{:db/id 316, :node/title "September 29, 2020", :block/uid "09-29-2020"}]}], :count 1}}
     (some (fn [[k v]]
             (:included v))))

(defn node-page-component
  [ident]
  (let [{:keys [#_block/uid node/title] :as node} (db/get-node-document ident)
        editing-uid @(subscribe [:editing/uid])]
    (when-not (str/blank? title)
      ;; TODO: let users toggle open/close references
      (let [ref-groups [["Linked References" (r/atom (get-linked-references (escape-str title)))]
                        ["Unlinked References" (r/atom (get-unlinked-references (escape-str title)))]]]
        [node-page-el node editing-uid ref-groups]))))
