(ns athens.ws-client
  (:require
    [athens.db :as db]
    [dat.sync.client]
    [re-frame.core :as rf :refer [subscribe dispatch dispatch-sync]]
    [taoensso.sente :as sente]
    [datascript.core :as d]))


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



(declare channel-socket)
(declare chsk)
(declare ch-chsk)
(declare chsk-send!)
(declare chsk-state)
(declare router)
(declare start-router!)
(defonce cur-random (str (random-uuid)))


;; ADDRESS BEFORE MERGE
;; host and port
(def config {:type     :auto
             :packer   :edn
             :protocol :http
             :host     "localhost"
             :port     3010})

#_:clj-kondo/ignore
(defn start-socket! []
  ;; ADDRESS BEFORE MERGE
  ;; x - is the csrf-token, since we don't have much user info
  ;; simple strategy here is to keep a baked in csrf token and build pipeline
  ;; for each enterprise app and deploy to them separately
  (defonce channel-socket (sente/make-channel-socket! "/chsk" "x" config))
  (defonce chsk (:chsk channel-socket))
  (defonce ch-chsk (:ch-recv channel-socket))
  (defonce chsk-send! (:send-fn channel-socket))
  (defonce chsk-state (:state channel-socket))
  (start-router!))


(defmulti event-msg-handler
  (fn [msg]
    (if (contains? #{:dat.sync.client/bootstrap :dat.sync.client/recv-remote-tx}
                   (->> msg :event second first))
      (->> msg :event second first)
      (:id msg))))


(defmethod event-msg-handler :default
  [{:keys [event]}]
  (println "Unhandled event: " (:id event)))


(defmethod event-msg-handler :chsk/state
  [{:keys [?data]}]
  (if (->> ?data second :first-open?)
    (do (println "Channel socket successfully established!")
        ((:send-fn channel-socket) [:dat.sync.client/request-bootstrap true]))
    (println "Channel socket state change:" ?data)))


(defmethod event-msg-handler :chsk/recv
  [{:keys [?data]}]
  (dispatch [:presence/new (second ?data)]))


(defn send-user-details []
  (chsk-send! [:user/details (merge @(subscribe [:user/current])
                                    {:editing/uid @(subscribe [:editing/uid])
                                     :current/uid @(subscribe [:current-route/uid])
                                     :random/id   cur-random})]))


(defmethod event-msg-handler :chsk/handshake
  [{:keys [?data]}]
  (println "Handshake:" ?data)
  (send-user-details))


(defn start-router! []
  #_:clj-kondo/ignore
  (defonce router
    (sente/start-client-chsk-router! ch-chsk event-msg-handler)))


(defn send-presence! []
  (send-user-details)
  (js/setTimeout (fn [] (send-presence!)) 5000))

;; txn
(def x (atom nil))
(defmethod event-msg-handler :dat.sync.client/recv-remote-tx
  [{:keys [?data]}]
  (let [remote-tx-meta {:dat.sync.prov/agent :dat.sync/remote}]
    (dat.sync.client/transact-with-middleware!
      db/dsdb dat.sync.client/wrap-remote-tx
      (second ?data) remote-tx-meta)))


(def x2 (atom nil))
(defmethod event-msg-handler :dat.sync.client/bootstrap
  [{:keys [?data]}]
  (let [remote-tx-meta {:dat.sync.prov/agent :dat.sync/remote}]
    (dat.sync.client/transact-with-middleware!
      db/dsdb dat.sync.client/wrap-remote-tx
      (second ?data) remote-tx-meta))
  #_(let [normalized-tx (dat.sync.client/normalize-tx (second ?data))
          translated-tx (dat.sync.client/translate-eids @db/dsdb normalized-tx)
          remote-tx-meta {:dat.sync.prov/agent :dat.sync/remote}]
      (d/transact db/dsdb translated-tx remote-tx-meta)

      #_(d/with @db/dsdb translated-tx remote-tx-meta))
  #_(dat.sync.client/transact-with-middleware!
      db/dsdb dat.sync.client/wrap-remote-tx
      (second ?data) {:dat.sync.prov/agent :dat.sync/remote}))
