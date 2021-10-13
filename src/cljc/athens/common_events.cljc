(ns athens.common-events
  "Event as Verbs executed on Knowledge Graph"
  (:require
    [athens.common.utils :as utils]))


;; building events

;; - confirmation events

(defn build-event-accepted
  "Builds ACK Event Response with `:accepted/tx-id` transaction id
  that accepted this event."
  [id tx-id]
  {:event/id       id
   :event/status   :accepted
   :accepted/tx-id tx-id})


(defn build-event-rejected
  "Builds Rejection Event Response with `:reject/reason & :reject/data`."
  [id message data]
  {:event/id      id
   :event/status  :rejected
   :reject/reason message
   :reject/data   data})


;; - datascript events

(defn build-db-dump-event
  "Builds `:datascript/db-dump` events with `datoms`."
  [last-tx datoms]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/db-dump
     :event/args    {:datoms datoms}}))


;; undo-redo events

(defn build-undo-redo-event
  "Builds `:datascript/undo-redo`"
  [last-tx redo?]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/undo-redo
     :event/args    {:redo? redo?}}))


;;   - page events

(defn build-page-rename-event
  "Builds `:datascript/page-rename` event with:
  - `uid`: of page to rename,
  - `old-name`: Old page name
  - `new-name`: New page name"
  [last-tx uid old-name new-name]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/rename-page
     :event/args    {:uid      uid
                     :old-name old-name
                     :new-name new-name}}))


(defn build-page-merge-event
  "Builds `:datascript/page-merge` event with:
  - `uid`: `:block/uid` of page being renamed
  - `old-name`: old page name
  - `new-name`: new page name"
  [last-tx uid old-name new-name]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/merge-page
     :event/args    {:uid      uid
                     :old-name old-name
                     :new-name new-name}}))


(defn build-page-delete-event
  "Builds `:datascript/page-delete` event with:
  - `uid`: of page to be deleted."
  [last-tx uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/delete-page
     :event/args    {:uid uid}}))


;; TODO: Do we need `value` here? can't we discover it during event resolution?
(defn build-paste-verbatim-event
  "Builds `:datascript/paste-verbatim` evnt with:
  - `uid`: of block that events applies to,
  - `text`: string that was pasted,
  - `start`: cursor position in block,
  - `value`: previous value (?) of block"
  [last-tx uid text start value]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/paste-verbatim
     :event/args    {:uid   uid
                     :text  text
                     :start start
                     :value value}}))


;;   - block events
;;     NOTE: `new-uid` is always passed from the caller,
;;           it would be safer to generate it during resolution
(defn build-block-save-event
  "Builds `:datascript/block-save` event with:
  - `uid`       : `:block/uid` of block to save
  - `new-string`: new value for `:block/string`
  - `add-time?` : Should `:edit/time` for this block be transacted"
  [last-tx uid new-string add-time?]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/block-save
     :event/args    {:uid        uid
                     :new-string new-string
                     :add-time?  add-time?}}))


(defn build-new-block-event
  "Builds `:datascript/new-block` event with:
  - `parent-uid`: `:block/uid` of parent node
  - `block-order`: order of current block
  - `new-uid`: `:block/uid` for new block"
  [last-tx parent-uid block-order new-uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/new-block
     :event/args    {:parent-uid  parent-uid
                     :block-order block-order
                     :new-uid     new-uid}}))


(defn build-add-child-event
  "Builds `:datascript/add-child` event with:
  - `parent-uid`: `:block/uid` of parent block
  - `new-uid`  : new child's block uid
  - `add-time?`: Should `:edit/time` for this block be transacted"
  ([last-tx parent-uid new-uid] (let [event-id (utils/gen-event-id)]
                                  {:event/id      event-id
                                   :event/last-tx last-tx
                                   :event/type    :datascript/add-child
                                   :event/args    {:parent-uid parent-uid
                                                   :new-uid    new-uid
                                                   :add-time?  false}}))
  ([last-tx parent-uid new-uid add-time?] (let [event-id (utils/gen-event-id)]
                                            {:event/id      event-id
                                             :event/last-tx last-tx
                                             :event/type    :datascript/add-child
                                             :event/args    {:parent-uid parent-uid
                                                             :new-uid    new-uid
                                                             :add-time?  add-time?}})))


