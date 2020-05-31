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


(defn with-style
  [styles]
  (fn f
    ([] (f nil))
    ([attrs] (merge-styles attrs styles))))


(comment

  ;; Combine with-classes and with-style
  (def +heavily-styled
    (comp
     (with-classes "strong" "happy")
     (with-style {:color :green})))

  ;; Usage:


  [:h1 (+heavily-styled) "some statement"]

  [:h1 (+heavily-styled {:on-click (fn [_e] (js/alert "something else"))}) "some statement"]

  )


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
              (throw (ex-info (str "Expected map or function, got " (class map-or-fn))
                              {:attributes attributes}))))
          {} attributes))


(comment

  (def +heavily-styled
    (comp
     (with-classes "strong" "happy")
     (with-style {:color :green})))


  (def +heavily-styled
    (comp
     (with-classes "strong" "happy")
     (with-style {:color :green})))


  (with-attributes
    +heavily-styled
    {:class "foo bar"}
    {:class "baz poo"} ;;
    )
  )
