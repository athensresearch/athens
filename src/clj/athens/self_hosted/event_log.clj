(ns athens.self-hosted.event-log
  (:require
    [clojure.edn :as edn]
    [clojure.tools.logging :as log]
    [fluree.db.api :as fdb]))


(def ledger "events/log")


(def schema
  [{:_id :_collection
    :_collection/name :event
    :_collection/doc "Athens semantic events."}
   {:_id :_predicate
    :_predicate/name :event/id
    :_predicate/doc "A globally unique event id."
    :_predicate/unique true
    :_predicate/type :string}
   {:_id :_predicate
    :_predicate/name :event/data
    :_predicate/doc "Event data serialized as an EDN string."
    :_predicate/type :string}])


(def initial-events
  [;; TODO: Convert welcome page etc into events, add them here.
   ])


(defn serialize
  [id data]
  {:_id :event
   :event/id (str id)
   :event/data (pr-str data)})


(defn deserialize
  [{id   "event/id"
    data "event/data"}]
  [id (edn/read-string data)])


;; Resources on lazy clojure ops.
;; https://clojuredocs.org/clojure.core/lazy-seq
;; https://clojuredocs.org/clojure.core/lazy-cat
;; http://clojure-doc.org/articles/language/laziness.html
;; https://stackoverflow.com/a/44102122/2116927
;; The lazy-* fns aren't explicitly used here, but all fns used here are lazy,
;; so the end result is lazy as well.
(defn lazy-cat-while
  "Returns a lazy concatenation of (f i), where i starts at 0 and increases by 1 each iteration.
   Stops when (f i) is an empty seq."
  [f]
  (transduce (comp (map f)
                   (take-while seq))
             concat
             (range)))


(comment
  (defn get-page [i]
    (println "get-page" i)
    (when (< i 3)
      [1 2 3]))

  (get-page 2)

  (lazy-cat-while get-page))


(defn- events-page
  "Returns a seq of events in page-number for all events in db split by page-size."
  [db page-size page-number]
  @(fdb/query db
              {:select {"?event" ["*"]}
               :where  [["?event" "event/id", "?id"]]
               ;; Subject (?event here) is a monotonically incrementing bigint,
               ;; so ordering by that gives us insertion order.
               :opts   {:orderBy ["ASC", "?event"]
                        :limit   page-size
                        :offset  (* page-size page-number)}}))


(defn events
  "Returns a lazy-seq of all events in conn up to now."
  [conn]
  (let [f (partial events-page (fdb/db conn ledger) 100)]
    (map deserialize (lazy-cat-while f))))


;; TODO: what happens on a very large event? e.g. 2mb, 10mb, 100mb
;; TODO: is this related to that error on the server?
;; 08:15:55.648 ERROR [AsyncHttpClient-3-1] fluree.db.util.log - "websocket error"
;; athens_1  | io.netty.handler.codec.http.websocketx.CorruptedWebSocketFrameException: Max frame length of 10240 has been exceeded.
(defn add-event!
  [conn id data]
  @(fdb/transact conn ledger [(serialize id data)]))


(defn ensure-ledger!
  [conn]
  (when (empty? @(fdb/ledger-info conn ledger))
    (log/info "Fluree ledger for event-log not found, creating" ledger)
    @(fdb/new-ledger conn ledger)
    (fdb/wait-for-ledger-ready conn ledger)
    @(fdb/transact conn ledger schema)
    (log/info "Populating fresh ledger with initial events...")
    (doseq [[id data] initial-events]
      (add-event! conn id data))
    (log/info "✅ Populated fresh ledger.")
    (log/info "✅ Fluree ledger for event-log created.")))


#_(defn events-since
  "TODO: All events since start-id."
  [_db _start-id]
  ;; convert start-id to subjects
  ;; use same query as all-events but add more where clauses
  )


#_(defn subscribe
  "TODO: Calls f with k, [id data], for each event starting at start-id.
  k is is a subscription key that you can use with unsubscribe."
  [_conn _k _start-id _f]
  ;; Fluree only has fdb/listen that starts at the calling time, but we can use
  ;; a notifier pattern to turn that into an arbitrary subscription since a past block.
  ;; Use events-since for the initial list, store the last id+subject, call f with each of those.
  ;; Make a listener fn that takes the listener data and determines if it's an event, extracts
  ;; the id if so, calls events-since with it, and call f with with block there.
  ;; When calling f, ignore subjects that have already been used, avoiding duplicated.
  )


#_(defn unsubscribe
  "TODO: remove subscription with key k."
  [_conn _k])


(comment
  (def conn (fdb/connect "http://localhost:8090"))

  ;; Create ledger if not present.
  (ensure-ledger! conn)

  ;; What are the current events in the ledger?
  (events conn)

  ;; Add a few events.
  (def my-events [["uuid-1" [1 2 3]]
                  ["uuid-2" [4 5 6]]
                  ["uuid-3" [7 8 9]]])

  (doseq [[id data] my-events]
    (add-event! conn id data))

  ;; Check the events again.
  (events conn)

  @(fdb/delete-ledger conn ledger))
