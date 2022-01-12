(ns athens.electron.monitoring.core
  (:require
     [re-frame.core :as rf]
     [athens.electron.db-picker :as db-picker]
     [athens.electron.utils :as utils]))



(rf/reg-event-fx
  :posthog/set-super-properties
  (fn [{:keys [db]} _]
    (let [selected-db      (db-picker/selected-db db)
          remote-db?       (utils/remote-db? selected-db)
          graph-props      (if remote-db?
                             {:graph {:type :self-hosted}}
                             {:graph {:type :local}})
          super-properties (merge graph-props)]
      (.. js/posthog (register (clj->js super-properties))))))
