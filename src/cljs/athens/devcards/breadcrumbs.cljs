(ns athens.devcards.breadcrumbs
  (:require
    [athens.views.breadcrumbs :refer [breadcrumbs-list breadcrumb]]
    [devcards.core :refer-macros [defcard-rg]]))


(defcard-rg Normal-Breadcrumb
  [breadcrumbs-list {}
   [breadcrumb {:key 0} "Athens"]
   [breadcrumb {:key 1} "Components"]
   [breadcrumb {:key 2} "Breadcrumbs"]])


(defcard-rg One-Item
  [breadcrumbs-list {}
   [breadcrumb {:key 2} "Athens"]])


(defcard-rg Breadcrumb-with-many-items
  [breadcrumbs-list {}
   [breadcrumb {:key "a"} "Lorem"]
   [breadcrumb {:key "b"} "Ipsum"]
   [breadcrumb {:key "c"} "Laudantium"]
   [breadcrumb {:key "d"} "Accusamus"]
   [breadcrumb {:key "e"} "Reprehenderit"]
   [breadcrumb {:key "f"} "Aliquam"]
   [breadcrumb {:key "g"} "Corrupti"]
   [breadcrumb {:key "h"} "Omnis"]
   [breadcrumb {:key "i"} "Quis"]
   [breadcrumb {:key "a"} "Lorem"]
   [breadcrumb {:key "b"} "Ipsum"]
   [breadcrumb {:key "c"} "Laudantium"]
   [breadcrumb {:key "d"} "Accusamus"]
   [breadcrumb {:key "e"} "Reprehenderit"]
   [breadcrumb {:key "f"} "Aliquam"]
   [breadcrumb {:key "g"} "Corrupti"]
   [breadcrumb {:key "h"} "Omnis"]
   [breadcrumb {:key "i"} "Quis"]
   [breadcrumb {:key "a"} "Lorem"]
   [breadcrumb {:key "b"} "Ipsum"]
   [breadcrumb {:key "c"} "Laudantium"]
   [breadcrumb {:key "d"} "Accusamus"]
   [breadcrumb {:key "e"} "Reprehenderit"]
   [breadcrumb {:key "f"} "Aliquam"]
   [breadcrumb {:key "g"} "Corrupti"]
   [breadcrumb {:key "h"} "Omnis"]
   [breadcrumb {:key "i"} "Quis"]
   [breadcrumb {:key "a"} "Lorem"]
   [breadcrumb {:key "b"} "Ipsum"]
   [breadcrumb {:key "c"} "Laudantium"]
   [breadcrumb {:key "d"} "Accusamus"]
   [breadcrumb {:key "e"} "Reprehenderit"]
   [breadcrumb {:key "f"} "Aliquam"]
   [breadcrumb {:key "g"} "Corrupti"]
   [breadcrumb {:key "h"} "Omnis"]
   [breadcrumb {:key "i"} "Quis"]
   [breadcrumb {:key "j"} "Necessitatibus"]])


(defcard-rg Breadcrumb-with-long-items
  [breadcrumbs-list {}
   [breadcrumb {:key 0} "Exercitationem qui dicta officia aut alias eum asperiores voluptates exercitationem"]
   [breadcrumb {:key 1} "Sapiente ad quia sunt libero"]
   [breadcrumb {:key 2} "Accusantium veritatis placeat quaerat unde odio officia"]])


(defcard-rg Breadcrumb-with-one-long-item-at-start
  [breadcrumbs-list {}
   [breadcrumb {:key 0} "Voluptates exercitationem dicta officia aut alias eum asperiores voluptates exercitationem"]
   [breadcrumb {:key 2} "Libero"]
   [breadcrumb {:key 3} "Unde"]])


(defcard-rg Breadcrumb-with-one-long-item-at-end
  [breadcrumbs-list {}
   [breadcrumb {:key 0} "Unde"]
   [breadcrumb {:key 1} "Libero"]
   [breadcrumb {:key 2} "Exercitationem qui dicta officia aut alias eum asperiores voluptates exercitationem dicta officia aut alias eum asperiores voluptates exercitationem"]])
