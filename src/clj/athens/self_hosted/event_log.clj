(ns athens.self-hosted.event-log
  (:require
    [athens.async :as athens.async]
    [athens.athens-datoms :as datoms]
    [athens.common.utils :as utils]
    [clojure.core.async :as async]
    [clojure.data :as data]
    [clojure.data.json :as json]
    [clojure.edn :as edn]
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [fluree.db.api :as fdb])
  (:import
    (java.util
      UUID)))


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
  datoms/welcome-events)


(defn serialize
  [id data]
  {:_id :event
   :event/id (str id)
   :event/data (pr-str data)})


(defn deserialize
  [{id   "event/id"
    data "event/data"}]
  ;; In some running ledgers we have "add Welcome page shortcut" that does not have a UUID
  ;; So we want the backup to be compatible for these previous version of ledgers.
  [(if (str/blank? id)
     (UUID/randomUUID)
     (UUID/fromString id)) (edn/read-string data)])


(defn- events-page
  "Returns a seq of events in page-number for all events in db split by page-size.
  If starting-subject-id is non-nil, only events after that one are returned."
  ([db starting-subject-id page-size page-number]
   @(fdb/query db
               {:select {"?event" ["*"]}
                :where  [["?event" "event/id", "?id"]
                         (when starting-subject-id
                           {:filter [(str "(> ?event " starting-subject-id ")")]})]
                ;; Subject ID (?event here) is a monotonically incrementing bigint,
                ;; so ordering by that gives us event insertion order since events are immutable.
                :opts   {:orderBy ["ASC", "?event"]
                         :limit   page-size
                         :offset  (* page-size page-number)}})))


(defn- event-id->subject-id
  [db event-id]
  (first @(fdb/query db {:select "?event"
                         :where  [["?event" "event/id", (str event-id)]]})))


(defn last-event-id
  [fluree]
  (-> fluree
      :conn-atom
      deref
      (fdb/db ledger)
      (fdb/query {:selectOne {"?event" ["*"]}
                  :where     [["?event" "event/id", "?id"]]
                  :opts      {:orderBy ["DESC" "?event"]
                              :limit   1}})
      deref
      deserialize
      first))


(defn events
  "Returns a lazy-seq of all events in conn up to now, starting at optional event-id.
  Can potentially be very large, so don't hold on to the seq head while
  processing, and don't use fns that realize the whole coll (e.g. count)."
  ([fluree]
   (events fluree nil))
  ([fluree event-id]
   (let [db (fdb/db (-> fluree :conn-atom deref) ledger)
         starting-subject-id (when event-id
                               (if-some [subject-id (event-id->subject-id db event-id)]
                                 subject-id
                                 (throw (ex-info "Cannot find starting id" {:event-id event-id}))))
         f (partial events-page db starting-subject-id 100)]
     ;; TODO: use `iteration` once clojure 1.11.0 is out, much simpler and standard
     ;; https://github.com/clojure/clojure/blob/master/changes.md#34-iteration
     (map deserialize (utils/range-mapcat-while f empty?)))))


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
  ([fluree id data]
   (add-event! fluree id data 5000 1000))
  ([fluree id data timeout backoff]
   (loop [retries-left 3]
     (if (= retries-left 0)
       (throw (ex-info (str "add-event! timed-out 3 times on " id)
                       {:id id}))
       (let [conn                (-> fluree :conn-atom deref)
             ch                  (fdb/transact-async conn ledger [(serialize id data)])
             {:keys [status block]
              :as   r}           (async/<!! (athens.async/with-timeout ch timeout :timed-out))]
         (cond
           ;; NB: payloads that are too large will time out without an error message.
           ;; The ledger will show `[server-loop] WARN - Max payload length 4m, get: 100000150`.
           (= :timed-out r)
           (let [s (str "fluree connection timeout on " id " " retries-left " retries left")
                 current-retry (- 4 retries-left)
                 exponential-backoff (* current-retry current-retry backoff)]
             (log/warn s "backing off" backoff "ms")
             (async/<!! (async/timeout exponential-backoff))
             (log/warn s "reconnecting")
             ((:reconnect-fn fluree))
             (recur (dec retries-left)))

           (and (= status 200) block)
           block

           (double-write? r)
           (-> r ex-data :meta :block)

           :else
           (throw (ex-info (str "add-event! failed to transact on " id)
                           {:id id :response r}))))))))


