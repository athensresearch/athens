(ns athens.devcards.blocks
  (:require
    [athens.views.core :refer [block-component]]
    [devcards.core :refer-macros [defcard-rg]]))


(defcard-rg Block
  "Pull entity 2347, a block within Athens FAQ, and its children. Doesn't pull parents, unlike `block-page`"
  [block-component 2347])
