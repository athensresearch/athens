(ns athens.common-events.resolver.position
  (:require
    [athens.common-db :as common-db]
    [athens.common-events.resolver.order :as order]))


(defn add-child
  [db uid position now]
  (let [{:keys [relation]}   position
        [ref-uid parent-uid] (common-db/position->uid+parent db position)
        children             (common-db/get-children-uids db [:block/uid parent-uid])
        children'            (order/insert children uid relation ref-uid)
        reorder              (order/reorder children children' order/block-map-fn)
        add-child            {:block/uid      parent-uid
                              :block/children [{:block/uid uid}]
                              :edit/time      now}]
    (concat [add-child] reorder)))


(defn remove-child
  [db uid parent-uid]
  (let [parent-children  (common-db/get-children-uids db [:block/uid parent-uid])
        parent-children' (order/remove parent-children uid)
        reorder          (order/reorder parent-children parent-children' order/block-map-fn)]
    reorder))


(defn move-child-within
  [db old-parent-uid uid position]
  (let [{:keys [relation]} position
        [ref-uid]          (common-db/position->uid+parent db position)
        children           (common-db/get-children-uids db [:block/uid old-parent-uid])
        children'          (order/move-within children uid relation ref-uid)
        reorder            (order/reorder children children' order/block-map-fn)]
    reorder))


(defn move-child-between
  [db old-parent-uid new-parent-uid uid position now]
  (let [{:keys [relation]}      position
        [ref-uid]               (common-db/position->uid+parent db position)
        origin-children         (common-db/get-children-uids db [:block/uid old-parent-uid])
        destination-children    (common-db/get-children-uids db [:block/uid new-parent-uid])
        [origin-children'
         destination-children'] (order/move-between origin-children destination-children uid relation ref-uid)
        reorder-origin          (order/reorder origin-children origin-children' order/block-map-fn)
        reorder-destination     (order/reorder destination-children destination-children' order/block-map-fn)
        update-parent           [[:db/retract [:block/uid old-parent-uid] :block/children [:block/uid uid]]
                                 {:block/uid      new-parent-uid
                                  :block/children [{:block/uid uid}]
                                  :edit/time      now}]]
    (concat reorder-origin reorder-destination update-parent)))
