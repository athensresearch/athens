(ns athens.common.utils
  "Athens Common Utilities.
  Shared between CLJ and CLJS."
  (:require
    [athens.common-db :as common-db])
  #?(:clj
     (:import
       (java.util
         Date
         UUID))))


(defn now-ts
  []
  #?(:clj  (.getTime (Date.))
     :cljs (.getTime (js/Date.))))


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


(defn find-page-links
  [s]
  (->> (common-db/string->lookup-refs s)
       (filter #(= :node/title (first %)))
       (map second)
       (into #{})))
