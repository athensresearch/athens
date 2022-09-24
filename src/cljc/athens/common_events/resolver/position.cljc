(ns athens.common-events.resolver.position
  (:require
    [athens.common-db :as common-db]
    [athens.common-events.resolver.order :as order]))


(defn position-type
  [{:keys [relation]}]
  (cond (#{:last :first :before :after} relation) :child
        (:page/title relation)                    :property))


(defn add-child
  [db uid position event-ref]
  (let [{:keys [relation]}   position
        [ref-uid parent-uid] (common-db/position->uid+parent db position)
        children             (common-db/get-children-uids db [:block/uid parent-uid])
        children'            (order/insert children uid relation ref-uid)
        reorder              (order/reorder children children' order/block-map-fn)
        add-child            {:block/uid      parent-uid
                              :block/children [{:block/uid uid}]
                              :block/edits    event-ref}]
    (concat [add-child] reorder)))


(defn remove-child
  [db uid parent-uid event-ref]
  (let [parent-children  (common-db/get-children-uids db [:block/uid parent-uid])
        parent-children' (order/remove parent-children uid)
        reorder          (order/reorder parent-children parent-children' order/block-map-fn)
        update-parent    [[:db/retract [:block/uid parent-uid] :block/children [:block/uid uid]]]
        remove-order     [[:db/retract [:block/uid uid] :block/order]]
        edit             [{:block/uid parent-uid
                           :block/edits event-ref}]]
    (concat reorder update-parent remove-order edit)))


(defn move-child-within
  [db parent-uid uid position event-ref]
  (let [{:keys [relation]} position
        [ref-uid]          (common-db/position->uid+parent db position)
        children           (common-db/get-children-uids db [:block/uid parent-uid])
        children'          (order/move-within children uid relation ref-uid)
        reorder            (order/reorder children children' order/block-map-fn)
        edit               [{:block/uid parent-uid
                             :block/edits event-ref}]]
    (concat reorder edit)))


(defn add-property
  "Add uid as property under position. Transaction will fail if a property for position already exists."
  [db uid position event-ref]
  (let [title          (->> position :relation :page/title)
        [_ parent-uid] (common-db/position->uid+parent db position)
        add-child      {:block/uid         uid
                        :block/key         [:node/title title]
                        :block/property-of {:block/uid parent-uid
                                            :block/edits event-ref}}]
    [add-child]))


(defn remove-property
  [_db uid parent-uid event-ref]
  (let [remove-key   [[:db/retract [:block/uid uid] :block/key]
                      [:db/retract [:block/uid uid] :block/property-of]]
        update-edits [{:block/uid uid
                       :block/edits event-ref}
                      {:block/uid parent-uid
                       :block/edits event-ref}]]
    (concat remove-key update-edits)))
