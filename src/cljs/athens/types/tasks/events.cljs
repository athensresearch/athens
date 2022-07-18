(ns athens.types.tasks.events
  "`re-frame` events for `:athens/task` block type."
  (:require
   [athens.common-db                     :as common-db]
   [athens.common-events                 :as common-events]
   [athens.common-events.graph.composite :as composite]
   [athens.common-events.graph.ops       :as graph-ops]
   [athens.common.logging                :as log]
   [athens.db                            :as db]
   [re-frame.core                        :as rf]))


(rf/reg-event-fx
 ::save-title
 (fn [_rfdb [_event-name {:keys [parent-block-uid title] :as args}]]
   (log/debug ":tasks/save-title" (pr-str args))
   (let [[title-uid title-create-ops] (graph-ops/build-property-path @db/dsdb parent-block-uid [":task/title"])
         save-op                      (graph-ops/build-block-save-op @db/dsdb title-uid title)
         title-block-exists?          (common-db/e-by-av @db/dsdb :block/uid title-uid)
         ops                          (if-not title-block-exists?
                                        (composite/make-consequence-op {:op/type :tasks/title-save}
                                                                       (conj title-create-ops save-op))
                                        save-op)
         event                        (common-events/build-atomic-event ops)]
     {:fx [[:dispatch [:resolve-transact-forward event]]]})))
