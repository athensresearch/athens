(defproject athens "1.0.0-beta.90-SNAPSHOT"

  :description "An open-source knowledege graph for research and notetaking"

  :url "https://github.com/athensresearch/athens"

  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments     "same as Clojure"}

  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/clojurescript "1.10.879"]
                 [thheller/shadow-cljs "2.15.3"]
                 [reagent/reagent "1.0.0"]
                 [re-frame/re-frame "1.2.0"]
                 [datascript/datascript "1.1.0"]
                 [datascript-transit/datascript-transit "0.3.0"]
                 [denistakeda/posh "0.5.8"]
                 [cljs-http/cljs-http "0.1.46"]
                 [day8.re-frame/async-flow-fx "0.2.0"]
                 [metosin/reitit "0.5.13"]
                 [metosin/komponentit "0.3.10"]
                 [instaparse/instaparse "1.4.10"]
                 [borkdude/sci "0.2.5"]
                 [garden/garden "1.3.10"]
                 [stylefy/stylefy "3.0.0"]
                 [stylefy/reagent "3.0.0"]
                 [tick/tick "0.4.26-alpha"]
                 [com.rpl/specter "1.1.3"]
                 [com.taoensso/sente "1.16.2"]
                 [org.flatland/ordered "1.5.9"]
                 ;; backend
                 ;;   logging hell
                 [org.clojure/tools.logging "1.1.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 ;;   IoC
                 [com.stuartsierra/component "1.0.0"]
                 ;;   configuration mgmt
                 [yogthos/config "1.1.7"]
                 ;;   Datahike
                 ;;   TODO: monitor https://github.com/replikativ/datahike/issues/364 and
                 ;;   and uncomment tests that refer to this issue when it is fixed.
                 [io.replikativ/datahike "0.3.7-SNAPSHOT"]
                 ;;   web server
                 [http-kit/http-kit "2.5.3"]
                 [compojure/compojure "1.6.2"]
                 ;;   data validation
                 [metosin/malli "0.5.1"]
                 ;;   networked repl
                 [com.stuartsierra/component.repl "0.2.0"]
                 [nrepl/nrepl "0.8.3"]]

  :plugins [[lein-auto "0.1.3"]
            [lein-shell "0.5.0"]
            [lein-ancient "0.7.0"]
            [cider/cider-nrepl "0.26.0"]]

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
