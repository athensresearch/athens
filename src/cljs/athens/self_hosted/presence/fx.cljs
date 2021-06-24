(ns athens.self-hosted.presence.fx
  (:require [athens.db :as db]
            [athens.self-hosted.client :as client]
            [athens.common-events :as common-events]
            [re-frame.core :as rf]))


(rf/reg-fx
  :presence/send-editing
  (fn [uid]
    (client/send! (common-events/build-presence-editing-event 42
                                                              (:name @(rf/subscribe [:user]))
                                                              uid))))

