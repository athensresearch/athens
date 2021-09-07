(ns athens.self-hosted.web.datascript
  (:require
    [athens.common-events          :as common-events]
    [athens.common-events.resolver :as resolver]
    [athens.self-hosted.clients    :as clients]
    [clojure.tools.logging         :as log]
    [datahike.api                  :as d])
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


(defn transact!
  "Transact with Datahike.

  Returns event accepte/rejected response.
  
  Log errors."
  [connection event-id tx]
  (try
    (log/debug "Transacting event-id:" event-id ", tx:" (pr-str tx))
    (let [{:keys [tempids]}       (d/transact connection tx) ; TODO this is a place to hook walk-transact thing
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


(defn default-handler
  [datahike _channel {:event/keys [id] :as event}]
  (let [txs (resolver/resolve-event-to-tx @datahike event)]
    (transact! datahike id txs)))


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

