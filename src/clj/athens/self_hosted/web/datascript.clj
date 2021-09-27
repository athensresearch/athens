(ns athens.self-hosted.web.datascript
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.resolver        :as resolver]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.self-hosted.clients           :as clients]
    [clojure.pprint                       :as pprint]
    [clojure.tools.logging                :as log]
    [datahike.api                         :as d])
  (:import
    (clojure.lang
      ExceptionInfo)))


(def supported-event-types
  #{:datascript/create-page
    :datascript/rename-page
    :datascript/merge-page
    :datascript/delete-page
    :datascript/block-save
    :datascript/new-block
    :datascript/add-child
    :datascript/open-block-add-child
    :datascript/split-block
    :datascript/split-block-to-children
    :datascript/unindent
    :datascript/indent
    :datascript/indent-multi
    :datascript/unindent-multi
    :datascript/page-add-shortcut
    :datascript/page-remove-shortcut
    :datascript/drop-child
    :datascript/drop-multi-child
    :datascript/drop-link-child
    :datascript/drop-diff-parent
    :datascript/drop-multi-diff-source-same-parents
    :datascript/drop-multi-diff-source-diff-parents
    :datascript/drop-link-diff-parent
    :datascript/drop-same
    :datascript/drop-multi-same-source
    :datascript/drop-multi-same-all
    :datascript/drop-link-same-parent
    :datascript/left-sidebar-drop-above
    :datascript/left-sidebar-drop-below
    :datascript/unlinked-references-link
    :datascript/unlinked-references-link-all
    :datascript/selected-delete
    :datascript/block-open
    :datascript/paste
    :datascript/paste-verbatim
    :datascript/delete-only-child
    :datascript/delete-merge-block
    :datascript/bump-up})


(def supported-atomic-ops
  #{:block/new
    :block/save
    :page/new
    :composite/consequence})


(defn transact!
  "Transact with Datahike.

  Returns event accepte/rejected response.
  
  Log errors."
  [connection event-id txs]
  (try
    (log/debug "Transacting event-id:" event-id ", txs:" (with-out-str
                                                           (pprint/pprint txs)))
    (let [last-tx (atom nil)
          ;; normalize txs
          txs     (if (map? txs)
                    [txs]
                    txs)]
      (log/debug "event-id:" event-id ", normalized-txs:" (with-out-str
                                                            (pprint/pprint txs)))
      (let [processed-tx            (->> txs
                                         (common-db/linkmaker @connection)
                                         (common-db/orderkeeper @connection))
            {:keys [tempids]}       (d/transact connection processed-tx)
            {:db/keys [current-tx]} tempids]
        (reset! last-tx current-tx)
        (log/debug "Transacted event-id:" event-id ", tx-id:" current-tx))
      (common-events/build-event-accepted event-id @last-tx))

    (catch ExceptionInfo ex
      (let [err-msg   (ex-message ex)
            err-data  (ex-data ex)
            err-cause (ex-cause ex)]
        (log/error ex (str "Transacting event-id: " event-id
                           " FAIL: " (pr-str {:msg   err-msg
                                              :data  err-data
                                              :cause err-cause})))
        (common-events/build-event-rejected event-id err-msg err-data)))))


(def single-writer-guard (Object.))


(defn default-handler
  [datahike _channel {:event/keys [id] :as event}]
  (locking single-writer-guard
    (let [txs (resolver/resolve-event-to-tx @datahike event)]
      (transact! datahike id txs))))


(defn datascript-handler
  [datahike channel {:event/keys [id type args] :as event}]
  (log/debug (clients/get-client-username channel) "-> Received:" type "with args:" args)
  ;; TODO Check if potentially conflicting event?
  ;; if so compare tx-id from client with HEAD master DB
  ;; current -> continue
  ;; stale -> reject
  (if (contains? supported-event-types type)
    (default-handler datahike channel event)
    (do
      (log/error "datascript-handler, unsupported event:" (pr-str event))
      (common-events/build-event-rejected id
                                          (str "Unsupported event: " type)
                                          {:unsupported-type type}))))


(defn atomic-op-exec
  [datahike _channel id op]
  (locking single-writer-guard
    (try
      (let [txs (atomic-resolver/resolve-atomic-op-to-tx @datahike op)]
        (transact! datahike id txs))
      (catch ExceptionInfo ex
        (let [err-msg   (ex-message ex)
              err-data  (ex-data ex)
              err-cause (ex-cause ex)]
          (log/error ex (str "Atomic Graph Op event-id: " id
                             " FAIL: " (pr-str {:msg   err-msg
                                                :data  err-data
                                                :cause err-cause})))
          (common-events/build-event-rejected id err-msg err-data))))))


(defn atomic-op-handler
  [datahike channel {:event/keys [id op]}]
  (let [username          (clients/get-client-username channel)
        {:op/keys [type]} op]
    (log/debug username "-> Received Atomic Op:" (pr-str op))
    (if (contains? supported-atomic-ops type)
      (atomic-op-exec datahike channel id op)
      (common-events/build-event-rejected id
                                          (str "Under development event: " type)
                                          {:unsuported-type type}))))
