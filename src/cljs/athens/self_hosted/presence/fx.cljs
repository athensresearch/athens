(ns athens.self-hosted.presence.fx
  (:require
    [athens.common-events :as common-events]
    [athens.self-hosted.client :as client]
    [re-frame.core :as rf]))


(rf/reg-fx
  :presence/send-update-fx
  (fn [m]
    (client/send! (common-events/build-presence-update-event @(rf/subscribe [:presence/session-id]) m))))
