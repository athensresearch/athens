(ns athens.macros-test
  (:require
    [athens.macros :as macros]
    [clojure.test :refer [deftest is] :as t]))


(defn wrap-log
  [body]
  `((let [~'res (do ~@body)]
      (println "result is" ~'res)
      ~'res)))


(defn wrap-log-xform
  [conf _]
  (->> wrap-log
       (partial macros/update-body-body)
       (macros/update-bodies conf)))


(deftest add-trycatch
  (is (= (macros/defn-args-xform wrap-log-xform '(abc [x] x))
         '(abc
            [x]
            (clojure.core/let [res (do x)]
              (clojure.core/println "result is" res)
              res)))
      "Should add a logging block around the fn body."))


(defn test-prepost-xform
  [conf name]
  (let [name-str       (str name)
        prepost-form   `{:pre  [(or (println "pre" ~name-str) true)]
                         :post [(or (println "post" ~name-str) true)]}
        body-update-fn (partial macros/add-prepost prepost-form)]
    (macros/update-bodies conf body-update-fn)))


(deftest add-prepost
  (is (= (macros/defn-args-xform test-prepost-xform '(abc [x] x))
         '(abc
            [x]
            {:pre  [(clojure.core/or (clojure.core/println "pre" "abc") true)],
             :post [(clojure.core/or (clojure.core/println "post" "abc") true)]}
            x))
      "Should add a pre/post block to the fn args."))
