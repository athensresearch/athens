(ns athens.cljs-parser-test)


(defmacro parses-to
  [parser & tests]
  `(t/are [in# out#] (= out# (do
                               (println in#)
                               (time (~parser in#))))
     ~@tests))
