(ns athens.common-events.graph.schema
  (:require
   [malli.core :as m]))


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


(def atomic-op
  [:map
   [:op/type atomic-op-types]])
