(ns athens.views.pages.block-page
  (:require
   ["@chakra-ui/react" :refer [Breadcrumb BreadcrumbItem BreadcrumbLink Heading]]
   ["@material-ui/icons/Link" :default Link]
   [athens.parse-renderer :as parse-renderer]
   [athens.reactive :as reactive]
   [athens.router :as router]
   [athens.views.blocks.core :as blocks]
   [athens.views.pages.node-page :as node-page]
   [komponentit.autosize :as autosize]
   [re-frame.core :as rf :refer [dispatch subscribe]]
   [reagent.core :as r]
   [stylefy.core :as stylefy :refer [use-style]]))


;; Helpers


(defn persist-textarea-string
  "A helper fn that takes `state` containing textarea changes and when user has made a text change dispatches `transact-string`.
   Used in `block-page-el` function to log when there is a diff and `on-blur`"
  [state block-uid]
  (dispatch [:block/save {:uid       block-uid
                          :string    (:string/local state)
                          :add-time? true}]))


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
      [:div (use-style node-page/references-style {:key "Linked References"})
       [:section
        [:h4 (use-style node-page/references-heading-style)
         [(r/adapt-react-class Link)]
         [:span "Linked References"]]
        ;; Hide button until feature is implemented
        ;; [:> Button {:disabled true} [(r/adapt-react-class FilterList)]]]
        [:div (use-style node-page/references-list-style)
         (doall
          (for [[group-title group] linked-refs]
            [:div (use-style node-page/references-group-style {:key (str "group-" group-title)})
             [:h4 (use-style node-page/references-group-title-style)
              [:a {:on-click (fn [e]
                               (let [shift?       (.-shiftKey e)
                                     parsed-title (parse-renderer/parse-title group-title)]
                                 (rf/dispatch [:reporting/navigation {:source :block-page-linked-refs
                                                                      :target :page
                                                                      :pane   (if shift?
                                                                                :right-pane
                                                                                :main-pane)}])
                                 (router/navigate-page parsed-title)))}
               group-title]]
             (doall
              (for [block group]
                [:div (use-style node-page/references-group-block-style {:key (str "ref-" (:block/uid block))})
                 [node-page/ref-comp block]]))]))]]])))


(defn parents-el
  [uid id]
  (let [parents (reactive/get-reactive-parents-recursively id)]
    [:> Breadcrumb
     (doall
      (for [{:keys [node/title block/string] breadcrumb-uid :block/uid} parents]
        ^{:key breadcrumb-uid}
        [:> BreadcrumbItem {:key (str "breadcrumb-" breadcrumb-uid)}
         [:> BreadcrumbLink {:onClick #(breadcrumb-handle-click % uid breadcrumb-uid)}
          [:span {:style {:pointer-events "none"}}
           [parse-renderer/parse-and-render (or title string)]]]]))]))


(defn block-page-el
  [_block]
  (let [state (r/atom {:string/local    nil
                       :string/previous nil})]
    (fn [block]
      (let [{:block/keys [string children uid] :db/keys [id]} block]
        (when (not= string (:string/previous @state))
          (swap! state assoc :string/previous string :string/local string))

        [:div.block-page (use-style node-page/page-style {:data-uid uid})
         ;; Parent Context
         [parents-el uid id]

         ;; Header
         [:> Heading {:size "lg"
                      :_hover {:textDecoration "underline"}
                      :onClick (fn [e]
                                 (.. e preventDefault)
                                 (if (.. e -shiftKey)
                                   (do
                                     (rf/dispatch [:reporting/navigation {:source :block-page
                                                                          :target :block
                                                                          :pane   :right-pane}])
                                     (router/navigate-uid uid e))
                                   (dispatch [:editing/uid uid])))}
          [autosize/textarea
           {:id          (str "editable-uid-" uid)
            :value       (:string/local @state)
            :class       (when @(subscribe [:editing/is-editing uid]) "is-editing")
            :auto-focus  true
            :on-blur     (fn [_] (persist-textarea-string @state uid))
            :on-key-down (fn [e] (node-page/handle-key-down e uid state nil))
            :on-change   (fn [e] (block-page-change e uid state))}]
          (if (clojure.string/blank? (:string/local @state))
            [:wbr]
            [:span [parse-renderer/parse-and-render (:string/local @state) uid]])]

         ;; Children
         [:div (for [child children]
                 (let [{:keys [db/id]} child]
                   ^{:key id} [blocks/block-el child]))]

         ;; Refs
         [linked-refs-el id]]))))


(defn page
  [ident]
  (let [block (reactive/get-reactive-block-document ident)]
    [block-page-el block]))
