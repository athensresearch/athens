(ns athens.devcards.left-sidebar
  (:require
    [devcards.core :refer [defcard-rg]]
    [posh.reagent :refer [transact!]]))


(defcard-rg Create-Shortcut
  [button-primary {:on-click-fn (fn []
                                  (let [n (:max-eid @db/dsdb)]
                                    (transact! db/dsdb [{:page/sidebar n
                                                         :node/title   (str "Page " n)
                                                         :block/uid    (str "uid" n)}]))) :label "Create Shortcut"}])


(defcard-rg Left-Sidebar
  [:div {:style {:display "flex" :height "60vh"}}
   [left-sidebar]]
  {:padding false})
