(ns athens-devcards.db
  (:require
    [athens.db]
    [devcards.core :refer-macros [defcard]]))


(defcard datascript-connection athens.db/dsdb)
