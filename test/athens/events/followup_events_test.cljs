#_{:clj-kondo/ignore [:unused-namespace]}


(ns athens.events.followup-events-test
  "Testing `followup-event` dispatching and cleaning."
  (:require
    [athens.effects]
    [athens.events]
    [athens.events.fixture :as fixture]
    [athens.subs]
    [cljs.test             :refer-macros [deftest is]]
    [day8.re-frame.test    :as rf-test]
    [re-frame.core         :as rf]))


(deftest initial-state-no-followup-events
  (rf-test/run-test-sync
    (fixture/test-fixtures)
    (rf/dispatch [:boot/web])
    (let [followup (rf/subscribe [:remote/followup])]
      (is (not (nil? @followup)))
      (is (empty? @followup)))))


(deftest that-we-can-register-followup
  (rf-test/run-test-sync
    (fixture/test-fixtures)
    (rf/dispatch [:boot/web])
    (let [event-id     "test-event-id"
          followup-fx  [:some-followup]
          followups    (rf/subscribe [:remote/followup])
          followup-for (rf/subscribe [:remote/followup-for event-id])]
      (is (nil? @followup-for))
      (is (empty? @followups))
      (rf/dispatch [:remote/register-followup event-id followup-fx])
      (is (seq @followups))
      (is (not (nil? @followup-for)))
      (is (= followup-fx @followup-for)))))


(deftest that-we-can-unregister-followup
  (rf-test/run-test-sync
    (fixture/test-fixtures)
    (rf/dispatch [:boot/web])
    (let [event-id     "test-event-id"
          followup-fx  [:some-followup]
          followups    (rf/subscribe [:remote/followup])
          followup-for (rf/subscribe [:remote/followup-for event-id])]
      (is (nil? @followup-for))
      (is (empty? @followups))
      (rf/dispatch [:remote/register-followup event-id followup-fx])
      (is (seq @followups))
      (is (not (nil? @followup-for)))
      (is (= followup-fx @followup-for))
      (rf/dispatch [:remote/unregister-followup event-id])
      (is (nil? @followup-for))
      (is (empty? @followups)))))
