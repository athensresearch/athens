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


;; TODO start using this resolution in handlers
(defmulti resolve-event-to-tx
  "Resolves `:datascript/*` event in context of existing DB into transactions."
  #(:event/type %2))


(defmethod resolve-event-to-tx :datascript/create-page
  [_db {:event/keys [args]}]
  (let [{:keys [uid
                title]} args
        now             (now-ts)
        child-uid       (gen-block-uid)
        child           {:db/id        -2
                         :block/string ""
                         :block/uid    child-uid
                         :block/order  0
                         :block/open   true
                         :create/time  now
                         :edit/time    now}
        page-tx         {:db/id          -1
                         :node/title     title
                         :block/uid      uid
                         :block/children [child]
                         :create/time    now
                         :edit/time      now}]
    [page-tx]))


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
  (let [{:keys [eid
                new-uid]} args
        new-child         {:db/id        -1
                           :block/uid    new-uid
                           :block/string ""
                           :block/order  0
                           :block/open   true}
        reindex           (concat [new-child]
                                  (common-db/inc-after db eid -1))
        new-block         {:db/id          eid
                           :block/children reindex}
        tx-data           [new-block]]
    (println "resolver :datascript/add-child" eid new-uid "=>" (pr-str tx-data))
    tx-data))


(defmethod resolve-event-to-tx :datascript/open-block-add-child
  [db {:event/keys [args]}]
  (let [{:keys [eid
                new-uid]} args
        open-block-tx     [:db/add eid :block/open true]
        ;; delegate add-child-tx creation
        add-child-tx      (resolve-event-to-tx db
                                               {:event/type :datascript/add-child
                                                :event/args args})
        tx-data           (apply conj [open-block-tx] add-child-tx)]
    (println ":datascript/open-block-add-child" eid new-uid)
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
  (let [{:keys [uids
                  blocks]}      args
          first-uid           (first uids)
          n-blocks            (count blocks)
          last-block-order    (:block/order (last blocks))
          {parent-eid :db/id} (common-db/get-parent    db [:block/uid first-uid])
          older-sib           (common-db/get-older-sib db first-uid)
          n-sib               (count (:block/children older-sib))
          new-blocks          (map-indexed
                                (fn [idx x] {:db/id       (:db/id x)
                                             :block-order (+ idx n-sib)})
                                blocks)
          new-older-sib       {:db/id          (:db/id older-sib)
                               :block/children new-blocks
                               :block-open     true}
          reindex             (common-db/minus-after db parent-eid last-block-order n-blocks)
          new-parent          {:db/id          parent-eid
                               :block/children reindex}
          retracts            (mapv (fn [x] [:db/retract     parent-eid
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
  (let [{:keys [uids
                f-uid]}              args
        {parent-order :block/order
         parent-eid   :db/id}        (common-db/get-parent db [:block/uid f-uid])
        sanitized-uids               (map (comp
                                            first
                                            common-db/uid-and-embed-id) uids)
        blocks                       (map #(common-db/get-block db [:block/uid %]) sanitized-uids)
        n-blocks                     (count blocks)
        last-block-order             (:block/order (last blocks))
        reindex-parent               (common-db/minus-after db  parent-eid last-block-order n-blocks)
        new-parent                   {:db/id          parent-eid
                                      :block/children reindex-parent}
        new-blocks                   (map-indexed (fn [idx uid] {:block/uid   uid
                                                                 :block/order (+ idx (inc parent-order))})
                                                  sanitized-uids)
        {grandpa-eid :db/id}          (common-db/get-parent db parent-eid)
        reindex-grandpa               (concat
                                        new-blocks
                                        (common-db/plus-after db grandpa-eid parent-order n-blocks))
        retracts                      (mapv (fn [x] [:db/retract     parent-eid
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
