(ns athens.util
  (:require
    ["/textarea" :as getCaretCoordinates]
    [athens.config :as config]
    [clojure.string :as string]
    [cognitect.transit :as tr]
    [com.rpl.specter :as s]
    [goog.dom :refer [getElement setProperties]]
    [posh.reagent :refer [#_pull]]
    [tick.alpha.api :as t]
    [tick.locale-en-us])
  (:require-macros
    [com.rpl.specter :refer [recursive-path]]))


;; Electron ipcMain Channels

(def ipcMainChannels
  {:toggle-max-or-min-win-channel "toggle-max-or-min-active-win"
   :close-win-channel "close-win"
   :exit-fullscreen-win-channel "exit-fullscreen-win"})


;; embed block

(declare specter-recursive-path)


(defn recursively-modify-block-for-embed
  "Modify the block and all the block children to have same embed-id for
   referencing the embed block rather than block in original page"
  [block embed-id]
  (s/transform
    (specter-recursive-path #(contains? % :block/uid))
    (fn [{:block/keys [uid] :as block}]
      (assoc block :block/uid (str uid "-embed-" embed-id)
             :block/original-uid uid))
    block))


;; -- DOM ----------------------------------------------------------------

;; TODO: move all these DOM utilities to a .cljs file instead of cljc
(defn scroll-top!
  [element pos]
  (when pos
    (set! (.. element -scrollTop) pos)))


(defn scroll-if-needed
  ;; https://stackoverflow.com/a/45851497
  [element container]
  (when (and element container)
    (let [e-top (.. element -offsetTop)
          e-height (.. element -offsetHeight)
          e-bottom (+ e-top e-height)
          cs-top (.. container -scrollTop)
          c-height (.. container -offsetHeight)
          cs-bottom (+ cs-top c-height)]
      (->> (cond
             (< e-top cs-top)       e-top
             (< cs-bottom e-bottom) (- e-bottom c-height))
           (scroll-top! container)))))


(defn mouse-offset
  "Finds offset between mouse event and container. If container is not passed, use target as container."
  ([e]
   (mouse-offset e (.. e -target)))
  ([e container]
   (let [rect (.. container getBoundingClientRect)
         offset-x (- (.. e -pageX) (.. rect -left))
         offset-y (- (.. e -pageY) (.. rect -top))]
     {:x offset-x :y offset-y})))


(defn vertical-center
  [el]
  (let [rect (.. el getBoundingClientRect)]
    (-> (- (.. rect -bottom)
           (.. rect -top))
        (/ 2))))


(defn is-beyond-rect?
  "Checks if any part of the element is above or below the container's bounding rect"
  [element container]
  (when (and element container)
    (let [el-box (.. element getBoundingClientRect)
          cont-box (.. container getBoundingClientRect)]
      (or
        (> (.. el-box -bottom) (.. cont-box -bottom))
        (< (.. el-box -top) (.. cont-box -top))))))


(defn scroll-into-view
  [element container align-top?]
  (when (is-beyond-rect? element container)
    (.. element (scrollIntoView align-top? {:behavior "auto"}))))


(defn get-dataset-uid
  [el]
  (let [block (when el (.. el (closest ".block-container")))
        uid   (when block (.getAttribute block "data-uid"))]
    uid))


(defn get-dataset-children-uids
  [el]
  (let [block         (when el (.. el (closest ".block-container")))
        children-uids (when block
                        (let [dom-children-uids ^String (.getAttribute block "data-childrenuids")]
                          (when-not (string/blank? dom-children-uids)
                            (-> dom-children-uids
                                (string/split #",")
                                set))))]
    children-uids))


(defn get-caret-position
  [target]
  (let [selectionEnd (.. target -selectionEnd)]
    (js->clj (getCaretCoordinates target selectionEnd) :keywordize-keys true)))


(defn dom-parents
  "This and common-ancestor taken from https://stackoverflow.com/a/5350888."
  [node]
  (loop [nodes [node]
         node node]
    (if (nil? node)
      (reverse nodes)
      (recur (conj nodes node) (.-parentNode node)))))


(defn common-ancestor
  [node1 node2]
  (let [p1 (dom-parents node1)
        p2 (dom-parents node2)]
    (if (not= (first p1) (first p2))
      (throw (js/Error. "No common ancestor!"))
      (let [n (dec (count p1))]
        (loop [i 0]
          (cond
            (not= (nth p1 i nil) (nth p2 i nil))
            (nth p1 (dec i))

            (= i n)
            (js/Error. "No common ancestor after n loops!")

            :else
            (recur (inc i))))))))


(defn destruct-key-down
  [e]
  (let [key   (.. e -keyCode)
        ctrl  (.. e -ctrlKey)
        meta  (.. e -metaKey)
        shift (.. e -shiftKey)
        alt   (.. e -altKey)]
    {:key-code key
     :ctrl     ctrl
     :meta     meta
     :shift    shift
     :alt      alt}))


(defn js-event->val
  [event]
  (.. event -target -value))


;; -- Date and Time ------------------------------------------------------


(def date-col-format (t/formatter "LLLL dd, yyyy h':'mma"))
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
      :title (t/format title-format day)}))
  ([date offset]
   (let [day (t/-
               (-> date (t/at "0"))
               (t/new-duration offset :days))]
     {:uid   (t/format US-format day)
      :title (t/format title-format day)})))


