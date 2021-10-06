(ns athens.self-hosted.web.datascript
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.resolver        :as resolver]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.common.logging                :as log]
    [athens.self-hosted.clients           :as clients]
    [clojure.pprint                       :as pprint]
    [datahike.api                         :as d])
  (:import
    (clojure.lang
      ExceptionInfo)))


(def supported-event-types
  #{:datascript/rename-page
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
    (let [txs (if (map? txs)
                [txs]
                txs)]
      (log/debug "transact! event-id:" event-id ", normalized-txs:" (with-out-str
                                                                      (pprint/pprint txs)))
      (let [processed-tx            (->> txs
                                         (common-db/linkmaker @connection)
                                         (common-db/orderkeeper @connection))
            {:keys [tempids]}       (d/transact connection processed-tx)
            {:db/keys [current-tx]} tempids]
        (log/debug "transact! event-id:" event-id ", transacted in tx-id:" current-tx)
        (common-events/build-event-accepted event-id current-tx)))

    (catch ExceptionInfo ex
      (let [err-msg   (ex-message ex)
            err-data  (ex-data ex)
            err-cause (ex-cause ex)]
        (log/error ex (str "event-id: " event-id
                           "Processing transaction FAIL: "
                           (pr-str {:msg   err-msg
                                    :data  err-data
                                    :cause err-cause})))
        (common-events/build-event-rejected event-id err-msg err-data)))))


(def single-writer-guard (Object.))


(defn default-handler
  [datahike channel {:event/keys [id type] :as event}]
  (let [username (clients/get-client-username channel)]
    (locking single-writer-guard
      (let [txs (resolver/resolve-event-to-tx @datahike event)]
        (log/debug (str "resolved-event-to-tx: username: " username ", event-id: " id ", type: " (pr-str type)))
        (transact! datahike id txs)))))


(defn datascript-handler
  [datahike channel {:event/keys [id type] :as event}]
  (let [username (clients/get-client-username channel)]
    (log/info (str "username: " username ", event-id: " id ", type: " (pr-str type)))
    ;; TODO Check if potentially conflicting event?
    ;; if so compare tx-id from client with HEAD master DB
    ;; current -> continue
    ;; stale -> reject
    (if (contains? supported-event-types type)
      (try
        (default-handler datahike channel event)
        (catch ExceptionInfo ex
          (let [msg (str "username: " username ", event-id: " id ", Exception during resolving or transacting.")]
            (log/error ex msg)
            (common-events/build-event-rejected id
                                                msg
                                                (ex-data ex)))))
      (do
        (log/error "FAIL Unsupported event type."
                   "username:" username
                   ", event-id:" id
                   ", type:" (pr-str type))
        (common-events/build-event-rejected id
                                            (str "Unsupported event type: " type)
                                            {:unsupported-type type})))))


(defn atomic-op-exec
  [datahike channel id op]
  (let [username (clients/get-client-username channel)]
    (locking single-writer-guard
      (try
        (let [txs (atomic-resolver/resolve-atomic-op-to-tx @datahike op)]
          (log/debug "username:" username
                     "event-id:" id
                     "atomic/op:" (pr-str (:op/type op))
                     "Resolved Atomic op to tx.")
          (transact! datahike id txs))
        (catch ExceptionInfo ex
          (let [err-msg   (ex-message ex)
                err-data  (ex-data ex)
                err-cause (ex-cause ex)]
            (log/error ex (str "Atomic Graph Op event-id: " id
                               " FAIL: " (pr-str {:msg   err-msg
                                                  :data  err-data
                                                  :cause err-cause})))
            (common-events/build-event-rejected id err-msg err-data)))))))


(defn atomic-op-handler
  [datahike channel {:event/keys [id op]}]
  (let [username          (clients/get-client-username channel)
        {:op/keys [type]} op]
    (log/debug "username:" username
               "event-id:" id
               "-> Received Atomic Op Type:" (pr-str type))
    (if (contains? supported-atomic-ops type)
      (atomic-op-exec datahike channel id op)
      (common-events/build-event-rejected id
                                          (str "Under development event: " type)
                                          {:unsuported-type type}))))
