(ns athens.self-hosted.components.datascript
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.resolver.atomic :as atomic]
    [athens.common.logging                :as log]
    [athens.self-hosted.event-log         :as event-log]
    [clojure.pprint                       :as pp]
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
    (let [in-memory? (-> config :config :in-memory?)
          conn       (d/create-conn common-db/schema)]

      (log/info "Lazily replaying events into empty Datascript conn...")
      (let [total (atom 0)]
        ;; NB: don't hold a ref to the lazy event seq, otherwise they
        ;; can't be GC'd as we go and are all kept in memory at once.
        (doseq [[id data] (if in-memory?
                            event-log/initial-events
                            (event-log/events fluree))]
          (log/info "Processing" (pr-str id) "with" (common-events/find-event-or-atomic-op-type data))
          (try
            (atomic/resolve-transact! conn data)
            (swap! total inc)
            (catch Error err
              (log/warn "Event that we've failed to process was:"
                        (with-out-str
                          (pp/pprint data)))
              (log/error err "Error during processing" (pr-str id)))
            (catch ExceptionInfo ex
              (log/warn "Event that we've failed to process was:"
                        (with-out-str
                          (pp/pprint data)))
              (log/error ex "Exception during processing" (pr-str id)))))
        (log/info "✅ Replayed" @total "events."))
      (common-db/health-check conn)
      (assoc component :conn conn)))


  (stop
    [component]
    (log/info "Stopping Datascript")
    (dissoc component :conn)))


(defn new-datascript
  []
  (map->Datascript {}))

