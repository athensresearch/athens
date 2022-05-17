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
         Date))))


(defn now-ts
  []
  #?(:clj  (.getTime (Date.))
     :cljs (.getTime (js/Date.))))


(defn now-ms
  []
  #?(:clj  (/ (.getNano (LocalDateTime/now)) 1000000)
     :cljs (js/performance.now)))


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
     (log/info ~prefix (double (- (now-ms) start#)) "ms")
     ret#))


(defmacro parses-to
  [parser & tests]
  `(t/are [in# out#] (= out# (do
                               (println in#)
                               (time (~parser in#))))
     ~@tests))


(defn spy
  "Pretty print and return x.
  Useful for debugging and logging."
  [x]
  (pprint/pprint x)
  x)
