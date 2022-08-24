(ns athens.views.blocks.reactions
  (:require
    ["@chakra-ui/react" :refer [Box
                                Tooltip
                                VStack
                                HStack
                                MenuItem
                                MenuGroup
                                Text]]
    [athens.common-db                          :as common-db]
    [athens.common-events.graph.ops            :as graph-ops]
    [athens.db                                 :as db]
    [re-frame.core                             :as rf]
    [reagent.core :as r]))


(def common-reactions ["👍" "👎" "❤️" "🔥" "😂" "😲" "😢" "😡"])


(defn toggle-reaction
  "Toggle reaction on block uid. Cleans up when toggling the last one off.
  Stores emojis in the [:reactions/emojis reaction user-id] property path."
  [id reaction user-id]
  (rf/dispatch [:graph/update-in id [":reactions" reaction user-id]
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


(defn reactions-menu-list-item
  [props]
  (let [{:keys [icon fn command]} props]
    [:> Box {:display "inline-flex" :flex 1}
     [:> Tooltip {:closeOnMouseDown true
                  :label (r/as-element [:> VStack {:align "center"}
                                        [:> Text "React with '" icon "'"]
                                        (when command [:> Text command])])}
      [:> MenuItem {:justifyContent "center" :on-click #(fn)} icon]]]))


(defn reactions-menu-list
  [uid user-id]
  [:> MenuGroup {:title "Add reaction"}
   [:> HStack {:spacing 0 :justifyContent "stretch"}
    (for [reaction-icon common-reactions]
      ^{:key reaction-icon}
      [reactions-menu-list-item {:icon reaction-icon :fn #(toggle-reaction uid reaction-icon user-id)}])]])
