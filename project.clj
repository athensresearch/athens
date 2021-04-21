(defproject athens "0.1.0-SNAPSHOT"

  :description "Open-Source Roam"

  :url "https://github.com/athensresearch/athens"

  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.764"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [thheller/shadow-cljs "2.10.22"]
                 [reagent "0.10.0"]
                 [re-frame "1.1.0"]
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
                 [tick "0.4.26-alpha"]
                 [com.rpl/specter "1.1.3"]
                 [com.taoensso/sente "1.16.2"]
                 [datsync "0.0.1-alpha2-SNAPSHOT"]]

  :plugins [[lein-auto "0.1.3"]
            [lein-shell "0.5.0"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs" "src/cljc" "src/js"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :shell {:commands {"open" {:windows ["cmd" "/c" "start"]
                             :macosx  "open"
                             :linux   "xdg-open"}}}

  :aliases {"dev"          ["with-profile" "dev" "do"
                            ["run" "-m" "shadow.cljs.devtools.cli" "watch" "main" "renderer"]]
            "compile"        ["with-profile" "dev" "do"
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
                            ["shell" "yarn" "run" "karma" "start" "--single-run" "--reporters" "junit,dots"]]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "1.0.0"]
                   [day8.re-frame/re-frame-10x "0.6.0"]
                   [day8.re-frame/tracing "0.5.3"]
                   [cider/cider-nrepl "0.25.1"]]

    :source-paths ["dev"]}
   :prod
   {:dependencies [[day8.re-frame/tracing-stubs "0.5.3"]]}}

  :prep-tasks [])
