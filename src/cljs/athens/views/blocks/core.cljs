(ns athens.views.blocks.core
  (:require
    [athens.common-db                     :as common-db]
    [athens.db                            :as db]
    [athens.views.blocks.types            :as types]
    ;; need to require it for multimethod participation
    [athens.views.blocks.types.default]
    [athens.views.blocks.types.dispatcher :as block-type-dispatcher]
    ;; need to require it for multimethod participation
    [athens.views.task.core]))


;; Components


(defn block-el
  "Two checks dec to make sure block is open or not: children exist and :block/open bool"
  ([block]
   [block-el block {:linked-ref false} {}])
  ([block linked-ref-data]
   [block-el block linked-ref-data {}])
  ([block linked-ref-data _opts]
   (let [block-uid  (:block/uid block)
         block-type (common-db/get-block-type @db/dsdb [:block/uid block-uid])
         renderer   (block-type-dispatcher/block-type->protocol block-type {:linked-ref-data linked-ref-data})]
     (println "XXX: :block/type prop:" (pr-str block-type))
     (types/outline-view renderer block block-el {}))))
