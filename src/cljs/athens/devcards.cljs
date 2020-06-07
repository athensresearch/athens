(ns athens.devcards
  (:require
   [athens.devcards.all-pages]
   [athens.devcards.athena]
   [athens.devcards.buttons]
   [athens.devcards.db]
   [athens.devcards.db-boxes]
   [athens.devcards.icons]
   [athens.devcards.left-sidebar]
   [athens.devcards.sci-boxes]
   [athens.devcards.style-guide]
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core]
   [stylefy.core :as stylefy]))


(defn ^:export main
  []
  (stylefy/init)
  (devcards.core/start-devcard-ui!))
