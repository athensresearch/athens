#_{:clj-kondo/ignore [:unused-namespace]}


(ns athens.events.last-seen-tx-test
  "Testing `last-seen-tx` events & subscriptions."
  (:require
    [athens.effects]
    [athens.events]
    [athens.events.fixture :as fixture]
    [athens.subs]
    [cljs.test             :refer-macros [deftest is testing]]
    [day8.re-frame.test    :as rf-test]
    [re-frame.core         :as rf]))


(deftest have-not-seen-tx-yet
  ;; when we haven't seen any remote tx, yet
  (rf-test/run-test-sync
    (fixture/test-fixtures)
    (rf/dispatch [:boot/web])
    (let [last-seen-tx (rf/subscribe [:remote/last-seen-tx])]
      (is (= -1 @last-seen-tx)))))


(deftest set-last-seen-tx

  (testing "that we can set `:remote/last-seen-tx`"
    (rf-test/run-test-async
      (fixture/test-fixtures)
      (rf/dispatch-sync [:boot/web])
      (let [last-seen-tx (rf/subscribe [:remote/last-seen-tx])
            new-tx-id    10]
        (is (= -1 @last-seen-tx))

        (rf/dispatch [:remote/last-seen-tx! new-tx-id])
        (rf-test/wait-for
          [:remote/updated-last-seen-tx]

          (is (= new-tx-id @last-seen-tx))))))

  (testing "that we can update `:remote/last-seen-tx`"
    (rf-test/run-test-async
      (fixture/test-fixtures)
      (rf/dispatch-sync [:boot/web])
      (let [last-seen-tx (rf/subscribe [:remote/last-seen-tx])
            new-1 10
            new-2 11]
        (is (= -1 @last-seen-tx))

        (rf/dispatch [:remote/last-seen-tx! new-1])
        (rf-test/wait-for
          [:remote/updated-last-seen-tx]
          (is (= new-1 @last-seen-tx))

          (rf/dispatch [:remote/last-seen-tx! new-2])
          (rf-test/wait-for
            [:remote/updated-last-seen-tx]
            (is (= new-2 @last-seen-tx))))))))


(deftest last-seen-not-updated-when-accepted-event
  (rf-test/run-test-async
    (fixture/test-fixtures)
    (rf/dispatch-sync [:boot/web])
    (let [last-seen-tx (rf/subscribe [:remote/last-seen-tx])
          awaited-tx   (rf/subscribe [:remote/awaited-tx])
          new-tx-id    10
          accept-event {:event-id (:event/id fixture/test-event)
                        :tx-id    new-tx-id}]
      (is (= -1 @last-seen-tx))

      (rf/dispatch [:remote/await-event fixture/test-event])
      (rf/dispatch [:remote/accept-event accept-event])
      (rf-test/wait-for
        [:remote/accepted-event]

        (is (= -1 @last-seen-tx))
        (is (= #{10} @awaited-tx))))))
