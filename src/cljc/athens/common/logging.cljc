(ns athens.common.logging
  "Athens logging for both clj & cljs."
  #?(:clj
     (:require
       [clojure.tools.logging :as log])))


#?(:clj (defmacro error
          [& args]
          `(log/error ~@args))
   :cljs (defn error
           [& args]
           (apply js/console.error args)))


#?(:clj (defmacro warn
          [& args]
          `(log/warn ~@args))
   :cljs (defn warn
           [& args]
           (apply js/console.warn args)))


#?(:clj (defmacro info
          [& args]
          `(log/info ~@args))
   :cljs (defn info
           [& args]
           (apply js/console.log args)))


#?(:clj (defmacro debug
          [& args]
          `(log/debug ~@args))
   :cljs (defn debug
           [& args]
           (apply js/console.debug args)))
