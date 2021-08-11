(ns athens.electron.core
  (:require
    [athens.db :as db]
    [athens.electron.boot]
    [athens.electron.db-picker]
    [athens.electron.fs]
    [athens.electron.window]
    [athens.util :as util]
    [day8.re-frame.async-flow-fx]
    [re-frame.core :as rf]))


;; XXX: most of these operations are effectful. They _should_ be re-written with effects, but feels like too much boilerplate.

(when (util/electron?)

  ;; Subs

  (rf/reg-sub
    :db/mtime
    (fn [db _]
      (:db/mtime db)))


  (rf/reg-event-fx
    :db/retract-athens-pages
    (fn []
      {:dispatch [:transact (concat (db/retract-page-recursively "Welcome")
                                    (db/retract-page-recursively "Changelog"))]})))


