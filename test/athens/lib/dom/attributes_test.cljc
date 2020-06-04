(ns athens.lib.dom.attributes-test
  (:require
    [athens.lib.dom.attributes :refer [with-attributes with-classes with-styles]]
    [clojure.test :refer [deftest is are run-tests]]))


(deftest with-styles-test
  (def flex-style-map {:style {:display "flex"}})

  (are [x] (= (with-styles x) flex-style-map)
    {:display "flex"}
    {:style {:display "flex"}}
    (fn [] {:display "flex"})
    (fn [] {:style {:display "flex"}}))

  (def +justify-center (with-styles {:justify-content "center"}))
  (def +align-center (with-styles {:align-items "center"}))

  (is (= (with-styles flex-style-map +justify-center +align-center)
         {:style {:display "flex" :justify-content "center" :align-items "center"}})
      "Support infinite arity"))


(def +heavily-styled
  (comp
    (with-classes "strong" "happy")
    (with-styles {:color :green
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
