(ns athens.common-events.graph.atomic
  "⚛️ Atomic Graph Ops.

  3 groups of Graph Ops:
  * block
  * page
  * shortcut")


;; Block Ops

(defn make-block-new-op
  "Creates `:block/new` atomic op.
   - `block-uid` - `:block/uid` of new block to be created
   - `position` - new blocks position
      - for siblings: `:before` or `:after` together with `ref-uid`
      - for children: `:first`, `:last` together with `:block/uid` or `:page/title`"
  [block-uid position]
  {:op/type    :block/new
   :op/atomic? true
   :op/args    {:block/uid      block-uid
                :block/position position}})


(defn make-block-save-op
  "Creates `:block/save` atomic op.
   - `block-uid` - `:block/uid` of block to be saved
   - `string` - new value of `:block/string` to be saved"
  [block-uid string]
  {:op/type    :block/save
   :op/atomic? true
   :op/args    {:block/uid    block-uid
                :block/string string}})


(defn make-block-open-op
  "Creates `:block/open` atomic op.
   - `block-uid` - `:block/uid` of block to be opened/closed
   - `open?` - should we open or close the block"
  [block-uid open?]
  {:op/type    :block/open
   :op/atomic? true
   :op/args    {:block/uid   block-uid
                :block/open? open?}})


(defn make-block-remove-op
  "Creates `:block/remove` atomic op.
   - `block-uid` - `:block/uid` of block to be removed"
  [block-uid]
  {:op/type    :block/remove
   :op/atomic? true
   :op/args    {:block/uid block-uid}})


(defn make-block-move-op
  "Creates `:block/move` atomic op.
   - `block-uid` - `:block/uid` of block to move
  - `position` - new blocks position
      - for siblings: `:before` or `:after` together with `ref-uid`
      - for children: `:first`, `:last` together with `:block/uid` or `:page/title`"
  [block-uid position]
  {:op/type    :block/move
   :op/atomic? true
   :op/args    {:block/uid      block-uid
                :block/position position}})

(defn make-comment-add-op
  "Creates a comment/add op
   - block/uid
   - new-comment - comment to be added to the vector of comments"
  [block-uid new-comment]
  {:op/type    :comment/add
   :op/atomic? true
   :op/args    {:block/uid   block-uid
                :new-comment new-comment}})


;; Page Ops

(defn make-page-new-op
  "Creates `:page/new` atomic op.
   - `title` - Page title to be created "
  [title]
  {:op/type    :page/new
   :op/atomic? true
   :op/args    {:page/title title}})


(defn make-page-rename-op
  "Creates `:page/rename` atomic op.
   - `title` - Page title before rename,
   - `new-title` - Page should have this title after operation"
  [title new-title]
  {:op/type    :page/rename
   :op/atomic? true
   :op/args    {:page/title title
                :target     {:page/title new-title}}})


(defn make-page-merge-op
  "Creates `:page/merge` atomic op.
   - `title` - title of page to be merged into `to-title`
   - `to-title` - title merge to this page"
  [title to-title]
  {:op/type    :page/merge
   :op/atomic? true
   :op/args    {:page/title title
                :target     {:page/title to-title}}})


(defn make-page-remove-op
  "Creates `:page/remove` atomic op.
   - `title` - title of page to be deleted"
  [title]
  {:op/type    :page/remove
   :op/atomic? true
   :op/args    {:page/title title}})


;; Shortcut

(defn make-shortcut-new-op
  "Creates `:shortcut/new` atomic op.
   - `title` - title of page to be added to shortcuts"
  [title]
  {:op/type    :shortcut/new
   :op/atomic? true
   :op/args    {:page/title title}})


(defn make-shortcut-remove-op
  "Creates `:shortcut/remove` atomic op.
   - `title` - title of page to be removed from shortcuts"
  [title]
  {:op/type    :shortcut/remove
   :op/atomic? true
   :op/args    {:page/title title}})


(defn make-shortcut-move-op
  "Creates `:shortcut/move` atomic op.
   - `title` - title of page to be moved to new position in shortcuts
   - `position` - new position for shortcut
      - `:page/title` - title of page relative to which source page is to be moved
      - `relation` - move the source-name :above or :below title"
  [title position]
  {:op/type    :shortcut/move
   :op/atomic? true
   :op/args    {:page/title        title
                :shortcut/position position}})
