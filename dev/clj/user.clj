(ns user
  (:require
    [athens.self-hosted.core      :as app]
    [clojure.tools.namespace.repl :as repl]
    [com.stuartsierra.component   :as component]))


(defn init
  []
  (alter-var-root #'app/system
                  (constantly (app/new-system))))


(defn start
  []
  (alter-var-root #'app/system component/start))


(defn stop
  []
  (alter-var-root #'app/system
                  (fn [s] (when s (component/stop s)))))


(defn go
  []
  (init)
  (start))


(defn reset
  []
  (stop)
  (repl/refresh :after 'user/go))
