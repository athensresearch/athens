(ns athens.views.onboarding
  (:require
    ["/components/Onboarding/ProductDemoModal" :refer [ProductDemoModal]]
    [re-frame.core :as rf :refer [subscribe dispatch reg-event-db reg-event-fx reg-sub]]
    [reagent.core :as r]))


;; Subscriptions
;; =============

(reg-sub
  :onboarding/open?
  (fn [db _]
    (boolean (:onboarding/open? db))))


(reg-sub
  :onboarding/step
  (fn [db _]
    (or (:onboarding/step db) 0)))


(reg-sub
  :onboarding/completed?
  (fn [db _]
    (boolean (get-in db [:athens/persist :settings :onboarding/completed?] false))))


;; Events
;; ======

(reg-event-db
  :onboarding/open
  (fn [db [_ step]]
    (assoc db :onboarding/open? true :onboarding/step (or step 0))))


(reg-event-db
  :onboarding/close
  (fn [db _]
    (assoc db :onboarding/open? false)))


(reg-event-db
  :onboarding/toggle
  (fn [db _]
    (update db :onboarding/open? not)))


(reg-event-db
  :onboarding/set-step
  (fn [db [_ step]]
    (assoc db :onboarding/step (or step 0))))


(reg-event-fx
  :onboarding/complete
  (fn [{:keys [db]} _]
    {:db (-> db
             (assoc :onboarding/open? false)
             (assoc-in [:athens/persist :settings :onboarding/completed?] true))}))


(reg-event-fx
  :onboarding/skip
  (fn [{:keys [db]} _]
    {:db (-> db
             (assoc :onboarding/open? false)
             (assoc-in [:athens/persist :settings :onboarding/completed?] true))}))


(reg-event-fx
  :onboarding/reset
  (fn [{:keys [db]} _]
    {:db (-> db
             (assoc :onboarding/open? true)
             (assoc :onboarding/step 0)
             (assoc-in [:athens/persist :settings :onboarding/completed?] false))}))


;; View Component
;; ==============

(defn product-demo-popup
  []
  (let [open? (subscribe [:onboarding/open?])
        step  (subscribe [:onboarding/step])]
    (fn []
      [:> ProductDemoModal
       {:isOpen      @open?
        :initialStep @step
        :onClose     #(dispatch [:onboarding/close])
        :onComplete  #(dispatch [:onboarding/complete])
        :onSkip      #(dispatch [:onboarding/skip])}])))
