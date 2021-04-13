(ns athens.coeffects
  (:require
    [re-frame.core :refer [reg-cofx]]))


(reg-cofx
  :local-storage
  (fn [cofx key]
    (assoc cofx :local-storage (js/localStorage.getItem key))))
