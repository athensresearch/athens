(ns athens.ws-client
  (:require
    [athens.datsync-utils :as dat-s]
    [cljs.reader :refer [read-string]]
    [dat.sync.client]
    [re-frame.core :as rf :refer [subscribe dispatch dispatch-sync]]
    [taoensso.sente :as sente]))


;;-------------------------------------------------------------------
;;--- re-frame ---

(declare start-socket!)


(rf/reg-sub
  :presence/current
  (fn [db]
    (:current-presence db)))


(rf/reg-event-db
  :presence/new
  (fn [db [_ new]]
    (assoc db :current-presence new)))


(rf/reg-sub
  :user/current
  (fn [db]
    (:user db)))


(rf/reg-event-db
  :user/set
  (fn [db [_ key val]]
    (assoc-in db [:user key] val)))


(rf/reg-event-db
  :user/reset
  (fn [db [_ new]]
    (assoc db :user new)))


(rf/reg-fx
  :start-socket
  (fn [_]
    (start-socket!)))


(rf/reg-event-fx
  :start-socket
  (fn [_]
    {:start-socket nil}))


(rf/reg-sub
  :socket-status
  (fn [db]
    (:socket-status db)))


(rf/reg-event-db
  :set-socket-status
  (fn [db [_ curr]]
    (assoc db :socket-status curr)))


;;-------------------------------------------------------------------
;;--- socket ---

(declare channel-socket)
(declare chsk)
(declare ch-chsk)
(declare chsk-send!)
(declare router)
(declare start-router!)
(defonce cur-random (str (random-uuid)))
(declare require-reload?)


(def base-config
  {:type     :auto
   :packer   :edn
   :protocol :http})


^:cljstyle/ignore
#_:clj-kondo/ignore
(defn start-socket!
  ([] (let [{:keys [address token] :as conf}
            (some->> "db/remote-graph-conf"
                     js/localStorage.getItem
                     read-string)]

        (when (and address token)
          (start-socket! conf))))
  ([{:keys [address token reload-on-init?]}]
   (try
     ;; ADDRESS BEFORE MERGE
     ;; x - is the csrf-token, since we don't have much user info
     ;; simple strategy here is to keep a baked in csrf token and build pipeline
     ;; for each enterprise app and deploy to them separately
     (def channel-socket
       (sente/make-channel-socket!
         "/chsk" token (merge base-config
                              {:host address})))
     (def chsk (:chsk channel-socket))
     (def ch-chsk (:ch-recv channel-socket))
     (def chsk-send! (:send-fn channel-socket))
     (def require-reload? reload-on-init?)
     (start-router!)
     (dispatch [:set-socket-status :running])
     (catch js/Error _
       (dispatch [:set-socket-status :closed])))))


(defmulti event-msg-handler
  (fn [msg]
    (if (contains? #{:dat.sync.client/bootstrap
                     :dat.sync.client/recv-remote-tx}
                   (->> msg :event second first))
      (->> msg :event second first)
      (and (keyword? (:id msg))
           (:id msg)))))


(defmethod event-msg-handler :default
  [{:keys [event]}]
  (println "Unhandled event: " (:id event)))


(defmethod event-msg-handler :chsk/state
  [{:keys [?data]}]
  (if (->> ?data second :first-open?)
    (do (println "Channel socket successfully established!")
        ((:send-fn channel-socket) [:dat.sync.client/request-bootstrap true]))
    (do (println "Channel socket state change:" ?data)
        (when (and (->> ?data second :last-ws-close)
                   (not (->> ?data second :last-ws-close :clean?)))
          (dispatch [:show-snack-msg {:msg "Connection failed"
                                      :type :fail}])
          (when (:default? @(subscribe [:db/remote-graph-conf]))
            (rf/dispatch-sync [:remote-graph/set-conf
                               :default? false]))
          (sente/chsk-disconnect! chsk)
          (when router (router))
          (dispatch [:set-socket-status :closed])))))


(defmethod event-msg-handler :chsk/recv
  [{:keys [?data]}]
  (dispatch [:presence/new (second ?data)]))


(defn send-user-details
  []
  (when (and (= @(subscribe [:socket-status]) :running)
             (not (chsk-send!
                    [:user/details
                     (merge @(subscribe [:user/current])
                            {:editing/uid @(subscribe [:editing/uid])
                             :current/uid @(subscribe [:current-route/uid])
                             :random/id   cur-random})])))
    (dispatch [:set-socket-status :closed])))


(defmethod event-msg-handler :chsk/handshake
  [{:keys [?data]}]
  (println "Handshake:" ?data)
  (if require-reload?
    (do (dispatch [:show-snack-msg {:msg "Connection established. Reloading ..."
                                    :type :success}])
        (rf/dispatch-sync [:remote-graph/set-conf
                           :default? true])
        (.reload js/location))
    (send-user-details)))


(defn start-router!
  []
  (when router (router))
  #_:clj-kondo/ignore
  (def router
    (sente/start-client-chsk-router! ch-chsk event-msg-handler)))


(defn send-presence!
  []
  (send-user-details)
  (when (= @(subscribe [:socket-status]) :running)
    (js/setTimeout (fn [] (send-presence!)) 500)))

;;-------------------------------------------------------------------
;;--- transactions ---


(defmethod event-msg-handler :dat.sync.client/recv-remote-tx
  [{:keys [?data]}]
  (let [[uid tx-data] (second ?data)]
    ;; If the current user sent the retract txn we don't
    ;; want that same information re-transacted as this is already
    ;; done locally and synced. Ent might be completely retracted
    ;; already and will throw error
    (cond->> tx-data
      (= cur-random uid)
      (remove #(contains? #{:db/retract :db/retractEntity}
                          (first %)))

      true dat-s/apply-remote-tx!)))


(defmethod event-msg-handler :dat.sync.client/bootstrap
  [{:keys [?data]}]
  (dat-s/apply-remote-tx! (second ?data))
  (send-presence!)
  (dispatch-sync [:loading/unset]))
