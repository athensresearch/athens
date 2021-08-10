(ns athens.coeffects
  (:require
    [re-frame.core :as rf]
    [athens.util :as util]))


(rf/reg-cofx
  :local-storage
  (fn [coeffects k]
    (assoc coeffects :local-storage (util/local-storage-get (str k)))))

