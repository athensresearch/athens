(ns athens.views.notifications.core
  (:require [athens.common-events.bfs :as bfs]
            [athens.common.utils :as common.utils]
            [athens.common-events.graph.composite :as composite]
            [athens.common-db :as common-db]
            [athens.db :as db]
            [athens.common-events.graph.ops :as graph-ops]
            [re-frame.core :as rf]))


(defn new-notification
  [db inbox-block-uid notification-position notification-type message state notification-from-block]
  ;; notification-from-block can be used to show context for the notification
  (->> (bfs/internal-representation->atomic-ops
         db
         [#:block{:uid        (common.utils/gen-block-uid)
                  :string     ""
                  :properties {":block/type"
                               #:block{:string "notification"
                                       :uid    (common.utils/gen-block-uid)}
                               ":notification/type"
                               #:block{:string notification-type
                                       :uid    (common.utils/gen-block-uid)}
                               ":notification/message"
                               #:block{:string message
                                       :uid    (common.utils/gen-block-uid)}
                               ":notification/state"
                               #:block{:string state
                                       :uid    (common.utils/gen-block-uid)}
                               ":notification/from-block"
                               #:block{:string notification-from-block
                                       :uid    (common.utils/gen-block-uid)}}}]
         {:block/uid inbox-block-uid
          :relation  notification-position})
       (composite/make-consequence-op {:op/type :new-type})))


(defn update-notification-state
  [notification-block-uid to-state]
  (rf/dispatch [:properties/update-in [:block/uid notification-block-uid] [":notification/state"]
                       (fn [db prop-uid]
                         [(graph-ops/build-block-save-op db prop-uid to-state)])]))



