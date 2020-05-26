(ns athens.devcards.db
  (:require
    [athens.db]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard]]))


(defcard datascript-connection athens.db/dsdb)
