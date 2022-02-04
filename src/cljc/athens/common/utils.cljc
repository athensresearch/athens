(ns athens.common.utils
  "Athens Common Utilities.
  Shared between CLJ and CLJS."
  (:require
    [clojure.pprint :as pprint])
  #?(:cljs
     (:require-macros
       [athens.common.utils]))
  #?(:clj
     (:import
       (java.time
         LocalDateTime)
       (java.util
         Date
         UUID))))


(defn now-ts
  []
  #?(:clj  (.getTime (Date.))
     :cljs (.getTime (js/Date.))))


(defn now-ms
  []
  #?(:clj  (/ (.getNano (LocalDateTime/now)) 1000000)
     :cljs (js/performance.now)))


#?(:clj
   (defn random-uuid
     "CLJ shim for CLJS `random-uuid`."
     []
     (UUID/randomUUID)))


(defn uuid->string
  "Useful for printing and to get around how cljs transit uuids are not uuids
  (see https://github.com/cognitect/transit-cljs/issues/41).
  It would be less characters to just type `str` instead of this fn, especially
  with a namespace, but forgetting to convert uuids at all is a common error,
  so having a dedicated fn helps us keep it in mind."
  [uuid]
  (str uuid))


(defn gen-block-uid
  "Generates new `:block/uid`."
  []
  (subs (str (random-uuid)) 27))


(defn gen-event-id
  []
  (random-uuid))


(defmacro log-time
  [prefix expr]
  `(let [start# (now-ms)
         ret# ~expr]
     (log/info ~prefix (- (now-ms) start#) "ms")
     ret#))


(defmacro parses-to
  [parser & tests]
  `(t/are [in# out#] (= out# (do
                               (println in#)
                               (time (~parser in#))))
     ~@tests))


;; Resources on lazy clojure ops.
;; https://clojuredocs.org/clojure.core/lazy-seq
;; https://clojuredocs.org/clojure.core/lazy-cat
;; http://clojure-doc.org/articles/language/laziness.html
;; https://stackoverflow.com/a/44102122/2116927
;; The lazy-* fns aren't explicitly used here, but all fns used here are lazy,
;; so the end result is lazy as well.
(defn range-mapcat-while
  "Returns a lazy concatenation of (f i), where i starts at 0 and increases by 1 each iteration.
   Continues while (stop? (f i)) is false."
  [f stop?]
  ;; Concat the result,
  (apply concat (->> (range)
                     (map f)
                     (take-while (complement stop?)))))


(defn spy
  "Pretty-print x and return it.
  Useful for debugging."
  [x]
  (pprint/pprint x)
  x)
