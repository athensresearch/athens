(ns athens.views.pages.block-page
  (:require
    ["/components/Page/Page" :refer [PageHeader PageBody PageFooter TitleContainer]]
    ["/components/References/References" :refer [PageReferences ReferenceBlock ReferenceGroup]]
    ["@chakra-ui/react" :refer [Breadcrumb Box BreadcrumbItem BreadcrumbLink]]
    [athens.common-db :as common-db]
    [athens.db :as db]
    [athens.parse-renderer :as parse-renderer]
    [athens.reactive :as reactive]
    [athens.router :as router]
    [athens.views.blocks.core :as blocks]
    [athens.views.comments.core :as comments]
    [athens.views.comments.inline :as inline-comments]
    [athens.views.pages.node-page :as node-page]
    [komponentit.autosize :as autosize]
    [re-frame.core :as rf :refer [dispatch subscribe]]
    [reagent.core :as r]))


;; Helpers


(defn- persist-textarea-string
  "A helper fn that takes `state` containing textarea changes and when user has made a text change dispatches `transact-string`.
   Used in `block-page-el` function to log when there is a diff and `on-blur`"
  [state block-uid]
  (rf/dispatch [:block/save {:uid       block-uid
                             :string    (:string/local state)
                             :add-time? true
                             :source    :on-blur-block-save}]))


;; Components

(defn block-page-change
  [e _uid state]
  (let [value (.. e -target -value)]
    (swap! state assoc :string/local value)))


(defn breadcrumb-handle-click
  "If block is in main, navigate to page. If in right sidebar, replace right sidebar item."
  [e uid breadcrumb-uid]
  (let [right-sidebar? (.. e -target (closest ".right-sidebar"))]
    (rf/dispatch [:reporting/navigation {:source :block-page-breadcrumb
                                         :target :block
                                         :pane   (if right-sidebar?
                                                   :right-pane
                                                   :main-pane)}])
    (if right-sidebar?
      (dispatch [:right-sidebar/navigate-item uid breadcrumb-uid])
      (router/navigate-uid breadcrumb-uid e))))


(defn linked-refs-el
  [id]
  (let [linked-refs (reactive/get-reactive-linked-references id)]
    (when (seq linked-refs)
      [:> PageReferences {:title "Linked References"
                          :count (count linked-refs)}
       (doall
         (for [[group-title group] linked-refs]
           [:> ReferenceGroup {:key (str "group-" group-title)
                               :title group-title
                               :onClickTitle (fn [e]
                                               (let [shift?       (.-shiftKey e)
                                                     parsed-title (parse-renderer/parse-title group-title)]
                                                 (rf/dispatch [:reporting/navigation {:source :block-page-linked-refs
                                                                                      :target :page
                                                                                      :pane   (if shift?
                                                                                                :right-pane
                                                                                                :main-pane)}])
                                                 (router/navigate-page parsed-title)))}
            (doall
              (for [block group]
                [:> ReferenceBlock {:key (str "ref-" (:block/uid block))}
                 [node-page/ref-comp block]]))]))])))


(defn parents-el
  [uid id]
  (let [parents (reactive/get-reactive-parents-recursively id)]
    [:> Breadcrumb {:gridArea "breadcrumb" :opacity 0.75}
     (doall
       (for [{breadcrumb-uid :block/uid} parents]
         ^{:key breadcrumb-uid}
         [:> BreadcrumbItem {:key (str "breadcrumb-" breadcrumb-uid)}
          [:> BreadcrumbLink {:onClick #(breadcrumb-handle-click % uid breadcrumb-uid)}
           [:span {:style {:pointer-events "none"}}
            [parse-renderer/parse-and-render (common-db/breadcrumb-string @db/dsdb breadcrumb-uid)]]]]))]))


(defn block-page-el
  [block]
  (let [state (r/atom {:string/local    nil
                       :string/previous nil})
        uid                    (:block/uid block)
        show-comment-textarea? (rf/subscribe [:comment/show-comment-textarea? uid])
        show-inline-comments? (rf/subscribe [:comment/show-inline-comments?])
        current-route         (rf/subscribe [:current-route/uid])
        right-side-items      (rf/subscribe [:right-sidebar/items])
        is-editing            (rf/subscribe [:editing/is-editing uid])]
    (fn [block]
      (let [{:block/keys [string children uid] :db/keys [id]} block
            is-current-route? (= @current-route uid)]
        (when (not= string (:string/previous @state))
          (swap! state assoc :string/previous string :string/local string))

        [:> Box

         ;; Header
         [:> PageHeader {:onClickOpenInMainView (when-not is-current-route?
                                                  (fn [e] (router/navigate-uid uid e)))
                         :onClickOpenInSidebar (when-not (contains? @right-side-items uid)
                                                 #(dispatch [:right-sidebar/open-item uid]))}

          ;; Parent Context
          [parents-el uid id]
          [:> TitleContainer {:isEditing @is-editing
                              :onClick (fn [e]
                                         (.. e preventDefault)
                                         (if (.. e -shiftKey)
                                           (do
                                             (dispatch [:reporting/navigation {:source :block-page
                                                                               :target :block
                                                                               :pane   :right-pane}])
                                             (router/navigate-uid uid e))

                                           (dispatch [:editing/uid uid])))}
           [autosize/textarea
            {:value       (:string/local @state)
             :class       (when @is-editing "is-editing")
             :id          (str "editable-uid-" uid)
             ;; :auto-focus  true
             :on-blur     (fn [_]
                            (persist-textarea-string @state uid)
                            (dispatch [:editing/uid nil]))
             :on-click    #(dispatch [:editing/uid uid])
             :on-key-down (fn [e] (node-page/handle-key-down e uid state nil))
             :on-change   (fn [e] (block-page-change e uid state))}]
           (if (clojure.string/blank? (:string/local @state))
             [:span [:wbr]]
             [parse-renderer/parse-and-render (:string/local @state) uid])]]

         ;; Show comments when the toggle is on
         [:> Box {:ml "4%"
                  :w "100%"}
          (when (or @show-comment-textarea?
                    (and @show-inline-comments?
                         (comments/get-comment-thread-uid @db/dsdb uid)))
            [inline-comments/inline-comments (comments/get-comments-in-thread @db/dsdb (comments/get-comment-thread-uid @db/dsdb uid)) uid false])]


         ;; Children
         [:> PageBody
          (for [child children]
            (let [{:keys [db/id]} child]
              ^{:key id} [blocks/block-el child]))]

         ;; Refs
         [:> PageFooter
          [linked-refs-el id]]]))))


(defn page
  [ident]
  (let [block (reactive/get-reactive-block-document ident)]
    [block-page-el block]))
