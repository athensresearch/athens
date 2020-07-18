(ns athens.devcards.athena
  (:require
    [athens.db :as db]
    [athens.devcards.db :refer [load-real-db-button]]
    [athens.subs]
    [athens.views.athena :refer [athena-prompt-el athena-component]]
    [athens.views.buttons :refer [button]]
    [datascript.core :as d]
    [devcards.core :refer-macros [defcard-rg]]))


(defcard-rg Create-Page
  "Press button and then search \"test\" "
  [button {:on-click (fn []
                       (let [n       (inc (:max-eid @db/dsdb))
                             n-child (inc n)]
                         (d/transact! db/dsdb [{:node/title     (str "Test Page " n)
                                                :block/uid      (str "uid-" n)
                                                :block/children [{:block/string (str "Test Block" n-child) :block/uid (str "uid-" n-child)}]}])))} "Create Test Pages and Blocks"])


(defcard-rg Load-Real-DB
  [load-real-db-button])


(defcard-rg Athena-Prompt
  [:<>
   [athena-prompt-el]
   [athena-component]])
