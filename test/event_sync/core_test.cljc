(ns event-sync.core-test
  (:refer-clojure :exclude [print remove type add-watch remove-watch])
  (:require
    [clojure.test :refer [deftest testing is are run-tests]]
    [event-sync.core :as es]))


(deftest create-state
  (let [{:keys [id stages last-op op-count] :as state}
        (es/create-state :test [:one :two 3 "four"])]
    (are [x y] (= x y)
      id             :test
      (count stages) 4
      last-op        :initialization
      op-count       0)
    (are [x] (->> x (get stages) (es/stage-log state) empty?)
      :one
      :two
      3
      "four")
    (is (-> state es/log empty?))))


(deftest add
  (let [state (es/create-state :test [:one :two 3 "four"])]
    (testing "adds"
      (are
        ;; Is the added event the newest in the log, last-op :add and not noop?
        [stage-id event-id event]
        (let [state' (es/add state stage-id event-id event)]
          (is (= (-> state' (es/stage-log stage-id) first) [event-id event]))
          (is (= :add (-> state' :last-op first)))
          (is (= false (-> state' :last-op (nth 4)))))

        :one "e1" "e1"
        :one 1 1
        :one :one :one
        3 3 3
        "four" :one 3))

    (testing "promotes"
      (let [state' (-> state
                       (es/add :one :first 1)
                       (es/add :one :second 1)
                       (es/add :two :third 1)
                       (es/add 3 33 1)
                       (es/add "four" "fourth" 1))]
        (are
          ;; Is the added event the newest in the log, last-op :promote and not noop?
          [stage-id event-id event]
          (let [state'' (es/add state' stage-id event-id event)]
            (is (= (-> state'' (es/stage-log stage-id) first) [event-id event]))
            (is (= :promote (-> state'' :last-op first)))
            (is (= false (-> state'' :last-op (nth 4)))))

          :two :first 1
          3 :third 1
          "four" 33 1)))

    (testing "noop"
      (let [state' (-> state
                       (es/add :one :first 1)
                       (es/add :one :second 1)
                       (es/add :one :third 1))]
        (are
          ;; Is the stage log unchanged, last-op :add and noop?
          [stage-id event-id event]
          (let [state'' (es/add state' stage-id event-id event)]
            (is (= (es/stage-log state'' stage-id)
                   (es/stage-log state' stage-id)))
            (is (= :add (-> state' :last-op first)))
            (is (-> state'' :last-op (nth 4))))

          :one :first 1
          :one :second 1
          :one :third 1)))))


(deftest remove
  (let [state (-> (es/create-state :test [:one :two 3 "four"])
                  (es/add :one :first 1)
                  (es/add :one :second 1)
                  (es/add :one :third 1))]

    (testing "remove"
      (are
        ;; Is the removed event gone, last-op :remove and not noop?
        [stage-id event-id event]
        (let [state' (es/remove state stage-id event-id event)]
          (is (= false (contains? (into #{} (map first) (es/stage-log stage-id state'))
                                  event-id)))
          (is (= :remove (-> state' :last-op first)))
          (is (= false (-> state' :last-op (nth 4)))))

        :one :first 1
        :one :second 1
        :one :third 1))

    (testing "noop"
      (are
        ;; Is the removed event still in the original stage, last-op :remove and not noop?
        [stage-id event-id event]
        (let [state' (es/remove state stage-id event-id event)]
          (is (= (es/stage-log state :one)
                 (es/stage-log state' :one)))
          (is (= :remove (-> state' :last-op first)))
          (is (= true (-> state' :last-op (nth 4)))))

        :two :first 1
        3 :third 1))))


(deftest stage-log
  (let [state (-> (es/create-state :test [:one :two 3 "four"])
                  (es/add :one :first 1)
                  (es/add :one :second 2)
                  (es/add :one :third 3))]

    (testing "First is newest event"
      (is (= :third (-> state (es/stage-log :one) first first))))

    (testing "Last is oldest event"
      (is (= :first (-> state (es/stage-log :one) last first))))

    (testing "Content"
      (is (= [[:third 3] [:second 2] [:first 1]]
             (es/stage-log state :one)))

      (is (empty? (es/stage-log state :two))))))


(deftest log
  (let [state (-> (es/create-state :test [:one :two 3 "four"])
                  (es/add 3 :first 1)
                  (es/add :two :second 2)
                  (es/add :two :third 3)
                  (es/add :one :fourth 4)
                  (es/add :one :fifth 5))]

    (testing "First is newest event"
      (is (= :fifth (-> state es/log first first))))

    (testing "Last is oldest event"
      (is (= :first (-> state es/log last first))))

    (testing "Content"
      (is (= [[:fifth 5] [:fourth 4] [:third 3] [:second 2] [:first 1]]
             (es/log state)))

      (is (empty? (es/log (es/create-state :test [:one :two 3 "four"])))))))


(deftest mutable-api

  (testing "create-state-atom"
    (is (= @(es/create-state-atom :test [:one :two 3 "four"])
           (es/create-state :test [:one :two 3 "four"]))))

  (testing "add!/remove!"
    (are
      ;; Is performing using the mutable API the same as using the immutable API?
      [op stage-id event-id event]
      (let [state-atom (es/create-state-atom :test [:one :two 3 "four"])
            _          (es/add! state-atom :one :first 1)
            state      @state-atom
            ops        (condp = op
                         :add    [es/add! es/add]
                         :remove [es/remove! es/remove])]
        ((first ops) state-atom stage-id event-id event)
        (= @state-atom ((second ops) state stage-id event-id event)))

      :add :one :second 2
      :add :two :first 1
      :remove :one :first 1))

  (testing "add/remove-watch"
    (let [state-atom (es/create-state-atom :test [:one :two 3 "four"])
          res        (atom nil)
          on-*       (fn [kw] (fn [last-op _] (reset! res [kw (first last-op)])))
          on-add     (on-* :on-add)
          on-promote (on-* :on-promote)
          on-remove  (on-* :on-remove)]

      (es/add! state-atom :one :first 1)
      (is (= nil @res))

      (es/add-watch state-atom :k on-add on-promote on-remove)
      (is (= nil @res))

      (es/add! state-atom :one :second 2)
      (is (= [:on-add :add] @res))

      (es/add! state-atom :two :first 1)
      (is (= [:on-promote :promote] @res))

      (es/remove! state-atom :two :first 1)
      (is (= [:on-remove :remove] @res))

      (es/remove-watch state-atom :k)
      (es/add! state-atom :one :third 3)
      (is (= [:on-remove :remove] @res)))))


(comment
  (run-tests))
