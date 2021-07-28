(ns athens.common-events.resolver
  (:require
    [athens.common-db :as common-db]
    [clojure.string :as string]
    #?(:clj  [datahike.api :as d]
       :cljs [datascript.core :as d]))
  #?(:clj
     (:import
       (java.util
         Date
         UUID))))


;; helpers

(defn- now-ts
  []
  #?(:clj  (.getTime (Date.))
     :cljs (.getTime (js/Date.))))


(defn- gen-block-uid
  []
  #?(:clj (subs (.toString (UUID/randomUUID)) 27)
     :cljs (subs (str (random-uuid)) 27)))


(defn between
  "http://blog.jenkster.com/2013/11/clojure-less-than-greater-than-tip.html"
  [s t x]
  (if (< s t)
    (and (< s x) (< x t))
    (and (< t x) (< x s))))


;; TODO start using this resolution in handlers
(defmulti resolve-event-to-tx
  "Resolves `:datascript/*` event in context of existing DB into transactions."
  #(:event/type %2))


(defmethod resolve-event-to-tx :datascript/create-page
  [_db {:event/keys [args]}]
  (let [{:keys [page-uid
                block-uid
                title]} args
        now             (now-ts)
        child           {:db/id        -2
                         :block/string ""
                         :block/uid    block-uid
                         :block/order  0
                         :block/open   true
                         :create/time  now
                         :edit/time    now}
        page-tx         {:db/id          -1
                         :node/title     title
                         :block/uid      page-uid
                         :block/children [child]
                         :create/time    now
                         :edit/time      now}]
    [page-tx]))


(defmethod resolve-event-to-tx :datascript/rename-page
  [db {:event/keys [args]}]
  (let [{:keys [uid
                old-name
                new-name]} args
        linked-refs        (common-db/get-linked-refs-by-page-title db old-name)
        new-linked-refs    (common-db/map-new-refs linked-refs old-name new-name)
        new-page           {:db/id      [:block/uid uid]
                            :node/title new-name}
        new-datoms         (concat [new-page] new-linked-refs)]
    (println ":datascript/rename-page args:" (pr-str args)
             "=>" (pr-str new-datoms))
    new-datoms))


(defmethod resolve-event-to-tx :datascript/merge-page
  [db {:event/keys [args]}]
  (let [{:keys [uid
                old-name
                new-name]}              args
        linked-refs                     (common-db/get-linked-refs-by-page-title db old-name)
        new-linked-refs                 (common-db/map-new-refs linked-refs old-name new-name)
        {old-page-kids :block/children} (common-db/get-page-document db [:block/uid uid])
        new-parent-uid                  (common-db/get-page-uid-by-title db new-name)
        existing-page-block-count       (common-db/existing-block-count db new-name)
        reindex                         (map (fn [{:block/keys [order uid]}]
                                               {:db/id           [:block/uid uid]
                                                :block/order     (+ order existing-page-block-count)
                                                :block/_children [:block/uid new-parent-uid]})
                                             old-page-kids)
        delete-page                     [:db/retractEntity [:block/uid uid]]
        new-datoms                      (concat [delete-page]
                                                new-linked-refs
                                                reindex)]
    (println ":datascript/merge-page args:" (pr-str args)
             "=>" (pr-str new-datoms))
    new-datoms))


(defmethod resolve-event-to-tx :datascript/delete-page
  [db {:event/keys [args]}]
  (let [{uid :uid}         args
        ;; NOTE: common DB query? find page title by page uid?
        title              (ffirst
                             (d/q '[:find ?title
                                    :where
                                    [?e :node/title ?title]
                                    [?e :block/uid ?uid]
                                    :in $ ?uid]
                                  db uid))
        retract-blocks     (common-db/retract-uid-recursively-tx db uid)
        delete-linked-refs (common-db/replace-linked-refs-tx db title)
        tx-data            (concat retract-blocks
                                   delete-linked-refs)]
    (println ":datascript/delete-page" uid title)
    tx-data))


