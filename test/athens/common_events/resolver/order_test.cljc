(ns athens.common-events.resolver.order-test
  (:refer-clojure :exclude [get remove])
  (:require
    [athens.common-events.resolver.order :as order]
    [clojure.test :as t]))


(t/deftest remove
  (t/are [before after x] (= after (order/remove before x))
    [1 2 3] [2 3] 1
    [1 2 3] [1 3] 2
    [1 2 3] [1 2] 3))


(t/deftest get
  (t/are [v x rel target] (= x (order/get v rel target))
    [:x 1 2 3] :x :first nil
    [1 2 3 :x] :x :last nil
    [:x 1 2 3] :x :before 1
    [1 :x 2 3] :x :after 1
    [1 :x 2 3] :x :before 2
    [1 2 :x 3] :x :after 2
    [1 2 :x 3] :x :before 3
    [1 2 3 :x] :x :after 3
    [1 2 3]   nil :after 4
    [1 2 3]   nil :before 4
    [1 2 3]   nil :after 3
    [1 2 3]   nil :before 1
    []        nil :first nil
    []        nil :last nil))


(t/deftest insert
  (t/are [before after x rel target] (= after (order/insert before x rel target))
    [1 2 3] [:x 1 2 3] :x :first nil
    [1 2 3] [1 2 3 :x] :x :last nil
    [1 2 3] [:x 1 2 3] :x :before 1
    [1 2 3] [1 :x 2 3] :x :after 1
    [1 2 3] [1 :x 2 3] :x :before 2
    [1 2 3] [1 2 :x 3] :x :after 2
    [1 2 3] [1 2 :x 3] :x :before 3
    [1 2 3] [1 2 3 :x] :x :after 3
    [1 2 3] [1 2 3] :x :after 4
    [1 2 3] [1 2 3] :x :before 4))


(t/deftest move-within
  (t/are [before after x rel target] (= after (order/move-within before x rel target))
    [1 2 3 :x] [:x 1 2 3] :x :first nil
    [1 :x 2 3] [1 2 3 :x] :x :last nil
    [1 2 3 :x] [1 :x 2 3] :x :before 2
    [1 :x 2 3] [1 2 :x 3] :x :after 2))


(t/deftest reorder
  (t/are [before after res fn] (= res (order/reorder before after fn))
    [:a :b :c] [:a :b :c]
    []
    order/block-map-fn

    [:a :b :c] [:a :c :b]
    [#:block{:uid :c, :order 1}
     #:block{:uid :b, :order 2}]
    order/block-map-fn

    [:a :b :c] [:a :c :b]
    [{:node/title :c, :page/sidebar 1}
     {:node/title :b, :page/sidebar 2}]
    order/shortcut-map-fn


    [1 2 3] (order/insert [1 2 3] :x :first nil)
    [#:block{:uid :x, :order 0}
     #:block{:uid 1, :order 1}
     #:block{:uid 2, :order 2}
     #:block{:uid 3, :order 3}]
    order/block-map-fn

    [1 2 3 :x] (order/move-within [1 :x 2 3] :x :before 2)
    [#:block{:uid :x, :order 1}
     #:block{:uid 2, :order 2}
     #:block{:uid 3, :order 3}]
    order/block-map-fn))
