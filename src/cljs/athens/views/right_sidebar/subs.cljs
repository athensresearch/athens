(ns athens.views.right-sidebar.subs
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [re-frame.core         :as rf]
    [athens.views.right-sidebar.shared :as shared]))


(rf/reg-sub
  :right-sidebar/open
  (fn-traced [db _]
             (shared/get-open?)))


(rf/reg-sub
  :right-sidebar/items
  (fn-traced [db _]
             (shared/get-items)))


(rf/reg-sub
  :right-sidebar/contains-item?
  (fn-traced [db [_ eid]]
             (let [items (shared/get-items)
                   [attr value] eid
                   ;; if the block string matches a page or uid, assume it contains
                   find (filter (fn [{:keys [name type]}]
                                  (and (= value name)
                                       (or (and (= type "page")
                                                (= attr :node/title))
                                           (and (= type "block")
                                                (= attr :block/uid)))))
                                items)]
               (seq find))))


(rf/reg-sub
  :right-sidebar/width
  (fn [db _]
    (:right-sidebar/width db)))
