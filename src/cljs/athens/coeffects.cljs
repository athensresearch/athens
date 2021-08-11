(ns athens.coeffects
  (:require
    [athens.util :as util]
    [re-frame.core :as rf]))


(rf/reg-cofx
  :local-storage
  (fn [coeffects k]
    (assoc coeffects :local-storage (util/local-storage-get (str k)))))

