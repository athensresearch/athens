(ns athens.listeners
  (:require
    [athens.db :refer [dsdb]]
    [athens.keybindings :refer [arrow-key-direction]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [clojure.string :as string]
    [datascript.core :as d]
    [goog.events :as events]
    [re-frame.core :refer [dispatch subscribe]])
  (:import
    (goog.events
      EventType
      KeyCodes)))


;; -- shift-up/down when multi-block selection ---------------------------

;; can no longer use on-key-down from keybindings.cljs. textarea is no longer focused, so events must be handled globally
(defn multi-block-selection
  [e]
  (let [selected-items @(subscribe [:selected/items])]
    (when (not-empty selected-items)
      (let [shift     (.. e -shiftKey)
            key-code (.. e -keyCode)
            direction (arrow-key-direction e)]
        ;; what should tab/shift-tab do? roam and workflowy have slightly different behavior
        (cond
          (= key-code KeyCodes.ENTER) (do
                                        (dispatch [:editing/uid (first selected-items)])
                                        (dispatch [:selected/clear-items]))
          (= key-code KeyCodes.BACKSPACE) (dispatch [:selected/delete selected-items])
          (and shift (= direction :up)) (dispatch [:selected/up selected-items])
          (and shift (= direction :down)) (dispatch [:selected/down selected-items])
          (= direction :up) (do
                              (.preventDefault e)
                              (dispatch [:selected/clear-items])
                              (dispatch [:up (first selected-items)]))
          (= direction :down) (do
                                (.preventDefault e)
                                (dispatch [:selected/clear-items])
                                (dispatch [:down (last selected-items)])))))))


;; -- When dragging across multiple blocks to select ---------------------

(defn get-dataset-uid
  [el]
  (let [block (when el (.. el (closest ".block-container")))
        uid (when block (.. block -dataset -uid))]
    uid))


(defn recursive-child?
  [e selected-items]
  (let [parent-set (->> (mapcat (fn [x] (get-parents-recursively [:block/uid x]))
                                selected-items)
                        (map :block/uid)
                        set)]
    (contains? parent-set (get-dataset-uid (.. e -target)))))

(defn multi-block-select-over
  "If selected-items are empty, add editing-uid, which is used as source block for start of drag.
  If the item is not already in the collection, and if the block is not a (nested) child, add it
  If the block is a parent of a block, remove the children of those blocks, and add the parent
  "
  [e]
  (let [target             (.. e -target)
        related-target     (.. e -relatedTarget)
        target-uid         (get-dataset-uid target)
        related-target-uid (get-dataset-uid related-target)
        prev-block-uid (db/prev-block-uid related-target-uid)
        next-block-uid (db/next-block-uid related-target-uid true)
        selected-items     (subscribe [:selected/items])
        editing-uid     @(subscribe [:editing/uid])]

    ;; the problem is sometimes select-over doesn't start with the source block.
    ;; if items is empty, then run everything with editing/uid. then rerun with target-uid

    (when (and (empty? (set @selected-items)) (not (nil? editing-uid)))
      (dispatch [:selected/add-item editing-uid]))

    (prn "OVER" #_(and (empty? set-items) (not (nil? editing-uid)))
         (= editing-uid target-uid)
         (= target-uid prev-block-uid)
         target-uid #_related-target-uid prev-block-uid)

    (cond
      (contains? (set @selected-items) target-uid) nil
      (= editing-uid target-uid) nil
      (= target-uid prev-block-uid) (dispatch [:selected/up @selected-items]))

      ;;(dispatch [:selected/add-item target-uid]))

    (.. e stopPropagation)
    (.. target blur)))


(defn multi-block-select-up
  [_]
  (events/unlisten js/window EventType.MOUSEOVER multi-block-select-over)
  (events/unlisten js/window EventType.MOUSEUP multi-block-select-up))

;; -- When user clicks elsewhere -----------------------------------------

(defn unfocus
  [e]
  (let [selected-items? (boolean @(subscribe [:selected/items]))
        editing-uid    @(subscribe [:editing/uid])
        closest-block (.. e -target (closest ".block-content"))
        closest-block-header (.. e -target (closest ".block-header"))
        closest-page-header (.. e -target (closest ".page-header"))
        closest (or closest-block closest-block-header closest-page-header)]
    (when selected-items?
      (dispatch [:selected/clear-items]))
    (when (and (nil? closest) editing-uid selected-items?)
      (dispatch [:editing/uid nil]))))


;; -- Close Athena -------------------------------------------------------

(defn click-outside-athena
  [e]
  (let [athena? @(subscribe [:athena/open])
        closest (.. e -target (closest ".athena"))]
    (when (and athena? (nil? closest))
      (dispatch [:athena/toggle]))))


;; -- Hotkeys ------------------------------------------------------------


(defn key-down
  [e]
  (let [key (.. e -keyCode)
        ctrl (.. e -ctrlKey)
        ;meta (.. e -metaKey)
        shift (.. e -shiftKey)]

    (cond
      (and (= key KeyCodes.S) ctrl)
      (dispatch [:save])

      (and (= key KeyCodes.Z) ctrl shift)
      (dispatch [:redo])

      (and (= key KeyCodes.Z) ctrl)
      (dispatch [:undo])

      (and (= key KeyCodes.K) ctrl)
      (dispatch [:athena/toggle])

      (and (= key KeyCodes.G) ctrl)
      (dispatch [:devtool/toggle])

      (and (= key KeyCodes.L) ctrl shift)
      (dispatch [:right-sidebar/toggle])

      (and (= key KeyCodes.L) ctrl)
      (dispatch [:left-sidebar/toggle]))))


;; -- Clipboard ----------------------------------------------------------

;; TODO: once :selected/items is a nested tree instead of flat list, walk tree and add hyphens instead of mapping
(defn to-markdown-list
  [blocks]
  (->> blocks
       (map (fn [x] [:block/uid x]))
       (d/pull-many @dsdb '[:block/string])
       (map #(str "- " (:block/string %) "\n"))
       (string/join "")))


(defn copy
  "If blocks are selected, copy blocks as markdown list."
  [^js e]
  (let [blocks @(subscribe [:selected/items])]
    (when (not-empty blocks)
      (.. e preventDefault)
      ;; Use -event_ because goog events quirk
      (.. e -event_ -clipboardData (setData "text/plain" (to-markdown-list blocks))))))


;; do same as copy AND delete selected blocks
(defn cut
  [^js e]
  (let [blocks @(subscribe [:selected/items])]
    (when (not-empty blocks)
      (.. e preventDefault)
      (.. e -event_ -clipboardData (setData "text/plain" (to-markdown-list blocks)))
      (dispatch [:selected/delete blocks]))))


(defn init
  []
  ;; (events/listen js/window EventType.MOUSEDOWN edit-block)
  (events/listen js/document EventType.MOUSEDOWN unfocus)
  (events/listen js/window EventType.CLICK click-outside-athena)
  (events/listen js/window EventType.KEYDOWN multi-block-selection)
  (events/listen js/window EventType.KEYDOWN key-down)
  (events/listen js/window EventType.COPY copy)
  (events/listen js/window EventType.CUT cut))

