(ns athens.common.utils
  "Athens Common Utilities.
  Shared between CLJ and CLJS."
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
     (log/debug ~prefix (- (now-ms) start#) "ms")
     ret#))
