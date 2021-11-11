(ns athens.self-hosted.web.datascript
  (:require
    [athens.common-events                 :as common-events]
    [athens.common-events.resolver.atomic :as atomic-resolver]
    [athens.common.logging                :as log]
    [athens.self-hosted.clients           :as clients])
  (:import
    (clojure.lang
      ExceptionInfo)))


(def supported-event-types
  #{:datascript/delete-page
    :datascript/block-save
    :datascript/new-block
    :datascript/add-child
    :datascript/split-block
    :datascript/left-sidebar-drop-above
    :datascript/left-sidebar-drop-below
    :datascript/selected-delete
    :datascript/delete-only-child
    :datascript/delete-merge-block
    :datascript/paste-internal})


(def supported-atomic-ops
  #{:block/new
    :block/save
    :block/open
    :block/remove
    :block/move
    :page/new
    :shortcut/add
    :shortcut/remove
    :page/rename
    :page/merge
    :page/remove
    :composite/consequence})


(def single-writer-guard (Object.))


(defn exec!
  [conn {:event/keys [id] :as event}]
  (locking single-writer-guard
    (try
      (atomic-resolver/resolve-transact! conn event)
      (common-events/build-event-accepted id)
      (catch ExceptionInfo ex
        (let [err-msg   (ex-message ex)
              err-data  (ex-data ex)
              err-cause (ex-cause ex)]
          (log/error ex (str "Exec event-id: " id
                             " FAIL: " (pr-str {:msg   err-msg
                                                :data  err-data
                                                :cause err-cause})))
          (common-events/build-event-rejected id err-msg err-data))))))


(defn datascript-handler
  [conn channel {:event/keys [id type] :as event}]
  (let [username (clients/get-client-username channel)]
    (log/info (str "username: " username ", event-id: " id ", type: " (pr-str type)))
    ;; TODO Check if potentially conflicting event?
    ;; if so compare tx-id from client with HEAD master DB
    ;; current -> continue
    ;; stale -> reject
    (if (contains? supported-event-types type)
      (try
        (exec! conn event)
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


(defn atomic-op-handler
  [conn channel {:event/keys [id op] :as event}]
  (let [username          (clients/get-client-username channel)
        {:op/keys [type]} op]
    (log/debug "username:" username
               "event-id:" id
               "-> Received Atomic Op Type:" (pr-str type))
    (if (contains? supported-atomic-ops type)
      (exec! conn event)
      (common-events/build-event-rejected id
                                          (str "Under development event: " type)
                                          {:unsuported-type type}))))
