(ns athens.walk-test
  (:require
    [athens.walk :as walk]
    [clojure.test :refer [deftest is are run-tests]]
    [datascript.core :as d]))


(deftest db-test
  (are [x y] (= (walk/walk-string x) y)

    "[[hey]]"
    {:node/titles ["hey"] :page/refs [[:node/title "hey"]]}

    "#hola"
    {:node/titles ["hola"] :page/refs [[:node/title "hola"]]}

             ;; order matters
             ;; ["ma" "ni hao"] != ["ni hao" "ma"]
             ;;"[[ni hao]] #ma"
             ;;{:node/titles ["ma" "ni hao"]}

    "#[[aloha]]"
    {:node/titles ["aloha"] :page/refs [[:node/title "aloha"]]}

    "((uid123))"
    {:block/refs ["uid123"]}))
