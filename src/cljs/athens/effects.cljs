(ns athens.effects
  (:require
    [athens.common-db            :as common-db]
    [athens.common-events.schema :as schema]
    [athens.db                   :as db]
    [athens.self-hosted.client   :as client]
    [cljs-http.client            :as http]
    [cljs.core.async             :refer [go <!]]
    [com.stuartsierra.component  :as component]
    [datascript.core             :as d]
    [day8.re-frame.async-flow-fx]
    [goog.dom.selection          :refer [setCursorPosition]]
    [malli.core                  :as m]
    [malli.error                 :as me]
    [re-frame.core               :as rf]
    [stylefy.core                :as stylefy]))


;; Effects

(rf/reg-fx
  :transact!
  (fn [tx-data]
    ;; 🎶 Sia "Cheap Thrills"
    (d/transact! db/dsdb (common-db/linkmaker @db/dsdb tx-data))))


(rf/reg-fx
  :reset-conn!
  (fn [new-db]
    (d/reset-conn! db/dsdb new-db)))


(rf/reg-fx
  :http
  (fn [{:keys [url method opts on-success on-failure]}]
    (go
      (let [http-fn (case method
                      :post http/post :get http/get
                      :put http/put :delete http/delete)
            res     (<! (http-fn url opts))
            {:keys [success body] :as all} res]
        (if success
          (rf/dispatch (conj on-success body))
          (rf/dispatch (conj on-failure all)))))))


(rf/reg-fx
  :timeout
  (let [timers (atom {})]
    (fn [{:keys [action id event wait]}]
      (case action
        :start (swap! timers assoc id (js/setTimeout #(rf/dispatch event) wait))
        :clear (do (js/clearTimeout (get @timers id))
                   (swap! timers dissoc id))))))


;; Using DOM, focus the target block.
;; There can actually be multiple elements with the same #editable-uid-UID HTML id
;; The same unique datascript block can be rendered multiple times: node-page, right sidebar, linked/unlinked references
;; In this case, find the all the potential HTML blocks with that uid. The one that shares the same closest ancestor as the
;; activeElement (where the text caret is before the new focus happens), is the container of the block to focus on.

;; If an index is passed, set cursor to that index.

;; TODO: some issues
;; - auto-focus on textarea
;; - searching for common-ancestor on inside of setTimeout vs outside
;;   - element sometimes hasn't been created yet (enter), sometimes has been just destroyed (backspace)
;; - uid sometimes nil

(rf/reg-fx
  :editing/focus
  (fn [[uid index]]
    (if (nil? uid)
      (when-let [active-el (.-activeElement js/document)]
        (.blur active-el))
      (js/setTimeout (fn []
                       (let [[uid embed-id]  (db/uid-and-embed-id uid)
                             html-id         (str "editable-uid-" uid)
                             ;; targets (js/document.querySelectorAll html-id)
                             ;; n       (count (array-seq targets))
                             el              (js/document.querySelector
                                               (if embed-id
                                                 (or
                                                   ;; find exact embed block
                                                   (str "textarea[id='" html-id "-embed-" embed-id "']")
                                                   ;; find embedded that starts with current html id (embed id changed due to re-render)
                                                   (str "textarea[id^='" html-id "-embed-']"))
                                                 ;; take default
                                                 (str "#" html-id)))]
                         #_(cond
                             (zero? n) (prn "No targets")
                             (= 1 n) (prn "One target")
                             (< 1 n) (prn "Several targets"))
                         (when el
                           (.focus el)
                           (when index
                             (setCursorPosition el index)))))
                     100))))


;; todo(abhinav)
;; think of this + up/down + editing/focus for common up down press
;; and cursor goes to apt position rather than last visited point in the block(current)
;; inspirations - intelli-j's up/down
(rf/reg-fx
  :set-cursor-position
  (fn [[uid start end]]
    (js/setTimeout (fn []
                     (when-let [target (js/document.querySelector (str "#editable-uid-" uid))]
                       (.focus target)
                       (set! (.-selectionStart target) start)
                       (set! (.-selectionEnd target) end)))
                   100)))


(rf/reg-fx
  :stylefy/tag
  (fn [[tag properties]]
    (stylefy/tag tag properties)))


(rf/reg-fx
  :alert/js!
  (fn [message]
    (js/alert message)))


(rf/reg-fx
  :right-sidebar/scroll-top
  (fn []
    (let [right-sidebar (js/document.querySelector ".right-sidebar-content")]
      (when right-sidebar
        (set! (.. right-sidebar -scrollTop) 0)))))


;; TODO: temporary, and limits to one client opened.
(def self-hosted-client (atom nil))


(rf/reg-fx
  :remote/client-connect!
  (fn [{:keys [ws-url] :as remote-db}]
    (js/console.debug ":remote/client-connect!" (pr-str remote-db))
    (when @self-hosted-client
      (js/console.log ":remote/client-connect! already connected, restarting")
      (component/stop @self-hosted-client))
    (js/console.log ":remote/client-connect! connecting")
    (reset! self-hosted-client (-> ws-url
                                   client/new-ws-client
                                   component/start))))


(rf/reg-fx
  :remote/client-disconnect!
  (fn []
    (js/console.debug ":remote/client-disconnect!")
    (when @self-hosted-client
      (component/stop @self-hosted-client)
      (reset! self-hosted-client nil))))


(rf/reg-fx
  :remote/send-event-fx!
  (fn [event]
    (if (schema/valid-event? event)
      ;; valid event let's send it
      (do
        (js/console.log "Sending event:" (pr-str event))
        (client/send! event))
      (let [explanation (-> schema/event
                            (m/explain event)
                            (me/humanize))]
        (js/console.warn "Tried to send invalid event. Error:" (pr-str explanation))))))


(rf/reg-fx
  :invoke-callback
  (fn [callback]
    (callback)))
