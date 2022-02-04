(ns athens.macros-test
  (:require
    [athens.macros :as macros]
    [clojure.test :refer [deftest is] :as t]))


(defn test-xform
  [conf name]
  (let [name-str       (str name)
        prepost-form   `{:pre  [(or (println "pre" ~name-str) true)]
                         :post [(or (println "post" ~name-str) true)]}
        body-update-fn (partial macros/add-prepost prepost-form)]
    (macros/update-bodies conf body-update-fn)))


(deftest add-prepost
  (is (= (macros/defn-args-xform test-xform '(abc [x] x))
         '(abc
            [x]
            {:pre  [(clojure.core/or (clojure.core/println "pre" "abc") true)],
             :post [(clojure.core/or (clojure.core/println "post" "abc") true)]}
            x))
      "Should add a pre/post block to the fn args."))
