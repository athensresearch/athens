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


(def test-event
  {:event/id      "test-event-id-1"
   :event/last-tx 0
   :event/type    :some-type})


(def rejection-event
  {:event-id  "test-event-id-1"
   :rejection {:reason "some reason"
               :data   {:more :details}}
   :event     test-event})