(defn build-open-block-add-child-event
  "Builds `:datascript/open-block-add-child` event with:
  - `parent-uid`: `:block/uid` of parent block
  - `new-uid`: `:block/uid` for new block"
  [last-tx parent-uid new-uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/open-block-add-child
     :event/args    {:parent-uid parent-uid
                     :new-uid    new-uid}}))


(defn build-split-block-event
  "Builds `:datascript/split-block` event with:
  - `uid`: `:block/uid` of block we're splitting
  - `value`: Current `:block/string` of block splitted
  - `index`: index of the split
  - `new-uid`: `:block/uid` of new block"
  [last-tx uid value index new-uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/split-block
     :event/args    {:uid     uid
                     :value   value
                     :index   index
                     :new-uid new-uid}}))


(defn build-split-block-to-children-event
  "Builds `:datascript/split-block-to-children` event with:
  - `uid`: `:block/uid` of block to split
  - `value`: Current `:block/string` of block splitted
  - `index`: index of the split
  - `new-uid`: `:block/uid` of new block"
  [last-tx uid value index new-uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/split-block-to-children
     :event/args    {:uid     uid
                     :value   value
                     :index   index
                     :new-uid new-uid}}))


(defn build-unindent-event
  "Builds `:datascript/unindent` event with:
  - `uid`: `:block/uid` of triggering block
  - `value`: `:block/string` of triggering block"
  [last-tx uid value]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/unindent
     :event/args    {:uid   uid
                     :value value}}))


(defn build-unindent-multi-event
  "Builds `:datascript/unindent-multi` event with:
  - `uids` : `:block/uid` of selected blocks"
  [last-tx uids]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/unindent-multi
     :event/args    {:uids uids}}))


(defn build-page-add-shortcut
  "Builds `:datascript/page-add-shortcut` event with:
  - `uid`: `:block/uid` of triggering block"
  [last-tx uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/page-add-shortcut
     :event/args    {:uid uid}}))


(defn build-page-remove-shortcut
  "Builds `:datascript/page-remove-shortcut` event with:
  - `uid`: `:block/uid` of triggering block"
  [last-tx uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/page-remove-shortcut
     :event/args    {:uid uid}}))


(defn build-left-sidebar-drop-above
  "Builds `:datascript/left-sidebar-drop-above` event with:
  - `source-order`: original position on the left sidebar
  - `target-order`: new position on the left sidebar"
  [last-tx source-order target-order]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/left-sidebar-drop-above
     :event/args    {:source-order source-order
                     :target-order target-order}}))


(defn build-left-sidebar-drop-below
  "Builds `:datascript/left-sidebar-drop-below` event with:
  - `source-order`: original position on the left sidebar
  - `target-order`: new position on the left sidebar"
  [last-tx source-order target-order]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/left-sidebar-drop-below
     :event/args    {:source-order source-order
                     :target-order target-order}}))


(defn build-indent-event
  "Builds `: `datascript/indent` event with:
  - `uid`  : `:block/uid` of triggering block
  - `value`: `:block/string` of triggering block"
  [last-tx uid value]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/indent
     :event/args    {:uid   uid
                     :value value}}))


(defn build-indent-multi-event
  "Builds `: `:datascript/indent-multi` event with:
  - `uids`  : `:block/uid` of selected blocks"
  [last-tx uids]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/indent-multi
     :event/args    {:uids   uids}}))


(defn build-bump-up-event
  "Builds `:datascript/bump-up` event with:
  - `uid`: `:block/uid` of trigerring block
  - `new-uid`: new `:block/uid`"
  [last-tx uid new-uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/bump-up
     :event/args    {:uid     uid
                     :new-uid new-uid}}))


(defn build-unlinked-references-link
  "Builds `:datascript/unlinked-references-link` event with:
  - `uid`:  `:block/uid` of the block with unlinked reference
  - `string `: content of the block
  - `title  `: title of the page"
  [last-tx uid string title]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/unlinked-references-link
     :event/args    {:uid    uid
                     :string string
                     :title  title}}))


