(ns athens.coeffects
  (:require
    [re-frame.core :as rf]))


(rf/reg-cofx
  :local-storage/get
  (fn [cofx key]
    (cond
      (keyword? key)
      (assoc cofx :local-storage (-> key
                                     str
                                     js/localStorage.getItem
                                     cljs.reader/read-string))

      (string? key)
      (assoc cofx :local-storage (-> key
                                     js/localStorage.getItem))

      :else (js/alert (js/Error. "local-storage/get failed")))))



(defn ls-key
  [key]
  (str key))


(defn ls-value
  [value]
  (if (string? value)
    value
    (pr-str value)))


;; Key: Expects a keyword or string. Try to pass in keyword, but supporting
;; "key" -> "key" but :ns/key -> ":ns/key"
;; Value
(rf/reg-fx
  :local-storage/set
  (fn [[key value]]
    (let [key (ls-key key)
          value (ls-value value)]
      (if (some? value)
        (.setItem js/localStorage key value)
        ;; Specifying `nil` as value removes the key instead.
        (.removeItem js/localStorage key)))))