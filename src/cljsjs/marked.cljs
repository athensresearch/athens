(ns cljsjs.marked
  (:require
    ["marked" :as marked]))


(js/goog.exportSymbol "marked" marked)
