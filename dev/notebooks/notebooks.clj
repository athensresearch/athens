(ns notebooks
  (:require
    [nextjournal.clerk :as clerk]))


(defn -main
  [& _args]
  ;; Show all files that end with _nodebook.clj in src.
  ;; Opens the browser automatically.
  ;; Shows whatever file you last saved.
  ;; See https://github.com/nextjournal/clerk and
  ;; https://github.com/nextjournal/clerk-demo for examples and docs.
  (clerk/serve! {:watch-paths    ["dev/notebooks"]
                 :show-filter-fn (fn [name]
                                   (println "show-filter-fn:" name)
                                   (clojure.string/ends-with? name "_notebook.clj"))
                 :browse?        true})
  (clerk/show! "dev/notebooks/intro_notebook.clj"))
