(ns athens.views.right-sidebar.shared
  (:require
    [athens.reactive :as reactive]
    [athens.common-db :as common-db]
    [re-frame.core :as rf]))

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


(defn get-eid
  [item-block]
  (let [{:keys [type name]} item-block]
    (if (= type "page")
      [:node/title name]
      [:block/uid name])))


(defn get-items
  []
  (let [user-page    @(rf/subscribe [:presence/user-page])
        props        (-> (reactive/get-reactive-node-document [:node/title user-page])
                         :block/properties)
        filter-fn    (fn [x]
                       (common-db/block-exists? @athens.db/dsdb (get-eid x)))
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

