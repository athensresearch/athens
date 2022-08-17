(ns athens.views.blocks.internal-representation
  (:refer-clojure :exclude [descendants])
  (:require
    [athens.common-db :as common-db]
    [athens.common.utils :as utils]
    [athens.parser  :as parser]
    [clojure.set  :as set]
    [clojure.string :as str]
    [clojure.walk :as walk]
    [datascript.core :as d]))


(defn descendants
  [{:block/keys [children properties]}]
  (concat children (vals properties)))


(defn new-uids-map
  "From Athens representation, extract the uids and create a mapping to new uids."
  [tree]
  (let [all-old-uids (mapcat #(->> %
                                   (tree-seq common-db/has-descendants? descendants)
                                   (mapv :block/uid))
                             tree)
        mapped-uids (reduce #(assoc %1 %2 (utils/gen-block-uid)) {} all-old-uids)] ; Replace with zipmap
    mapped-uids))


(defn string->block-lookups
  "Given string s, compute the set of block refs and block embeds."
  [s]
  (let [ast (parser/parse-to-ast s)
        block-ref-str->uid #(common-db/strip-markup % "((" "))")
        block-lookups (into #{}
                            (map (fn [uid] uid))
                            (common-db/extract-tag-values ast
                                                          #{:block-ref :component}
                                                          identity
                                                          #(let [arg (second %)]
                                                             (cond
                                                               ;; If it is a uid
                                                               (:from arg)
                                                               [:uid (block-ref-str->uid (:from arg))]

                                                               ;; If it is a block embed
                                                               (= "[[embed]]:"
                                                                  (first (str/split arg #" ")))
                                                               [:embed (block-ref-str->uid (last (str/split arg #" ")))]))))]
    (set/union block-lookups)))


(defn wrap-uid-in-pattern
  [pattern-type uid]
  (if (= :embed pattern-type)
    (str "{{[[embed]]: ((" uid "))}}")
    (str "((" uid "))")))


(defn update-strings-with-new-uids
  "Takes a string of text and parses it for block refs, block embeds using regex. Then replace the matched pattern
   with new refs.

   Could also use block ref information from the db instead (refs->uids->replacements).
   Just something to keep in mind for the future if this gets hard to maintain.

   Pattern: Strings should not have a space before, after or in between the block uid
            In the following example no pattern is valid:
            (()) (( uid)) ((uid )) (( uid )) ((Uid with space))

            To understand the regex pattern like lookback etc. checkout this link: https://stackoverflow.com/questions/2973436/regex-lookahead-lookbehind-and-atomic-groups
   "
  [block-string mapped-uids]
  (let [parsed-uids     (string->block-lookups block-string)
        replaced-string (reduce (fn [block-string ref]
                                  (let [embed?       (= :embed
                                                        (first ref))
                                        uid          (last ref)
                                        current-ref  (if embed?
                                                       (wrap-uid-in-pattern :embed uid)
                                                       (wrap-uid-in-pattern :uid   uid))
                                        new-uid      (get mapped-uids uid nil)
                                        replace-with (cond
                                                       (and embed? new-uid)       (wrap-uid-in-pattern :embed new-uid)
                                                       (and (not embed?) new-uid) (wrap-uid-in-pattern :uid new-uid)
                                                       :else                      current-ref)]
                                    (if new-uid
                                      (str/replace block-string
                                                   current-ref
                                                   replace-with)
                                      block-string)))
                                block-string
                                parsed-uids)]
    replaced-string))


(defn walk-tree-to-replace
  "Walk the internal representation and replace specific key-value pairs. This is inspired from the
  `walk/postwalk-replace` implementation."
  [tree mapped-uids replace-keyword]
  (walk/postwalk (fn [x]
                   (if (and (vector? x)
                            (= (first x) replace-keyword))
                     (cond
                       (= replace-keyword :block/uid)    [:block/uid    (mapped-uids (last x))]
                       (= replace-keyword :block/string) [:block/string (update-strings-with-new-uids (last x)
                                                                                                      mapped-uids)])
                     x))
                 tree))


(defn update-uids
  "In the internal representation replace the uids and block-strings with new uids."
  [tree mapped-uids]
  (let [block-uids-replaced          (walk-tree-to-replace tree
                                                           mapped-uids
                                                           :block/uid)
        blocks-with-replaced-strings (walk-tree-to-replace block-uids-replaced
                                                           mapped-uids
                                                           :block/string)]
    blocks-with-replaced-strings))


(defn text-to-blocks
  [text uid root-order]
  (let [;; Split raw text by line
        lines       (->> (clojure.string/split-lines text)
                         (filter (comp not clojure.string/blank?)))
        ;; Count left offset
        left-counts (->> lines
                         (map #(re-find #"^\s*(-|\*)?" %))
                         (map #(-> % first count)))
        ;; Trim * - and whitespace
        sanitize    (map (fn [x] (clojure.string/replace x #"^\s*(-|\*)?\s*" ""))
                         lines)
        ;; Generate blocks with tempids
        blocks      (map-indexed (fn [idx x]
                                   {:db/id        (dec (* -1 idx))
                                    :block/string x
                                    :block/open   true
                                    :block/uid    (utils/gen-block-uid)}) ; TODO(BUG): UID generation during resolution
                                 sanitize)
        top_uids    []
        ;; Count blocks
        n           (count blocks)
        ;; Assign parents
        parents     (loop [i   1
                           res [(first blocks)]]
                      (if (= n i)
                        res
                        ;; Nested loop: worst-case O(n^2)
                        (recur (inc i)
                               (loop [j (dec i)]
                                 ;; If j is negative, that means the loop has been compared to every previous line,
                                 ;; and there are no previous lines with smaller left-offsets, which means block i
                                 ;; should be a root block.
                                 ;; Otherwise, block i's parent is the first block with a smaller left-offset
                                 (if (neg? j)
                                   (do
                                     (conj top_uids (nth blocks i))
                                     (conj res (nth blocks i)))
                                   (let [curr-count (nth left-counts i)
                                         prev-count (nth left-counts j nil)]
                                     (if (< prev-count curr-count)
                                       (conj res {:db/id          (:db/id (nth blocks j))
                                                  :block/children (nth blocks i)})
                                       (recur (dec j)))))))))
        ;; assign orders for children. order can be local or based on outer context where paste originated
        ;; if local, look at order within group. if outer, use root-order
        tx-data     (->> (group-by :db/id parents)
                         ;; maps smaller than size 8 are ordered, larger are not https://stackoverflow.com/a/15500064
                         (into (sorted-map-by >))
                         (mapcat (fn [[_tempid blocks]]
                                   (loop [order 0
                                          res   []
                                          data  blocks]
                                     (let [{:block/keys [children] :as block} (first data)]
                                       (cond
                                         (nil? block) res
                                         (nil? children) (let [new-res (conj res {:db/id          [:block/uid uid]
                                                                                  :block/children (assoc block :block/order @root-order)})]
                                                           (swap! root-order inc)
                                                           (recur order
                                                                  new-res
                                                                  (next data)))
                                         :else (recur (inc order)
                                                      (conj res (assoc-in block [:block/children :block/order] order))
                                                      (next data))))))))]
    (into [] tx-data)))


(defn text-to-internal-representation
  [text]
  (let [cpdb                  (common-db/create-conn)
        copy-paste-block      [{:db/id          -1
                                :block/uid      "copy-paste-uid"
                                :block/children []
                                :block/string   "Block for copy paste"}]
        tx-data               (text-to-blocks text
                                              "copy-paste-uid"
                                              (atom 0))]
    ;; transact first block
    (d/transact! cpdb copy-paste-block)

    ;; transact the copied blocks
    (d/transact! cpdb tx-data)

    ;; get the internal representation 
    ;; we need the eid of the copy-paste-block because that is where all the blocks are added to
    ;; all the copied data will be added as the children of the `copy-paste-block`
    (:block/children (common-db/get-internal-representation @cpdb
                                                            (:db/id (common-db/get-block @cpdb [:block/uid "copy-paste-uid"]))))))
