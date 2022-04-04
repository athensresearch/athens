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
          electron-build      {:electron-build-version (util/athens-version)}
          super-properties    (merge graph-type electron-build)
          js-super-properties (clj->js super-properties)]
      (.. js/posthog (register js-super-properties)))))


(rf/reg-fx
  :posthog/capture-event!
  (fn [{:keys [event-name opts-map]}]
    (when-not (js/posthog.has_opted_out_capturing)
      (js/posthog.capture event-name (clj->js opts-map)))))


(rf/reg-event-fx
  :posthog/report-feature
  (fn [{:keys [_db]} [_ feature on?]]
    {:posthog/capture-event! {:event-name (str "feature/" (name feature))
                              :opts-map   {feature on?}}}))


(rf/reg-event-fx
  :reporting/navigation
  (fn [{:keys [_db]} [_ {:keys [source target pane]}]]
    {:posthog/capture-event! {:event-name "feature/navigate"
                              :opts-map   {:source source
                                           :target target
                                           :pane   pane}}}))


(rf/reg-event-fx
  :reporting/page.create
  (fn [{:keys [_db]} [_ {:keys [source count]}]]
    {:posthog/capture-event! {:event-name "feature/page.create"
                              :opts-map   {:source        source
                                           :pages-created count}}}))


(rf/reg-event-fx
  :reporting/block.create
  (fn [{:keys [_db]} [_ {:keys [source count]}]]
    {:posthog/capture-event! {:event-name "feature/block.create"
                              :opts-map   {:source         source
                                           :blocks-created count}}}))


(rf/reg-event-fx
  :reporting/page.link
  (fn [{:keys [_db]} [_ {:keys [source count]}]]
    {:posthog/capture-event! {:event-name "feature/page.link"
                              :opts-map   {:source        source
                                           :links-created count}}}))


(rf/reg-event-fx
  :reporting/block.link
  (fn [{:keys [_db]} [_ {:keys [source count]}]]
    {:posthog/capture-event! {:event-name "feature/block.link"
                              :opts-map   {:source        source
                                           :links-created count}}}))


(defn build-reporting-link-creation
  [added source]
  (let [{:keys [page-link block-ref]} (reduce (fn [agg [link-type _]]
                                                (update agg link-type (fnil inc 0)))
                                              {}
                                              added)]
    (cond-> []
      (pos? page-link)
      (conj [:reporting/page.link {:source source
                                   :count  page-link}])
      (pos? block-ref)
      (conj [:reporting/block.link {:source source
                                    :count  block-ref}]))))
