(ns athens.self-hosted.web.datascript
  (:require
    [clojure.tools.logging :as log]
    #_[datahike.api          :as d]))


(def supported-event-types
  #{:datascript/paste-verbatim
    ;; TODO: all the events
    })


(defn paste-verbatim-handler
  [_channel {:event/keys [_args] :as _event}]
  ;; TODO process it
  ;; 1. with cljc common events resolve event into txs
  ;; 2. transact!
  ;; 3. confirm event processed
  )


(defn datascript-handler
  [channel {:event/keys [type args] :as event}]
  (log/info channel "Received:" type "with args:" args)
  ;; TODO Check if potentially conflicting event?
  ;; if so compare tx-id from client with HEAD master DB
  ;; current -> continue
  ;; stale -> reject
  (condp = type
    :datascript/paste-verbatim (paste-verbatim-handler channel event)))

