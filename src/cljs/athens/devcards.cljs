(ns athens.devcards
  (:require
    [athens.devcards.db]
    [athens.devcards.sci-boxes]
    [athens.devcards.style-guide]
    [athens.devcards.table]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core]))


(defn ^:export main
  []
  (devcards.core/start-devcard-ui!))
