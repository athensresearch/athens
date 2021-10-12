(ns athens.self-hosted.components.datascript
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.resolver.atomic :as atomic]
    [athens.common.logging                :as log]
    [athens.self-hosted.event-log         :as event-log]
    [com.stuartsierra.component           :as component]
    [datascript.core                      :as d])
  (:import
    (clojure.lang
      ExceptionInfo)))


(defrecord Datascript
  [config fluree conn]

  component/Lifecycle

  (start
    [component]
    (let [in-memory?     (-> config :config :in-memory?)
          fluree-conn    (:conn fluree)
          conn           (d/create-conn common-db/schema)
          events         (if in-memory?
                           event-log/initial-events
                           (event-log/all-events fluree-conn))]

      (log/info "Replaying" (count events) "events into empty Datascript conn...")
      (doseq [[id data] events]
        (try
          (d/transact! conn (atomic/resolve-to-tx @conn data))
          (catch ExceptionInfo ex
            (let [err-msg   (ex-message ex)
                  err-data  (ex-data ex)
                  err-cause (ex-cause ex)]
              (log/error ex (str "event-id: " id
                                 "Replaying transaction FAIL: "
                                 (pr-str {:msg   err-msg
                                          :data  err-data
                                          :cause err-cause})))))))
      (log/info "✅ Replayed" (count events) "events.")

      ;; NB: these could be events as well, and then we wouldn't always rerun them.
      ;; But rerunning them after replaying all events helps us find events that produce
      ;; states that need fixing.
      (log/info "Knowledge graph health check...")
      (let [linkmaker-txs       (common-db/linkmaker @conn)
            orderkeeper-txs     (common-db/orderkeeper @conn)
            block-nil-eater-txs (common-db/block-uid-nil-eater @conn)]
        (when-not (empty? linkmaker-txs)
          (log/warn "linkmaker fixes#:" (count linkmaker-txs))
          (log/info "linkmaker fixes:" (pr-str linkmaker-txs))
          (d/transact! conn linkmaker-txs))
        (when-not (empty? orderkeeper-txs)
          (log/warn "orderkeeper fixes#:" (count orderkeeper-txs))
          (log/info "orderkeeper fixes:" (pr-str orderkeeper-txs))
          (d/transact! conn orderkeeper-txs))
        (when-not (empty? block-nil-eater-txs)
          (log/warn "block-uid-nil-eater fixes#:" (count block-nil-eater-txs))
          (log/info "block-uid-nil-eater fixes:" (pr-str block-nil-eater-txs))
          (d/transact! conn block-nil-eater-txs))
        (log/info "✅ Knowledge graph health check."))
      (assoc component :conn conn)))


  (stop
    [component]
    (log/info "Stopping Datascript")
    (dissoc component :conn)))


(defn new-datascript
  []
  (map->Datascript {}))

