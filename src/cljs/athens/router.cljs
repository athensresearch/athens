(ns athens.router
  (:require
    [athens.common-db            :as common-db]
    [athens.common.logging       :as log]
    [athens.dates                :as dates]
    [athens.db                   :as db]
    [athens.electron.db-picker   :as db-picker]
    [day8.re-frame.tracing       :refer-macros [fn-traced]]
    [posh.reagent                :refer [pull]]
    [re-frame.core               :as rf :refer [reg-sub reg-event-fx]]
    [reitit.coercion.spec        :as rss]
    [reitit.frontend             :as rfe]
    [reitit.frontend.controllers :as rfc]
    [reitit.frontend.easy        :as rfee]))


;; subs
(reg-sub
  :current-route
  (fn [db]
    (-> db :current-route)))


(reg-sub
  :current-route/uid
  (fn [db]
    (-> db :current-route :path-params :id)))


(rf/reg-sub
  :current-route/page-title
  (fn [db]
    (-> db :current-route :path-params :title)))


(reg-sub
  :current-route/name
  (fn [db]
    (-> db :current-route :data :name)))


;; events
(rf/reg-event-fx
  :navigate
  (fn [{:keys [db]} [_ & route]]
    (log/debug ":navigate route:" (pr-str route))
    (let [db-id       (-> db db-picker/selected-db :id)
          nav-type    (first route)
          route-id    (-> route second :id)
          route-title (-> route second :title)
          new-db      (if db-id
                        (assoc-in db
                                  [:athens/persist :db-picker/all-dbs db-id (if (= :page-by-title nav-type)
                                                                              :current-route/title
                                                                              :current-route/uid)]
                                  (if (= :page-by-title nav-type)
                                    route-title
                                    route-id))
                        db)]
      {:navigate! route
       :db        new-db})))


(rf/reg-event-fx
  :navigated
  (fn [{:keys [db]} [_ new-match]]
    (log/debug "navigated, new-match:" (pr-str new-match))
    (let [old-match   (:current-route db)
          route-name  (-> new-match :data :name)
          nav-page?   (= :page-by-title route-name)
          controllers (rfc/apply-controllers (:controllers old-match) new-match)
          loading?    (:loading? db)]
      (when-not loading?
        (if nav-page?
          (let [page-title (-> new-match :path-params :title)
                page-block (common-db/get-block @db/dsdb [:node/title page-title])
                html-title (str page-title " | Athens")]
            (set! (.-title js/document) html-title)
            {:db       (-> db
                           (assoc :current-route (assoc new-match :controllers controllers))
                           (dissoc :merge-prompt))
             :timeout  {:action :clear
                        :id     :merge-prompt}
             :dispatch [:editing/first-child (:block/uid page-block)]})
          (let [uid               (-> new-match :path-params :id)
                node              (pull db/dsdb '[*] [:block/uid uid]) ; TODO make the page title query work when zoomed in on a block
                node-title        (:node/title @node)
                home?             (= route-name :home)
                html-title-prefix (cond
                                    node-title            node-title
                                    (= route-name :pages) "All Pages"
                                    home?                 "Daily Notes")
                html-title        (if html-title-prefix
                                    (str html-title-prefix " | Athens")
                                    "Athens")
                today             (dates/get-day)]
            (set! (.-title js/document) html-title)
            {:db         (-> db
                             (assoc :current-route (assoc new-match :controllers controllers))
                             (dissoc :merge-prompt))
             :timeout    {:action :clear
                          :id     :merge-prompt}
             :dispatch-n [(when (and (not loading?)
                                     home?)
                            [:daily-note/ensure-day today])
                          (when-let [parent-uid (and (not loading?)
                                                     (or uid
                                                         (and home?
                                                              (:uid today))))]
                            [:editing/first-child parent-uid])]}))))))


;; doesn't reliably work. notably, Daily Notes are often not remembered as last open page, leading to incorrect restore
(reg-event-fx
  :restore-navigation
  (fn [{:keys [db]} _]
    (let [prev-title (-> db db-picker/selected-db :current-route/title)
          prev-uid   (-> db db-picker/selected-db :current-route/uid)]
      (cond
        prev-title {:dispatch [:navigate :page-by-title {:title prev-title}]}
        prev-uid   {:dispatch [:navigate :page {:id prev-uid}]}
        :else      {:dispatch [:navigate :home]}))))


;; effects

(rf/reg-fx
  :navigate!
  (fn-traced [route]
             (apply rfee/push-state route)))


;; router definition

(def routes
  ["/"
   ["" {:name :home}]
   ["settings" {:name :settings}]
   ["pages" {:name :pages}]
   ["page-t/:title" {:name :page-by-title}]
   ["page/:id" {:name :page}]
   ["graph" {:name :graph}]])


(def router
  (rfe/router
    routes
    {:data {:coercion rss/coercion}}))


(defn on-navigate
  [new-match]
  (when new-match
    (rf/dispatch [:navigated new-match])))


(defn navigate
  [page]
  (log/debug "navigate:" (pr-str page))
  (rf/dispatch [:navigate page]))


(defn nav-daily-notes
  "When user is already on a date node-page, clicking on daily notes goes to that date and allows scrolling."
  []
  (let [route-uid @(rf/subscribe [:current-route/uid])]
    (if (dates/is-daily-note route-uid)
      (rf/dispatch [:daily-note/reset [route-uid]])
      (rf/dispatch [:daily-note/reset []]))
    (navigate :home)))


(defn navigate-page
  "Navigate to page by it's title"
  ([title]
   (let [current-route-page-title @(rf/subscribe [:current-route/page-title])]
     (log/debug "navigate-page:" (pr-str {:title                    title
                                          :current-route-page-title current-route-page-title}))
     (when-not (= current-route-page-title title)
       (rf/dispatch [:navigate :page-by-title {:title title}]))))
  ([title e]
   (let [shift? (.-shiftKey e)]
     (if shift?
       (do
         (.. js/window getSelection empty)
         (.. e preventDefault)
         (rf/dispatch [:right-sidebar/open-page title]))
       (navigate-page title)))))


(defn navigate-uid
  "Don't navigate if already on the page."
  ([uid]
   (let [[uid _embed-id]   (db/uid-and-embed-id uid)
         current-route-uid @(rf/subscribe [:current-route/uid])]
     (when (not= current-route-uid uid)
       (rf/dispatch [:navigate :page {:id uid}]))))
  ([uid e]
   (let [[uid _embed-id] (db/uid-and-embed-id uid)
         shift           (.. e -shiftKey)]
     (if shift
       (do
         (.. js/window getSelection empty)
         (.. e preventDefault)
         (rf/dispatch [:right-sidebar/open-item uid]))
       (navigate-uid uid)))))


(defn init-routes!
  []
  (log/info "Initializing routes")
  (rfee/start!
    router
    on-navigate
    {:use-fragment true}))
