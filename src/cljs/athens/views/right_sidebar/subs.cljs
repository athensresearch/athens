(ns athens.views.right-sidebar.subs
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [re-frame.core         :as rf]))


(rf/reg-sub
  :right-sidebar/open
  (fn-traced [db _]
             (:right-sidebar/open db)))


(rf/reg-sub
  :right-sidebar/items
  (fn-traced [db _]
             (:right-sidebar/items db)))


(rf/reg-sub
  :right-sidebar/width
  (fn [db _]
    (:right-sidebar/width db)))