(defn build-unlinked-references-link-all
  "Builds `:datascript/unlinked-references-link` event with:
  - `unlinked-refs`: list of maps that contains the :block/string and :block/uid of unlinked refs
  - `title        `: title of the page in which the unlinked refs will be linked"
  [last-tx unlinked-refs title]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/unlinked-references-link-all
     :event/args    {:unlinked-refs unlinked-refs
                     :title         title}}))


(defn build-drop-child-event
  "Builds `:datascript/drop-child` event with:
  - `source-uid` : uid of the source block
  - `target-uid` : uid of the target block"
  [last-tx source-uid target-uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/drop-child
     :event/args    {:source-uid source-uid
                     :target-uid target-uid}}))


(defn build-drop-multi-child-event
  "Builds `:datascript/drop-multi-child` event with:
  - `source-uids` : Vector of uids of the selected source blocks
  - `target-uid`  : uid of the target block"
  [last-tx source-uids target-uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/drop-multi-child
     :event/args    {:source-uids source-uids
                     :target-uid  target-uid}}))


(defn build-drop-link-child-event
  "Builds `:datascript/drop-link-child` event with:
  - `source-uid` : uid of the source block
  - `target-uid` : uid of the target block"
  [last-tx source-uid target-uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/drop-link-child
     :event/args    {:source-uid source-uid
                     :target-uid target-uid}}))


(defn build-drop-diff-parent-event
  "Builds `:datascript/drop-diff-parent` event with:
  - `source-uid` : uid of the source block
  - `target-uid` : uid of the target block
  - `drag-target`: defines where is the block dragged it can be :above or :below the target block"
  [last-tx drag-target source-uid target-uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/drop-diff-parent
     :event/args    {:source-uid  source-uid
                     :target-uid  target-uid
                     :drag-target drag-target}}))


(defn build-drop-multi-diff-source-same-parents-event
  "Builds `:datascript/drop-multi-diff-source-same-parents` event with:
  - `source-uids` : Vector of uids of the selected source blocks
  - `target-uid`  : uid of the target block
  - `drag-target`: defines where is the block dragged it can be :above or :below the target block"
  [last-tx drag-target source-uids target-uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/drop-multi-diff-source-same-parents
     :event/args    {:source-uids  source-uids
                     :target-uid   target-uid
                     :drag-target  drag-target}}))


(defn build-drop-multi-diff-source-diff-parents-event
  "Builds `:datascript/drop-multi-diff-source-diff-parents` event with:
  - `source-uids` : Vector of uids of the selected source blocks
  - `target-uid`  : uid of the target block
  - `drag-target`: defines where is the block dragged it can be :above or :below the target block"
  [last-tx drag-target source-uids target-uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/drop-multi-diff-source-diff-parents
     :event/args    {:source-uids  source-uids
                     :target-uid   target-uid
                     :drag-target  drag-target}}))


(defn build-drop-link-diff-parent-event
  "Builds `:datascript/drop-link-diff-parent` event with:
  - `source-uid` : uid of the source block
  - `target-uid` : uid of the target block
  - `drag-target`: defines where is the block dragged it can be :above or :below the target block"
  [last-tx drag-target source-uid target-uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/drop-link-diff-parent
     :event/args    {:source-uid  source-uid
                     :target-uid  target-uid
                     :drag-target drag-target}}))


(defn build-drop-same-event
  "Builds `:datascript/drop-same` event with:
  - `source-uid` : uid of the source block
  - `target-uid` : uid of the target block
  - `drag-target`: defines where is the block dragged it can be :above or :below the target block"
  [last-tx drag-target source-uid target-uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/drop-same
     :event/args    {:drag-target drag-target
                     :source-uid  source-uid
                     :target-uid  target-uid}}))


(defn build-drop-multi-same-source-event
  "Builds `:datascript/drop-multi-same-source` event with:
  - `source-uids` : Vector of uids of the selected source blocks
  - `target-uid` : uid of the target block
  - `drag-target`: defines where is the block dragged it can be :above or :below the target block"
  [last-tx drag-target source-uids target-uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/drop-multi-same-source
     :event/args    {:source-uids  source-uids
                     :target-uid   target-uid
                     :drag-target  drag-target}}))


