(ns athens.self-hosted.components.datascript
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events.graph.ops       :as graph-ops]
    [athens.common-events.resolver.atomic :as atomic]
    [athens.common.logging                :as log]
    [athens.self-hosted.event-log         :as event-log]
    [athens.self-hosted.web.datascript    :as web-datascript]
    [com.stuartsierra.component           :as component]
    [datascript.core                      :as d]))


(defrecord Datascript
  [config fluree conn]

  component/Lifecycle

  (start
    [component]
    (let [in-memory?     (-> config :config :in-memory?)
          fluree-conn    (:conn fluree)
          conn           (d/create-conn common-db/schema)]

      (log/info "Lazily replaying events into empty Datascript conn...")
      (let [total (atom 0)]
        ;; NB: don't hold a ref to the lazy event seq, otherwise they
        ;; can't be GC'd as we go and are all kept in memory at once.
        (doseq [[id data] (if in-memory?
                            event-log/initial-events
                            (event-log/events fluree-conn))]
          (log/debug "Processing" (pr-str id) "with" (pr-str data))
          ;; TODO(now) use proper iterative resolver
          (if (graph-ops/atomic-composite? data)
            (doseq [atomic (graph-ops/extract-atomics data)
                    :let   [atomic-txs (atomic/resolve-atomic-op-to-tx @conn atomic)]]
              (web-datascript/transact! conn id (atomic/resolve-to-tx @conn atomic-txs)))
            (web-datascript/transact! conn id (atomic/resolve-to-tx @conn data)))
          (swap! total inc))
        (log/info "✅ Replayed" @total "events."))

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

