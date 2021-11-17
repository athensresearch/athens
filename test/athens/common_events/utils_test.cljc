(ns athens.common-events.utils-test
  (:require
   [athens.common-events                 :as sut]
   [athens.common-events.graph.atomic    :as atomic-graph-ops]
   [athens.common-events.graph.composite :as composite-graph-ops]
   [clojure.test                         :as t]))


(t/deftest find-event-or-atomic-op-type-test
  (t/testing "event type extraction"
    (let [event (sut/build-presence-offline-event "session-id")]
      (t/is (= :presence/offline (sut/find-event-or-atomic-op-type event)))))

  (t/testing "atomic operation extraction from op"
    (let [op (atomic-graph-ops/make-block-save-op "block-uid" "block text")]
      (t/is (= :block/save (sut/find-event-or-atomic-op-type op)))))
  
  (t/testing "atomic operation extraction from event"
    (let [event (sut/build-atomic-event
                 (atomic-graph-ops/make-block-save-op "block-uid" "block text"))]
      (t/is (= :block/save (sut/find-event-or-atomic-op-type event)))))

  (t/testing "composite operation extraction from op"
    (let [op (composite-graph-ops/make-consequence-op {:op/type :madeup/name} [])]
      (t/is (= :madeup/name (sut/find-event-or-atomic-op-type op)))))

  (t/testing "composite operation extraction from event"
    (let [event (sut/build-atomic-event
                 (composite-graph-ops/make-consequence-op {:op/type :madeup/name} []))]
      (t/is (= :madeup/name (sut/find-event-or-atomic-op-type event))))))
