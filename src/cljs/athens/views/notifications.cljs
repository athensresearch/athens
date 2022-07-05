(ns athens.views.notifications
  (:require
   ["/components/Inbox/Inbox" :refer [Inbox]]))


(defn inbox
  []
  [:> Inbox])