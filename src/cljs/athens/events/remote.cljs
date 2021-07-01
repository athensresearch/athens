(ns athens.events.remote
  "`re-frame` events related to `:remote/*`."
  (:require
    [athens.common-events        :as common-events]
    [athens.common-events.schema :as schema]
    [athens.db                   :as db]
    [malli.core                  :as m]
    [malli.error                 :as me]
    [re-frame.core               :as rf]))


;; Connection Management

(rf/reg-event-fx
  :remote/connect!
  (fn [{:keys [db]} [_ connection-config]]
    (js/console.log ":remote/connect!" (pr-str connection-config))
    {:db                     (-> db
                                 (dissoc :db/filepath)
                                 (assoc :db/remote connection-config))
     :remote/client-connect! connection-config
     :local-storage/set!     ["db/remote" connection-config]
     :fx                     [[:dispatch [:loading/set]]]}))


(rf/reg-event-fx
  :remote/connected
  (fn [{:keys [db]} _]
    (js/console.log ":remote/connected")
    {:db (dissoc db :db/remote)
     :fx [[:dispatch-n [[:loading/unset]
                        [:db/sync]]]]}))


(rf/reg-event-fx
  :remote/disconnect!
  (fn [{:keys [db]} _]
    {:db                        (dissoc db :db/remote)
     :remote/client-disconnect! nil
     :local-storage/set!        ["db/remote" nil]}))


;; Remote protocol management (awaiting txs & events, accepting/rejecting events)

