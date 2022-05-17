(ns athens.electron.core
  (:require
    [athens.electron.boot]
    [athens.electron.db-picker]
    [athens.electron.fs]
    [athens.electron.monitoring.core]
    [athens.electron.window]
    [day8.re-frame.async-flow-fx]
    [re-frame.core :as rf]))


;; XXX: most of these operations are effectful. They _should_ be re-written with effects, but feels like too much boilerplate.

;; Subs

(rf/reg-sub
  :db/mtime
  (fn [db _]
    (:db/mtime db)))


;; This event isn't used at the moment in boot. Figure out if it's still needed next
;; time boot is revisited.
#_(rf/reg-event-fx
    :db/retract-athens-pages
    (fn []
      {:dispatch [:transact (concat (db/retract-page-recursively "Welcome")
                                    (db/retract-page-recursively "Changelog"))]}))
