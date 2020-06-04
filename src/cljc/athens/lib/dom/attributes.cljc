(ns athens.lib.dom.attributes
  (:require
    [clojure.string]))


(defn merge-dom-classes
  [attrs dom-classes]
  (let [class-str (if (string? dom-classes)
                    dom-classes
                    (clojure.string/join " " dom-classes))]
    (update attrs :class (fn [s]
                           (if s
                             (str s " " class-str)
                             class-str)))))


(defn with-classes
  [& dom-classes]
  (fn f
    ([] (f nil))
    ([attrs]
     (merge-dom-classes attrs dom-classes))))


(defn merge-styles
  [attrs styles]
  (update attrs :style merge styles))


(defn with-styles
  ([map-or-fn]
   (if (fn? map-or-fn)
     (with-styles (map-or-fn))
     (if (:style map-or-fn)
       map-or-fn
       {:style map-or-fn})))
  ([map-or-fn & more]
   (reduce (fn [acc x]
             (update acc :style merge (:style (with-styles x))))
           (with-styles map-or-fn)
           more)))


(defn with-attributes
  [& attributes]
  (reduce (fn [acc map-or-fn]
            (cond
              (fn? map-or-fn)
              (map-or-fn acc)

              (map? map-or-fn)
              (reduce-kv (fn [acc0 attribute v]
                           (case attribute
                             :style (merge-styles      acc0 v)
                             :class (merge-dom-classes acc0 v)

                             ;; Potentially override whatever is there
                             ;;  (e.g. we cannot merge on-change handlers)
                             (assoc acc0 attribute v)))
                         acc
                         map-or-fn)

              :else
              (throw (ex-info (str "Expected map or function") {:value map-or-fn}))))
          {} attributes))


(comment

  (with-attributes
    {:class "foo bar"}
    {:class "baz poo"}
    {:style {:color :red}}
    )
  )
