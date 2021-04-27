(ns athens.views.pages.daily-notes
  (:require
    [athens.db :as db]
    [athens.style :refer [DEPTH-SHADOWS]]
    [athens.util :refer [get-day uid-to-date]]
    [athens.views.pages.node-page :as node-page]
    [cljsjs.react]
    [cljsjs.react.dom]
    [goog.dom :refer [getElement]]
    [goog.functions :refer [debounce]]
    [posh.reagent :refer [pull]]
    [re-frame.core :refer [dispatch subscribe]]
    [stylefy.core :refer [use-style]]))


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
   :border-radius "0.5rem"
   :min-height "calc(100vh - 10rem)"})


(def daily-notes-notional-page-style
  (merge daily-notes-page-style {:box-shadow (:4 DEPTH-SHADOWS)
                                 :opacity "0.5"}))


;;; Helpers



(defn scroll-daily-notes
  [_]
  (let [daily-notes @(subscribe [:daily-notes/items])
        el          (getElement "daily-notes")
        offset-top  (.. el -offsetTop)
        rect        (.. el getBoundingClientRect)
        from-bottom (.. rect -bottom)
        from-top    (.. rect -top)
        doc-height  (.. js/document -documentElement -scrollHeight)
        top-delta   (- offset-top from-top)
        bottom-delta (- from-bottom doc-height)]
    ;; Don't allow user to scroll up for now.
    (cond
      (< top-delta 1) nil #_(dispatch [:daily-note/prev (get-day (uid-to-date (first daily-notes)) -1)])
      (< bottom-delta 1) (dispatch [:daily-note/next (get-day (uid-to-date (last daily-notes)) 1)]))))


(def db-scroll-daily-notes (debounce scroll-daily-notes 500))


(defn safe-pull-many
  "Need a safe pull because block/uid doesn't exist yet in datascript, but is found in :daily-notes/items.
  This happens because (dispatch [:daily-note/next (get-day)]) updates re-frame faster than the datascript tx can happen

  Bug: It's still possible for a day to not get created. The UI for this just shows an empty page without a title. Acceptable bug :)"
  [ids]
  (->> ids
       (map (fn [x] [:block/uid x]))
       (map (fn [x]
              (try
                @(pull db/dsdb '[*] x)
                (catch js/Error _e
                  nil))))
       (filter (fn [x]
                 (not (nil? x))))))


;;; Components


(defn page
  []
  (let [note-refs (subscribe [:daily-notes/items])]
    (fn []
      (if (empty? @note-refs)
        (dispatch [:daily-note/next (get-day)])
        (let [notes (safe-pull-many @note-refs)]
          [:div#daily-notes (use-style daily-notes-scroll-area-style)
           (doall
             (for [{:keys [block/uid]} notes]
               ^{:key uid}
               [:<>
                [:div (use-style daily-notes-page-style)
                 [node-page/page [:block/uid uid]]]]))
           [:div (use-style daily-notes-notional-page-style)
            [:h1 "Earlier"]]])))))
