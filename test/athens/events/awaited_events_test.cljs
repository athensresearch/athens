(ns athens.events.awaited-events-test
  "Testing `awaited-event` events and subscriptions."
  (:require
   [athens.effects     :as effects]
   [athens.events      :as events]
   [athens.subs        :as subs]
   [cljs.test          :refer-macros [deftest is]]
   [day8.re-frame.test :as rf-test]
   [re-frame.core      :as rf]))


(defn test-fixtures
  []
  (rf/reg-cofx
   :local-storage
   (fn [cofx key]
     (js/console.debug "local-storage cofx" key)
     (assoc cofx :local-storage ({"theme/dark" true}
                                 key)))))

(deftest initial-state-not-awaiting-any-events
  (rf-test/run-test-sync
   (test-fixtures)
   (rf/dispatch [:boot/web])
   (let [awaited-events (rf/subscribe [:remote/awaited-events])]
     (is (not (nil? @awaited-events)))
     (is (empty? @awaited-events)))))


(def test-event
  {:event/id      "test-event-id-1"
   :event/last-tx 0
   :event/type    :some-type})


(deftest awaiting-one-event
  (rf-test/run-test-async
   (test-fixtures)
   (rf/dispatch-sync [:boot/web])
   (let [awaited-events (rf/subscribe [:remote/awaited-events])]

     ;; empty to start with
     (is (not (nil? @awaited-events)))
     (is (empty? @awaited-events))

     ;; start awaiting an event
     (rf/dispatch [:remote/await-event test-event])
     (rf-test/wait-for
      [:remote/await-event]
      
      (is (seq @awaited-events))
      (is (= 1 (count @awaited-events)))
      (is (= test-event (first @awaited-events)))))))


(def rejection-event
  {:event-id  "test-event-id-1"
   :rejection {:reason "some reason"
               :data   {:more :details}}
   :event test-event})


(deftest reject-awaited-event
  (rf-test/run-test-async
   (test-fixtures)
   (rf/dispatch-sync [:boot/web])
   (let [awaited-events  (rf/subscribe [:remote/awaited-events])
         rejected-events (rf/subscribe [:remote/rejected-events])]
     ;; empty to start with
     (is (not (nil? @awaited-events)))
     (is (empty? @awaited-events))
     (is (not (nil? @rejected-events)))
     (is (empty? @rejected-events))

     ;; start awaiting an event
     (rf/dispatch [:remote/await-event test-event])
     (rf-test/wait-for
      [:remote/await-event]

      (is (seq @awaited-events))
      (is (= 1 (count @awaited-events)))
      (is (= test-event (first @awaited-events)))
      ;; no rejections yet
      (is (empty? @rejected-events))

      (rf/dispatch [:remote/reject-event {:event-id (:event/id test-event)
                                          :reason   "some reason"
                                          :data     {:more :details}}])
      (rf-test/wait-for
       [:remote/reject-event]

       (is (empty? @awaited-events))
       (is (seq @rejected-events))
       (is (= rejection-event (first @rejected-events))))))))


(deftest accept-awaited-event
  (rf-test/run-test-async
   (test-fixtures)
   (rf/dispatch-sync [:boot/web])
   (let [awaited-events  (rf/subscribe [:remote/awaited-events])
         accepted-events (rf/subscribe [:remote/accepted-events])
         tx-id           10
         accept-event    {:event-id (:event/id test-event)
                          :tx-id    tx-id}]
     ;; empty to start with
     (is (not (nil? @awaited-events)))
     (is (empty? @awaited-events))
     (is (not (nil? @accepted-events)))
     (is (empty? @accepted-events))

     ;; start awaiting an event
     (rf/dispatch [:remote/await-event test-event])
     (rf-test/wait-for
      [:remote/await-event]

      (is (seq @awaited-events))
      (is (= 1 (count @awaited-events)))
      (is (= test-event (first @awaited-events)))
      ;; not accepted yet
      (is (empty? @accepted-events))

      (rf/dispatch [:remote/accept-event accept-event])
      (rf-test/wait-for
       [:remote/accept-event]

       (is (empty? @awaited-events))
       (is (seq @accepted-events))
       (is (= (assoc accept-event
                     :event test-event)
              (first @accepted-events))))))))


(deftest fail-awaited-event
  (rf-test/run-test-async
   (test-fixtures)
   (rf/dispatch-sync [:boot/web])
   (let [awaited-events (rf/subscribe [:remote/awaited-events])
         failed-events  (rf/subscribe [:remote/failed-events])
         fail-event     {:event-id (:event/id test-event)
                         :reason   "some explanation string from malli"}]
     ;; empty to start with
     (is (not (nil? @awaited-events)))
     (is (empty? @awaited-events))
     (is (not (nil? @failed-events)))
     (is (empty? @failed-events))

     (rf/dispatch [:remote/await-event test-event])
     (rf-test/wait-for
      [:remote/await-event]

      (is (seq @awaited-events))
      (is (= 1 (count @awaited-events)))
      (is (= test-event (first @awaited-events)))
      
      (rf/dispatch [:remote/fail-event fail-event])
      (rf-test/wait-for
       [:remote/fail-event]

       (is (empty? @awaited-events))
       (is (seq @failed-events))
       (is (= (assoc fail-event
                     :event test-event)
              (first @failed-events))))))))
