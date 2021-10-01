;; COPY OF UNRELEASED CHANGES FROM https://github.com/clj-commons/ordered/pull/59#issuecomment-932299803
;; TODO: REMOVE IN FAVOR OF USING GIT DEPS OR A REAL RELEASE
(ns flatland.ordered.map)

(declare equiv-impl)

(defn print-ordered-map [writer kvs ks opts]
  (pr-sequential-writer
   writer
   (fn [k w opts]
     (-write w \[)
     (-write w (pr-str k))
     (-write w \space)
     (-write w (pr-str (get kvs k)))
     (-write w \]))
   "(" " " ")"
   opts
   ks))

(deftype OrderedMap [kvs ks]
  Object
  (toString [this] (pr-str* this))
  (equiv [this that] (equiv-impl kvs that))

  ;; js/map interface
  (keys [this] (es6-iterator ks))
  (entries [this] (es6-entries-iterator (seq kvs)))
  (values [this] (es6-iterator (vals kvs)))
  (has [this k] (not (nil? (.get kvs k))))
  (get [this k] (.get kvs k))
  (forEach [this f]
    (doseq [k ks]
      (f k (get kvs k) this)))
  (forEach [this f use-as-this]
    (doseq [k ks]
      (.call f use-as-this k (get kvs k) this)))

  ;; js fallbacks
  (key_set   [this] (to-array (keys kvs)))
  (entry_set [this] (to-array (map to-array kvs)))
  (value_set [this] (to-array (map val kvs)))

  ICloneable
  (-clone [_] (OrderedMap. kvs ks))

  ;; IIterable
  ;; (-iterator [_] )

  IWithMeta
  (-with-meta [this new-meta]
    (if (identical? (meta kvs) new-meta)
      this
      (OrderedMap. (with-meta kvs new-meta) ks)))

  IMeta
  (-meta [this] (meta kvs))

  ICollection
  (-conj [coll entry]
    (if (vector? entry)
      (OrderedMap. (conj kvs entry) (if (contains? kvs (-nth entry 0))
                                      ks
                                      (conj ks (-nth entry 0))))
      (OrderedMap. (conj kvs entry) (into ks
                                          (comp (map #(-nth % 0))
                                                (remove #(contains? kvs %)))
                                          entry))))

  IEmptyableCollection
  (-empty [this]
    (if (seq ks)
      (OrderedMap. (-empty kvs) [])
      this))

  IEquiv
  (-equiv [this that] (equiv-impl kvs that))

  IHash
  (-hash [_] (hash kvs))

  ISeqable
  (-seq [this]
    (when (seq ks)
      (map #(-find kvs %) ks)))

  IReversible
  (-rseq [this]
    (when (seq ks)
      (map #(-find kvs %) (rseq ks))))

  ICounted
  (-count [this] (count kvs))

  ILookup
  (-lookup [this attr]           (-lookup kvs attr))
  (-lookup [this attr not-found] (-lookup kvs attr not-found))

  IAssociative
  (-assoc [coll k v]
    (OrderedMap. (assoc kvs k v) (if (contains? kvs k)
                                   ks
                                   (conj ks k))))
  (-contains-key? [this k]
    (contains? kvs k))

  IFind
  (-find [this k]
    (-find kvs k))

  IMap
  (-dissoc [this k]
    (if (contains? kvs k)
      (OrderedMap. (dissoc kvs k) (into [] (remove #{k}) ks))
      this))

  IKVReduce
  (-kv-reduce [coll f init]
    (reduce
     (fn [acc k]
       (f acc k (get kvs k)))
     init
     ks))

  IFn
  (-invoke [this k] (kvs k))
  (-invoke [this k not-found] (kvs k not-found))

  IPrintWithWriter
  (-pr-writer [_ writer opts]
    (-write writer "#ordered/map ")
    (print-ordered-map writer kvs ks opts)))

(defn equiv-impl [kvs that]
  (= kvs (if (instance? OrderedMap that)
           (.-kvs that)
           that)))

(def ^:private empty-ordered-map (OrderedMap. {} []))

(defn ordered-map
  ([]
   empty-ordered-map)
  ([coll]
   (into empty-ordered-map coll))
  ([k v & kvs]
   (apply assoc empty-ordered-map k v kvs)))
