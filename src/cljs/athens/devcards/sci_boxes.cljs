(ns athens.devcards.sci-boxes
  (:require
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as str]
    [devcards.core :as devcards :refer [defcard defcard-rg]]
    [reagent.core :as rg]
    [sci.core :as sci]))


(def log js/console.log)


(defn trace
  [x]
  (log x) x)


(defcard "
  # An experiment in connecting mini SCI environments

  Let's say you could put executable code in Athens' blocks.

  Some questions:
   - In what order do we evaluate our blocks?
   - How do we pass data in and out of our blocks?
   - How do we handle async code?

  Attempted approach:
   - Blocks are passed the evaluated result of their parent (`*1`)

   Some other approaches:
   - Blocks inherit the environment of their parent
   - Blocks mutate a global environment
   - Blocks are babashka pods?

  Fun stuff to try:
   - Pass in the datascript connection
   - `spit`/`slurp` to IPFS etc.
  ")


(defcard sci
  "## Small Clojure Interpreter
   https://github.com/borkdude/sci")


(defn remove-from-vec
  "Returns a new vector with the element at 'index' removed.

  (remove-from-vec [:a :b :c] 1)  =>  [:a :c]"
  [v index]
  (vec (concat (subvec v 0 index) (subvec v (inc index)))))


(defn index-of
  [col val]
  (first (keep-indexed (fn [idx x]
                         (when (= x val)
                           idx))
                       col)))


(defcard sci-examples
  (for [[s opts]
        [["(inc 1)"]
         ["x" {:bindings {'x 1}}]
         ["{:hiccup [:span \"Hello\"]}"]
         ["(def a 1)"]
         [":a"]
         ["(require '[lib]) lib/msg" {:namespaces {'lib {'msg "hi"}}}]]]
    (merge {:s s :result (sci/eval-string s opts)}
           (when opts
             {:opts opts}))))


(def key-code->key
  {8   :backspace
   9   :tab
   13  :return
   57  :left-paren
   219 :left-brace})


(def empty-box
  {:str-content ""
   :children-ids []})


(defcard "
  ## Experiment #1
   - A tree of boxes
   - If a box's `:str-content` begins with `:sci`,
     evaluate the rest of the string with SCI and assign it to `:result`
   - Child boxes are passed their parent's `:result` as `*1`, like a REPL
   - Every time a box's content changes, naively re-evaluate the whole tree top to bottom!
   - If a box's `:result` is a map with a `hiccup` key, render it after the box

  ENTER key makes a new sibling (if not root)

  SHIFT-ENTER to make a new line

  BACKSPACE in an empty box deletes it
  ")


(defonce box-state*
  (rg/atom {:next-id 4
            :boxes {0 (merge empty-box {:children-ids [1 3]
                                        :str-content ":sci {:message \"🌻\" :size 70}"})
                    1 (merge empty-box {:children-ids [2]
                                        :str-content ":sci (merge *1 {:hiccup [:div {:style {:font-size (:size *1)}} (:message *1)]})"})

                    2 (merge empty-box {:str-content "I am just a 🍃"})
                    3 (merge empty-box {:str-content ":sci (:message *1)"})}}))


(defcard box-state* box-state*)


(defn get-parent-id
  [boxes child-id]
  (some (fn [[id box]]
          (when (some #{child-id} (:children-ids box))
            id))
        boxes))


(defn sci-node?
  [{:keys [str-content]}]
  (str/starts-with? str-content ":sci"))


(defn eval-box
  [{:keys [str-content] :as box} parent]
  (if-not (sci-node? box)
    box
    (let [code (subs str-content 4)
          result (try
                   (sci/eval-string code {:bindings {'*1 (:result parent)}})
                   (catch js/Error e
                     (trace e)))]
      (assoc box :result result))))


;; very naive depth-first search, probably buggy
(defn next-box-id
  [boxes visited id]
  (if (not (visited id))
    id
    (let [go-up #(when-let [parent-id (get-parent-id boxes id)]
                   (next-box-id boxes visited parent-id))]
      (if-let [children (-> boxes (get id) :children-ids seq)]
        (if-let [unvisited-child (some #(when (not (visited %))
                                          %)
                                       children)]
          unvisited-child
          (go-up))
        (let [parent (get-parent-id boxes id)
              siblings (:children-ids parent)]
          (if-let [unvisited-sibling (some #(when (not (visited %))
                                              %)
                                           siblings)]
            unvisited-sibling
            (go-up)))))))


(defn eval-all-boxes
  [boxes]
  (loop [boxes boxes
         visited #{}
         id 0]
    (let [box (get boxes id)
          parent (get boxes (get-parent-id boxes id))
          boxes' (assoc boxes id (eval-box box parent))
          visited' (conj visited id)
          id' (next-box-id boxes visited' id)]
      (if-not id'
        boxes'
        (recur boxes' visited' id')))))


(defn add-child
  [{:keys [children-ids] :as box} idx id]
  (let [new-idx (inc idx)]
    (assoc box :children-ids (apply conj
                                    (subvec children-ids 0 new-idx)
                                    id
                                    (subvec children-ids new-idx)))))


(defn remove-child
  [parent child-id]
  (let [idx (index-of (:children-ids parent) child-id)]
    (update parent :children-ids remove-from-vec idx)))


(defn add-sibling
  [{:keys [next-id boxes] :as state} id]
  (let [parent-id (get-parent-id boxes id)
        siblings (get-in boxes [parent-id :children-ids])
        idx (index-of siblings id)]
    (-> state
        (update :next-id inc)
        (update :boxes update parent-id add-child idx next-id)
        (update :boxes assoc next-id empty-box)
        (update :boxes eval-all-boxes))))


(defn delete-box
  [{:keys [boxes] :as state} id]
  (let [parent-id (get-parent-id boxes id)]
    (-> state
        (update-in [:boxes parent-id] remove-child id)
        (update :boxes dissoc id)
        (update :boxes eval-all-boxes))))


(defn update-box-content
  [boxes id value]
  (update boxes id assoc :str-content value))


(defn handle-return-key!
  [e id]
  (.preventDefault e)
  (swap! box-state* add-sibling id))


(defn handle-backspace-key!
  [e id]
  (let [{:keys [str-content]} (get-in @box-state* [:boxes id])]
    (when (empty? str-content)
      (.preventDefault e)
      (swap! box-state* delete-box id))))


(defn handle-box-key-down!
  [e id]
  (let [key-code (.-keyCode e)
        shift? (.-shiftKey e)
        k (key-code->key key-code)]
    (case k
      :return (when (not shift?)
                (handle-return-key! e id))
      :backspace (handle-backspace-key! e id)
      nil)))


(defn handle-box-change!
  [e id]
  (let [target (.-target e)
        value (.-value target)]
    (swap! box-state*
           #(-> %
                (update :boxes update-box-content id value)
                (update :boxes eval-all-boxes)))))


(defn sci-result-component
  [result]
  (when result
    (let [{:keys [hiccup]} result]
      (if hiccup
        hiccup
        (str result)))))


;; resulting :hiccup could be malformed, catch errors & allow retry
(defn sci-result-wrapper
  []
  (let [err* (rg/atom nil)]
    (rg/create-class
      {:component-did-catch (fn [err info]
                              (reset! err* [err info]))
       :reagent-render (fn [result]
                         (if (nil? @err*)
                           [sci-result-component result]
                           (let [[_ info] @err*]
                             [:div
                              [:code (str info)]
                              [:div
                               [:button {:on-click #(reset! err* nil)}
                                "re-render"]]])))})))


(defn box-component
  [id]
  (let [{:keys [boxes]} @box-state*
        {:keys [str-content children-ids result] :as box} (get boxes id)]
    [:div
     [:div {:style {:display "flex"}}
      id
      [:textarea {:style {:font-size "1rem"
                          :width "30rem"}
                  :value str-content
                  :on-change #(handle-box-change! % id)
                  :on-key-down #(handle-box-key-down! % id)}]
      (when (sci-node? box)
        [sci-result-wrapper result])]
     (when (seq children-ids)
       (into [:div {:style {:margin-left "1rem"}}]
             (for [id children-ids]
               [box-component id])))]))


(defcard-rg boxes
  (do
    (swap! box-state* update :boxes eval-all-boxes)
    [box-component 0]))
