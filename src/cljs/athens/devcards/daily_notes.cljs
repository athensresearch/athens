(ns athens.devcards.daily-notes
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.devcards.node-page :refer [node-page-component]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as string]
    [devcards.core :refer-macros [defcard-rg]]
    [garden.selectors :as selectors]
    [goog.functions :refer [debounce]]
    [posh.reagent :refer [pull pull-many q]]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]
    [tick.alpha.api :as t]
    [tick.locale-en-us]))

;;; Styles


;;; Helpers




(def US-format (t/formatter "MM-dd-yyyy"))


(def title-format (t/formatter "LLLL dd, yyyy"))


(defn date-string
  [ts]
  (if (< ts 1) ;; TODO why this predicate?
    [:span "(unknown date)"]
    (as-> (js/Date. ts) x
      (t/instant x)
      (t/date-time x)
      (t/format (t/formatter "LLLL MM, yyyy h':'ma") x)
      (string/replace x #"AM" "am")
      (string/replace x #"PM" "pm"))))


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
  [e]
  (let [daily-notes @(subscribe [:daily-notes])
        main-content (.. js/document (getElementById "main-content"))
        client-height (.. main-content -clientHeight) ;; could also use -offsetHeight. get the same value
        scroll-top (.. main-content -scrollTop)
        ratio (/ client-height scroll-top)]
    (prn client-height scroll-top ratio)
    (when (<= ratio 30) ;; when scrolled all the way down, ratio is 10, because height is 110vh
      (prn "DISPATCH")
      (dispatch [:next-daily-note (get-day (count daily-notes))]))))

(def db-scroll-daily-notes (debounce scroll-daily-notes 500))

;;; Scroll

;;; Components

(defn daily-notes-panel
  []
  (let [note-refs (subscribe [:daily-notes])]
    (if (empty? @note-refs)
      (dispatch [:next-daily-note (get-day)]))
    (fn []

      (let [notes (pull-many db/dsdb
                    '[*]
                    ;;'[:db/id :block/uid :block/string :block/open :block/order {:block/children ...}]
                    (map (fn [x] [:block/uid x]) @note-refs))]
        [:div.daily-notes (use-style {:display        "flex"
                                      :flex           "0 0 auto"
                                      :flex-direction "column"
                                      :height "110vh"}
                            {:on-scroll (fn [x] (prn "hi" x))})

         (doall
           (for [{:keys [block/uid node/title]} @notes]
             ^{:key uid}
             [:<>
              [:div (use-style {:min-height "550px"})
               [:h1 title]]
              ;;[node-page-component [:block/uid uid]]
              [:hr (use-style {:border "1px solid black"
                               :width  "100%"}
                     {:key title})]]))

         [:div
          [:h1 (use-style {:color "gray"}) "Preview"]]]))))


;;; Devcards

(defcard-rg Daily-Notes
  [daily-notes-panel])
