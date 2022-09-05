(ns athens.views.left-sidebar.subs
  (:require
    [athens.views.left-sidebar.shared :as shared]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [re-frame.core         :as rf]
    [athens.reactive :as reactive]))

(defn get-sidebar-open?
  []
  (let [user-page @(rf/subscribe [:presence/user-page])
        props     (-> (reactive/get-reactive-node-document [:node/title user-page])
                      :block/properties)
        open?     (-> (get props (shared/ns-str "/closed?"))
                      boolean)]
    open?))


(defn get-max-tasks
  []
  (let [user-page         @(rf/subscribe [:presence/user-page])
        props             (-> (reactive/get-reactive-node-document [:node/title user-page])
                              :block/properties)
        default-max-tasks 3
        max-tasks         (-> (get props (shared/ns-str "/tasks/max-tasks"))
                              :block/string)]
    (js/parseInt (or max-tasks default-max-tasks))))

(defn get-widget-open?
  [widget-id]
  (let [user-page @(rf/subscribe [:presence/user-page])
        props (-> (reactive/get-reactive-node-document [:node/title user-page])
                  :block/properties)
        open? (-> (get props (shared/ns-str "/widgets/" widget-id "/closed?"))
                  boolean)]
    open?))


(defn get-task-section-open?
  "section-id is by page currently"
  [section-id]
  (let [user-page @(rf/subscribe [:presence/user-page])
        props (-> (reactive/get-reactive-node-document [:node/title user-page])
                  :block/properties)
        open? (-> (get props (shared/ns-str "/tasks/" section-id "/closed?"))
                  boolean)]
    open?))
