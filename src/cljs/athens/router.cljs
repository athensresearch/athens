(ns athens.router
  (:require
   [athens.views :as views]
   [re-frame.core :refer [subscribe dispatch reg-sub reg-event-db reg-event-fx reg-fx]]
   [reitit.frontend :as rfe]
   [reitit.frontend.easy :as rfee]
   [reitit.frontend.controllers :as rfc]
   [reitit.coercion.spec :as rss]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
))

;; subs
(reg-sub
 :current-route
 (fn-traced [db]
   (:current-route db)))


;; events
(reg-event-db
 :navigated
 (fn-traced [db [_ new-match]]
   (let [old-match   (:current-route db)
         controllers (rfc/apply-controllers (:controllers old-match) new-match)]
     (assoc db :current-route (assoc new-match :controllers controllers)))))

(def routes
  ["/"
   [""      {:name :home}]
   ["about" {:name :about}]
   ["pages" {:name :pages}]
   ["page/:id" {:name :page}]
   ])

(def router
  (rfe/router
   routes
   {:data {:coercion rss/coercion}}))

(defn on-navigate [new-match]
  (when new-match
    (dispatch [:navigated new-match])))

(defn init-routes! []
  (prn "Initializing routes")
  (rfee/start!
   router
   on-navigate
   {:use-fragment true}))
