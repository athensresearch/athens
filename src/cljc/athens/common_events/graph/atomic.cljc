(ns athens.common-events.graph.atomic
  "⚛️ Atomic Graph Ops.

  3 groups of Graph Ops:
  * block
  * page
  * shortcut"
  (:require))


;; Block Ops

(defn make-block-new
  "Creates `:block/new` atomic op.
   - `parent-uid` - `:block/uid` of parent block (or page)
   - `block-uid` - `:block/uid` of new block to be created
   - `block-order` - `:block/order` of new block to be created
       - `int` or 2 keywords `:first` & `:last` (to say that we want this new block to be 1st among the children of `parent-uid` or last)"
  [parent-uid block-uid block-order]
  {:op/type :block/new
   :op/args {:parent-uid  parent-uid
             :block-uid   block-uid
             :block-order block-order}})


(defn make-block-save
  "Creates `:block/save` atomic op.
   - `block-uid` - `:block/uid` of block to be saved
   - `new-string` - new value of `:block/string` to be saved"
  [block-uid new-string]
  {:op/type :block/save
   :op/args {:block-uid  block-uid
             :new-string new-string}})


(defn make-block-open
  "Creates `:block/open` atomic op
   - `block-uid` - `:block/uid` of block to be opened/closed
   - `open?` - should we open or close the block"
  [block-uid open?]
  {:op/type :block/open
   :op/args {:block-uid block-uid
             :open?     open?}})


(defn make-block-remove
  "Creates `:block/remove` atomic op.
   - `block-uid` - `:block/uid` of block to be removed"
  [block-uid]
  {:op/type :block/remove
   :op/args {:block-uid block-uid}})


(defn make-block-move
  "Creates `:block/move` atomic op.
   - `block-uid` - `:block/uid` of block to move
   - `parent-uid` - `:block/uid` of new parent block
   - `index` - (optional) `:block/order` new position on `:block/children`
       - if not provided, position is preserved"
  [block-uid parent-uid index]
  {:op/type :block/move
   :op/args {:block-uid  block-uid
             :parent-uid parent-uid
             :index      index}})


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
