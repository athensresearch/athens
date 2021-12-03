(ns athens.notebooks
  (:require
    [nextjournal.clerk :as clerk]))


(defn -main
  [& _args]
  ;; Show all files that end with _nodebook.clj in src.
  ;; Opens the browser automatically.
  ;; Shows whatever file you last saved.
  ;; See https://github.com/nextjournal/clerk and
  ;; https://github.com/nextjournal/clerk-demo for examples and docs.
  (clerk/serve! {:watch-paths ["src"]
                 :show-filter-fn #(clojure.string/ends-with? % "_notebook.clj")
                 :browse? true}))
