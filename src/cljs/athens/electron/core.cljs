(ns athens.electron.core
  (:require
    [athens.athens-datoms :as athens-datoms]
    [athens.db :as db]
    [athens.electron.boot]
    [athens.electron.db-picker]
    [athens.electron.fs]
    [athens.electron.window]
    [athens.util :as util]
    [cljs.reader :refer [read-string]]
    [day8.re-frame.async-flow-fx]
    [re-frame.core :as rf]))


;; XXX: most of these operations are effectful. They _should_ be re-written with effects, but feels like too much boilerplate.

(when (util/electron?)

  (def electron (js/require "electron"))
  (def remote (.. electron -remote))


  (def fs (js/require "fs"))
  (def path (js/require "path"))


  ;; Subs

  (rf/reg-sub
    :db/mtime
    (fn [db _]
      (:db/mtime db)))


  (rf/reg-sub
    :db/filepath
    (fn [db _]
      (:db/filepath db)))


  (rf/reg-sub
    :db/filepath-dir
    (fn [db _]
      (.dirname path (:db/filepath db))))



  (rf/reg-event-fx
    :local-storage/navigate
    [(rf/inject-cofx :local-storage "current-route/uid")]
    (fn [{:keys [local-storage]} _]
      {:dispatch [:navigate {:page {:id local-storage}}]}))




  (rf/reg-event-fx
    :db/retract-athens-pages
    (fn []
      {:dispatch [:transact (concat (db/retract-page-recursively "Welcome")
                                    (db/retract-page-recursively "Changelog"))]}))


  (rf/reg-event-fx
    :db/transact-athens-pages
    (fn []
      {:dispatch [:transact athens-datoms/datoms]})))


