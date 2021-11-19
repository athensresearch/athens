(ns athens.async
  (:require
    [clojure.core.async :refer [go alt! timeout]]))


(defn with-timeout
  "Return first val from ch, or timed-out after ms."
  [ch ms timed-out]
  (go
    (alt!
      ch           ([v] v)
      (timeout ms) timed-out)))
