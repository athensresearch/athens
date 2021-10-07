(defproject athens "1.0.0-beta.90-SNAPSHOT"

  :description "An open-source knowledege graph for research and notetaking"

  :url "https://github.com/athensresearch/athens"

  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments     "same as Clojure"}

  :plugins [[lein-auto "0.1.3"]
            [lein-shell "0.5.0"]
            [lein-ancient "0.7.0"]
            [cider/cider-nrepl "0.26.0"]
            [lein-tools-deps "0.4.5"]]

  :middleware [lein-tools-deps.plugin/resolve-dependencies-with-deps-edn]

  :lein-tools-deps/config {:config-files [:install :user :project]}

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs" "src/cljc" "src/js" "src/gen"]

  :main athens.self-hosted.core
  :aot [athens.self-hosted.core]
  :uberjar-name "athens-lan-party-standalone.jar"

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :shell {:commands {"open" {:windows ["cmd" "/c" "start"]
                             :macosx  "open"
                             :linux   "xdg-open"}}}

  :aliases {"dev"          ["with-profile" "dev" "do"
                            ["run" "-m" "shadow.cljs.devtools.cli" "watch" "main" "renderer" "app"]]
            "compile-js"   ["with-profile" "dev" "do"
                            ["run" "-m" "shadow.cljs.devtools.cli" "compile" "main" "renderer" "app"]]
            "prod"         ["with-profile" "prod" "do"
                            ["run" "-m" "shadow.cljs.devtools.cli" "release" "main" "renderer" "app"]]
            "build-report" ["with-profile" "prod" "do"
                            ["run" "-m" "shadow.cljs.devtools.cli" "run" "shadow.cljs.build-report" "app" "target/build-report.html"]
                            ["shell" "open" "target/build-report.html"]]
            "test-jvm"     ["test"]
            "test-karma"   ["shell" "karma" "start" "--single-run"]
            "gh-pages"     ["shell" "yarn" "gh-pages" "-d" "resources/public"]
            "karma"        ["do"
                            ["run" "-m" "shadow.cljs.devtools.cli" "compile" "karma-test"]
                            ["shell" "yarn" "run" "karma" "start" "--single-run" "--reporters" "junit,dots"]]
            "cljstyle"     ["with-profile" "+cljstyle" "run" "-m" "cljstyle.main"]}

  :profiles {:dev      {:dependencies [[binaryage/devtools "1.0.3"]
                                       [day8.re-frame/re-frame-10x "1.1.1"]
                                       [day8.re-frame/tracing "0.6.2"]
                                       [day8.re-frame/test "0.1.5"]]
                        :source-paths ["dev/clj"]}
             :prod     {:dependencies [[day8.re-frame/tracing-stubs "0.6.2"]]}
             :uberjar  {:aot :all}
             :cljstyle {:dependencies
                        [[mvxcvi/cljstyle "0.15.0" :exclusions [org.clojure/clojure]]]}}

  :test-selectors {:default (complement :stress)
                   :stress :stress}

  :prep-tasks ["compile"]

  :repl-options {:init-ns user
                 :welcome (println "Welcome to Athens Self-Hosted magical world of the REPL!

To start the server `(dev)` & `(start)`.
To reload server `(reset)`, and to stop `(stop)`")})
