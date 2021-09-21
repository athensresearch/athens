(ns athens.common-events.graph.atomic
  "⚛️ Atomic Graph Ops.

  3 groups of Graph Ops:
  * block
  * page
  * shortcut")


;; Block Ops

(defn make-block-new-op
  "Creates `:block/new` atomic op.
   - `parent-uid` - `:block/uid` of parent block (or page)
   - `block-uid` - `:block/uid` of new block to be created
   - `block-order` - `:block/order` of new block to be created
       - `int` or 2 keywords `:first` & `:last` (to say that we want this new block to be 1st among the children of `parent-uid` or last)"
  [parent-uid block-uid block-order]
  {:op/type    :block/new
   :op/atomic? true
   :op/args    {:parent-uid  parent-uid
                :block-uid   block-uid
                :block-order block-order}})


(defn make-block-save-op
  "Creates `:block/save` atomic op.
   - `block-uid` - `:block/uid` of block to be saved
   - `old-string` - old value of `:block/string`
   - `new-string` - new value of `:block/string` to be saved"
  [block-uid old-string new-string]
  {:op/type    :block/save
   :op/atomic? true
   :op/args    {:block-uid  block-uid
                :old-string old-string
                :new-string new-string}})


(defn make-block-open-op
  "Creates `:block/open` atomic op.
   - `block-uid` - `:block/uid` of block to be opened/closed
   - `open?` - should we open or close the block"
  [block-uid open?]
  {:op/type    :block/open
   :op/atomic? true
   :op/args    {:block-uid block-uid
                :open?     open?}})


(defn make-block-remove-op
  "Creates `:block/remove` atomic op.
   - `block-uid` - `:block/uid` of block to be removed"
  [block-uid]
  {:op/type    :block/remove
   :op/atomic? true
   :op/args    {:block-uid block-uid}})


(defn make-block-move-op
  "Creates `:block/move` atomic op.
   - `block-uid` - `:block/uid` of block to move
   - `parent-uid` - `:block/uid` of new parent block
   - `index` - (optional) `:block/order` new position on `:block/children`
       - if not provided, position is preserved"
  [block-uid parent-uid index]
  {:op/type    :block/move
   :op/atomic? true
   :op/args    {:block-uid  block-uid
                :parent-uid parent-uid
                :index      index}})


;; Page Ops

;; TODO(RTC): remove page-uid, use just title, after we've migrated fully to atomic ops
(defn make-page-new-op
  "Creates `:page/new` atomic op.
   - `title` - Page title page to be created
   - `page-uid` - `:block/uid` of page to be created
   - `block-uid` - `:block/uid` of 1st block to be created in page to be created"
  [title page-uid block-uid]
  {:op/type    :page/new
   :op/atomic? true
   :op/args    {:title     title
                :page-uid  page-uid
                :block-uid block-uid}})


(defn make-page-rename-op
  "Creates `:page/rename` atomic op.
   - `page-uid` - `:block/uid` of page to be renamed
   - `new-name` - Page should have this name after operation"
  [page-uid new-name]
  {:op/type    :page/rename
   :op/atomic? true
   :op/args    {:page-uid page-uid
                :new-name new-name}})


(defn make-page-merge-op
  "Creates `:page/merge` atomic op.
   - `page-uid` - `:block/uid` of page to be merged into `new-page`
   - `new-page` - page name of a page we'll merge contents of `page-uid` page into"
  [page-uid new-page]
  {:op/type    :page/merge
   :op/atomic? true
   :op/args    {:page-uid page-uid
                :new-page new-page}})


(defn make-page-remove-op
  "Creates `:page/remove` atomic op.
   - `page-uid` - `:block/uid` of the page to be deleted"
  [page-uid]
  {:op/type    :page/remove
   :op/atomic? true
   :op/args    {:page-uid page-uid}})


;; Shortcut

(defn make-shortcut-new-op
  "Creates `:shortcut/new` atomic op.
   - `page-uid` - `:block/uid` of page to be added to shortcuts"
  [page-uid]
  {:op/type    :shortcut/new
   :op/atomic? true
   :op/args    {:page-uid page-uid}})


(defn make-shortcut-remove-op
  "Creates `:shortcut/remove` atomic op.
   - `page-uid` - `:block/uid` of page to be removed from shortcuts"
  [page-uid]
  {:op/type    :shortcut/remove
   :op/atomic? true
   :op/args    {:page-uid page-uid}})


(defn make-shortcut-move-op
  "Creates `:shortcut/move` atomic op.
   - `page-uid` - `:block/uid` of page to be moved to new position in shortcuts
   - `index` - new position for `page-uid` shortcut"
  [page-uid index]
  {:op/type    :shortcut/move
   :op/atomic? true
   :op/args    {:page-uid page-uid
                :index    index}})
