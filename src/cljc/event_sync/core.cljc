(ns event-sync.core
  (:refer-clojure :exclude [print remove type add-watch remove-watch])
  (:require
    [clojure.pprint :as pprint]
    [flatland.ordered.map :refer [ordered-map]]))


(defn op
  "Create an operation in the format of [type stage-id event-id event noop?].
   type can be one of :add, :promote, :remove. noop? is true if the operation did nothing."
  [type stage-id event-id event noop?]
  (vector type stage-id event-id event noop?))


(defn update-op
  "Set last operation in state to op and increase operation count."
  [state op]
  (-> state
      (assoc :last-op op)
      (update :op-count inc)))


(defn event-stage
  "Return the stage an event is in, if any."
  [state event-id]
  (some (fn [[stage-id events]]
          (when (get events event-id)
            stage-id))
        (:stages state)))


(defn first-match
  "Returns the first match in matches to appear in coll."
  [coll matches]
  (let [s (set matches)]
    (some s coll)))


(defn stage<
  "Returns true if s1 appears before s2 in stage-ids.
   Does not verify both s1 and s2 are in stage-ids."
  [stage-ids s1 s2]
  (and
    (not (= s1 s2))
    (= s1 (first-match stage-ids [s1 s2]))))


(defn promotion?
  "Returns true if adding event-id to-stage is a promotion given event-id is in from-stage."
  [state from-stage to-stage event-id]
  (and
    ;; is to-stage immediately after from-stage?
    (= to-stage (second (drop-while #(not= % from-stage) (-> state :stages keys))))
    ;; does event-id match the first event in from-stage?
    ;; NB: in ordered map first = oldest, last = newest
    (= event-id (ffirst (get-in state [:stages from-stage])))))


;; API

(defn create-state
  "Create a state with id and the stages in stage-ids.
   NB: use log and print to show events in newest-to-oldest order."
  [id stage-ids]
  {:id       id
   :stages   (into (ordered-map) (map #(vector % (ordered-map)) stage-ids))
   :last-op  :initialization
   :op-count 0})


(defn add
  "Add event to stage-id and remove it from previous stages.
   If this addition would be a promotion, the resulting operation will be :promote instead of :add.
   If the event is already in a further stage last-op will be marked as a noop (last element is true)."
  [stage-id event-id event state]
  (let [current (event-stage state event-id)
        noop?   (boolean (and current
                              (or (= stage-id current)
                                  (stage< (-> state :stages keys) stage-id current))))
        type    (if (and current
                         (not noop?)
                         (promotion? state current stage-id event-id))
                  :promote
                  :add)]
    (cond-> state
      ;; remove from current stage
      (and current (not noop?)) (update-in [:stages current] dissoc event-id)
      ;; add to the new stage
      (not noop?)               (update-in [:stages stage-id] assoc event-id event)
      ;; update last operation
      true                      (update-op (op type stage-id event-id event noop?)))))


(defn remove
  "Remove event from stage-id.
   If the event is not there last-op will be marked as a noop (last element is true)."
  [stage-id event-id event state]
  (let [current (event-stage state event-id)]
    (cond-> state
      ;; remove from current stage
      current (update-in [:stages current] dissoc event-id)
      ;; update last operation
      true    (update-op (op :remove stage-id event-id event (not current))))))


(defn stage-log
  "Returns a vector of all events in a stage, as [event-id event] pairs, from newest to oldest."
  [state stage]
  (when-let [events (-> state :stages (get stage))]
    ;; Reverse the order of each event list before concatenating to show newest to oldest.
    (-> events reverse vec)))


(defn log
  "Returns a vector of all events in state, as [event-id event] pairs, from newest to oldest."
  [state]
  (vec (mapcat (comp (partial stage-log state) first) (:stages state))))


(defn print
  "Pretty prints a state, with added log.
   Events are shown newest to oldest for readability."
  [state]
  (pprint/pprint
    (reduce (fn [state k]
              (update-in state [:stages k] (comp vec rseq)))
            (assoc state :log (log state))
            (-> state :stages keys))))


;; Mutable API

(defn create-state-atom
  "Create a mutable atom from create-state."
  [id stage-ids]
  (atom (create-state id stage-ids)))


(defn add!
  "Mutate state-atom via add."
  [state-atom stage-id event-id event]
  (swap! state-atom (partial add stage-id event-id event)))


(defn remove!
  "Mutate state-atom via remove."
  [state-atom stage-id event-id event]
  (swap! state-atom (partial remove stage-id event-id event)))


(defn add-watch
  "Add a watch fn to state-atom under key.
   on-init, on-add, on-promote, on-remove are fns that receive op and state.
   on-init the first time the watcher is triggered instead of the callback matching the operation."
  [state-atom key on-init on-add on-promote on-remove]
  (let [init? (atom true)
        f     (fn state-atom-watcher
                [_ _ _ {:keys [last-op] :as new-state}]
                (if @init?
                  (do (on-init last-op new-state)
                      (reset! init? false))
                  (condp = (first last-op)
                    :add     (on-add last-op new-state)
                    :promote (on-promote last-op new-state)
                    :remove  (on-remove last-op new-state))))]
    (clojure.core/add-watch state-atom key f)))


(defn remove-watch
  "Remove watcher under key added via add-watch."
  [state-atom key]
  (clojure.core/remove-watch state-atom key))


(comment
  (-> (create-state :mario [:one :two :three])
      (add :one "event-id-1" "event-1")
      (add :one "event-id-2" "event-2")
      (add :two "event-id-1" "event-1")
      print
      )

  ;; from readme, up to Alice's last state in "Two Offline Alices"
  (-> (create-state :alice [:in-memory :local-storage :server])
      ;; something simple
      (add :in-memory "a1" "a1")
      (add :local-storage "a1" "a1")
      (add :server "a1" "a1")
      ;; concurrency
      (add :in-memory "a2" "a2")
      (add :server "b1" "b1")
      (add :local-storage "a2" "a2")
      (add :server "a2" "a2")
      ;; alice is offline
      (add :in-memory "a3" "a3")
      (add :in-memory "a4" "a4")
      (add :in-memory "a5" "a5")
      (add :local-storage "a3" "a3")
      (add :local-storage "a4" "a4")
      (add :local-storage "a5" "a5")
      (add :in-memory "a6" "a6")
      (add :in-memory "a7" "a7")
      (add :local-storage "a6" "a6")
      (add :local-storage "a7" "a7")
      ;; two offline alices
      (add :local-storage "e1" "e1")
      print)

  ;; prints
  ;; {:id :alice,
  ;;  :stages
  ;;  {:in-memory [],
  ;;   :local-storage
  ;;   [["e1" "e1"]
  ;;    ["a7" "a7"]
  ;;    ["a6" "a6"]
  ;;    ["a5" "a5"]
  ;;    ["a4" "a4"]
  ;;    ["a3" "a3"]],
  ;;   :server [["a2" "a2"] ["b1" "b1"] ["a1" "a1"]]},
  ;;  :last-op [:add :local-storage "e1" "e1" false],
  ;;  :op-count 18,
  ;;  :log
  ;;  [["e1" "e1"]
  ;;   ["a7" "a7"]
  ;;   ["a6" "a6"]
  ;;   ["a5" "a5"]
  ;;   ["a4" "a4"]
  ;;   ["a3" "a3"]
  ;;   ["a2" "a2"]
  ;;   ["b1" "b1"]
  ;;   ["a1" "a1"]]}

  (stage< [:one :two :three] :two 1)
  (vals (ordered-map :a 1 :b 2))
  ;
  )

