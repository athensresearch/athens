(ns athens.coeffects
  (:require
    [re-frame.core :as rf]))


(rf/reg-cofx
  :local-storage/get
  (fn [cofx key]
    (assoc cofx :local-storage (or
                                 (js/localStorage.getItem (str key))
                                 (js/localStorage.getItem key)))))


(rf/reg-fx
  :local-storage/set
  (fn [[key value]]
    (let [key (str key)]
      (if (some? value)
        (.setItem js/localStorage key #_(t/write (t/writer :json-verbose) value))
        ;; Specifying `nil` as value removes the key instead.
        (.removeItem js/localStorage key)))))