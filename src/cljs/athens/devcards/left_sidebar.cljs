(ns athens.devcards.left-sidebar
  (:require
    [athens.db :as db]
    [athens.dbrx :refer [transact!]]
    [athens.views.buttons :refer [button]]
    [athens.views.left-sidebar :refer [left-sidebar]]
    [devcards.core :refer [defcard-rg]]))


(defcard-rg Create-Shortcut
  [button {:on-click (fn []
                       (let [n (:max-eid @db/dsdb)]
                         (transact! [{:page/sidebar n
                                      :node/title   (str "Page " n)
                                      :block/uid    (str "uid" n)}])))} "Create Shortcut"])


(defcard-rg Left-Sidebar
  [:div {:style {:display "flex" :height "60vh"}}
   [left-sidebar]]
  {:padding false})
