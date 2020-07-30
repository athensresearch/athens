(ns athens.util
  (:require
    [clojure.string :as string]
    [posh.reagent :refer [#_pull]]
    [tick.alpha.api :as t]
    [tick.locale-en-us]))


(defn gen-block-uid
  []
  (subs (str (random-uuid)) 27))


;; -- DOM ----------------------------------------------------------------

;; TODO: move all these DOM utilities to a .cljs file instead of cljc
(defn scroll-if-needed
  ;; https://stackoverflow.com/a/45851497
  [element container]
  (if (< (.. element -offsetTop) (.. container -scrollTop))
    ;; If the element is higher than its container's top...
    (set! (.. container -scrollTop) (.. element -offsetTop))
    ;; Otherwise, find the bottom of the element and the container...
    (let [offsetBottom (+ (.. element -offsetTop) (.. element -offsetHeight))
          scrollBottom (+ (.. container -scrollTop) (.. container -offsetHeight))]
      ;; ..and if it's lower than the container's bottom
      (when (< scrollBottom offsetBottom)
        ;; Scroll the container so the element is in view
        (set!
          (.. container -scrollTop)
          (- offsetBottom (.. container -offsetHeight)))))))


(defn mouse-offset
  [e]
  (let [rect (.. e -target getBoundingClientRect)
        offset-x (- (.. e -pageX) (.. rect -left))
        offset-y (- (.. e -pageY) (.. rect -top))]
    {:x offset-x :y offset-y}))


(defn vertical-center
  [el]
  (let [rect (.. el getBoundingClientRect)]
    (-> (- (.. rect -bottom)
           (.. rect -top))
        (/ 2))))


(defn is-beyond-rect?
  "Checks if any part of the element is above or below the container's bounding rect"
  [element container]
  (let [el-box (.. element getBoundingClientRect)
        cont-box (.. container getBoundingClientRect)]
    (or
      (> (.. el-box -bottom) (.. cont-box -bottom))
      (< (.. el-box -top) (.. cont-box -top)))))


;; -- Date and Time ------------------------------------------------------


(def date-col-format (t/formatter "LLLL dd, yyyy h':'mma"))
(def US-format (t/formatter "MM-dd-yyyy"))
(def title-format (t/formatter "LLLL dd, yyyy"))


(defn now-ts
  []
  (-> (js/Date.) .getTime))


(defn get-day
  "Returns today's date or a date OFFSET days before today"
  ([] (get-day 0))
  ([offset]
   (let [day (t/-
               (t/date-time)
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
