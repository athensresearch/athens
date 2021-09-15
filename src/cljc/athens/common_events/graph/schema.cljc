(ns athens.common-events.graph.schema
  (:require
   [malli.core  :as m]
   [malli.error :as me]
   [malli.util  :as mu]))


(def atomic-op-types
  [:enum
   :block/new ;; ✓
   :block/save
   :block/open
   :block/remove
   :block/move
   :page/new ;; ✓
   :page/rename
   :page/merge
   :page/remove
   :shortcut/new
   :shortcut/remove
   :shortcut/move])


(def op-type-atomic-common
  [:map
   [:op/type atomic-op-types]
   [:op/atomic? true?]])


(def op-block-new
  [:map
   [:op/args
    [:map
     [:parent-uid string?]
     [:block-uid string?]
     [:block-order int?]]]])


(def op-block-save
  [:map
   [:op/args
    [:map
     [:block-uid string?]
     [:new-string string?]
     [:old-string string?]]]])


(def op-page-new
  [:map
   [:op/args
    [:map
     [:title string?]
     [:page-uid string?]
     [:block-uid string?]]]])


(def op-composite-consequence
  [:map
   [:op/type [:enum :composite/consequence]]
   [:op/atomic? false?]
   [:op/trigger map?]
   [:op/consequences [:vector map?]]])


(def atomic-op
  [:multi {:dispatch :op/type}
   [:block/new (mu/merge
                op-type-atomic-common
                op-block-new)]

   [:page/new (mu/merge
               op-type-atomic-common
               op-page-new)]

   [:composite/consequence op-composite-consequence]])


(def valid-atomic-op?
  (m/validator atomic-op))


(defn explain-atomic-op
  [data]
  (-> atomic-op
      (m/explain data)
      (me/humanize)))
