(ns user
  (:require [athens.self-hosted.core      :as app]
            [clojure.tools.namespace.repl :as repl]
            [com.stuartsierra.component   :as component]))

(def system nil)

(defn init []
  (alter-var-root #'system
                  (constantly (app/new-system))))

(defn start []
  (alter-var-root #'system component/start))

(defn stop []
  (alter-var-root #'system
                  (fn [s] (when s (component/stop s)))))

(defn go []
  (init)
  (start))

(defn reset []
  (stop)
  (repl/refresh :after 'user/go))
