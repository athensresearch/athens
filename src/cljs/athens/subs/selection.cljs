(ns athens.subs.selection
  (:require
    [re-frame.core :as rf]))


(rf/reg-sub
  ::items
  (fn [db _]
    (get-in db [:selection :items])))


(rf/reg-sub
  ::selected?
  :<- [::items]
  (fn [selected-items [_ uid]]
    (contains? (set selected-items)
               uid)))
