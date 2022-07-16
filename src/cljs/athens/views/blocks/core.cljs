(ns athens.views.blocks.core
  (:require
    [athens.views.blocks.types :as types]
    ;; need to require it for multimethod participation
    [athens.views.blocks.types.default]
    [athens.views.blocks.types.dispatcher :as block-type-dispatcher]))


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
