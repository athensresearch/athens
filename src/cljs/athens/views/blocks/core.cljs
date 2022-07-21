(ns athens.views.blocks.core
  (:require
    ["/components/Block/Container"           :refer [Container]]
    ["@chakra-ui/react"                      :refer [MenuList MenuItem]]
    [athens.common.logging                   :as log]
    [athens.db                               :as db]
    [athens.electron.images                  :as images]
    [athens.electron.utils                   :as electron.utils]
    [athens.events.dragging                  :as drag.events]
    [athens.events.inline-refs               :as inline-refs.events]
    [athens.events.linked-refs               :as linked-ref.events]
    [athens.events.selection                 :as select-events]
    [athens.reactive                         :as reactive]
    [athens.subs.dragging                    :as drag.subs]
    [athens.subs.selection                   :as select-subs]
    [athens.util                             :as util :refer [mouse-offset vertical-center specter-recursive-path]]
    [athens.views.blocks.context-menu        :as ctx-menu]
    [athens.views.blocks.drop-area-indicator :as drop-area-indicator]
    [athens.views.blocks.editor              :as editor]
    [com.rpl.specter                         :as s]
    [goog.functions                          :as gfns]
    [re-frame.core                           :as rf]
    [reagent.core                            :as r]
    [reagent.ratom                           :as ratom]))


;; Components


(defn block-el
  "Two checks dec to make sure block is open or not: children exist and :block/open bool"
  ([block]
   [block-el block {:linked-ref false} {}])
  ([block linked-ref-data]
   [block-el block linked-ref-data {}])
  ([block linked-ref-data _opts]
   (let [block-type nil ; TODO make real block type discovery
         renderer   (block-type-dispatcher/block-type->protocol block-type {:linked-ref-data linked-ref-data})]
     (types/outline-view renderer block block-el {}))))