(defn build-drop-multi-same-all-event
  "Builds `:datascript/drop-multi-same-all` event with:
  - `source-uids` : Vector of uids of the selected source blocks
  - `target-uid` : uid of the target block
  - `drag-target`: defines where is the block dragged it can be :above or :below the target block"
  [last-tx drag-target source-uids target-uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/drop-multi-same-all
     :event/args    {:source-uids  source-uids
                     :target-uid   target-uid
                     :drag-target  drag-target}}))


(defn build-drop-link-same-parent-event
  "Builds `:datascript/drop-link-same` event with:
  - `source-uid` : Vector of uids of the selected source blocks
  - `target-uid` : uid of the target block
  - `drag-target`: defines where is the block dragged it can be :above or :below the target block"
  [last-tx drag-target source-uid target-uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/drop-link-same-parent
     :event/args    {:source-uid  source-uid
                     :target-uid   target-uid
                     :drag-target  drag-target}}))


(defn build-selected-delete-event
  "Builds `:datascript/selected-delete` event with:
  - uids : The uids of blocks to be deleted "
  [last-tx uids]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/selected-delete
     :event/args    {:uids uids}}))


(defn build-block-open-event
  "Builds `:datascript/block-open` event with:
  - block-uid : The uid of block to be opened
  - open?     : Bool to set the block state to open or close"
  [last-tx block-uid open?]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/block-open
     :event/args    {:block-uid block-uid
                     :open?     open?}}))


(defn build-paste-internal-event
  "Builds `:datascript/paste-internal` event with:
  - uid  : The uid of block to which text is to be pasted
  - internal-representation : Data to be pasted from another Athens"
  [last-tx uid internal-representation]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/paste-internal
     :event/args    {:uid                     uid
                     :internal-representation internal-representation}}))


(defn build-delete-only-child-event
  "Builds `:datascript/delete-only-child` event with:
  - uid  : The uid of block to delete"
  [last-tx uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/delete-only-child
     :event/args    {:uid uid}}))


(defn build-delete-merge-block-event
  "Builds `:datascript/delete-merge-block` event with:
  - uid  : The uid of block to delete
  - value: The text content of the block"
  [last-tx uid value]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :datascript/delete-merge-block
     :event/args    {:uid uid
                     :value value}}))


;; - presence events

(defn build-presence-hello-event
  "Builds `:presence/hello` event with `username`"
  ([last-tx username]
   (build-presence-hello-event last-tx username nil))
  ([last-tx username password]
   (let [event-id (utils/gen-event-id)]
     {:event/id      event-id
      :event/last-tx last-tx
      :event/type    :presence/hello
      :event/args    (cond-> {:username username}
                       password (merge {:password password}))})))


(defn build-presence-online-event
  "Builds `:presence/online` event with `username` that went online."
  [last-tx username]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :presence/online
     :event/args    {:username username}}))


(defn build-presence-all-online-event
  "Builds `:presence/all-online` event with all active users, excluding origin client."
  [last-tx clients]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :presence/all-online
     :event/args     (mapv (fn [username]
                             {:username username})
                           clients)}))


(defn build-presence-offline-event
  [last-tx username]
  (let [event (build-presence-online-event last-tx username)]
    (assoc event :event/type :presence/offline)))


(defn build-presence-editing-event
  "Sent by client."
  [last-tx username uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :presence/editing
     :event/args    {:username  username
                     :block-uid uid}}))


(defn build-presence-broadcast-editing-event
  "Sent by server."
  [last-tx username block-uid]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :presence/broadcast-editing
     :event/args    {:username  username
                     :block-uid block-uid}}))


(defn build-presence-rename-event
  "Sent by client when username is updated"
  [last-tx current-username new-username]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :presence/rename
     :event/args    {:current-username current-username
                     :new-username new-username}}))


(defn build-presence-broadcast-rename-event
  "Sent by server when the updated username is broadcasted"
  [last-tx current-username new-username]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :presence/broadcast-rename
     :event/args    {:current-username current-username
                     :new-username new-username}}))


(defn build-atomic-event
  "Builds atomic graph operation"
  [last-tx atomic-op]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :op/atomic
     :event/op      atomic-op}))