(defn ensure-ledger!
  ([fluree]
   (ensure-ledger! fluree initial-events))
  ([fluree seed-events]
   (let [conn (-> fluree :conn-atom deref)]
     (log/info "Looking for event-log fluree ledger")
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
         (events-page (fdb/db conn ledger {:syncTo @block}) nil 1 0)
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


;; Recovery fns

(defn- get-asserted-txs
  [asserted]
  (->> asserted
       (map #(get % "_tx/tx"))
       (remove nil?)
       (map json/read-str)
       (mapcat #(get % "tx"))
       (filter #(get % "event/data"))))


(defn- get-block-txs
  [blocks]
  (->> blocks
       (map :asserted)
       (mapcat get-asserted-txs)
       (remove nil?)))


(defn- recover-block-events
  "Returns a seq of recovered events in conn for block=idx+1 in conn."
  [conn idx]
  (let [res @(fdb/block-query conn ledger {:block (inc idx) :pretty-print true})
        ex-msg (ex-message res)]
    ;; If the query because the is higher than the total blocks,
    ;; result will be an error map instead of seq.
    ;; Used with range-mapcat-while so we return ::stop to stop the iteration instead.
    (if (and ex-msg (str/starts-with? ex-msg "Start block is out of range for this ledger."))
      ::stop
      (get-block-txs res))))


(defn recovered-events
  "Returns a lazy-seq of all recovered events in conn up to now.
  Recovered events include events from failed transactions, as well as ones that succeed.
  Can potentially be very large, so don't hold on to the seq head while
  processing, and don't use fns that realize the whole coll (e.g. count)."
  [fluree]
  (let [f (partial recover-block-events (-> fluree :conn-atom deref))]
    (map deserialize (utils/range-mapcat-while f #(= % ::stop)))))


(comment

  (def fluree-comp
    (let [conn-atom    (atom nil)
          reconnect-fn (fn []
                         (when-some [conn @conn-atom]
                           (fdb/close conn))
                         (reset! conn-atom (fdb/connect "http://localhost:8090")))
          fluree-comp         {:conn-atom    conn-atom
                               :reconnect-fn reconnect-fn}]
      fluree-comp))
  ((:reconnect-fn fluree-comp))

  ;; Create ledger if not present.
  (ensure-ledger! fluree-comp)

  ;; What are the current events in the ledger?
  (events fluree-comp)

  ;; What are the events since this event-id?
  (events fluree-comp "71b7ac32-11d2-4b14-bc72-a791a50a6e03")

  ;; Add a few events.
  (def my-events [["uuid-2" [1 2 3]]
                  ["uuid-2" [4 5 6]]
                  ["uuid-3" [7 8 9]]])

  (doseq [[id data] my-events]
    (add-event! fluree-comp id data))

  ;; Add the same event multiple times, or with large sizes.
  (add-event! fluree-comp "uuid-4" (apply str (repeat 1000 "a")))

  ;; Check the events again.
  (events fluree-comp)

  ;; How many events do we have total?
  (count (events fluree-comp))
  ;; How many events do we have if we count failed ones?
  (count (recovered-events fluree-comp))

  ;; Debug event recovery
  (events-page (fdb/db (-> fluree-comp :conn-atom deref) ledger) nil 1 1)
  (recover-block-events (-> fluree-comp :conn-atom deref) 3)
  (take 3 (recovered-events fluree-comp))
  (take 3 (events fluree-comp))

  ;; This should be the same (e.g. [nil nil _]) for new graphs.
  (data/diff (take 3 (recovered-events fluree-comp))
             (take 3 (events fluree-comp)))

  ;; Delete ledger.
  @(fdb/delete-ledger (-> fluree-comp :conn-atom deref) ledger))
