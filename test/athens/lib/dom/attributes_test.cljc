(ns athens.lib.dom.attributes-test
  (:require
    [athens.lib.dom.attributes :refer [with-attributes with-classes with-style]]
    [clojure.test :refer [deftest is are]]))


(def +heavily-styled
  (comp
    (with-classes "strong" "happy")
    (with-style {:color :green
                 :background :white})))


(deftest attributes-test
  (are [x y z] (= (with-attributes x y) z)

    {:class "foo bar"}
    {:class "baz poo"}
    {:class "foo bar baz poo"}

    {:class "foo bar"}
    {:class ["baz" "poo"]}
    {:class "foo bar baz poo"}

    {:class "foo bar"}
    {:style {:color :green}}
    {:class "foo bar"
     :style {:color :green}}

    {:class "foo bar"}
    {:something-else {:color :green}}
    {:class "foo bar"
     :something-else {:color :green}}

    {:something-else 1}
    {:something-else 2}
    {:something-else 2}

    +heavily-styled
    {:style {:color :red}}
    {:class "strong happy"
     :style {:color :red
             :background :white}}))
