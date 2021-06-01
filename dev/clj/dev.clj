#_{:clj-kondo/ignore [:unused-referred-var]}


(ns dev
  (:require
    [athens.self-hosted.core         :as app]
    [com.stuartsierra.component.repl :as repl :refer [reset start stop system]]))


(defn- local-new-system
  [_]
  (app/new-system))


(repl/set-init local-new-system)
