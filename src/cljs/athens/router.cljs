(ns athens.router
  (:require
    [athens.db :as db]
    #_[athens.views :as views]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [posh.reagent :refer [pull]]
    [re-frame.core :refer [#_subscribe dispatch reg-sub reg-event-fx reg-fx]]
    [reitit.coercion.spec :as rss]
    [reitit.frontend :as rfe]
    [reitit.frontend.controllers :as rfc]
    [reitit.frontend.easy :as rfee]))

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


(reg-event-fx
  :navigated
  (fn [{:keys [db]} [_ new-match]]
    (let [old-match   (:current-route db)
          controllers (rfc/apply-controllers (:controllers old-match) new-match)
          node (pull db/dsdb '[*] [:block/uid (-> new-match :path-params :id)]) ;; TODO make the page title query work when zoomed in on a block
          node-title (:node/title @node)
          page-title (str (or node-title "untitled") " – Athens")]
      (set! (.-title js/document) page-title) ;; TODO make this side effect explicit
      {:db (-> db
               (assoc :current-route (assoc new-match :controllers controllers))
               (dissoc :merge-prompt))
       :timeout {:action :clear
                 :id :merge-prompt}})))


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


(defn on-navigate
  [new-match]
  (when new-match
    (dispatch [:navigated new-match])))


(defn navigate
  [page]
  (dispatch [:navigate page]))


(defn navigate-uid
  ([uid]
   (dispatch [:navigate :page {:id uid}]))
  ([uid e]
   (let [shift (.. e -shiftKey)]
     (if shift
       (do
         (.. js/window getSelection empty)
         (.. e preventDefault)
         (dispatch [:right-sidebar/open-item uid]))
       (navigate-uid uid)))))


(defn init-routes!
  []
  (prn "Initializing routes")
  (rfee/start!
    router
    on-navigate
    {:use-fragment true}))
