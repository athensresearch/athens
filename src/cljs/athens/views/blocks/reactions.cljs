(ns athens.views.blocks.reactions
  (:require
    [athens.common-db                          :as common-db]
    [athens.common-events.graph.ops            :as graph-ops]
    [athens.db                                 :as db]
    [re-frame.core                             :as rf]))


(defn toggle-reaction
  "Toggle reaction on block uid. Cleans up when toggling the last one off.
  Stores emojis in the [:reactions/emojis reaction user-id] property path."
  [id reaction user-id]
  (rf/dispatch [:properties/update-in id [":reactions" reaction user-id]
                (fn [db user-reaction-uid]
                  (let [user-reacted?       (common-db/block-exists? db [:block/uid user-reaction-uid])
                        reaction            (when user-reacted?
                                              (->> [:block/uid user-reaction-uid]
                                                   (common-db/get-parent-eid db)
                                                   (common-db/get-block db)))
                        reactions           (when reaction
                                              (->> (:db/id reaction)
                                                   (common-db/get-parent-eid db)
                                                   (common-db/get-block db)))
                        last-user-reaction? (= 1 (count (-> reaction :block/properties)))
                        last-reaction?      (= 1 (count (-> reactions :block/properties)))]
                    [(cond
                       ;; This reaction doesn't exist yet, so we add it.
                       (not user-reacted?)
                       (graph-ops/build-block-save-op db user-reaction-uid "")

                       ;; This was the last of all reactions, remove the reactions property
                       ;; on the parent.
                       (and last-user-reaction? last-reaction?)
                       (graph-ops/build-block-remove-op @db/dsdb (:block/uid reactions))

                       ;; This was the last user reaction of this type, but not the last
                       ;; of all reactions. Remove reaction block.
                       last-user-reaction?
                       (graph-ops/build-block-remove-op @db/dsdb (:block/uid reaction))

                       ;; Just remove this particular user reaction.
                       :else
                       (graph-ops/build-block-remove-op @db/dsdb user-reaction-uid))]))]))


(defn props->reactions
  [props]
  (->> (get props ":reactions")
       :block/properties
       (map (fn [[k {props :block/properties}]]
              [k (->> props
                      (map (fn [[user-id block]]
                             [(-> block :block/edits last :event/time :time/ts)
                              user-id]))
                      (sort-by first)
                      (mapv second))]))
       (sort-by first)
       (into [])))

