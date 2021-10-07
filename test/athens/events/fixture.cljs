(ns athens.events.fixture
  (:require
    [re-frame.core :as rf]))


(defn test-fixtures
  []
  (rf/reg-cofx
    :local-storage
    (fn [cofx key]
      (js/console.debug "local-storage cofx" key)
      (assoc cofx :local-storage ({"theme/dark" true}
                                  key)))))
