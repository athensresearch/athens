(ns athens.devcards.daily-notes
  (:require
    [athens.db :as db]
    [athens.devcards.node-page :refer [node-page-component]]
    [athens.style :refer [DEPTH-SHADOWS]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-rg]]
    [goog.functions :refer [debounce]]
    [posh.reagent :refer [pull-many]]
    [re-frame.core :refer [dispatch subscribe]]
    [stylefy.core :refer [use-style]]
    [tick.alpha.api :as t]
    [tick.locale-en-us]))


;;; Styles


(def daily-notes-scroll-area-style
  {:min-height "calc(100vh + 1px)"
   :display        "flex"
   :padding        "1.25rem 0"
   :align-items    "stretch"
   :flex           "1 1 100%"
   :flex-direction "column"})


(def daily-notes-page-style
  {:box-shadow (:16 DEPTH-SHADOWS)
   :align-self "stretch"
   :justify-self "stretch"
   :margin "1.25rem 2.5rem"
   :padding "1rem 2rem"
   :transition-duration "0s"
   :border-radius "8px"
   :min-height "calc(100vh - 10rem)"})


(def daily-notes-notional-page-style
  (merge daily-notes-page-style {:box-shadow (:4 DEPTH-SHADOWS)
                                 :opacity "0.5"}))


;;; Helpers



(def US-format (t/formatter "MM-dd-yyyy"))
(def title-format (t/formatter "LLLL dd, yyyy"))


(defn get-day
  "Returns today's date or a date OFFSET days before today"
  ([] (get-day 0))
  ([offset]
   (let [day (t/-
               (t/date-time)
               (t/new-duration offset :days))]
     {:uid   (t/format US-format day)
      :title (t/format title-format day)})))


(defn scroll-daily-notes
  [_]
  (let
    [daily-notes @(subscribe [:daily-notes])
     from-bottom (.. js/document (getElementById "daily-notes") getBoundingClientRect -bottom)
     doc-height (.. js/document -documentElement -scrollHeight)
     delta (- from-bottom doc-height)]
    (when (< delta 1)
      (dispatch [:next-daily-note (get-day (count daily-notes))]))))


(def db-scroll-daily-notes (debounce scroll-daily-notes 500))


;;; Components


(defn daily-notes-panel
  []
  (let [note-refs (subscribe [:daily-notes])]
    (when (empty? @note-refs)
      (dispatch [:next-daily-note (get-day)]))
    (fn []
      (let [notes (pull-many db/dsdb
                             '[*]
                             (map (fn [x] [:block/uid x]) @note-refs))]
        [:div#daily-notes (use-style daily-notes-scroll-area-style)
         (doall
           (for [{:keys [block/uid]} @notes]
             ^{:key uid}
             [:<>
              [:div (use-style daily-notes-page-style)
               [node-page-component [:block/uid uid]]]]))
         [:div (use-style daily-notes-notional-page-style)
          [:h1 "Earlier"]]]))))


;;; Devcards

(defcard-rg Daily-Notes
  [daily-notes-panel])
