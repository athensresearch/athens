(ns notebooks
  (:require
    [nextjournal.clerk :as clerk]))


(defn -main
  [& _args]
  ;; Start by showing the intro notebook.
  (clerk/show! "dev/notebooks/intro_notebook.clj")
  ;; The watch all files in dev/notebooks, and display the last one that changed.
  ;; Opens the browser automatically.
  ;; See https://github.com/nextjournal/clerk and
  ;; https://github.com/nextjournal/clerk-demo for examples and docs.
  (clerk/serve! {:watch-paths    ["dev/notebooks"]
                 :show-filter-fn (fn [name]
                                   (and (clojure.string/starts-with? name "dev/notebooks/")
                                        ;; Ignore this file though.
                                        (not (= name "dev/notebooks/notebooks.clj"))))
                 :browse?        true}))
