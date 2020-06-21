(ns athens.devcards
  (:require
    [athens.devcards.all-pages]
    [athens.devcards.athena]
    [athens.devcards.block-page]
    [athens.devcards.blocks]
    [athens.devcards.breadcrumbs]
    [athens.devcards.buttons]
    [athens.devcards.db]
    [athens.devcards.db-boxes]
    [athens.devcards.icons]
    [athens.devcards.left-sidebar]
    [athens.devcards.node-page]
    [athens.devcards.parser]
    [athens.devcards.sci-boxes]
    [athens.devcards.style-guide]
    [athens.devcards.styling-with-stylefy]
    [athens.listeners :as listeners]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core]
    [stylefy.core :as stylefy]))


(defn ^:export main
  []
  (stylefy/init)
  (listeners/init)
  (devcards.core/start-devcard-ui!))
