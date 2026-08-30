(ns athens.onboarding-test
  (:require
    [athens.db :as db]
    [athens.subs]
    [athens.views.onboarding :as onboarding]
    [clojure.test :refer [deftest is testing]]
    [re-frame.core :as rf]))


(deftest onboarding-events-and-state-test
  (testing "Initial default state"
    (rf/dispatch-sync [:init-rfdb])
    (is (false? @(rf/subscribe [:onboarding/open?])))
    (is (= 0 @(rf/subscribe [:onboarding/step])))
    (is (false? @(rf/subscribe [:onboarding/completed?]))))

  (testing ":onboarding/open event"
    (rf/dispatch-sync [:onboarding/open 2])
    (is (true? @(rf/subscribe [:onboarding/open?])))
    (is (= 2 @(rf/subscribe [:onboarding/step]))))

  (testing ":onboarding/set-step event"
    (rf/dispatch-sync [:onboarding/set-step 4])
    (is (= 4 @(rf/subscribe [:onboarding/step]))))

  (testing ":onboarding/close event"
    (rf/dispatch-sync [:onboarding/close])
    (is (false? @(rf/subscribe [:onboarding/open?]))))

  (testing ":onboarding/toggle event"
    (rf/dispatch-sync [:onboarding/toggle])
    (is (true? @(rf/subscribe [:onboarding/open?])))
    (rf/dispatch-sync [:onboarding/toggle])
    (is (false? @(rf/subscribe [:onboarding/open?]))))

  (testing ":onboarding/complete event persists completion"
    (rf/dispatch-sync [:onboarding/open 0])
    (rf/dispatch-sync [:onboarding/complete])
    (is (false? @(rf/subscribe [:onboarding/open?])))
    (is (true? @(rf/subscribe [:onboarding/completed?]))))

  (testing ":onboarding/reset event resets persistence and reopens at step 0"
    (rf/dispatch-sync [:onboarding/reset])
    (is (true? @(rf/subscribe [:onboarding/open?])))
    (is (= 0 @(rf/subscribe [:onboarding/step])))
    (is (false? @(rf/subscribe [:onboarding/completed?]))))

  (testing ":onboarding/skip event marks as completed"
    (rf/dispatch-sync [:onboarding/skip])
    (is (false? @(rf/subscribe [:onboarding/open?])))
    (is (true? @(rf/subscribe [:onboarding/completed?])))))
