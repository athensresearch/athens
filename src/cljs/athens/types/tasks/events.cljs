(ns athens.types.tasks.events
  "`re-frame` events for `[[athens/task]]` entity type."
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.graph.composite :as composite]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common.logging                :as log]
    [athens.db                            :as db]
    [re-frame.core                        :as rf]))


(defn- save-prop-value
  [parent-block-uid prop-key value]
  (let [[prop-uid prop-create-ops] (graph-ops/build-property-path @db/dsdb parent-block-uid [prop-key])
        save-op                    (graph-ops/build-block-save-op @db/dsdb prop-uid value)
        prop-block-exists?         (common-db/e-by-av @db/dsdb :block/uid prop-uid)
        ops                        (if-not prop-block-exists?
                                     (composite/make-consequence-op {:op/type :tasks/title-save}
                                                                    (conj prop-create-ops save-op))
                                     save-op)
        event                      (common-events/build-atomic-event ops)]
    {:fx [[:dispatch [:resolve-transact-forward event]]]}))


(rf/reg-event-fx
  ::save-title
  (fn [_rfdb [_event-name {:keys [parent-block-uid title] :as args}]]
    (log/debug ":tasks/save-title" (pr-str args))
    (save-prop-value parent-block-uid ":task/title" title)))


(rf/reg-event-fx
  ::save-description
  (fn [_rfdb [_event-name {:keys [parent-block-uid description] :as args}]]
    (log/debug ":tasks/save-description" (pr-str args))
    (save-prop-value parent-block-uid ":task/description" description)))


(rf/reg-event-fx
  ::save-due-date
  (fn [_rfdb [_event-name {:keys [parent-block-uid due-date] :as args}]]
    (log/debug ":tasks/save-due-date" (pr-str args))
    (save-prop-value parent-block-uid ":task/due-date" due-date)))


(rf/reg-event-fx
  ::save-status
  (fn [_rfdb [_event-name {:keys [parent-block-uid status] :as args}]]
    (log/debug ":tasks/save-status" (pr-str args))
    (save-prop-value parent-block-uid ":task/status" status)))
