(ns athens.effects
  (:require
    [athens.db :as db]
    [athens.parser :as parser]
    [athens.util :as util :refer [now-ts gen-block-uid]]
    [cljs-http.client :as http]
    [cljs.core.async :refer [go <!]]
    [cljs.pprint :refer [pprint]]
    [clojure.string :as str]
    [datascript.core :as d]
    [datascript.transit :as dt]
    [day8.re-frame.async-flow-fx]
    [goog.dom.selection :refer [setCursorPosition]]
    [instaparse.core :as parse]
    [posh.reagent :as p :refer [transact!]]
    [re-frame.core :refer [dispatch reg-fx]]
    [stylefy.core :as stylefy]
    [athens.walk :as walk]))


;;; Effects


(reg-fx
  :transact!
  (fn [tx-data]
    (prn "TX RAW INPUTS") ;; event tx-data
    (pprint tx-data)
    (try
      (let [with-tx-data  (:tx-data (d/with @db/dsdb tx-data))
            more-tx-data  (walk/parse-for-links with-tx-data)
            final-tx-data (vec (concat tx-data more-tx-data))]
        ;;(prn "TX WITH") ;; tx-data normalized by datascript to flat datoms
        ;;(pprint with-tx-data)
        (prn "TX FINAL INPUTS")                             ;; parsing block/string (and node/title) to derive asserted or retracted titles and block refs
        (pprint final-tx-data)
        (prn "TX OUTPUTS")
        (let [outputs (:tx-data (transact! db/dsdb final-tx-data))]
          (pprint outputs)))
      (catch js/Error e
        (js/alert (str e))
        (prn "EXCEPTION" e)))))


(reg-fx
  :reset-conn!
  (fn [new-db]
    (d/reset-conn! db/dsdb new-db)))


(reg-fx
  :local-storage/set!
  (fn [[key value]]
    (js/localStorage.setItem key value)))


(reg-fx
  :local-storage/set-db!
  (fn [db]
    (js/localStorage.setItem "datascript/DB" (dt/write-transit-str db))))


(reg-fx
  :http
  (fn [{:keys [url method opts on-success on-failure]}]
    (go
      (let [http-fn (case method
                      :post http/post :get http/get
                      :put http/put :delete http/delete)
            res     (<! (http-fn url opts))
            {:keys [success body] :as all} res]
        (if success
          (dispatch (conj on-success body))
          (dispatch (conj on-failure all)))))))


(reg-fx
  :timeout
  (let [timers (atom {})]
    (fn [{:keys [action id event wait]}]
      (case action
        :start (swap! timers assoc id (js/setTimeout #(dispatch event) wait))
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

(reg-fx
  :editing/focus
  (fn [[uid index]]
    (if (nil? uid)
      (when-let [active-el (.-activeElement js/document)]
        (.blur active-el))
      (js/setTimeout (fn []
                       (let [html-id (str "#editable-uid-" uid)
                             ;;targets (js/document.querySelectorAll html-id)
                             ;;n       (count (array-seq targets))
                             el      (js/document.querySelector html-id)]
                         #_(cond
                             (zero? n) (prn "No targets")
                             (= 1 n) (prn "One target")
                             (< 1 n) (prn "Several targets"))
                         (when el
                           (.focus el)
                           (when index
                             (setCursorPosition el index)))))
                     100))))


(reg-fx
  :set-cursor-position
  (fn [[uid start end]]
    (js/setTimeout (fn []
                     (when-let [target (js/document.querySelector (str "#editable-uid-" uid))]
                       (.focus target)
                       (set! (.-selectionStart target) start)
                       (set! (.-selectionEnd target) end)))
                   100)))


(reg-fx
  :stylefy/tag
  (fn [[tag properties]]
    (stylefy/tag tag properties)))
