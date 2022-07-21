(ns athens.views.notifications
  (:require
    ["/components/inbox/Inbox" :refer [Inbox]]))


(defn inbox
  []
  [:> Inbox])