(defn date-string
  [ts]
  (if (not ts)
    [:span "(unknown date)"]
    (as->
      (t/instant ts) x
      (t/date-time x)
      (t/format date-col-format x)
      (string/replace x #"AM" "am")
      (string/replace x #"PM" "pm"))))


(defn uid-to-date
  [uid]
  (try
    (let [[m d y] (string/split uid "-")
          rejoin (string/join "-" [y m d])]
      (t/date rejoin))
    (catch js/Object _ nil)))


(defn is-daily-note
  [uid]
  (boolean (uid-to-date uid)))


;; -- Regex -----------------------------------------------------------

;; https://stackoverflow.com/a/11672480
(def regex-esc-char-map
  (let [esc-chars "()*&^%$#![]"]
    (zipmap esc-chars
            (map #(str "\\" %) esc-chars))))


(defn escape-str
  "Take a string and escape all regex special characters in it"
  [str]
  (string/escape str regex-esc-char-map))


;; -- specter --------------------------------------------------------


(defn specter-recursive-path
  "Navigates across maps and lists to find the sub that
   satisfies the function"
  [afn]
  (recursive-path [] p
                  (s/cond-path
                    map? (s/multi-path [s/MAP-VALS p] afn)
                    sequential? [s/ALL p])))


;; OS

(defn get-os
  []
  (let [os (.. js/window -navigator -appVersion)]
    (cond
      (re-find #"Windows" os) :windows
      (re-find #"Linux" os) :linux
      (re-find #"Mac" os) :mac)))


(defn app-classes
  ([{:keys [os electron? theme-dark? win-focused? win-fullscreen? win-maximized?]}]
   [(case os
      :windows "os-windows"
      :mac "os-mac"
      :linux "os-linux")
    (if electron? "is-electron" "is-web")
    (if theme-dark? "is-theme-dark" "is-theme-light")
    (when win-focused? "is-focused")
    (when win-fullscreen? "is-fullscreen")
    (when win-maximized? "is-maximized")]))


(defn add-body-classes
  [classes]
  (let [cl js/document.body.classList]
    (doseq [class (remove nil? classes)]
      (.add cl class))))


(defn switch-body-classes
  [[from to]]
  (let [cl js/document.body.classList]
    (.add cl to)
    (.remove cl from)))


(defn shortcut-key?
  "Use meta for mac, ctrl for others."
  [meta ctrl]
  (let [os (get-os)]
    (or (and (= os :mac) meta)
        (and (= os :windows) ctrl)
        (and (= os :linux) ctrl))))


;; re-frame-10x

(defn re-frame-10x-open?
  []
  (when config/debug?
    (let [el-10x (getElement "--re-frame-10x--")
          display-10x (.. el-10x -style -display)]
      (not (= "none" display-10x)))))


(defn open-10x
  []
  (when config/debug?
    (let [el (js/document.querySelector "#--re-frame-10x--")]
      (setProperties el (clj->js {"style" "display: block"})))))


(defn hide-10x
  []
  (when config/debug?
    (let [el (js/document.querySelector "#--re-frame-10x--")]
      (setProperties el (clj->js {"style" "display: none"})))))


(defn toggle-10x
  []
  (when config/debug?
    (let [open? (re-frame-10x-open?)]
      (if open?
        (hide-10x)
        (open-10x)))))


(defn electron?
  []
  (let [user-agent (.. js/navigator -userAgent toLowerCase)]
    (boolean (re-find #"electron" user-agent))))


;; (goog-define COMMIT_URL "")


(defn athens-version
  []
  (cond
    (electron?) (.. (js/require "electron") -remote -app getVersion)))


;; (not (string/blank? COMMIT_URL)) COMMIT_URL
;; :else "Web"))


;; Local Storage
;; Inspired by intermine/bluegenes:
;; https://github.com/intermine/bluegenes/blob/4589ef8b09b26dcf23d434d4d7d9d56fd01a259f/src/cljs/bluegenes/effects.cljs#L14-L30

(defn local-storage-set!
  "Set v to local storage under k, replacing the value that was there before.
  k is coerced to string, v is written as json-verbose transit."
  [k v]
  (if (some? v)
    (.setItem js/localStorage (str k) (tr/write (tr/writer :json-verbose) v))
    (.removeItem js/localStorage (str k))))


(defn local-storage-get
  "Get value from local storage under k.
  k is coerced to string, v is read as json-verbose transit."
  [k]
  (tr/read (tr/reader :json-verbose) (.getItem js/localStorage (str k))))


