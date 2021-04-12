(ns athens.coeffects
  (:require
    [re-frame.core :refer [reg-cofx]]))


(reg-cofx
  :local-storage
  (fn [cofx key]
    (assoc cofx :local-storage (js/localStorage.getItem key))))


(reg-cofx
  :local-storage-map
  (fn [cofx {:keys [ls-key key]}]
    (assoc cofx key (js/localStorage.getItem ls-key))))
