(ns athens.views.right-sidebar.shared
  (:require
    [athens.common-db :as common-db]
    [athens.db :as db]
    [athens.parse-renderer :as parse-renderer]
    [athens.reactive :as reactive]
    [athens.router :as router]
    [athens.views.blocks.core :as blocks]
    [athens.views.pages.graph :as graph]
    [athens.views.pages.node-page :as node-page]
    [re-frame.core :as rf]
    [reagent.core :as r]))


(def NS "athens/right-sidebar")


(defn ns-str
  ([]
   (ns-str ""))
  ([s]
   (str NS s)))


(defn get-open?
  []
  (let [user-page @(rf/subscribe [:presence/user-page])
        props (-> (reactive/get-reactive-node-document [:node/title user-page])
                  :block/properties)
        open? (-> (get props (ns-str "/open?"))
                  boolean)]

    open?))


(defn get-width
  []
  (let [user-page  @(rf/subscribe [:presence/user-page])
        props      (-> (reactive/get-reactive-node-document [:node/title user-page])
                       :block/properties)
        default-vw (str 32)
        width      (-> (get props (ns-str "/width"))
                       :block/string)]
    ;; can also use the clamp function in RightSidebarResizeControl.tsx to make sure the width is always bounded
    (or width default-vw)))


(defn get-eid
  [item-block]
  (let [{:keys [type name]} item-block]
    (cond
      (= type "page") [:node/title name]
      (= type "graph") [:node/title name]
      :else [:block/uid name])))


(defn eid->type
  [eid]
  (let [[attr _value] eid]
    (cond
      (= attr :node/title) "page"
      :else "block")))


(defn get-items
  []
  (let [user-page    @(rf/subscribe [:presence/user-page])
        props        (-> (reactive/get-reactive-node-document [:node/title user-page])
                         :block/properties)
        filter-fn    (fn [x]
                       (common-db/block-exists? @db/dsdb (get-eid x)))
        map-props-fn (fn [{:block/keys [uid string properties]}]
                       (let [type  (or (-> (get properties (ns-str "/items/type"))
                                           :block/string)
                                       "block")
                             open? (-> (get properties (ns-str "/items/open?"))
                                       boolean)]
                         {:source-uid uid
                          :name       string
                          :type       type
                          :open?      open?}))
        items        (->> (get props (ns-str "/items"))
                          :block/children
                          (sort-by :block/order)
                          (mapv map-props-fn)
                          (filter filter-fn))]
    items))


(defn create-sidebar-list
  "Accepts right-sidebar as a map of uids and entities.
  Entity contains either the block uid or node title, and additionally open/close state and whether the page is a graph view or not."
  [items]
  (doall
    (mapv (fn [entity]
            (let [{:keys [open?
                          name
                          source-uid
                          type]}      entity
                  eid                 (get-eid entity)
                  {:keys [node/title
                          block/string
                          block/uid]} (reactive/get-reactive-right-sidebar-item eid)
                  ;; NOTE: add support for BlockTypeProtocol
                  entity-title        (parse-renderer/parse-to-text (or title string))]
              {:isOpen     open?
               :key        source-uid
               :id         source-uid
               :type       type
               :onRemove   #(rf/dispatch [:right-sidebar/close-item source-uid])
               :onToggle   #(rf/dispatch [:right-sidebar/toggle-item source-uid])
               :onNavigate (if (= type "page")
                             #(router/navigate-page title %)
                             #(router/navigate-uid uid %))
               :title      entity-title
               :children   (r/as-element (cond
                                           (= type "graph") [graph/page name]
                                           (= type "page")  [node-page/page eid {:size "sm"}]
                                           :else            [blocks/page eid {:size "sm"}]))}))

          items)))
