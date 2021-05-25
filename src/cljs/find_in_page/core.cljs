(ns find-in-page.core
  (:require
    ["@material-ui/icons/ChevronLeft" :default ChevronLeft]
    ["@material-ui/icons/ChevronRight" :default ChevronRight]
    ["@material-ui/icons/Clear" :default Clear]
    [clojure.string :as str]
    [find-in-page.style :as style]
    [garden.core :refer [css]]
    [goog.dom :refer [getElement setProperties]]
    [goog.events :as events]
    [re-frame.core :as rf :refer [subscribe dispatch]]
    [reagent.dom :as r-dom])
  (:import
    (goog.events
      EventType
      KeyCodes)))


(def electron (js/require "electron"))


;; -------------------------------------------------------------------
;; --- parent messages ---


(defonce __ipc-listener-find-window__
  (.. electron -remote -ipcMain
      (on "parent->find-window"
          (fn [_ msg]
            (let [[event & args] (js->clj msg :keywordize-keys true)]
              (case (keyword event)
                :opened (dispatch [:set-init-data (first args)])

                :search-result-index
                (dispatch [:set-find-in-page-index
                           (first args)])))))))


;; -------------------------------------------------------------------
;; --- send to parent ---


(defn send-msg-to-parent!
  [& msg]
  (.. electron -ipcRenderer
      (send "find-window->parent"
            (-> msg vec clj->js))))


;; -------------------------------------------------------------------
;; --- subs ---


(rf/reg-sub
  :find-in-page-info
  (fn [db]
    (:find-in-page-info db)))


;; -------------------------------------------------------------------
;; --- evts ---


(rf/reg-fx
  :send-msg
  (fn [args]
    (apply send-msg-to-parent! args)))


(rf/reg-fx
  :focus-on-input
  (fn [_]
    (.focus (js/document.querySelector "#find-in-page-input"))))


(rf/reg-event-fx
  :set-find-in-page-text
  (fn [{:keys [db]} [_ text no-msg-to-main?]]
    (merge
      {:db             (assoc-in db [:find-in-page-info :text]
                                 (some-> text seq str/join))
       :focus-on-input _}
      (when-not no-msg-to-main?
        {:send-msg [:text text]}))))


(rf/reg-event-fx
  :set-init-data
  (fn [{:keys [db]} [_ {:keys [text] :as init-data}]]
    {:db       (update db :find-in-page-info
                       merge (select-keys
                               init-data [:index-info :is-dark?]))
     :dispatch [:set-find-in-page-text text true]}))


(rf/reg-event-db
  :set-find-in-page-index
  (fn [db [_ index-info]]
    (assoc-in db [:find-in-page-info :index] index-info)))


;; -------------------------------------------------------------------
;; --- comp stuff ---


(defn find-in-page-styles
  [is-dark?]
  (let [{:keys [background color]}
        (if is-dark? style/THEME-DARK style/THEME-LIGHT)]
    [:html
     {:height     "100%"
      :background background
      :color      color}
     [:body :#app :.find-in-page-root
      {:height "100%"
       :margin "0"}]
     [:#app
      [:.find-in-page-root
       {:padding     "0px 12px"
        :visibility  "unset"
        :display     "flex"
        :align-items "center"}
       [:input
        {:background  "transparent"
         :flex        "1 0 auto"
         :font-size   "16px"
         :padding-top "4px"
         :border      "none"
         :outline     "none"
         :color       color}]
       [:span
        {:cursor "pointer"
         :height "1.5rem"
         :color  color}
        [:&:hover
         {:background "rgb(67 63 56 / 10%)"}]]]]]))


(defn find-in-page-comp
  []
  (let [{:keys [text is-dark?]} @(subscribe [:find-in-page-info])]
    [:div.find-in-page-root
     [:style (css (find-in-page-styles is-dark?))]
     [:input
      {:id          "find-in-page-input"
       :placeholder "Find in page"
       :on-change   #(dispatch [:set-find-in-page-text
                                (.. % -target -value)])
       :value       text}]
     [:<>
      (->> [{:icon ChevronLeft :key :prev}
            {:icon ChevronRight :key :next}
            {:icon Clear :key :close}]
           (map (fn [{:keys [icon key]}]
                  ^{:key key}
                  [:span {:on-click
                          #(send-msg-to-parent! key)}
                   [:> icon]]))
           doall)]]))


(defn find-win-key-down!
  [e]
  (let [key    (.. e -keyCode)
        shift? (.. e -shiftKey)]
    (when-let [op (cond
                    (and (= key KeyCodes.ENTER) shift?) :prev
                    (= key KeyCodes.ENTER) :next
                    (= key KeyCodes.ESC) :close
                    :else nil)]
      (send-msg-to-parent! op))))


(defn init
  []
  ;; 10x
  (when-let [el (js/document.querySelector "#--re-frame-10x--")]
    (setProperties el (clj->js {"style" "display: none"})))

  ;; render
  (r-dom/render [find-in-page-comp] (getElement "app"))

  ;; listener
  (events/listen js/window EventType.KEYDOWN find-win-key-down!))


