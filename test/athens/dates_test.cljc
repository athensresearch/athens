(ns athens.dates-test
  (:require
    [athens.dates :as dates]
    [clojure.test :as t]))


(t/deftest uid-to-date
  (t/is (= (-> "10-22-2021" dates/uid-to-date (dates/get-day 0))
           {:uid "10-22-2021", :title "October 22, 2021"}))
  (t/is (nil? (dates/uid-to-date "bork"))))


(t/deftest title-to-date
  (t/is (= (-> "October 22, 2021" dates/title-to-date (dates/get-day 0))
           {:uid "10-22-2021", :title "October 22, 2021"}))
  (t/is (nil? (dates/title-to-date "bork"))))


(t/deftest is-daily-note
  (t/is (dates/is-daily-note "01-01-1990"))
  (t/is (dates/is-daily-note "10-22-2021"))
  (t/is (not (dates/is-daily-note "1-1-2021")))
  (t/is (not (dates/is-daily-note "bork bork"))))

