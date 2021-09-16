(ns athens.common-events.stress-test
  (:require
    [athens.common-db              :as common-db]
    [athens.common-events          :as common-events]
    [athens.common-events.fixture  :as fixture]
    [athens.common-events.resolver :as resolver]
    [athens.common.utils           :as common.utils]
    [clojure.test                  :as t]
    [datahike.api                  :as d])
  (:import
    (java.util.concurrent
      TimeUnit)))


(defn random-tmp-folder-config
  []
  {:store {:backend :file
           :path    (str "/tmp/example-" (common.utils/gen-block-uid))}})


(t/use-fixtures :each (partial fixture/integration-test-fixture [] (random-tmp-folder-config)))


(defn transact-with-linkmaker
  [tx-data]
  (d/transact @fixture/connection (common-db/linkmaker @@fixture/connection tx-data)))


(defn transact-without-linkmaker
  [tx-data]
  (d/transact @fixture/connection tx-data))


(defn get-block
  [uid]
  (common-db/get-block @@fixture/connection [:block/uid uid]))


(defn used-memory
  []
  (quot (- (.. Runtime getRuntime totalMemory)
           (.. Runtime getRuntime freeMemory))
        (* 1024 1024)))


(defn run-gc
  []
  (.. Runtime getRuntime gc))


(defn sleep
  [n]
  (.. TimeUnit -SECONDS (sleep n)))


(defn remove-enclosing-quotes-and-newline
  [s]
  (subs s 1 (- (count s) 2)))


(defn step
  [iteration f]
  (when (and (not= iteration 0)
             (= (rem iteration 50) 0))
    (println "Sleeping 5 seconds every 50 iterations to allow for GC")
    (sleep 5))
  (run-gc)
  (println "Iteration:" iteration "-"
           "Used memory:" (used-memory) "-"
           (remove-enclosing-quotes-and-newline
             (with-out-str (time
                             ;; ignore printed output from f
                             (with-out-str (f)))))))


(t/deftest ^:stress block-split
  (t/testing "Block split, 100 iterations"
    (let [iterations        100
          source-string     (apply str (repeat iterations "a"))
          testing-block-uid "testing-block-uid"
          testing-page-uid  "testing-page-uid"
          setup-tx          [{:node/title     testing-page-uid
                              :block/uid      "Source Page Title"
                              :block/children [{:block/uid    testing-block-uid
                                                :block/string source-string
                                                :block/order  0}]}]]

      (transact-with-linkmaker setup-tx)

      (dotimes [n iterations]
        (step n (fn []
                  (let [{curr-string :block/string} (get-block testing-block-uid)
                        split-block-event           (common-events/build-split-block-event -1
                                                                                           testing-block-uid
                                                                                           curr-string
                                                                                           ;; split at the last char
                                                                                           (dec (count curr-string))
                                                                                           (common.utils/gen-block-uid))
                        split-block-tx              (resolver/resolve-event-to-tx @@fixture/connection split-block-event)]

                    (transact-with-linkmaker split-block-tx))))))))


(t/deftest ^:stress basic
  (t/testing "Insert block structure, 100 iterations, no common events usage"
    (let [iterations 100]
      (dotimes [n iterations]
        (step n (fn []
                  (let [tx [{:db/id -1
                             :block/order 1
                             :block/uid (common.utils/gen-block-uid)
                             :block/open true
                             :block/children []
                             :block/string "random string"}]]
                    (transact-without-linkmaker tx))))))))


(comment
  (t/test-vars [#'athens.common-events.stress-test/block-split])
  (t/test-vars [#'athens.common-events.stress-test/basic]))
