(ns athens.devcards.devtool
  (:require
    [athens.db :as db :refer [dsdb]]
    [athens.devcards.db :refer [load-real-db-button]]
    [athens.views.buttons :refer [button]]
    [athens.views.devtool :refer [state* handler devtool-prompt-el
                                  devtool-component initial-state eval-box!]]
    [datascript.db]
    [devcards.core :as devcards :refer [defcard-rg]]
    [me.tonsky.persistent-sorted-set]
    [reagent.ratom]
    [shadow.remote.runtime.cljs.browser]))


(defcard-rg Load-Real-DB
  [load-real-db-button dsdb])


(defcard-rg Create-Page
  "Press button and then search \"test\" "
  [button {:on-click handler} "Create Test Pages and Blocks"])


(defcard-rg Reset-to-all-pages
  (fn []
    [button {:on-click #(do (swap! state* assoc :eval-str (:eval-str initial-state))
                            (eval-box!))}
     "Reset"]))


(defcard-rg Devtool-box
  [:<>
   [devtool-prompt-el]
   [devtool-component]])


(comment
  (tap> (deref state*))

  nil)
