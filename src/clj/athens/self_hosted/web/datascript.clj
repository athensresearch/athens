(ns athens.self-hosted.web.datascript
  (:require
    [athens.common-events  :as common-events]
    [clojure.tools.logging :as log]
    [datahike.api          :as d]))


(def supported-event-types
  #{:datascript/paste-verbatim
    ;; TODO: all the events
    })


(defn paste-verbatim-handler
  [datahike _channel {:event/keys [args] :as _event}]
  (let [{:keys [uid
                text
                start
                value]} args
        txs             (common-events/paste-verbatim->tx uid text start value)]
    ;; TODO process result and emit response
    (d/transact (:conn datahike) txs))
  ;; TODO process it
  ;; 1. with cljc common events resolve event into txs
  ;; 2. transact!
  ;; 3. confirm event processed
  )


(defn datascript-handler
  [datahike channel {:event/keys [type args] :as event}]
  (log/info channel "Received:" type "with args:" args)
  ;; TODO Check if potentially conflicting event?
  ;; if so compare tx-id from client with HEAD master DB
  ;; current -> continue
  ;; stale -> reject
  (condp = type
    :datascript/paste-verbatim (paste-verbatim-handler datahike channel event)))

