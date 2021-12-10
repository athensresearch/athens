(ns athens.common.utils-test
  (:require
    [athens.common.utils                  :as utils]
    [clojure.test                         :as t]))


(t/deftest lazy-cat-while
  (let [call-count (atom 0)
        stopped?   (atom false)
        reset      (fn []
                     (reset! call-count 0)
                     (reset! stopped? false))
        f          (fn [i]
                     (swap! call-count inc)
                     ;; Each call returns 2 elements
                     ;; e.g. 4 -> [5 :-]
                     ;; We increment i to make it easier to
                     ;; reason about take and stop?.
                     [(inc i) :-])
        stop?      (fn [[x]]
                     ;; Stops when the first element of the seq is
                     ;; greater than five.
                     (when (>= x 5)
                       (reset! stopped? true)
                       true))]

    (t/testing "take matches stop point"
      (reset)
      (let [res (take 10 (utils/range-mapcat-while f stop?))]
        ;; 5 :- is not returned because take-while stops there.
        (t/is (= res '(1 :- 2 :- 3 :- 4 :-)))
        ;; But f was still called 5 times.
        (t/is (= @call-count 5))
        ;; stop? was called too.
        (t/is @stopped?)))

    (t/testing "take more than stop point"
      (reset)
      (let [res (take 20 (utils/range-mapcat-while f stop?))]
        ;; Same results as taking up to stop point.
        (t/is (= res '(1 :- 2 :- 3 :- 4 :-)))
        (t/is (= @call-count 5))
        (t/is @stopped?)))

    (t/testing "take less than stop point"
      (reset)
      (let [res (take 1 (utils/range-mapcat-while f stop?))]
        ;; Only got the very first element.
        (t/is (= res '(1)))
        ;; I expected this to be just 1, but I guess there's
        ;; some eagerness or batching in realizing the first few elements.
        (t/is (= @call-count 4))
        ;; Never got to the point where stop? was called.
        ;; This is the really important part.
        (t/is (not @stopped?))))))

