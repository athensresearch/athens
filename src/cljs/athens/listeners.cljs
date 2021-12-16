(ns athens.listeners
  (:require
    [athens.common-db :as common-db]
    [athens.db :as db]
    [athens.electron.utils :as electron-utils]
    [athens.events.selection :as select-events]
    [athens.router :as router]
    [athens.subs.selection :as select-subs]
    [athens.util :as util]
    [clojure.string :as string]
    [goog.events :as events]
    [re-frame.core :refer [dispatch dispatch-sync subscribe]])
  (:import
    (goog.events
      EventType
      KeyCodes)))


(defn multi-block-selection
  "When blocks are selected, handle various keypresses:
  - shift+up/down: increase/decrease selection.
  - enter: deselect and begin editing textarea
  - backspace: delete all blocks
  - up/down: change editing textarea
  - tab: indent/unindent blocks
  Can't use textarea-key-down from keybindings.cljs because textarea is no longer focused."
  [e]
  (let [selected-items @(subscribe [::select-subs/items])]
    (when (not-empty selected-items)
      (let [shift    (.. e -shiftKey)
            key-code (.. e -keyCode)
            enter?   (= key-code KeyCodes.ENTER)
            bksp?    (= key-code KeyCodes.BACKSPACE)
            up?      (= key-code KeyCodes.UP)
            down?    (= key-code KeyCodes.DOWN)
            tab?     (= key-code KeyCodes.TAB)
            delete?  (= key-code KeyCodes.DELETE)]
        (cond
          enter? (do
                   (dispatch [:editing/uid (first selected-items)])
                   (dispatch [::select-events/clear]))
          (or bksp? delete?)  (do
                                (dispatch [::select-events/delete])
                                (dispatch [::select-events/clear]))
          tab? (do
                 (.preventDefault e)
                 (if shift
                   (dispatch [:unindent/multi {:uids selected-items}])
                   (dispatch [:indent/multi {:uids selected-items}])))
          (and shift up?) (dispatch [:selected/up selected-items])
          (and shift down?) (dispatch [:selected/down selected-items])
          (or up? down?) (do
                           (.preventDefault e)
                           (dispatch [::select-events/clear])
                           (if up?
                             (dispatch [:up (first selected-items) e])
                             (dispatch [:down (last selected-items) e]))))))))


(defn unfocus
  "Clears editing/uid when user clicks anywhere besides bullets, header, or on a block.
  Clears selected/items when user clicks somewhere besides a bullet point."
  [e]
  (let [selected-items?      (not-empty @(subscribe [::select-subs/items]))
        editing-uid          @(subscribe [:editing/uid])
        closest-block        (.. e -target (closest ".block-content"))
        closest-block-header (.. e -target (closest ".block-header"))
        closest-page-header  (.. e -target (closest ".page-header"))
        closest-bullet       (.. e -target (closest ".bullet"))
        closest-dropdown     (.. e -target (closest "#dropdown-menu"))
        closest              (or closest-block closest-block-header closest-page-header closest-dropdown)]
    (when (and selected-items?
               (nil? closest-bullet))
      (dispatch [::select-events/clear]))
    (when (and (nil? closest)
               editing-uid)
      (dispatch [:editing/uid nil]))))


;; -- Hotkeys ------------------------------------------------------------


(defn key-down!
  [e]
  (let [{:keys [key-code ctrl meta shift alt]} (util/destruct-key-down e)
        editing-uid @(subscribe [:editing/uid])]
    (cond
      (util/shortcut-key? meta ctrl) (condp = key-code
                                       KeyCodes.S (dispatch [:save])

                                       KeyCodes.EQUALS (dispatch [:zoom/in])
                                       KeyCodes.DASH (dispatch [:zoom/out])
                                       KeyCodes.ZERO (dispatch [:zoom/reset])

                                       KeyCodes.K (dispatch [:athena/toggle])

                                       KeyCodes.G (dispatch [:devtool/toggle])

                                       KeyCodes.Z (let [editing-uid    @(subscribe [:editing/uid])
                                                        selected-items @(subscribe [::select-subs/items])]
                                                    ;; editing/uid must be nil or selected-items must be non-empty
                                                    (when (or (nil? editing-uid)
                                                              (not-empty selected-items))
                                                      (if shift
                                                        (dispatch [:redo])
                                                        (dispatch [:undo]))))

                                       KeyCodes.BACKSLASH (if shift
                                                            (dispatch [:right-sidebar/toggle])
                                                            (dispatch [:left-sidebar/toggle]))

                                       KeyCodes.COMMA (router/navigate :settings)

                                       KeyCodes.T (util/toggle-10x)
                                       nil)
      alt (condp = key-code
            KeyCodes.LEFT (when (nil? editing-uid) (.back js/window.history))
            KeyCodes.RIGHT (when (nil? editing-uid) (.forward js/window.history))
            KeyCodes.D (router/nav-daily-notes)
            KeyCodes.G (router/navigate :graph)
            KeyCodes.A (router/navigate :pages)
            KeyCodes.T (dispatch [:theme/toggle])
            nil))))


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
                                     [orig-str (str "((" (db/v-by-ea eid :block/string) "))")]
                                     [orig-str (str "((" match-str "))")])))))]
    (loop [replacements replacements
           s            s]
      (let [[orig-str replace-str] (first replacements)]
        (if (empty? replacements)
          s
          (recur (rest replacements)
                 (clojure.string/replace s orig-str replace-str)))))))


