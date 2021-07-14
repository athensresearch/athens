#_{:clj-kondo/ignore [:unused-referred-var :unused-namespace]}


(ns dev
  (:require
    [athens.self-hosted.core         :as app]
    [com.stuartsierra.component.repl :as repl :refer [reset start stop system]]
    [datahike.api                    :as d]))


(defn- local-new-system
  [_]
  (app/new-system))


(repl/set-init local-new-system)


(defn- datahike-conn
  "Gets you Datahike connection from system."
  []
  (get-in system [:datahike :conn]))
