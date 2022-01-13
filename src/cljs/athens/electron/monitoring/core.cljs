(ns athens.electron.monitoring.core
  (:require
    [athens.electron.db-picker :as db-picker]
    [athens.electron.utils :as utils]
    [athens.util :as util]
    [re-frame.core :as rf]))


;; https://posthog.com/docs/integrate/client/js#super-properties
(rf/reg-event-fx
  :posthog/set-super-properties
  (fn [{:keys [db]} _]
    (let [selected-db         (db-picker/selected-db db)
          remote-db?          (utils/remote-db? selected-db)
          graph-type          {:graph-type (if remote-db?
                                             :self-hosted
                                             :local)}
          electron-build      {:electron-build (util/athens-version)}
          super-properties    (merge graph-type electron-build)
          js-super-properties (clj->js super-properties)]
      (.. js/posthog (register js-super-properties)))))