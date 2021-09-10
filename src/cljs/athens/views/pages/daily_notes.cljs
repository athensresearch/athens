(ns athens.views.pages.daily-notes
  (:require
    [athens.common-db :as common-db]
    [athens.db :as db]
    [athens.style :refer [DEPTH-SHADOWS]]
    [athens.util :refer [get-day]]
    [athens.views.pages.node-page :as node-page]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :refer [use-style]]))


;; Styles


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


(defn safe-pull-many
  "Need a safe pull because block/uid doesn't exist yet in datascript, but is found in :daily-notes/items.
  This happens because (dispatch [:daily-note/next (get-day)]) updates re-frame faster than the datascript tx can happen

  Bug: It's still possible for a day to not get created. The UI for this just shows an empty page without a title. Acceptable bug :)"
  [db ids]
  (keep
    (fn [uid]
      (try (common-db/get-block db [:block/uid uid])
           (catch js/Error _e
             (dispatch [:daily-note/remove uid]))))
    ids))


;; Components


(defn page
  []
  (let [note-refs (subscribe [:daily-notes/items])
        db        (r/track #(:db-after @db/tx-update))]
    (fn []
      (if (empty? @note-refs)
        (dispatch [:daily-note/next (get-day)])
        (let [notes (safe-pull-many @db @note-refs)]
          [:div#daily-notes (use-style daily-notes-scroll-area-style)
           (doall
             (for [{:keys [block/uid]} notes]
               ^{:key uid}
               [:<>
                [:div (use-style daily-notes-page-style)
                 [node-page/page [:block/uid uid]]]]))
           [:div (use-style daily-notes-notional-page-style)
            [:h1 "Earlier"]]])))))
