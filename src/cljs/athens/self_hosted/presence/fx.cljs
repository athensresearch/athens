(ns athens.self-hosted.presence.fx
  (:require
    [athens.common-events :as common-events]
    [athens.self-hosted.client :as client]
    [re-frame.core :as rf]))


(rf/reg-fx
  :presence/send-editing
  (fn [uid]
    (client/send! (common-events/build-presence-editing-event 42 @(rf/subscribe [:username]) uid))))


(rf/reg-fx
  :presence/send-username!
  (fn [[current-username new-username]]
    (client/send! (common-events/build-presence-username-event 42 current-username new-username))))
