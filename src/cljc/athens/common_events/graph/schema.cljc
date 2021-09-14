(ns athens.common-events.graph.schema
  (:require
   [malli.core  :as m]
   [malli.error :as me]
   [malli.util  :as mu]))


(def atomic-op-types
  [:enum
   :block/new
   :block/save
   :block/open
   :block/remove
   :block/move
   :page/new
   :page/rename
   :page/merge
   :page/remove
   :shortcut/new
   :shortcut/remove
   :shortcut/move])


(def op-type-atomic-common
  [:map
   [:op/type atomic-op-types]
   [:op/atomic? boolean?]])


(def op-block-new
  [:map
   [:op/args
    [:map
     [:parent-uid string?]
     [:block-uid string?]
     [:block-order int?]]]])

(def atomic-op
  [:multi {:dispatch :op/type}
   [:block/new (mu/merge
                op-type-atomic-common
                op-block-new)]])


(def valid-atomic-op?
  (m/validator atomic-op))


(defn explain-atomic-op
  [data]
  (-> atomic-op
      (m/explain data)
      (me/humanize)))