(defmethod resolve-event-to-tx :datascript/block-save
  [_db {:event/keys [args]}]
  (let [{:keys [uid
                new-string]} args
        new-block-string     {:db/id        [:block/uid uid]
                              :block/string new-string}
        tx-data              [new-block-string]]
    (println ":datascript/block-save" (pr-str args)
             "=>" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/new-block
  [db {:event/keys [args]}]
  (let [{:keys [parent-eid
                block-order
                new-uid]} args
        new-block         {:db/id        -1
                           :block/uid    new-uid
                           :block/string ""
                           :block/order  (inc block-order)
                           :block/open   true}
        reindex           (concat [new-block]
                                  (common-db/inc-after db parent-eid block-order))
        tx-data           [{:db/id          parent-eid
                            :block/children reindex}]]
    (println ":datascript/new-block" parent-eid new-uid)
    tx-data))


(defmethod resolve-event-to-tx :datascript/add-child
  [db {:event/keys [args]}]
  (let [{:keys [parent-uid
                new-uid]} args
        new-child         {:db/id        -1
                           :block/uid    new-uid
                           :block/string ""
                           :block/order  0
                           :block/open   true}
        reindex           (concat [new-child]
                                  (common-db/inc-after db [:block/uid parent-uid] -1))
        new-block         {:block/uid      parent-uid
                           :block/children reindex}
        tx-data           [new-block]]
    (println "resolver :datascript/add-child" parent-uid new-uid "=>" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/open-block-add-child
  [db {:event/keys [args]}]
  (let [{:keys [parent-uid
                new-uid]} args
        open-block-tx     [:db/add [:block/uid parent-uid] :block/open true]
        ;; delegate add-child-tx creation
        add-child-tx      (resolve-event-to-tx db
                                               {:event/type :datascript/add-child
                                                :event/args args})
        tx-data           (apply conj [open-block-tx] add-child-tx)]
    (println ":datascript/open-block-add-child" parent-uid new-uid)
    tx-data))


(defmethod resolve-event-to-tx :datascript/split-block
  [db {:event/keys [args]}]
  (println "resolver :datascript/split-block" (pr-str args))
  (let [{:keys [uid
                value
                index
                new-uid]}             args
        parent                        (common-db/get-parent db [:block/uid uid])
        block                         (common-db/get-block db [:block/uid uid])
        {:block/keys [order
                      children
                      open]
         :or         {children []
                      open     true}} block
        head                          (subs value 0 index)
        tail                          (subs value index)
        retracts                      (mapv (fn [child]
                                              [:db/retract (:db/id block) :block/children (:db/id child)])
                                            children)
        next-block                    {:db/id          -1
                                       :block/order    (inc order)
                                       :block/uid      new-uid
                                       :block/open     open
                                       :block/children children
                                       :block/string   tail}
        reindex                       (->> (common-db/inc-after db (:db/id parent) order)
                                           (concat [next-block]))
        new-block                     {:db/id (:db/id block) :block/string head}
        new-parent                    {:db/id (:db/id parent) :block/children reindex}
        tx-data                       (conj retracts new-block new-parent)]
    tx-data))


(defmethod resolve-event-to-tx :datascript/split-block-to-children
  [db {:event/keys [args]}]
  (println "resolver :datascript/split-block-to-children" (pr-str args))
  (let [{:keys [uid
                value
                index
                new-uid]} args
        {:db/keys [id]}   (common-db/get-block db [:block/uid uid])
        head              (subs value 0 index)
        tail              (subs value index)
        new-block         {:db/id        -1
                           :block/order  0
                           :block/uid    new-uid
                           :block/open   true
                           :block/string tail}
        reindex           (concat [new-block]
                                  (common-db/inc-after db id -1))
        tx-data           [{:db/id          id
                            :block/string   head
                            :block/children reindex
                            :edit/time      (now-ts)}]]
    (println "resolver :datascript/split-block-to-children tx-data" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/indent
  [db {:event/keys [args]}]
  (println "resolver :datascript/indent args" (pr-str args))
  (let [{:keys [uid
                value]}            args
        {block-eid :db/id
         block-order :block/order} (common-db/get-block db [:block/uid uid])
        {parent-eid :db/id}        (common-db/get-parent db [:block/uid uid])
        older-sib                  (common-db/get-older-sib db uid)
        new-block                  {:db/id block-eid
                                    :block/order (count (:block/children older-sib))
                                    :block/string value}
        reindex                    (common-db/dec-after db parent-eid block-order)
        retract                    [:db/retract parent-eid
                                    :block/children block-eid]
        new-older-sib              {:db/id (:db/id older-sib)
                                    :block/children [new-block]
                                    :block/open true}
        new-parent                 {:db/id parent-eid :block/children reindex}
        tx-data                    [retract new-older-sib new-parent]]
    (println "resolver :datascript/indent tx-data" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/indent-multi
  [db {:event/keys [args]}]
  (println "resolver :datascript/indent-multi args" (pr-str args))
  (let [{:keys [uids]}      args
        blocks              (map #(common-db/get-block db [:block/uid %]) uids)
        first-uid           (first uids)
        n-blocks            (count blocks)
        last-block-order    (:block/order (last blocks))
        {parent-eid :db/id} (common-db/get-parent    db [:block/uid first-uid])
        older-sib           (common-db/get-older-sib db first-uid)
        n-sib               (count (:block/children older-sib))
        new-blocks          (map-indexed
                              (fn [idx x]
                                {:db/id       (:db/id x)
                                 :block/order (+ idx n-sib)})
                              blocks)
        new-older-sib       {:db/id          (:db/id older-sib)
                             :block/children new-blocks
                             :block/open     true}
        reindex             (common-db/minus-after db parent-eid last-block-order n-blocks)
        new-parent          {:db/id          parent-eid
                             :block/children reindex}
        retracts            (mapv (fn [x]
                                    [:db/retract     parent-eid
                                     :block/children (:db/id x)])
                                  blocks)
        tx-data              (conj retracts
                                   new-older-sib
                                   new-parent)]
    (println "resolver :datascript/indent tx-data" (pr-str args))
    tx-data))


(defmethod resolve-event-to-tx :datascript/unindent
  [db {:event/keys [args]}]
  (println "resolver :datascript/unindent args" (pr-str args))
  (let [{:keys [uid
                value]}             args
        {block-order :block/order}  (common-db/get-block db [:block/uid uid])
        {parent-eid   :db/id
         parent-uid   :block/uid
         parent-order :block/order} (common-db/get-parent db [:block/uid uid])
        {grandpa-eid :db/id}        (common-db/get-parent db [:block/uid parent-uid])
        new-block                   {:block/uid    uid
                                     :block/order  (inc parent-order)
                                     :block/string value}
        reindex-grandpa             (concat [new-block]
                                            (common-db/inc-after db grandpa-eid parent-order))
        reindex-parent              (common-db/dec-after db parent-eid block-order)
        new-parent                  {:db/id          parent-eid
                                     :block/children reindex-parent}
        retract                     [:db/retract parent-eid :block/children [:block/uid uid]]
        new-grandpa                 {:db/id          grandpa-eid
                                     :block/children reindex-grandpa}
        tx-data                     [retract new-parent new-grandpa]]
    (println "resolver :datascript/unindent tx-data" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/unindent-multi
  [db {:event/keys [args]}]
  (println "resolver :datascript/unindent-multi args" args)
  (let [{:keys [uids]}              args
        {parent-order :block/order
         parent-eid   :db/id}        (common-db/get-parent db [:block/uid (first uids)])
        blocks                       (map #(common-db/get-block db [:block/uid %]) uids)
        n-blocks                     (count blocks)
        last-block-order             (:block/order (last blocks))
        reindex-parent               (common-db/minus-after db  parent-eid last-block-order n-blocks)
        new-parent                   {:db/id          parent-eid
                                      :block/children reindex-parent}
        new-blocks                   (map-indexed (fn [idx uid]
                                                    {:block/uid   uid
                                                     :block/order (+ idx (inc parent-order))})
                                                  uids)
        {grandpa-eid :db/id}          (common-db/get-parent db parent-eid)
        reindex-grandpa               (concat
                                        new-blocks
                                        (common-db/plus-after db grandpa-eid parent-order n-blocks))
        retracts                      (mapv (fn [x]
                                              [:db/retract     parent-eid
                                               :block/children (:db/id x)])
                                            blocks)
        new-grandpa                    {:db/id          grandpa-eid
                                        :block/children reindex-grandpa}
        tx-data                        (conj retracts new-parent new-grandpa)]
    (println "resolver :datascript/unindent-multi tx-data" tx-data)
    tx-data))


(defmethod resolve-event-to-tx :datascript/bump-up
  [db {:event/keys [args]}]
  (println "resolver :datascript/bump-up args" (pr-str args))
  (let [{:keys [uid
                new-uid]}          args
        {block-order :block/order} (common-db/get-block db [:block/uid uid])
        {parent-eid :db/id}        (common-db/get-parent db [:block/uid uid])
        new-block                  {:db/id        -1
                                    :block/order  block-order
                                    :block/uid    new-uid
                                    :block/open   true
                                    :block/string ""}
        reindex                    (concat [new-block]
                                           (common-db/inc-after db
                                                                parent-eid
                                                                (dec block-order)))
        tx-data                    [{:db/id          parent-eid
                                     :block/children reindex}]]
    (println "resolver :datascript/bump-up tx-data" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/paste-verbatim
  [_db {:event/keys [args]}]
  (let [{:keys [uid
                text
                start
                value]} args
        block-empty? (string/blank? value)
        block-start? (zero? start)
        new-string   (cond
                       block-empty?       text
                       (and (not block-empty?)
                            block-start?) (str text value)
                       :else              (str (subs value 0 start)
                                               text
                                               (subs value start)))
        tx-data      [{:db/id        [:block/uid uid]
                       :block/string new-string}]]
    tx-data))


(defmethod resolve-event-to-tx :datascript/page-add-shortcut
  [db {:event/keys [args]}]
  (let [{:keys [uid]}        args
        reindex-shortcut-txs (->> (d/q '[:find [(pull ?e [*]) ...]
                                         :where
                                         [?e :page/sidebar _]]
                                       db)
                                  (sort-by :page/sidebar)
                                  (map-indexed (fn [i m] (assoc m :page/sidebar i)))
                                  vec)
        add-shortcut-tx      {:block/uid uid :page/sidebar (or (count reindex-shortcut-txs) 1)}
        tx-data              (conj reindex-shortcut-txs add-shortcut-tx)]
    tx-data))


(defmethod resolve-event-to-tx :datascript/page-remove-shortcut
  [db {:event/keys [args]}]
  (let [{:keys [uid]}        args
        reindex-shortcut-txs (->> (d/q '[:find [(pull ?e [*]) ...]
                                         :where
                                         [?e :page/sidebar _]]
                                       db)
                                  (remove #(= uid (:block/uid %)))
                                  (sort-by :page/sidebar)
                                  (map-indexed (fn [i m] (assoc m :page/sidebar i)))
                                  vec)
        remove-shortcut-tx    [:db/retract [:block/uid uid] :page/sidebar]
        tx-data               (conj reindex-shortcut-txs remove-shortcut-tx)]
    tx-data))


(defmethod resolve-event-to-tx :datascript/drop-child
  [db {:event/keys [args]}]
  (println "resolver :datascript/drop-child args" (pr-str args))
  (let [{:keys [source-uid
                target-uid]}               args
        {target-eid :db/id}                (common-db/get-block  db [:block/uid target-uid])
        {source-block-order :block/order}  (common-db/get-block  db [:block/uid source-uid])
        {source-parent-eid :db/id}         (common-db/get-parent db [:block/uid source-uid])
        new-source-block                   {:block/uid   source-uid
                                            :block/order 0}
        reindex-source-parent              (common-db/dec-after db
                                                                source-parent-eid
                                                                source-block-order)
        reindex-target-parent              (common-db/inc-after db
                                                                target-eid
                                                                -1)
        retract                            [:db/retract     source-parent-eid
                                            :block/children [:block/uid source-uid]]
        new-source-parent                  {:db/id          source-parent-eid
                                            :block/children reindex-source-parent}
        new-target-parent                  {:db/id          target-eid
                                            :block/children (conj reindex-target-parent
                                                                  new-source-block)}
        tx-data                            [retract
                                            new-source-parent
                                            new-target-parent]]
    (println "resolver :datascript/drop-child tx-data" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/drop-multi-child
  [db {:event/keys [args]}]
  ;; Drop multiple selected blocks as child to some other block
  ;; - source-parent: The block from which all the blocks are selected and removed
  ;; - target-parent: The block under which all the blocks are dropped
  ;; - DnD          : Short for dragged and dropped
  ;; After the selected blocks are DnD we need to reindex the remaining children of source-parent
  ;; and target-parent as a result all the children of source-parent have their order decreased
  ;; and the previous children under target-parent have their block order increased
  (println "resolver :datascript/drop-multi-child args" (pr-str args))
  (let [{:keys [source-uids
                target-uid]}                args
        {target-eid :db/id}                 (common-db/get-block  db [:block/uid target-uid])
        source-blocks                       (mapv #(common-db/get-block  db [:block/uid %]) source-uids)
        source-parents                      (mapv #(common-db/get-parent db [:block/uid %]) source-uids)
        {last-source-order :block/order}    (last source-blocks)
        {last-source-parent-uid :block/uid
         last-source-parent-eid :db/id}     (last source-parents)
        new-source-blocks                   (map-indexed (fn [idx x]
                                                           {:block/uid   (:block/uid x)
                                                            :block/order idx})
                                                         source-blocks)
        n                                   (count (filter (fn [x] (= (:block/uid x) last-source-parent-uid))
                                                           source-parents))
        reindex-source-parent               (common-db/minus-after db
                                                                   last-source-parent-eid
                                                                   last-source-order
                                                                   n)
        reindex-target-parent               (common-db/plus-after  db
                                                                   target-eid
                                                                   -1
                                                                   n)
        retracts                            (mapv (fn [uid parent]
                                                    [:db/retract     (:db/id parent)
                                                     :block/children [:block/uid uid]])
                                                  source-uids
                                                  source-parents)
        new-source-parent                   {:db/id          last-source-parent-eid
                                             :block/children reindex-source-parent}
        new-target-parent                   {:db/id          target-eid
                                             :block/children (concat reindex-target-parent
                                                                     new-source-blocks)}
        tx-data                             (conj retracts
                                                  new-source-parent
                                                  new-target-parent)]
    (println "resolver :datascript/drop-multi-child tx-data" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/drop-link-child
  [db {:event/keys [args]}]
  (println "resolver :datascript/drop-link-child args" (pr-str args))
  (let [{:keys [source-uid
                target-uid]}               args
        {target-eid :db/id}                (common-db/get-block  db [:block/uid target-uid])
        new-uid                            (gen-block-uid)
        new-string                         (str "((" source-uid "))")
        new-source-block                   {:block/uid    new-uid
                                            :block/string new-string
                                            :block/order  0
                                            :block/open   true}
        reindex-target-parent              (common-db/inc-after db target-eid -1)
        new-target-parent                  {:db/id          target-eid
                                            :block/children (conj reindex-target-parent
                                                                  new-source-block)}
        tx-data                            [new-target-parent]]
    (println "resolver :datascript/drop-link-child tx-data" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/drop-diff-parent
  [db {:event/keys [args]}]
  ;; Drop selected block under some other block not as a child
  ;; - source-parent: The block from which the block is selected and removed
  ;; - target-parent: The block under which all the blocks are dropped
  ;; - DnD          : Short for dragged and dropped
  ;; After the selected block is DnD we need to reindex the remaining children of source-parent
  ;; and target-parent as a result all the children of source-parent have their order decreased
  ;; and the previous children under target-parent have their block order increased
  ;; drag-target affects the calculation of till which block all the blocks under source-parent or
  ;; target-parent need to be re-indexed.
  (println "resolver :datascript/drop-diff-parent args" (pr-str args))
  (let [{:keys [drag-target
                source-uid
                target-uid]}                args
        {source-block-eid   :db/id
         source-block-order :block/order}   (common-db/get-block  db [:block/uid source-uid])
        {source-parent-eid  :db/id}         (common-db/get-parent db [:block/uid source-uid])
        {target-block-order :block/order}   (common-db/get-block  db [:block/uid target-uid])
        {target-parent-eid  :db/id}         (common-db/get-parent db [:block/uid target-uid])
        new-block                           {:db/id       source-block-eid
                                             :block/order (if (= drag-target :above)
                                                            target-block-order
                                                            (inc target-block-order))}
        reindex-source-parent               (common-db/dec-after db
                                                                 source-parent-eid
                                                                 source-block-order)
        reindex-target-parent               (concat
                                              [new-block]
                                              (common-db/inc-after db
                                                                   target-parent-eid
                                                                   (if (= drag-target :above)
                                                                     (dec target-block-order)
                                                                     target-block-order)))
        retract                             [:db/retract     source-parent-eid
                                             :block/children source-block-eid]
        new-source-parent                   {:db/id          source-parent-eid
                                             :block/children reindex-source-parent}
        new-target-parent                   {:db/id          target-parent-eid
                                             :block/children reindex-target-parent}
        tx-data                             [retract
                                             new-source-parent
                                             new-target-parent]]
    (println "resolver :datascript/drop-diff-parent tx-data" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/drop-link-diff-parent
  [db {:event/keys [args]}]
  (println "resolver :datascript/drop-link-diff-parent args" (pr-str args))
  (let [{:keys [drag-target
                source-uid
                target-uid]}                args
        {target-block-order :block/order}   (common-db/get-block  db [:block/uid target-uid])
        {target-parent-eid  :db/id}         (common-db/get-parent db [:block/uid target-uid])
        new-uid                             (gen-block-uid)
        new-string                          (str "((" source-uid "))")
        new-block                           {:block/uid        new-uid
                                             :block/string     new-string
                                             :block/order      (if (= drag-target :above)
                                                                 target-block-order
                                                                 (inc target-block-order))}
        reindex-target-parent               (concat
                                              [new-block]
                                              (common-db/inc-after db
                                                                   target-parent-eid
                                                                   (if (= drag-target :above)
                                                                     (dec target-block-order)
                                                                     target-block-order)))
        new-target-parent                   {:db/id          target-parent-eid
                                             :block/children reindex-target-parent}
        tx-data                             [new-target-parent]]
    (println "resolver :datascript/drop-link-diff-parent tx-data" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/drop-same
  [db {:event/keys [args]}]
  ;; When a selected block is DnD under the same parent this event is triggered
  ;; - source-parent: The block from which the block is selected and removed
  ;; - DnD          : Short for dragged and dropped
  ;; As the source block is moved under the same parent so we need to reindex all the blocks
  ;; under the source-block's parent. Let's take an example, here a block with some children:
  ;;  -1
  ;;    -2
  ;;    -3
  ;;    -4
  ;;    -5
  ;;    -6
  ;; We can have 2 cases here :
  ;;  - Take the source block and move it to somewhere above its current position
  ;;    for e.g If we take block 5 and move it below block 2, we will have to reindex
  ;;    blocks 3 and 4 in the current setup by increasing their block order after DnD.
  ;;  - Take the source block and move it to somewhere below its current position
  ;;    for e.g If we take block 3 and move it below block 5 we will have to reindex
  ;;    blocks 4 and 5 in the current setup by decreasing their current block order after DnD.

  (println "resolver :datascript/drop-same args" (pr-str args))
  (let [{:keys [drag-target
                source-uid
                target-uid]}              args
        {source-order :block/order
         source-eid   :db/id}             (common-db/get-block db  [:block/uid source-uid])
        {target-block-order :block/order} (common-db/get-block db  [:block/uid target-uid])
        {source-parent-eid :db/id}        (common-db/get-parent db [:block/uid source-uid])
        target-above-source?              (< target-block-order source-order)
        inc-or-dec                        (if target-above-source? + -)
        drag-target-above?                (= drag-target :above)
        drag-target-below?                (= drag-target :below)
        lower-bound                       (cond
                                            (and drag-target-above? target-above-source?) (dec target-block-order)
                                            (and drag-target-below? target-above-source?) target-block-order
                                            :else                                         source-order)
        upper-bound                       (cond
                                            (and drag-target-above? (not target-above-source?)) target-block-order
                                            (and drag-target-below? (not target-above-source?)) (inc target-block-order)
                                            :else                                               source-order)
        reindex                           (common-db/reindex-blocks-between-bounds db
                                                                                   inc-or-dec
                                                                                   source-parent-eid
                                                                                   lower-bound
                                                                                   upper-bound
                                                                                   1)
        new-source-order                  (cond
                                            (and drag-target-above? target-above-source?)       target-block-order
                                            (and drag-target-above? (not target-above-source?)) (dec target-block-order)
                                            (and drag-target-below? target-above-source?)       (inc target-block-order)
                                            (and drag-target-below? (not target-above-source?)) target-block-order)
        new-source-block                  {:db/id       source-eid
                                           :block/order new-source-order}
        new-parent-children               (concat [new-source-block] reindex)
        new-parent                        {:db/id          source-parent-eid
                                           :block/children new-parent-children}
        tx-data                           [new-parent]]
    (println "resolver :datascript/drop-same tx-data" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/drop-multi-same-all
  [db {:event/keys [args]}]
  ;; When multiple blocks are selected under some block and then they are dragged and dropeed under the
  ;; same parent this event is triggered. Working mechanism is the same as `drop-same` event above
  (println "resolver :datascript/drop-multi-same-all args" (pr-str args))
  (let [{:keys [drag-target
                source-uids
                target-uid]}              args
        source-blocks                     (mapv #(common-db/get-block db [:block/uid %]) source-uids)
        first-source-order                (:block/order (first source-blocks))
        last-source-order                 (:block/order (last source-blocks))
        {target-block-order :block/order} (common-db/get-block db [:block/uid target-uid])
        {first-source-parent-eid :db/id}  (common-db/get-parent db [:block/uid (first source-uids)])
        target-above-source?              (< target-block-order first-source-order)
        inc-or-dec                        (if target-above-source? + -)
        drag-target-above?                (= drag-target :above)
        drag-target-below?                (= drag-target :below)
        lower-bound                       (cond
                                            (and drag-target-above? target-above-source?) (dec target-block-order)
                                            (and drag-target-below? target-above-source?) target-block-order
                                            :else                                         last-source-order)
        upper-bound                       (cond
                                            (and drag-target-above? (not target-above-source?)) target-block-order
                                            (and drag-target-below? (not target-above-source?)) (inc target-block-order)
                                            :else                                               first-source-order)
        n                                 (count source-uids)
        reindex                           (common-db/reindex-blocks-between-bounds db
                                                                                   inc-or-dec
                                                                                   first-source-parent-eid
                                                                                   lower-bound
                                                                                   upper-bound
                                                                                   n)
        new-source-blocks                 (if target-above-source?
                                            (map-indexed (fn [idx x]
                                                           (let [new-order (cond-> (+ idx target-block-order)
                                                                                   drag-target-below?
                                                                                   inc)]
                                                             {:db/id       (:db/id x)
                                                              :block/order new-order}))
                                                         source-blocks)
                                            (map-indexed (fn [idx x]
                                                           (let [new-order (cond-> (- target-block-order idx)
                                                                                   drag-target-above?
                                                                                   dec)]
                                                             {:db/id       (:db/id x)
                                                              :block/order new-order}))
                                                         (reverse source-blocks)))
        new-parent-children               (concat new-source-blocks
                                                  reindex)
        new-parent                        {:db/id          first-source-parent-eid
                                           :block/children new-parent-children}
        tx-data                           [new-parent]]
    (println "resolver :datascript/drop-multi-same-all tx-data" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/drop-multi-same-source
  [db {:event/keys [args]}]
  ;; When multiple blocks under the same parent are dragged and dropped under differnt parent
  ;; this event is triggered. Mechanism for this is :
  ;; - Blocks under the source-block's parent are all reindexed in increasing order
  ;; - Blocks under the target-blocks's parent are all reindexed after the target-block in increasing order.
  (println "resolver :datascript/drop-multi-same-source args" (pr-str args))
  (let [{:keys [drag-target
                source-uids
                target-uid]}              args
        {target-block-order :block/order} (common-db/get-block db  [:block/uid target-uid])
        {target-parent-eid :db/id}        (common-db/get-parent db [:block/uid target-uid])
        source-blocks                     (mapv #(common-db/get-block db [:block/uid %]) source-uids)
        {first-source-parent-eid :db/id}  (common-db/get-parent db [:block/uid (first source-uids)])
        {last-source-order :block/order}  (last source-blocks)
        n                                 (count source-uids)
        new-source-blocks                 (map-indexed (fn [idx x]
                                                         (let [new-order (if (= drag-target :above)
                                                                           (+ idx target-block-order)
                                                                           (inc (+ idx target-block-order)))]
                                                           {:db/id       (:db/id x)
                                                            :block/order new-order}))
                                                       source-blocks)
        reindex-source-parent             (common-db/minus-after db
                                                                 first-source-parent-eid
                                                                 last-source-order
                                                                 n)
        bound                             (if (= drag-target :above)
                                            (dec target-block-order)
                                            target-block-order)
        reindex-target-parent             (->> (common-db/plus-after db
                                                                     target-parent-eid
                                                                     bound
                                                                     n)
                                               (concat new-source-blocks))
        retracts                          (map (fn [x] [:db/retract     first-source-parent-eid
                                                        :block/children [:block/uid x]])
                                               source-uids)
        new-source-parent                 {:db/id          first-source-parent-eid
                                           :block/children reindex-source-parent}
        new-target-parent                 {:db/id          target-parent-eid
                                           :block/children reindex-target-parent}
        tx-data                           (conj retracts
                                                new-source-parent
                                                new-target-parent)]
    (println "resolver :datascript/drop-multi-same-source tx-data" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/drop-link-same-parent
  [db {:event/keys [args]}]
  (println "resolver :datascript/drop-link-same-parent args" (pr-str args))
  (let [{:keys [drag-target
                source-uid
                target-uid]}               args
        new-uid                           (gen-block-uid)
        new-string                        (str "((" source-uid "))")
        {target-block-order :block/order} (common-db/get-block db [:block/uid target-uid])
        {source-parent-eid :db/id}        (common-db/get-parent db [:block/uid source-uid])
        {source-order :block/order}       (common-db/get-block db [:block/uid source-uid])
        target-above-source?              (< target-block-order source-order)
        inc-or-dec                        (if target-above-source? + -)
        drag-target-above?                (= drag-target :above)
        drag-target-below?                (= drag-target :below)
        lower-bound                       (cond
                                            (and drag-target-above? target-above-source?) (dec target-block-order)
                                            (and drag-target-below? target-above-source?) target-block-order
                                            :else                                         source-order)
        upper-bound                       (cond
                                            (and drag-target-above? (not target-above-source?)) target-block-order
                                            (and drag-target-below? (not target-above-source?)) (inc target-block-order)
                                            :else                                               source-order)
        reindex                           (common-db/reindex-blocks-between-bounds db
                                                                                   inc-or-dec
                                                                                   source-parent-eid
                                                                                   lower-bound
                                                                                   upper-bound
                                                                                   1)
        new-source-order                  (cond
                                            (and drag-target-above? target-above-source?)       target-block-order
                                            (and drag-target-above? (not target-above-source?)) (dec target-block-order)
                                            (and drag-target-below? target-above-source?)       (inc target-block-order)
                                            (and drag-target-below? (not target-above-source?)) target-block-order)
        new-source-block                  {:block/uid    new-uid
                                           :block/string new-string
                                           :block/order  new-source-order}
        new-parent-children               (concat [new-source-block]
                                                  reindex)
        new-parent                        {:db/id          source-parent-eid
                                           :block/children new-parent-children}
        tx-data                           [new-parent]]
    (println "resolver :datascript/drop-link-same-parent tx-data" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/left-sidebar-drop-above
  [db {:event/keys [args]}]
  (let [{:keys [source-order target-order]}  args
        source-eid  (d/q '[:find ?e .
                           :in $ ?source-order
                           :where [?e :page/sidebar ?source-order]]
                         db source-order)
        new-source  {:db/id source-eid :page/sidebar (if (< source-order target-order)
                                                       (dec target-order)
                                                       target-order)}
        inc-or-dec  (if (< source-order target-order) dec inc)
        tx-data     (->> (d/q '[:find ?shortcut ?new-order
                                :keys db/id page/sidebar
                                :in $ ?source-order ?target-order ?between ?inc-or-dec
                                :where
                                [?shortcut :page/sidebar ?order]
                                [(?between ?source-order ?target-order ?order)]
                                [(?inc-or-dec ?order) ?new-order]]
                              db source-order (if (< source-order target-order)
                                                target-order
                                                (dec target-order))
                              between inc-or-dec)
                         (concat [new-source]))]
    tx-data))


(defmethod resolve-event-to-tx :datascript/left-sidebar-drop-below
  [db {:event/keys [args]}]
  (let [{:keys [source-order target-order]}  args
        source-eid (d/q '[:find ?e .
                          :in $ ?source-order
                          :where [?e :page/sidebar ?source-order]]
                        db source-order)
        new-source {:db/id source-eid :page/sidebar target-order}
        tx-data (->> (d/q '[:find ?shortcut ?new-order
                            :keys db/id page/sidebar
                            :in $ ?source-order ?target-order ?between
                            :where
                            [?shortcut :page/sidebar ?order]
                            [(?between ?source-order ?target-order ?order)]
                            [(dec ?order) ?new-order]]
                          db source-order (inc target-order) between)
                     (concat [new-source]))]
    tx-data))


(defmethod resolve-event-to-tx :datascript/selected-delete
  [db {:event/keys [args]}]
  ;; We know that we only need to dec indices after the last block. The former blocks
  ;; are necessarily going to remove all tail children, meaning we only need to be
  ;; concerned with the last N blocks that are selected, adjacent siblings, to
  ;; determine the minus-after value.
  (println "resolver :datascript/drop-multi-child args" (pr-str args))
  (let [{:keys [uids]}                    args
        selected-sibs-of-last             (->> uids
                                               (d/q '[:find ?sib-uid ?o
                                                      :in $ ?uid [?selected ...]
                                                      :where
                                                      ;; get all siblings of the last block
                                                      [?e :block/uid ?uid]
                                                      [?p :block/children ?e]
                                                      [?p :block/children ?sib]
                                                      [?sib :block/uid ?sib-uid]
                                                      ;; filter selected
                                                      [(= ?sib-uid ?selected)]
                                                      [?sib :block/order ?o]]
                                                    db
                                                    (last uids))
                                               (sort-by second))
        [uid order]                       (last selected-sibs-of-last)
        parent-eid                        (:db/id (common-db/get-parent db [:block/uid uid]))
        n                                 (count selected-sibs-of-last)
        reindex                           (common-db/minus-after db
                                                                 parent-eid
                                                                 order
                                                                 n)
        retracted-vec                     (mapcat #(common-db/retract-uid-recursively-tx db %) uids)
        tx-data                           (concat retracted-vec
                                                  reindex)]
    (println "resolver :selected/delete tx-data is " (pr-str tx-data))
    tx-data))


;; resetting ds-conn re-computes all subs and is the cause
;;    for huge delays when undo/redo is pressed
;; here is another alternative strategy for undo/redo
;; Core ideas are inspired from here https://tonsky.me/blog/datascript-internals/
;;    1. db-before + tx-data = db-after
;;    2. DataScript DB contains only currently relevant datoms.
;; 1 is the math that is happening here when you undo/redo but in a much more performant way
;; 2 asserts that even if you are reapplying same txn over and over only relevant ones will be present
;;    - and overall size of transit file does not increase
;; Note: only relevant txns(ones that user deliberately made, not undo/redo ones) go into history
;; Note: Once session is lost(App is closed) edit history is also lost
;; Also cmd + z -> cmd + z -> edit something --- future(cmd + shift + z) doesn't work
;;    - as there is no logical way to assert the future when past has changed hence history is reset
;;    - very similar to intelli-j edit model or any editor's undo/redo mechanism for that matter
(defmethod resolve-event-to-tx :datascript/undo-redo
  [db-history {:event/keys [args]}]
  (let [{:keys [redo?]}  args
        [tx-m-id datoms] (cond->> @db-history
                           redo? reverse
                           true (some (fn [[tx bool datoms]]
                                        (and ((if redo? not (complement not)) bool) [tx datoms]))))]

    (reset! db-history (->> @db-history
                            (map (fn [[tx-id bool datoms]]
                                   (if (= tx-id tx-m-id)
                                     [tx-id redo? datoms]
                                     [tx-id bool datoms])))
                            doall))

    (cond->> datoms
      (not redo?) reverse
      true (map (fn [datom]
                  (let [[id attr val _txn sig?] #?(:clj datom
                                                   :cljs (vec datom))]
                    [(cond
                       (and sig? (not redo?)) :db/retract
                       (and (not sig?) (not redo?)) :db/add
                       (and sig? redo?) :db/add
                       (and (not sig?) redo?) :db/retract)
                     id attr val])))
      ;; Caveat -- we need a way to signal history watcher if this txn is relevant
      ;;     - send a dummy datom, this will get added to user's data
      ;;     - we can easily filter it out while writing to fs but it will have a perf penalty
      ;;     - Unless we are exporting transit to a common format, this can stay(only one datom -- point 2 mentioned above)
      ;;           - although a filter while exporting is more strategic -- once in a while op, compared to fs write(very frequent)
      true (concat [[:db/add "new" :from-undo-redo true]]))))


(defmethod resolve-event-to-tx :datascript/unlinked-references-link
  [_ {:event/keys [args]}]
  (let [{:keys [uid string title]} args
        ignore-case-title          (re-pattern (str "(?i)" title))
        new-str                    (string/replace string ignore-case-title (str "[[" title "]]"))
        tx-data                    [{:db/id [:block/uid uid] :block/string new-str}]]
    tx-data))


(defmethod resolve-event-to-tx :datascript/unlinked-references-link-all
  [_ {:event/keys [args]}]
  (let [{:keys [unlinked-refs title]} args
        tx-data (mapv
                  (fn [{:block/keys [string uid]}]
                    (let [ignore-case-title (re-pattern (str "(?i)" title))
                          new-str           (string/replace string ignore-case-title (str "[[" title "]]"))]
                      {:db/id [:block/uid uid] :block/string new-str}))
                  unlinked-refs)]
    tx-data))
