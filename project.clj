(defproject athens "0.1.0-SNAPSHOT"

  :description "Open-Source Roam"

  :url "https://github.com/athensresearch/athens"

  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.597"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [thheller/shadow-cljs "2.8.83"]
                 [reagent "0.9.1"]
                 [re-frame "0.11.0"]
                 [datascript "1.0.0"]
                 [datascript-transit "0.3.0"]
                 [denistakeda/posh "0.5.8"]
                 [cljs-http "0.1.46"]
                 [day8.re-frame/async-flow-fx "0.1.0"]
                 [metosin/reitit "0.4.2"]
                 [metosin/komponentit "0.3.10"]
                 [instaparse "1.4.10"]
                 [devcards "0.2.6"]
                 [borkdude/sci "0.0.13-alpha.22"]
                 [garden "1.3.10"]
                 [stylefy "2.2.0"]
                 [tick "0.4.26-alpha"]]

  :plugins [[lein-shell "0.5.0"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs" "src/cljc"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]


  :shell {:commands {"open" {:windows ["cmd" "/c" "start"]
                             :macosx  "open"
                             :linux   "xdg-open"}}}

  :aliases {"dev"          ["with-profile" "dev" "do"
                            ["run" "-m" "shadow.cljs.devtools.cli" "watch" "app"]]
            "devcards"     ["with-profile" "dev" "do"
                            ["run" "-m" "shadow.cljs.devtools.cli" "watch" "devcards"]]
            "compile"      ["with-profile" "dev" "do"
                            ["run" "-m" "shadow.cljs.devtools.cli" "compile" "app"]
                            ["run" "-m" "shadow.cljs.devtools.cli" "compile" "devcards"]]
            "prod"         ["with-profile" "prod" "do"
                            ["run" "-m" "shadow.cljs.devtools.cli" "release" "app"]]
            "build-report" ["with-profile" "prod" "do"
                            ["run" "-m" "shadow.cljs.devtools.cli" "run" "shadow.cljs.build-report" "app" "target/build-report.html"]
                            ["shell" "open" "target/build-report.html"]]
            "test-jvm"     ["test"]
            "test-karma"   ["shell" "karma" "start" "--single-run"]
            "gh-pages"     ["shell" "yarn" "gh-pages" "-d" "resources/public"]
            "karma"        ["do"
                            ["run" "-m" "shadow.cljs.devtools.cli" "compile" "karma-test"]
                            ["shell" "karma" "start" "--single-run" "--reporters" "junit,dots"]]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "1.0.0"]
                   [day8.re-frame/re-frame-10x "0.5.1"]
                   [day8.re-frame/tracing "0.5.3"]]
    :source-paths ["dev"]}
   :prod
   {:dependencies [[day8.re-frame/tracing-stubs "0.5.3"]]}}

  :prep-tasks [])
