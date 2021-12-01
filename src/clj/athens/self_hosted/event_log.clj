(ns athens.self-hosted.event-log
  (:require
    [athens.async :as athens.async]
    [athens.athens-datoms :as datoms]
    [clojure.core.async :refer [<!!]]
    [clojure.edn :as edn]
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [fluree.db.api :as fdb]
    [clojure.string :as string])
  (:import [java.util UUID]))

(def ledger "events/log")

(defn create-fluree-comp
  [address]
  (let [conn-atom    (atom nil)
        reconnect-fn (fn []
                       (when-some [conn @conn-atom]
                         (fdb/close conn))
                       (reset! conn-atom (fdb/connect address)))
        comp         {:conn-atom    conn-atom
                      :reconnect-fn reconnect-fn}]
    (reconnect-fn)
    comp))

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
  datoms/welcome-events)


(defn serialize
  [id data]
  {:_id :event
   :event/id (str id)
   :event/data (pr-str data)})


(defn deserialize
  [{id   "event/id"
    data "event/data"}]
  [(if (string/blank? id)
     (UUID/randomUUID)
     (UUID/fromString id)) (edn/read-string data)])


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
  "Returns a lazy-seq of all events in conn up to now.
  Can potentially be very large, so don't hold on to the seq head while
  processing, and don't use fns that realize the whole coll (e.g. count)."
  [fluree]
  (let [f (partial events-page (fdb/db (-> fluree :conn-atom deref) ledger) 100)]
    (map deserialize (lazy-cat-while f))))


(defn double-write?
  "Returns true if response is for a double-write.
  Double-writes for adding events are not treated as errors, since event writes are idempotent.
  Double-writes can occur on reconnect or multi-writer scenarios."
  [response]
  (let [{:keys [status error]} (ex-data response)
        message                (ex-message response)]
    (and message
         (= status 400)
         (= error :db/invalid-tx)
         ;; e.g. "Unique predicate event/id with value: uuid-3 matched an existing subject: 351843720888324."
         (str/starts-with? message "Unique predicate event/id with value:")
         (str/includes? message "matched an existing subject:"))))


(defn add-event!
  "Returns the block the event guaranteed to be present in."
  [fluree id data]
  (loop [retries-left 3]
    (if (= retries-left 0)
      (throw (ex-info "add-event! timed-out 3 times" {:id id}))
      (let [conn                (-> fluree :conn-atom deref)
            ch                  (fdb/transact-async conn ledger [(serialize id data)])
            {:keys [status block]
             :as   r}           (<!! (athens.async/with-timeout ch 5000 :timed-out))]
        (cond
          ;; NB: payloads that are too large will time out without an error message.
          ;; The ledger will show `[server-loop] WARN - Max payload length 4m, get: 100000150`.
          (= :timed-out r)
          (do
            (log/warn "fluree connection timed-out, reconnecting")
            ((:reconnect-fn fluree))
            (recur (dec retries-left)))

          (and (= status 200) block)
          block

          (double-write? r)
          (-> r ex-data :meta :block)

          :else
          (throw (ex-info "add-event! failed to transact" {:id id :response r})))))))

(defn ensure-ledger!
  ([fluree]
   (ensure-ledger! fluree initial-events))
  ([fluree seed-events]
   (let [conn (-> fluree :conn-atom deref)]
     (when (empty? @(fdb/ledger-info conn ledger))
       (let [block (atom nil)]
         (log/info "Fluree ledger for event-log not found, creating" ledger)
         @(fdb/new-ledger conn ledger)
         (fdb/wait-for-ledger-ready conn ledger)
         (reset! block (:block @(fdb/transact conn ledger schema)))
         (log/info "Populating fresh ledger with initial events...")
         (doseq [[id data] seed-events]
           (reset! block (add-event! fluree id data)))
         (log/info "✅ Populated fresh ledger.")
         (log/info "Bringing local ledger to to date with latest transactions...")
         (events-page (fdb/db conn ledger {:syncTo @block}) 1 0)
         (log/info "✅ Fluree local ledger up to date.")
         (log/info "✅ Fluree ledger for event-log created."))))))



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

  (def comp
    (let [conn-atom    (atom nil)
          reconnect-fn (fn []
                         (when-some [conn @conn-atom]
                           (fdb/close conn))
                         (reset! conn-atom (fdb/connect "http://localhost:8090")))
          comp         {:conn-atom    conn-atom
                        :reconnect-fn reconnect-fn}]
      comp))
  ((:reconnect-fn comp))

  ;; Create ledger if not present.
  (ensure-ledger! comp )

  ;; What are the current events in the ledger?
  (events comp)

  ;; Add a few events.
  (def my-events [["uuid-2" [1 2 3]]
                  ["uuid-2" [4 5 6]]
                  ["uuid-3" [7 8 9]]])

  (doseq [[id data] my-events]
    (add-event! comp id data))

  ;; Add the same event multiple times, or with large sizes.
  (add-event! comp "uuid-4" (apply str (repeat 1000 "a")))

  ;; Check the events again.
  (events comp)

  @(fdb/delete-ledger (-> comp :conn-atom deref) ledger))
