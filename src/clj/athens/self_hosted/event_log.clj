(ns athens.self-hosted.event-log
  (:require
    [athens.async :as athens.async]
    [athens.athens-datoms :as datoms]
    [athens.self-hosted.event-log-migrations :as event-log-migrations]
    [athens.self-hosted.fluree.utils :as fu]
    [athens.self-hosted.migrate :as migrate]
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


(def default-ledger "events/log")


(defn fluree-comp->ledger
  [fluree]
  (-> fluree
      :ledger
      (or default-ledger)))


(def initial-events
  datoms/welcome-events)


(defn serialize
  [id data]
  (assert (uuid? id))
  (let [str-id      (str id)
        self-tempid (str "event$self-" str-id)]
    {:_id         self-tempid
     :event/id    (str id)
     :event/data  (pr-str data)
     ;; See athens.self-hosted.event-log-migrations/migration-3-schema
     ;; for how ordering works.
     :event/order self-tempid}))


(defn deserialize
  [{id   "event/id"
    data "event/data"}]
  [(UUID/fromString id)
   (edn/read-string data)])


(defn- events-page
  "Returns {:next-page ... :items ...}, where items is a vector of events in
   page-number for all events in db split by page-size. For use with `iteration`."
  ([db since-order page-size page-number]
   {:next-page (inc page-number)
    :items     (fu/query db
                         {:select {"?event" ["*"]}
                          :where  [["?event" "event/id", "?id"]
                                   (if since-order
                                     ["?event" "event/order" (str "#(> ?order " since-order ")")]
                                     ["?event" "event/order" "?order"])]
                          :opts   {:orderBy ["ASC", "?order"]
                                   :limit   page-size
                                   :offset  (* page-size page-number)}})}))


(defn event-id->order
  [db event-id]
  (first (fu/query db {:select "?order"
                       :where  [["?event" "event/id", (str event-id)]
                                ["?event" "event/order" "?order"]]})))


(defn last-event-id
  [fluree]
  (-> fluree
      :conn-atom
      deref
      (fdb/db (fluree-comp->ledger fluree))
      (fu/query {:selectOne {"?event" ["*"]}
                 :where     [["?event" "event/id" "?id"]
                             ["?event" "event/order" "?order"]]
                 :opts      {:orderBy ["DESC" "?order"]
                             :limit   1}})
      deserialize
      first))


(defn events
  "Returns a lazy-seq of all events in conn up to now, starting at optional event-id.
  Can potentially be very large, so don't hold on to the seq head while
  processing, and don't use fns that realize the whole coll (e.g. count)."
  [fluree & {:keys [since-event-id since-order]}]
  (let [db           (fdb/db (-> fluree :conn-atom deref)
                             (fluree-comp->ledger fluree))
        since-order' (cond
                       since-order    since-order
                       since-event-id (or (event-id->order db since-event-id)
                                          (throw (ex-info "Cannot find starting id"
                                                          {:since-event-id since-event-id})))
                       :else          nil)
        step         (partial events-page db since-order' 100)]
    ;; New core fn added in Clojure 11.
    ;; See https://www.juxt.pro/blog/new-clojure-iteration for usage example.
    (->> (iteration step
                    :kf :next-page
                    :vf :items
                    :somef #(-> % :items seq)
                    :initk 0)
         (sequence cat)
         (map deserialize))))


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
             ledger              (fluree-comp->ledger fluree)
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


(defn init!
  ([fluree]
   (init! fluree initial-events))
  ([fluree seed-events]
   (let [conn (-> fluree :conn-atom deref)
         ledger (fluree-comp->ledger fluree)]
     (log/info "Looking for event-log fluree ledger")
     (when (empty? @(fdb/ledger-info conn ledger))
       (log/info "Fluree ledger for event-log not found, creating" ledger)
       @(fdb/new-ledger conn ledger)
       (fdb/wait-for-ledger-ready conn ledger)
       (migrate/migrate-ledger! conn ledger event-log-migrations/migrations)
       (when seed-events
         (let [block (atom nil)]
           (log/info "Populating fresh ledger with initial events...")
           (doseq [[id data] seed-events]
             (reset! block (add-event! fluree id data)))
           (log/info "✅ Populated fresh ledger.")
           (log/info "Bringing local ledger to to date with latest transactions...")
           (fu/wait-for-block conn ledger @block)
           (log/info "✅ Fluree local ledger up to date.")))
       (log/info "✅ Fluree ledger for event-log created."))
     (migrate/migrate-ledger! conn ledger event-log-migrations/migrations))))


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
  "Returns {:stop ... :next-page ... :items ...}, where items is a seq of recovered
  events in conn for block=idx+1 in conn. For use with `iteration`."
  [conn idx]
  (let [res @(fdb/block-query conn default-ledger {:block (inc idx) :pretty-print true})
        ex-msg (ex-message res)]
    ;; If the query because the is higher than the total blocks,
    ;; result will be an error map instead of seq.
    (if (and ex-msg (str/starts-with? ex-msg "Start block is out of range for this ledger."))
      {:stop true}
      {:next-page (inc idx)
       :items     (get-block-txs res)})))


(defn recovered-events
  "Returns a lazy-seq of all recovered events in conn up to now.
  Recovered events include events from failed transactions, as well as ones that succeed.
  Can potentially be very large, so don't hold on to the seq head while
  processing, and don't use fns that realize the whole coll (e.g. count)."
  [fluree]
  (let [step (partial recover-block-events (-> fluree :conn-atom deref))]
    (->> (iteration step
                    :kf :next-page
                    :vf :items
                    :somef #(-> % :stop not)
                    :initk 0)
         (sequence cat)
         (map deserialize))))


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
  (init! fluree-comp)

  ;; What's the first event in the ledger?
  (take 1 (events fluree-comp))

  ;; What's the first event since this event-id?
  (take 1 (events fluree-comp (UUID/fromString "e6dad544-ef29-43b5-911e-9c4bfdca3fda")))

  ;; Add a few events.
  (def my-events [["uuid-1" [1 2 3]]
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
  (recover-block-events (-> fluree-comp :conn-atom deref) 3)
  (take 3 (recovered-events fluree-comp))
  (take 3 (events fluree-comp))

  ;; This should be the same (e.g. [nil nil _]) for new graphs.
  (data/diff (take 3 (recovered-events fluree-comp))
             (take 3 (events fluree-comp)))

  ;; Delete ledger.
  @(fdb/delete-ledger (-> fluree-comp :conn-atom deref) default-ledger))
