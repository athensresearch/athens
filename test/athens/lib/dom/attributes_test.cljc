(ns athens.lib.dom.attributes-test
  (:require
    [athens.lib.dom.attributes :refer [with-attributes]]
    [clojure.test :refer [deftest are]]))


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
    {:something-else 2}))
