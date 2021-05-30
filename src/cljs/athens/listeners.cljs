(ns athens.listeners
  (:require
    [athens.db :as db]
    [athens.router :as router]
    [athens.util :as util]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as string]
    [goog.events :as events]
    [re-frame.core :refer [dispatch subscribe]])
  (:import
    (goog.events
      EventType
      KeyCodes)))


(defn unfocus
  "Clears editing/uid when user clicks anywhere besides bullets, header, or on a block.
  Clears selected/items when user clicks somewhere besides a bullet point."
  [e]
  (let [selected-items?      (not-empty @(subscribe [:selected/items]))
        editing-uid          @(subscribe [:editing/uid])
        closest-block        (.. e -target (closest ".block-content"))
        closest-block-header (.. e -target (closest ".block-header"))
        closest-page-header  (.. e -target (closest ".page-header"))
        closest-bullet       (.. e -target (closest ".bullet"))
        closest-dropdown     (.. e -target (closest "#dropdown-menu"))
        closest              (or closest-block closest-block-header closest-page-header closest-dropdown)]
    (when (and selected-items?
               (nil? closest-bullet))
      (dispatch [:selected/clear-items]))
    (when (and (nil? closest)
               editing-uid)
      (dispatch [:editing/uid nil]))))


;; -- Clipboard ----------------------------------------------------------

(defn unformat-double-brackets
  "https://github.com/ryanguill/roam-tools/blob/eda72040622555b52e40f7a28a14744bce0496e5/src/index.js#L336-L345"
  [s]
  (-> s
      (string/replace #"\[([^\[\]]+)\]\((\[\[|\(\()([^\[\]]+)(\]\]|\)\))\)" "$1")
      (string/replace #"\[\[([^\[\]]+)\]\]" "$1")))


(defn block-refs-to-plain-text
  "If there is a valid ((uid)), find the original block's string.
  If invalid ((uid)), no-op.
  TODO: If deep block ref, convert deep block ref to plain-text.

  Want to put this in athens.util, but circular dependency from athens.db"
  [s]
  (let [replacements (->> s
                          (re-seq #"\(\(([^\(\)]+)\)\)")
                          (map (fn [[orig-str match-str]]
                                 (let [eid (db/e-by-av :block/uid match-str)]
                                   (if eid
                                     [orig-str (db/v-by-ea eid :block/string)]
                                     [orig-str (str "((" match-str "))")])))))]
    (loop [replacements replacements
           s            s]
      (let [orig-str    (first (first replacements))
            replace-str (second (first replacements))]
        (if (empty? replacements)
          s
          (recur (rest replacements)
                 (clojure.string/replace s orig-str replace-str)))))))


(defn blocks-to-clipboard-data
  "Four spaces per depth level."
  ([depth node]
   (blocks-to-clipboard-data depth node false))
  ([depth node unformat?]
   (let [{:block/keys [string children header]} node
         left-offset   (apply str (repeat depth "    "))
         walk-children (apply str (map #(blocks-to-clipboard-data (inc depth) % unformat?) children))
         string (let [header-to-str (case header
                                      1 "# "
                                      2 "## "
                                      3 "### "
                                      "")]
                  (str header-to-str string))
         string (if unformat?
                  (-> string unformat-double-brackets athens.listeners/block-refs-to-plain-text)
                  string)
         dash (if unformat? "" "- ")]
     (str left-offset dash string "\n" walk-children))))


(defn copy
  "If blocks are selected, copy blocks as markdown list.
  Use -event_ because goog events quirk "
  [^js e]
  (let [uids @(subscribe [:selected/items])]
    (when (not-empty uids)
      (let [copy-data (->> (map #(db/get-block-document [:block/uid %]) uids)
                           (map #(blocks-to-clipboard-data 0 %))
                           (apply str))]
        (.. e preventDefault)
        (.. e -event_ -clipboardData (setData "text/plain" copy-data))))))


(defn cut
  "Cut is essentially copy AND delete selected blocks"
  [^js e]
  (let [uids @(subscribe [:selected/items])]
    (when (not-empty uids)
      (let [copy-data (->> (map #(db/get-block-document [:block/uid %]) uids)
                           (map #(blocks-to-clipboard-data 0 %))
                           (apply str))]
        (.. e preventDefault)
        (.. e -event_ -clipboardData (setData "text/plain" copy-data))
        (dispatch [:selected/delete uids])))))


(defn prevent-save
  "Google Closure's events/listen isn't working for some reason anymore.

  beforeunload is called before unload, where the window would be redirected/refreshed/quit.
  https://developer.mozilla.org/en-US/docs/Web/API/Window/beforeunload_event "
  []
  (js/window.addEventListener
    EventType.BEFOREUNLOAD
    (fn [e]
      (let [synced? (or @(subscribe [:db/synced])
                        (:default? @(subscribe [:db/remote-graph-conf])))]
        (when-not synced?
          (dispatch [:alert/js "Athens hasn't finished saving yet. Athens is finished saving when the sync dot is green. Try refreshing or quitting again once the sync is complete."])
          (.. e preventDefault)
          (set! (.. e -returnValue) "Setting e.returnValue to string prevents exit for some browsers.")
          "Returning a string also prevents exit on other browsers.")))))


(defn init
  []
  (events/listen js/document EventType.MOUSEDOWN unfocus)
  (events/listen js/window EventType.COPY copy)
  (events/listen js/window EventType.CUT cut)
  (prevent-save))
