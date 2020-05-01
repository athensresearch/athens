(ns athens.router
  (:require
   [athens.views :as views]
   [re-frame.core :refer [subscribe dispatch reg-sub reg-event-db reg-event-fx reg-fx]]
   [reitit.frontend :as rfe]
   [reitit.frontend.easy :as rfee]
   [reitit.frontend.controllers :as rfc]
   [reitit.coercion.spec :as rss]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))

;; subs
(reg-sub
 :current-route
 (fn [db]
   (:current-route db)))

;; events
(reg-event-fx
 :navigate
 (fn [_ [_ & route]]
   {:navigate! route}))

(reg-event-db
 :navigated
 (fn [db [_ new-match]]
   (let [old-match   (:current-route db)
         controllers (rfc/apply-controllers (:controllers old-match) new-match)
         node (subscribe [:node [:block/uid (-> new-match :path-params :id)]])] ;; TODO make the page title query work when zoomed in on a block
     (set! (.-title js/document) (or (:node/title @node) "Athens Research")) ;; TODO make this side effect explicit
     (assoc db :current-route (assoc new-match :controllers controllers)))))

;; effects

(reg-fx
 :navigate!
 (fn-traced [route]
   (apply rfee/push-state route)))

;; router definition

(def routes
  ["/"
   [""      {:name :home}]
   ["about" {:name :about}]
   ["pages" {:name :pages}]
   ["page/:id" {:name :page}]])

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
