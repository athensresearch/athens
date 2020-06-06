(ns athens.devcards
  (:require
    [athens.devcards.all-pages]
    [athens.devcards.athena]
    [athens.devcards.buttons]
    [athens.devcards.db]
    [athens.devcards.icons]
    [athens.devcards.left-sidebar]
    [athens.devcards.sci-boxes]
    [athens.devcards.style-guide]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core]))


(defn ^:export main
  []
  (devcards.core/start-devcard-ui!))
