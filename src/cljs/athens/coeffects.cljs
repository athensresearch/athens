(ns athens.coeffects
  (:require
    [re-frame.core :as rf]
    [cognitect.transit :as t]))




;; Athens local-storage is stored in a single nested map with key `:athens/persist`
(rf/reg-cofx
  :local-storage
  (fn [coeffects key]
    (let [key (str key)
          value (t/read (t/reader :json) (.getItem js/localStorage key))]
      (assoc coeffects :local-storage value))))


;; Serialize entire db whenever running :persist effect
(rf/reg-fx
  :persist
  (fn [{:keys [athens/persist] :as _app-db}]
    (.setItem js/localStorage :athens/persist (t/write (t/writer :json-verbose) persist))))