(defn blocks-to-clipboard-data
  "Four spaces per depth level."
  ([depth node]
   (blocks-to-clipboard-data depth node false))
  ([depth node unformat?]
   (let [{:block/keys [string
                       children
                       _header]} node
         left-offset             (apply str (repeat depth "    "))
         walk-children           (apply str (map #(blocks-to-clipboard-data (inc depth) % unformat?)
                                                 children))
         string                  (if unformat?
                                   (-> string
                                       unformat-double-brackets
                                       block-refs-to-plain-text)
                                   (block-refs-to-plain-text string))
         dash                    (if unformat? "" "- ")]
     (str left-offset dash string "\n" walk-children))))


(defn copy
  "If blocks are selected, copy blocks as markdown list.
  Use -event_ because goog events quirk "
  [^js e]
  (let [uids @(subscribe [::select-subs/items])]
    (when (not-empty uids)
      (let [copy-data      (->> uids
                                (map #(db/get-block-document [:block/uid %]))
                                (map #(blocks-to-clipboard-data 0 %))
                                (apply str))
            clipboard-data (.. e -event_ -clipboardData)
            copied-blocks  (mapv
                             #(common-db/get-internal-representation  @db/dsdb [:block/uid %])
                             uids)]

        (doto clipboard-data
          (.setData "text/plain" copy-data)
          (.setData "application/athens-representation" (pr-str copied-blocks))
          (.setData "application/athens" (pr-str {:uids uids})))
        (.preventDefault e)))))


(defn cut
  "Cut is essentially copy AND delete selected blocks"
  [^js e]
  (let [uids @(subscribe [::select-subs/items])]
    (when (not-empty uids)
      (copy e)
      (dispatch [::select-events/delete]))))


(def force-leave (atom false))


(defn prevent-save
  "Google Closure's events/listen isn't working for some reason anymore.

  beforeunload is called before unload, where the window would be redirected/refreshed/quit.
  https://developer.mozilla.org/en-US/docs/Web/API/Window/beforeunload_event "
  []
  (js/window.addEventListener
    EventType.BEFOREUNLOAD
    (fn [e]
      (let [synced? @(subscribe [:db/synced])
            editing? @(subscribe [:editing/uid])
            remote? (electron-utils/remote-db? @(subscribe [:db-picker/selected-db]))]
        (cond
          (and (or (not synced?)
                   (not (= nil editing?)))
               (not @force-leave))
          (do
            ;; The browser blocks the confirm window during beforeunload, so
            ;; instead we always cancel unload and separately show a confirm window
            ;; that allows closing the window.
            (dispatch [:confirm/js
                       (str "Athens hasn't finished saving yet. Athens is finished saving when the sync dot is green. "
                            "Try refreshing or quitting again once the sync is complete. Make sure you exit out of any block you may be editing"
                            "Press OK to wait, or Cancel to leave without saving (will cause data loss!).")
                       #()
                       (fn []
                         (reset! force-leave true)
                         (js/window.close))])
            (.. e preventDefault)
            (set! (.. e -returnValue) "Setting e.returnValue to string prevents exit for some browsers.")
            "Returning a string also prevents exit on other browsers.")

          remote?
          (dispatch-sync [:remote/disconnect!]))))))


(defn init
  []
  (events/listen js/document EventType.MOUSEDOWN unfocus)
  (events/listen js/window EventType.KEYDOWN multi-block-selection)
  (events/listen js/window EventType.KEYDOWN key-down!)
  (events/listen js/window EventType.COPY copy)
  (events/listen js/window EventType.CUT cut)
  (prevent-save))
