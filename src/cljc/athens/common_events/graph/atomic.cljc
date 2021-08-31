(ns athens.common-events.graph.atomic
  "⚛️ Atomic Graph Ops.

  3 groups of Graph Ops:
  * block
  * page
  * shortcut"
  (:require))


;; Block Ops

(defn make-block-new
  "Creates `:block/new` atomic op"
  [] ;; TODO continue
  )


(defn make-block-save
  "Creates `:block/save` atomic op"
  [] ;; TODO implement
  )


(defn make-block-remove
  "Creates `:block/remove` atomic op"
  [] ;; TODO implement
  )


(defn make-block-move
  "Creates `:block/move` atomic op"
  [] ;; TODO implement
  )


;; Page Ops

;; :page/new
(defn make-page-new
  "Creates `:page/new` atomic op"
  [] ;; TODO implement
  )


;; :page/rename
(defn make-page-rename
  "Creates `:page/rename` atomic op"
  [] ;; TODO implement
  )


;; :page/merge
(defn make-page-merge
  "Creates `:page/merge` atomic op"
  [] ;; TODO implement
  )


;; :page/remove
(defn make-page-remove
  "Creates `:page/remove` atomic op"
  [] ;; TODO implement
  )



;; Shortcut

;; :shortcut/new
(defn make-shortcut-new
  "Creates `:shortcut/new` atomic op"
  [] ;; TODO implement
  )


;; :shortcut/remove
(defn make-shortcut-remove
  "Creates `:shortcut/remove` atomic op"
  [] ;; TODO implement
  )


;; :shortcut/move
(defn make-shortcut-move
  "Creates `:shortcut/move` atomic op"
  [] ;; TODO implement
  )
