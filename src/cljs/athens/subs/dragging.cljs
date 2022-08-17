(ns athens.subs.dragging
  (:require
    [re-frame.core :as rf]))


(rf/reg-sub
  ::drag-target
  (fn [db [_ uid]]
    (get-in db [:dragging uid :drag-target])))


(rf/reg-sub
  ::dragging?
  (fn [db [_ uid]]
    (get-in db [:dragging uid :dragging?])))
