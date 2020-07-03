(ns athens.util
  (:require
    [clojure.string :as str]
    [tick.alpha.api :as t]
    [tick.locale-en-us]))


(defn gen-block-uid
  []
  (subs (str (random-uuid)) 27))


(defn now-ts
  []
  (-> (js/Date.) .getTime))


(def US-format (t/formatter "MM-dd-yyyy"))


(def title-format (t/formatter "LLLL dd, yyyy"))


(defn date-string
  [ts]
  (if (< ts 1) ;; TODO why this predicate?
    [:span "(unknown date)"]
    (as-> (js/Date. ts) x
      (t/instant x)
      (t/date-time x)
      (t/format (t/formatter "LLLL MM, yyyy h':'ma") x)
      (str/replace x #"AM" "am")
      (str/replace x #"PM" "pm"))))


(defn get-day
  "Returns today's date or a date OFFSET days before today"
  ([] (get-day 0))
  ([offset]
   (let [day (t/-
               (t/date-time)
               (t/new-duration offset :days))]
     {:uid   (t/format US-format day)
      :title (t/format title-format day)})))

