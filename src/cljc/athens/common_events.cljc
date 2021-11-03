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


;; - presence events

(defn build-presence-hello-event
  "Builds `:presence/hello` event with `session-intro` and `password` (optional)."
  ([last-tx session-intro]
   (build-presence-hello-event last-tx session-intro nil))
  ([last-tx session-intro password]
   (let [event-id (utils/gen-event-id)]
     {:event/id      event-id
      :event/last-tx last-tx
      :event/type    :presence/hello
      :event/args    (cond-> {:session-intro session-intro}
                       password (merge {:password password}))})))


(defn build-presence-session-id-event
  "Builds `:presence/session-id` event with `session-id` for the client."
  [last-tx session-id]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :presence/session-id
     :event/args    {:session-id session-id}}))


(defn build-presence-online-event
  "Builds `:presence/online` event with `session` that went online."
  [last-tx session]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :presence/online
     :event/args    session}))


(defn build-presence-all-online-event
  "Builds `:presence/all-online` event with all active users."
  [last-tx sessions]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :presence/all-online
     :event/args    (vec sessions)}))


(defn build-presence-offline-event
  [last-tx session]
  (let [event (build-presence-online-event last-tx session)]
    (assoc event :event/type :presence/offline)))


(defn build-presence-update-event
  "Builds `:presence/update` event with `session-id` and map of session props to update."
  [last-tx session-id updates]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :presence/update
     :event/args    (merge {:session-id session-id}
                           updates)}))


(defn build-atomic-event
  "Builds atomic graph operation"
  [last-tx atomic-op]
  (let [event-id (utils/gen-event-id)]
    {:event/id      event-id
     :event/last-tx last-tx
     :event/type    :op/atomic
     :event/op      atomic-op}))