(rf/reg-event-db
  :remote/await-event
  (fn [db [_ event]]
    (js/console.log "await event" (pr-str event))
    (update db :remote/awaited-events (fnil conj #{}) event)))


(rf/reg-event-db
  :remote/await-tx
  (fn [db [_ awaited-tx-id]]
    (js/console.log "await tx" awaited-tx-id)
    (update db :remote/awaited-tx (fnil conj #{}) awaited-tx-id)))


(rf/reg-event-fx
  :remote/accepted-event
  (fn [{db :db} [_ {:keys [event-id]}]]
    (let [followups (get-in db [:remote/followup event-id])]
      (js/console.debug ":remote/accepted-event: " event-id "followup" (pr-str followups))
      (when (seq followups)
        {:fx followups}))))


(rf/reg-event-fx
  :remote/accept-event
  (fn [{db :db} [_ {:keys [event-id tx-id] :as acceptance-event}]]
    (js/console.log "accept event" (pr-str acceptance-event))
    (let [awaited-event   (->> (:remote/awaited-events db)
                               (filter #(= event-id (:event/id %)))
                               first)
          acceptance-info {:event-id event-id
                           :tx-id    tx-id
                           :event    awaited-event}
          last-seen-tx    (:remote/last-seen-tx db -1)
          events          (cond-> []
                            (< last-seen-tx tx-id)
                            (conj [:remote/await-tx tx-id])
                            true
                            (conj [:remote/accepted-event acceptance-info]))]
      (js/console.debug "events to dispatch:" (pr-str events))
      {:db (-> db
               (update :remote/awaited-events disj awaited-event)
               (update :remote/accepted-events (fnil conj #{}) acceptance-info))
       :fx [[:dispatch-n events]]})))


(rf/reg-event-db
  :remote/reject-event
  (fn [db [_ {:keys [event-id reason data] :as rejection-event}]]
    (js/console.log "reject event" (pr-str rejection-event))
    (let [awaited-event  (->> (:remote/awaited-events db)
                              (filter #(= event-id (:event/id %)))
                              first)
          rejection-info {:event-id  event-id
                          :rejection {:reason reason
                                      :data   data}
                          :event     awaited-event}]
      (-> db
          (update :remote/awaited-events disj awaited-event)
          (update :remote/rejected-events (fnil conj #{}) rejection-info)))))


(rf/reg-event-db
  :remote/fail-event
  (fn [db [_ {:keys [event-id reason] :as failure-event}]]
    (js/console.warn "fail event" (pr-str failure-event))
    (let [awaited-event (->> (:remote/awaited-events db)
                             (filter #(= event-id (:event/id %)))
                             first)
          failure-info  {:event-id event-id
                         :reason   reason
                         :event    awaited-event}]
      (-> db
          (update :remote/awaited-events disj awaited-event)
          (update :remote/failed-events (fnil conj #{}) failure-info)))))


(rf/reg-event-db
  :remote/updated-last-seen-tx
  (fn [db _]
    (js/console.debug ":remote/updated-last-seen-tx")
    db))


(rf/reg-event-fx
  :remote/last-seen-tx!
  (fn [{db :db} [_ new-tx-id]]
    (js/console.debug "last-seen-tx!" new-tx-id)
    {:db (assoc db :remote/last-seen-tx new-tx-id)
     :fx [[:dispatch [:remote/updated-last-seen-tx]]]}))


;; `re-frame` followup events

(defn- get-event-acceptance-info
  [db event-id]
  (->> db
       :remote/accepted-events
       (filter #(= event-id (:event-id %)))
       first))


(defn- followup-fx
  [db event-id fx]
  (update db :remote/followup (fnil assoc {}) event-id fx))


(rf/reg-event-db
  :remote/register-followup
  (fn [db [_ event-id fx]]
    (followup-fx db event-id fx)))


(rf/reg-event-db
  :remote/unregister-followup
  (fn [db [_ event-id]]
    (update db :remote/followup dissoc event-id)))


;; Send it

(rf/reg-event-fx
  :remote/send-event!
  (fn [_ [_ event]]
    (if (schema/valid-event? event)
      ;; valid event, send item
      {:fx                    [[:dispatch [:remote/await-event event]]]
       :remote/send-event-fx! event}
      (let [explanation (-> schema/event
                            (m/explain event)
                            (me/humanize))]
        (js/console.warn "Not sending invalid event. Error:" (pr-str explanation))))))


;; Remote Datascript related events

;; - Page related


(rf/reg-event-fx
  :remote/followup-page-create
  (fn [{db :db} [_ event-id]]
    (js/console.debug ":remote/followup-page-create" event-id)
    (let [{:keys [event]} (get-event-acceptance-info db event-id)
          {:keys [uid]}   (:event/args event)
          page-id         (db/e-by-av :block/uid uid)
          page            (db/get-node-document page-id)
          children        (:block/children page)
          child-block-uid (-> children
                              first
                              :block/uid)]
      (js/console.log ":remote/followup-page-create, child-block-uid" child-block-uid)
      {:fx [[:dispatch-n [[:editing/uid child-block-uid]
                          [:remote/unregister-followup event-id]]]]})))


(rf/reg-event-fx
  :remote/page-create
  (fn [{db :db} [_ uid title]]
    (let [last-seen-tx                 (:remote/last-seen-tx db)
          {event-id :event/id
           :as      page-create-event} (common-events/build-page-create-event last-seen-tx
                                                                              uid
                                                                              title)
          followup-fx                  [[:dispatch [:remote/followup-page-create event-id]]]]
      (js/console.debug ":remote/page-create" (pr-str page-create-event))
      {:fx [[:dispatch-n [[:remote/register-followup event-id followup-fx]
                          [:remote/send-event! page-create-event]]]]})))


(rf/reg-event-fx
  :remote/page-delete
  (fn [{db :db} [_ uid]]
    (let [last-seen-tx      (:remote/last-seen-tx db)
          page-delete-event (common-events/build-page-delete-event last-seen-tx
                                                                   uid)]
      (js/console.debug ":remote/page-delete" (pr-str page-delete-event))
      {:fx [[:dispatch [:remote/send-event! page-delete-event]]]})))


(rf/reg-event-fx
  :remote/page-add-shortcut
  (fn [{db :db} [_ uid]]
    (let [last-seen-tx       (:remote/last-seen-tx db)
          add-shortcut-event (common-events/build-page-add-shortcut last-seen-tx uid)]
      (js/console.debug ":remote/page-add-shortcut:" (pr-str add-shortcut-event))
      {:fx [[:dispatch [:remote/send-event! add-shortcut-event]]]})))


(rf/reg-event-fx
  :remote/page-remove-shortcut
  (fn [{db :db} [_ uid]]
    (let [last-seen-tx          (:remote/last-seen-tx db)
          remove-shortcut-event (common-events/build-page-remove-shortcut last-seen-tx uid)]
      (js/console.debug ":page/remove-shortcut:" (pr-str remove-shortcut-event))
      {:fx [[:dispatch [:remote/send-event! remove-shortcut-event]]]})))


;; - Block related

(rf/reg-event-fx
  :remote/followup-block-save
  (fn [{_db :db} [_ {:keys [event-id callback]}]]
    (js/console.debug ":remote/followup-block-save" event-id)
    {:fx [[:invoke-callback callback]
          [:dispatch [:remote/unregister-followup event-id]]]}))


(rf/reg-event-fx
  :remote/block-save
  (fn [{db :db} [_ {:keys [uid new-string callback]}]]
    (let [last-seen-tx     (:remote/last-seen-tx db)
          {event-id :event/id
           :as      event} (common-events/build-block-save-event last-seen-tx
                                                                 uid
                                                                 new-string)
          followup-fx      [[:dispatch [:remote/followup-block-save {:event-id event-id
                                                                     :callback callback}]]]]
      (js/console.debug ":remote/block-stave" (pr-str event))
      {:fx [[:dispatch-n [[:remote/register-followup event-id followup-fx]
                          [:remote/send-event! event]]]]})))


(rf/reg-event-fx
  :remote/followup-new-block
  (fn [{db :db} [_ {:keys [event-id embed-id]}]]
    (js/console.debug ":remote/followup-new-block" event-id)
    (let [{:keys [event]}   (get-event-acceptance-info db event-id)
          {:keys [new-uid]} (:event/args event)]
      (js/console.log ":remote/followup-new-block, new-uid" new-uid)
      {:fx [[:dispatch-n [[:editing/uid (str new-uid (when embed-id
                                                       (str "-embed-" embed-id)))]
                          [:remote/unregister-followup event-id]]]]})))


(rf/reg-event-fx
  :remote/new-block
  (fn [{db :db} [_ {:keys [block parent new-uid embed-id]}]]
    (let [last-seen-tx               (:remote/last-seen-tx db)
          {event-id :event/id
           :as      new-block-event} (common-events/build-new-block-event last-seen-tx
                                                                          (:remote/db-id parent)
                                                                          (:block/order block)
                                                                          new-uid)
          followup-fx                [[:dispatch [:remote/followup-new-block {:event-id event-id
                                                                              :embed-id embed-id}]]]]
      (js/console.debug ":remote/new-block" (pr-str new-block-event))
      {:fx [[:dispatch-n [[:remote/register-followup event-id followup-fx]
                          [:remote/send-event! new-block-event]]]]})))


(rf/reg-event-fx
  :remote/followup-add-child
  (fn [{db :db} [_ {:keys [event-id embed-id]}]]
    (js/console.debug ":remote/followup-add-child" event-id)
    (let [{:keys [event]} (get-event-acceptance-info db event-id)
          {:keys [new-uid]} (:event/args event)]
      (js/console.log ":remote/followup-add-child, new-uid" new-uid)
      {:fx [[:dispatch-n [[:editing/uid (str new-uid (when embed-id
                                                       (str "-embed-" embed-id)))]
                          [:remote/unregister-followup event-id]]]]})))


(rf/reg-event-fx
  :remote/add-child
  (fn [{db :db} [_ {:keys [block-eid new-uid embed-id]}]]
    (let [last-seen-tx               (:remote/last-seen-tx db)
          {event-id :event/id
           :as      add-child-event} (common-events/build-add-child-event last-seen-tx
                                                                          block-eid
                                                                          new-uid)
          followup-fx                [[:dispatch [:remote/followup-add-child {:event-id event-id
                                                                              :embed-id embed-id}]]]]
      (js/console.debug ":remote/add-child" (pr-str add-child-event))
      {:fx [[:dispatch-n [[:remote/register-followup event-id followup-fx]
                          [:remote/send-event! add-child-event]]]]})))


(rf/reg-event-fx
  :remote/followup-open-block-add-child
  (fn [{db :db} [_ {:keys [event-id embed-id] :as args}]]
    (js/console.debug ":remote/followup-open-block-add-child" (pr-str args))
    (let [{:keys [event]}   (get-event-acceptance-info db event-id)
          {:keys [new-uid]} (:event/args event)]
      (js/console.log ":remote/followup-open-block-add-child, new-uid" new-uid
                      ", embed-id" embed-id)
      {:fx [[:dispatch [:editing/uid (str new-uid (when embed-id
                                                    (str "-embed-" embed-id)))]]]})))


(rf/reg-event-fx
  :remote/open-block-add-child
  (fn [{db :db} [_ {:keys [block-eid new-uid embed-id]}]]
    (let [last-seen-tx               (:remote/last-seen-tx db)
          {event-id :event/id
           :as      add-child-event} (common-events/build-open-block-add-child-event last-seen-tx
                                                                                     block-eid
                                                                                     new-uid)
          followup-fx                [[:dispatch [:remote/followup-open-block-add-child {:event-id event-id
                                                                                         :embed-id embed-id}]]]]
      (js/console.debug ":remote/add-child" (pr-str add-child-event))
      {:fx [[:dispatch-n [[:remote/register-followup event-id followup-fx]
                          [:remote/send-event! add-child-event]]]]})))


(rf/reg-event-fx
  :remote/followup-split-block
  (fn [{db :db} [_ {:keys [event-id embed-id] :as args}]]
    (js/console.debug ":remote/followup-split-block args" (pr-str args))
    (let [{:keys [event]} (get-event-acceptance-info db event-id)
          {:keys [new-uid]} (:event/args event)]
      (js/console.debug ":remote/followup-split-block new-uid:" new-uid
                        ", embed-id" embed-id)
      {:fx [[:dispatch [:editing/uid (str new-uid (when embed-id
                                                    (str "-embed-" embed-id)))]]]})))


(rf/reg-event-fx
  :remote/split-block
  (fn [{db :db} [_ {:keys [uid value index new-uid embed-id] :as args}]]
    (js/console.debug ":remote/split-block args" (pr-str args))
    (let [last-seen-tx                 (:remote/last-seen-tx db)
          {event-id :event/id
           :as      split-block-event} (common-events/build-split-block-event last-seen-tx
                                                                              uid
                                                                              value
                                                                              index
                                                                              new-uid)
          followup-fx                  [[:dispatch [:remote/followup-split-block {:event-id event-id
                                                                                  :embed-id embed-id}]]]]
      (js/console.debug ":remote/split-block event" (pr-str split-block-event))
      {:fx [[:dispatch-n [[:remote/register-followup event-id followup-fx]
                          [:remote/send-event! split-block-event]]]]})))


(rf/reg-event-fx
  :remote/followup-split-block-to-children
  (fn [{db :db} [_ {:keys [event-id embed-id] :as args}]]
    (js/console.debug ":remote/followup-split-block-to-children args" (pr-str args))
    (let [{:keys [event]}   (get-event-acceptance-info db event-id)
          {:keys [new-uid]} (:event/args event)]
      (js/console.debug ":remote/followup-split-block-to-children new-uid:" new-uid
                        ", embed-id" embed-id)
      {:fx [[:dispatch [:editing/uid (str new-uid (when embed-id
                                                    (str "-embed-" embed-id)))]]]})))


(rf/reg-event-fx
  :remote/split-block-to-children
  (fn [{db :db} [_ {:keys [uid value index new-uid embed-id] :as args}]]
    (js/console.debug ":remote/split-block-to-children args" (pr-str args))
    (let [last-seen-tx     (:remote/last-seen-tx db)
          {event-id :event/id
           :as      event} (common-events/build-split-block-to-children-event last-seen-tx
                                                                              uid
                                                                              value
                                                                              index
                                                                              new-uid)
          followup-fx      [[:dispatch [:remote/followup-split-block-to-children {:event-id event-id
                                                                                  :embed-id embed-id}]]]]
      (js/console.debug ":remote/split-block-to-children event" (pr-str event))
      {:fx [[:dispatch-n [[:remote/register-followup event-id followup-fx]
                          [:remote/send-event! event]]]]})))


(rf/reg-event-fx
  :remote/followup-indent
  (fn [{db :db} [_ {:keys [event-id start end] :as args}]]
    (js/console.debug ":remote/followup-indent args" (pr-str args))
    (let [{:keys [event]} (get-event-acceptance-info db event-id)
          {:keys [uid]}   (:event/args event)]
      (js/console.debug ":remote/followup-indent uid:" uid)
      {:fx [[:set-cursor-position [uid start end]]]})))


(rf/reg-event-fx
  :remote/indent
  (fn [{db :db} [_ {:keys [uid value start end] :as args}]]
    (js/console.debug ":remote/indent args" (pr-str args))
    (let [last-seen-tx        (:remote/last-seen-tx db)
          {event-id :event/id
           :as      event}    (common-events/build-indent-event last-seen-tx uid value)
          followup-fx         [[:dispatch [:remote/followup-indent {:event-id event-id
                                                                    :start    start
                                                                    :end      end}]]]]
      (js/console.debug ":remote/indent event" (pr-str event))
      {:fx [[:dispatch-n [[:remote/register-followup event-id followup-fx]
                          [:remote/send-event! event]]]]})))

(rf/reg-event-fx
  :remote/indent-multi
  (fn [{db :db} [_ {:keys [uids blocks] :as args}]]
    (js/console.debug ":remote/indent-multi args" args)
    (let [last-seen-tx        (:remote/last-seen-tx db)
          indent-multi-event  (common-events/build-indent-multi-event last-seen-tx uids blocks)]
      (js/console.debug ":remote/indent-multi event" (pr-str indent-multi-event))
      {:fx [[:dispatch [[:remote/send-event! indent-multi-event]]]]})))


(rf/reg-event-fx
  :remote/followup-unindent
  (fn [{db :db} [_ {:keys [event-id embed-id start end] :as args}]]
    (js/console.debug ":remote/followup-unindent args" (pr-str args))
    (let [{:keys [event]} (get-event-acceptance-info db event-id)
          {:keys [uid]}   (:event/args event)]
      (js/console.debug ":remote/followup-unindent uid:" uid
                        ", embed-id:" embed-id)
      {:fx [[:dispatch [:editing/uid (str uid (when embed-id
                                                (str "-embed-" embed-id)))]]
            [:set-cursor-position [uid start end]]]})))


(rf/reg-event-fx
  :remote/unindent
  (fn [{db :db} [_ {:keys [uid value start end embed-id] :as args}]]
    (js/console.debug ":remote/unindent args" (pr-str args))
    (let [last-seen-tx     (:remote/last-seen-tx db)
          {event-id :event/id
           :as      event} (common-events/build-unindent-event last-seen-tx uid value)
          followup-fx      [[:dispatch [:remote/followup-unindent {:event-id event-id
                                                                   :embed-id embed-id
                                                                   :start    start
                                                                   :end      end}]]]]
      (js/console.debug ":remote/unindent event" (pr-str event))
      {:fx [[:dispatch-n [[:remote/register-followup event-id followup-fx]
                          [:remote/send-event! event]]]]})))



(rf/reg-event-fx
  :remote/unindent-multi
  (fn [{db :db} [_ {:keys [uids f-uid] :as args}]]
    (js/console.debug ":remote/unindent-multi args" args)
    (let [last-seen-tx           (:remote/last-seen-tx db)
          unindent-multi-event   (common-events/build-unindent-multi-event last-seen-tx uids f-uid)]
      (js/console.debug ":remote/unindent-multi event" (pr-str unindent-multi-event))
      {:fx [[:dispatch [[:remote/send-event! unindent-multi-event]]]]})))


(rf/reg-event-fx
  :remote/followup-bump-up
  (fn [{db :db} [_ {:keys [event-id embed-id] :as args}]]
    (js/console.debug ":remote/followup-bump-up args" (pr-str args))
    (let [{:keys [event]}   (get-event-acceptance-info db event-id)
          {:keys [new-uid]} (:event/args event)]
      (js/console.debug ":remote/followup-bump-up new-uid:" new-uid
                        ", embed-id:" embed-id)
      {:fx [[:dispatch [:editing/uid (str new-uid (when embed-id
                                                    (str "-embed-" embed-id)))]]]})))


(rf/reg-event-fx
  :remote/bump-up
  (fn [{db :db} [_ {:keys [uid new-uid embed-id] :as args}]]
    (js/console.debug ":remote/bump-up args" (pr-str args))
    (let [last-seen-tx     (:remote/last-seen-tx db)
          {event-id :event/id
           :as      event} (common-events/build-bump-up-event last-seen-tx
                                                              uid
                                                              new-uid)
          followup-fx [[:dispatch [:remote/followup-bump-up {:event-id event-id
                                                             :embed-id embed-id}]]]]
      (js/console.debug ":remote/bump-up event" (pr-str event))
      {:fx [[:dispatch-n [[:remote/register-followup event-id followup-fx]
                          [:remeote/send-event! event]]]]})))


(rf/reg-event-fx
  :remote/paste-verbatim
  (fn [{db :db} [_ uid text start value]]
    (let [last-seen-tx         (:remote/last-seen-tx db)
          paste-verbatim-event (common-events/build-paste-verbatim-event last-seen-tx
                                                                         uid
                                                                         text
                                                                         start
                                                                         value)]
      {:fx [[:dispatch [:remote/send-event! paste-verbatim-event]]]})))
