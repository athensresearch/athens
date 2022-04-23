(ns athens.dates
  (:require
    [cljc.java-time.local-date :as local-date]
    [clojure.string :as string]
    [tick.core :as t]
    [tick.locale-en-us]))


(def date-col-format (t/formatter "LLLL dd, yyyy h':'mma"))
(def US-format (t/formatter "MM-dd-yyyy"))
(def title-format (t/formatter "LLLL dd, yyyy"))


(defn get-day
  "Returns today's date or a date OFFSET days before today"
  ([] (get-day 0))
  ([offset]
   (let [day (t/<<
               (t/date-time)
               (t/new-duration offset :days))]
     {:uid   (t/format US-format day)
      :title (t/format title-format day)}))
  ([date offset]
   (let [day (t/<<
               (-> date (t/at "00:00"))
               (t/new-duration offset :days))]
     {:uid   (t/format US-format day)
      :title (t/format title-format day)})))


(defn date-string
  [ts]
  (if (not ts)
    [:span "(unknown date)"]
    (as->
      (t/instant ts) x
      (t/date-time x)
      (t/format date-col-format x)
      (string/replace x #"AM" "am")
      (string/replace x #"PM" "pm"))))


(defn uid-to-date
  [uid]
  (try
    (let [[m d y] (string/split uid #"-")
          rejoin (string/join "-" [y m d])]
      (t/date rejoin))
    (catch #?(:cljs :default
              :clj Exception) _ nil)))


(defn title-to-date
  [title]
  (try
    (local-date/parse title title-format)
    (catch #?(:cljs :default
              :clj Exception) _ nil)))


(defn date-to-day
  [date]
  (try
    (get-day date 0)
    (catch #?(:cljs :default
              :clj Exception) _ nil)))


(defn is-daily-note
  [uid]
  (boolean (uid-to-date uid)))

