(defproject athens "0.1.0-SNAPSHOT"

  :description "Open-Source Roam"

  :url "https://github.com/athensresearch/athens"

  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments     "same as Clojure"}

  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/clojurescript "1.10.866"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [thheller/shadow-cljs "2.14.0"]
                 [reagent "1.0.0"]
                 [re-frame "1.2.0"]
                 [datascript "1.1.0"]
                 [datascript-transit "0.3.0"]
                 [denistakeda/posh "0.5.8"]
                 [cljs-http "0.1.46"]
                 [day8.re-frame/async-flow-fx "0.2.0"]
                 [metosin/reitit "0.5.13"]
                 [metosin/komponentit "0.3.10"]
                 [instaparse "1.4.10"]
                 [devcards "0.2.7"]
                 [borkdude/sci "0.2.5"]
                 [garden "1.3.10"]
                 [stylefy "3.0.0"]
                 [stylefy/reagent "3.0.0"]
                 [tick "0.4.26-alpha"]
                 [com.rpl/specter "1.1.3"]
                 [com.taoensso/sente "1.16.2"]
                 [datsync "0.0.1-alpha2-SNAPSHOT"]
                 ;; backend
                 ;;   logging hell
                 [org.clojure/tools.logging "1.1.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 ;;   IoC
                 [com.stuartsierra/component "1.0.0"]
                 ;;   configuration mgmt
                 [yogthos/config "1.1.7"]
                 ;;   Datahike
                 [io.replikativ/datahike "0.3.6"]
                 ;;   web server
                 [http-kit "2.5.3"]
                 [compojure "1.6.2"]]

  :plugins [[lein-auto "0.1.3"]
            [lein-shell "0.5.0"]
            [lein-ancient "0.7.0"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs" "src/cljc" "src/js"]

  :main "athens.self-hosted.core"

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :shell {:commands {"open" {:windows ["cmd" "/c" "start"]
                             :macosx  "open"
                             :linux   "xdg-open"}}}

  :aliases {"dev"          ["with-profile" "dev" "do"
                            ["run" "-m" "shadow.cljs.devtools.cli" "watch" "main" "renderer"]]
            "compile"      ["with-profile" "dev" "do"
                            ["run" "-m" "shadow.cljs.devtools.cli" "compile" "main" "renderer"]]
            "devcards"     ["with-profile" "dev" "do"
                            ["run" "-m" "shadow.cljs.devtools.cli" "watch" "devcards"]]
            "prod"         ["with-profile" "prod" "do"
                            ["run" "-m" "shadow.cljs.devtools.cli" "release" "app" "main" "renderer"]]
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

  :profiles {:dev {:dependencies [[binaryage/devtools "1.0.3"]
                                  [day8.re-frame/re-frame-10x "1.1.0"]
                                  [day8.re-frame/tracing "0.6.2"]
                                  [nrepl/nrepl "0.8.3"]]
                   :plugins      [[cider/cider-nrepl "0.26.0"]]
                   :source-paths ["dev/clj"]}
             :prod     {:dependencies [[day8.re-frame/tracing-stubs "0.6.2"]]}
             :cljstyle {:dependencies [[mvxcvi/cljstyle "0.15.0" :exclusions [org.clojure/clojure]]]}}

  :prep-tasks [])
