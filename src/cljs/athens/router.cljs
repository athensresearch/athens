(ns athens.router
  (:require
    [athens.db :as db]
    [athens.util :as util]
    #_[athens.views :as views]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [posh.reagent :refer [pull]]
    [re-frame.core :as rf :refer [subscribe dispatch reg-sub reg-event-fx reg-fx]]
    [reitit.coercion.spec :as rss]
    [reitit.frontend :as rfe]
    [reitit.frontend.controllers :as rfc]
    [reitit.frontend.easy :as rfee]))

;; subs
(reg-sub
  :current-route
  (fn [db]
    (-> db :current-route)))


(reg-sub
  :current-route/uid
  (fn [db]
    (-> db :current-route :path-params :id)))


(reg-sub
  :current-route/name
  (fn [db]
    (-> db :current-route :data :name)))


;; events
(reg-event-fx
  :navigate
  (fn [_ [_ & route]]
    {:navigate!          route
     :local-storage/set! ["current-route/uid" (-> route second :id)]}))


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


(reg-event-fx
  :local-storage/navigate
  [(rf/inject-cofx :local-storage "current-route/uid")]
  (fn [{:keys [local-storage]} _]
    (if (= "null" local-storage)
      {:dispatch [:navigate :home]}
      {:dispatch [:navigate :page {:id local-storage}]})))


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


(defn nav-daily-notes
  "When user is already on a date node-page, clicking on daily notes goes to that date and allows scrolling."
  []
  (let [route-uid @(subscribe [:current-route/uid])]
    (if (util/is-timeline-page route-uid)
      (dispatch [:daily-notes/add route-uid])
      (dispatch [:daily-notes/reset]))
    (navigate :home)))


(defn navigate-uid
  "Don't navigate if already on the page."
  ([uid]
   (let [current-route-uid @(subscribe [:current-route/uid])]
     (when (not= current-route-uid uid)
       (dispatch [:navigate :page {:id uid}]))))
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
