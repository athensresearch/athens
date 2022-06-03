(ns athens.subs.inline-refs
  (:require
    [re-frame.core :as rf]))


(rf/reg-sub
  ::open?
  (fn [db [_ uid]]
    (let [val (get-in db [:inline-refs uid :open?] false)]
      (println ::open? (pr-str uid) (pr-str val))
      val)))


(rf/reg-sub
  ::state-open?
  (fn [db [_ uid]]
    (let [val (get-in db [:inline-refs uid :state :open?] false)]
      (println ::state-open? (pr-str uid) (pr-str val))
      val)))


(rf/reg-sub
  ::state-focus?
  (fn [db [_ uid]]
    (let [val (get-in db [:inline-refs uid :state :focus?] false)]
      (println ::state-focus? (pr-str uid) (pr-str val))
      val)))


(rf/reg-sub
  ::state-block
  (fn [db [_ uid]]
    (let [val (get-in db [:inline-refs uid :state :block])]
      (println ::state-block (pr-str uid) (pr-str val))
      val)))


(rf/reg-sub
  ::state-parents
  (fn [db [_ uid]]
    (let [val (get-in db [:inline-refs uid :state :parents])]
      (println ::state-parents (pr-str uid) (pr-str val))
      val)))


(rf/reg-sub
  ::state-embed-id
  (fn [db [_ uid]]
    (let [val (get-in db [:inline-refs uid :state :focus?])]
      (println ::state-embed-id (pr-str uid) (pr-str val))
      val)))

