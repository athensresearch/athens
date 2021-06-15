(ns athens.self-hosted.web.datascript
  (:require
    [athens.common-events  :as common-events]
    [clojure.tools.logging :as log]
    [datahike.api          :as d])
  (:import
    (clojure.lang
      ExceptionInfo)))


(def supported-event-types
  #{:datascript/paste-verbatim
    :datascript/create-page
    ;; TODO: all the events
    })


(defn transact!
  "Transact with Datahike.

  Returns event accepte/rejected response.
  
  Log errors."
  [connection event-id tx]
  (try
    (log/debug "Transacting event-id:" event-id ", tx:" (pr-str tx))
    (let [{:keys [tempids]}       (d/transact connection tx)
          {:db/keys [current-tx]} tempids]
      (log/info "Transacted event-id:" event-id ", tx-id:" current-tx)
      (common-events/build-event-accepted event-id current-tx))
    (catch ExceptionInfo ex
      (let [err-msg   (ex-message ex)
            err-data  (ex-data ex)
            err-cause (ex-cause ex)]
        (log/error ex (str "Transacting event-id: " event-id
                           " FAIL: " (pr-str {:msg   err-msg
                                              :data  err-data
                                              :cause err-cause})))
        (common-events/build-event-rejected event-id err-msg err-data)))))


(defn create-page-handler
  [datahike _channel {:event/keys [id args] :as _event}]
  (let [{:keys [uid
                title]} args
        txs             (common-events/page-create->tx uid title)]
    (transact! (:conn datahike) id txs)))


(defn paste-verbatim-handler
  [datahike _channel {:event/keys [id args] :as _event}]
  (let [{:keys [uid
                text
                start
                value]} args
        txs             (common-events/paste-verbatim->tx uid text start value)]
    (transact! (:conn datahike) id txs)))


(defn datascript-handler
  [datahike channel {:event/keys [type args] :as event}]
  (log/info channel "Received:" type "with args:" args)
  ;; TODO Check if potentially conflicting event?
  ;; if so compare tx-id from client with HEAD master DB
  ;; current -> continue
  ;; stale -> reject
  (condp = type
    :datascript/create-page    (create-page-handler datahike channel event)
    :datascript/paste-verbatim (paste-verbatim-handler datahike channel event)))

