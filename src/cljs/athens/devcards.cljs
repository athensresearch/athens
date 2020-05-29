(ns athens.devcards
  (:require
    [athens.devcards.db]
    [athens.devcards.sci-boxes]
    [athens.devcards.block]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :as devcards :include-macros true :refer [defcard]]
    [reagent.core :as r :include-macros true]))

(defcard hello-world
  "DevCards API and examples: http://rigsomelight.com/devcards/")

(defn ^:export main
  []
  (devcards.core/start-devcard-ui!))
