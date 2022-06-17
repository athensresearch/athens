(ns athens.subs.inline-refs
  (:require
    [re-frame.core :as rf]))


(rf/reg-sub
  ::open?
  (fn [db [_ uid]]
    (get-in db [:inline-refs uid :open?] false)))


(rf/reg-sub
  ::state-open?
  (fn [db [_ uid]]
    (get-in db [:inline-refs uid :state :open?] false)))


(rf/reg-sub
  ::state-focus?
  (fn [db [_ uid]]
    (get-in db [:inline-refs uid :state :focus?] false)))


(rf/reg-sub
  ::state-block
  (fn [db [_ uid]]
    (get-in db [:inline-refs uid :state :block])))


(rf/reg-sub
  ::state-parents
  (fn [db [_ uid]]
    (get-in db [:inline-refs uid :state :parents])))


(rf/reg-sub
  ::state-embed-id
  (fn [db [_ uid]]
    (get-in db [:inline-refs uid :state :focus?])))

