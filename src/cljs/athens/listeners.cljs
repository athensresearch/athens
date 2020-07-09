(ns athens.listeners
  (:require
    ;;[athens.util :refer [get-day]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [goog.events :as events]
    [re-frame.core :refer [dispatch subscribe]])
  (:import
    (goog.events
      EventType
      KeyCodes)))


;; -- Turn read block or header into editable on mouse down --------------

(defn edit-block
  [e]
  ;; Consider refactor if we add more editable targets
  (let [closest-block (.. e -target (closest ".block-contents"))
        closest-block-header (.. e -target (closest ".block-header"))
        closest-page-header (.. e -target (closest ".page-header"))
        closest (or closest-block closest-block-header closest-page-header)]
    (when closest
      (dispatch [:editing/uid (.. closest -dataset -uid)]))))


;; -- Close Athena -------------------------------------------------------

(defn mouse-down-outside-athena
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
        meta (.. e -metaKey)
        shift (.. e -shiftKey)]

    (cond
      (and (= key KeyCodes.Z) meta shift)
      (dispatch [:redo])

      (and (= key KeyCodes.Z) meta)
      (dispatch [:undo])

      (and (= key KeyCodes.K) meta)
      (dispatch [:athena/toggle])

      (and (= key KeyCodes.G) ctrl)
      (dispatch [:devtool/toggle])

      (and (= key KeyCodes.R) ctrl)
      (dispatch [:right-sidebar/toggle])

      (and (= key KeyCodes.L) ctrl)
      (dispatch [:left-sidebar/toggle]))))


(defn init
  []
  (events/listen js/window EventType.MOUSEDOWN edit-block)
  (events/listen js/window EventType.MOUSEDOWN mouse-down-outside-athena)
  (events/listen js/window EventType.KEYDOWN key-down))

