(ns athens.views.find-in-page
  (:require
    ["@material-ui/icons/VerticalSplit" :default VerticalSplit]
    [athens.style :as styles]
    [clojure.string :as str]
    [re-frame.core :as rf :refer [subscribe dispatch]]
    [stylefy.core :as stylefy :refer [use-style]]
    [athens.views.textinput :as textinput]))


(def electron (js/require "electron"))


;;-------------------------------------------------------------------
;;--- subs and evts ---

(rf/reg-sub
  :find-in-page-text
  (fn [db]
    (some-> db :find-in-page-text str/trim)))


(rf/reg-sub
  :is-find-in-page-open?
  (fn [db]
    (:is-find-in-page-open? db)))


(rf/reg-event-db
  :set-find-in-page-text
  (fn [db [_ text]]
    (assoc db :find-in-page-text (some-> text seq str/join))))


(rf/reg-event-db
  :open-find-in-page
  (fn [db _]
    (assoc db :is-find-in-page-open? true)))


(rf/reg-event-db
  :close-find-in-page
  (fn [db _]
    (assoc db :is-find-in-page-open? false)))


;;-------------------------------------------------------------------
;;--- find fn ---


(defn find-in-page!
  ([] (find-in-page! @(subscribe [:find-in-page-text])))
  ([text-to-search]
   (do (println " - - - -start  - - -  ")
       (cljs.pprint/pprint text-to-search)
       (println " - - - - end  - - -  "))
   (when (some-> text-to-search seq str/join)
     (let [browser-win (.. electron -remote -BrowserWindow)
           win         (. browser-win getFocusedWindow)
           opts        (clj->js {:forward                  true,
                                 :findNext                 false,
                                 :matchCase                false,
                                 :wordStart                false,
                                 :medialCapitalAsWordStart false})]
       (.. win -webContents (findInPage text-to-search))

       #_(.. win -webContents (on "found-in-page"
                                  (fn [event result]
                                    (do (println " - - - -start  - - -  ")
                                        (cljs.pprint/pprint event)
                                        (cljs.pprint/pprint result)
                                        (println " - - - - end  - - -  ")))))))))


(defn clear-find-in-page!
  []
  (do (println " - - - -start  - - -  ")
      (cljs.pprint/pprint "clear")
      (println " - - - - end  - - -  "))
  (let [browser-win (.. electron -remote -BrowserWindow)
        win         (. browser-win getFocusedWindow)]
    (.. win -webContents (stopFindInPage "clearSelection"))))


(defn start-find-in-page!
  []
  (dispatch [:open-find-in-page])
  (find-in-page!))


(defn stop-find-in-page!
  []
  (dispatch [:close-find-in-page])
  (clear-find-in-page!))


;;-------------------------------------------------------------------
;;--- comp stuff ---


(defn find-in-page-styles
  [theme]
  {#_#_:background (color :background-minus-2 :opacity-med)
   :position "absolute"
   :top      "50px"
   :right    "20px"})


(defn find-in-page-comp
  []
  (when @(subscribe [:is-find-in-page-open?])
    (let [theme (if @(subscribe [:theme/dark])
                  styles/THEME-DARK
                  styles/THEME-LIGHT)]

      [:div (use-style (find-in-page-styles theme))

       [textinput/textinput
        {:placeholder " Find in page "
         :on-change   #(let [text (.. % -target -value)]
                         (dispatch [:set-find-in-page-text text])
                         (find-in-page! text))
         :value       @(subscribe [:find-in-page-text])}]])))
