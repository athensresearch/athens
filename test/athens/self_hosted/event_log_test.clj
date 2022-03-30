(ns athens.self-hosted.event-log-test
  (:require
    [athens.self-hosted.event-log :as event-log]
    [athens.self-hosted.fluree.test-helpers :as fth]
    [clojure.test :as t :refer [deftest testing is]]))


(t/use-fixtures :each fth/with-ledger)


(defn make-comp
  []
  {:conn-atom    fth/conn-atom
   :ledger       @fth/ledger-atom
   :reconnect-fn #()})


(deftest ^:fluree logs-events
  (let [comp   (make-comp)
        events (map (fn [id] [id {:id id}])
                    (repeatedly 4 random-uuid))]
    (event-log/init! comp [])
    (doseq [[id data] events]
      (->> (event-log/add-event! comp id data)
           fth/wait-for-block))
    (is (= events (event-log/events comp)))))


(deftest ^:fluree lists-events-since
  (let [comp    (make-comp)
        events  (map (fn [id] [id {:id id}])
                     (repeatedly 4 random-uuid))
        since   (-> events second first)
        events' (drop 2 events)]
    (event-log/init! comp [])
    (doseq [[id data] events]
      (->> (event-log/add-event! comp id data)
           fth/wait-for-block))
    (is (= events' (event-log/events comp :since-event-id since)))))
