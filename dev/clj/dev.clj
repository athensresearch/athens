#_{:clj-kondo/ignore [:unused-referred-var :unused-namespace]}


(ns dev
  (:require
    [athens.self-hosted.core         :as app]
    [com.stuartsierra.component.repl :as repl :refer [reset start stop system]]
    [datascript.core                 :as d]))


(defn- local-new-system
  [_]
  (app/new-system))


(repl/set-init local-new-system)


(defn datascript-conn
  "Gets you Datascript connection from system."
  []
  (get-in system [:datascript :conn]))


(comment
  (d/q '[:find ?eid
         :keys db/id
         :where [?eid :block/order ?block-order]
                (not [?eid :block/uid])]
       @(datascript-conn)))
